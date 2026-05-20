package co.edu.corhuila.audit_service.Service;

import co.edu.corhuila.audit_service.Dto.AuditHistoryResponse;
import co.edu.corhuila.audit_service.Dto.AuditInconsistencyResponse;
import co.edu.corhuila.audit_service.Dto.AuditMetricsResponse;
import co.edu.corhuila.audit_service.Dto.AuditNoteRequest;
import co.edu.corhuila.audit_service.Dto.AuditObservationResponse;
import co.edu.corhuila.audit_service.Dto.AuditRecalculateResponse;
import co.edu.corhuila.audit_service.Dto.AuditStatusUpdateRequest;
import co.edu.corhuila.audit_service.Dto.InventoryMovementResponse;
import co.edu.corhuila.audit_service.Dto.ManualAuditCaseRequest;
import co.edu.corhuila.audit_service.Dto.ManualAuditCaseResponse;
import co.edu.corhuila.audit_service.Entity.AuditCase;
import co.edu.corhuila.audit_service.Entity.AuditCaseSource;
import co.edu.corhuila.audit_service.Entity.AuditCaseStatus;
import co.edu.corhuila.audit_service.Entity.AuditObservation;
import co.edu.corhuila.audit_service.Entity.AuditPriority;
import co.edu.corhuila.audit_service.Entity.AuditRuleResult;
import co.edu.corhuila.audit_service.Repository.AuditCaseRepository;
import co.edu.corhuila.audit_service.Repository.AuditObservationRepository;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class AuditService {

    private static final ZoneId BOGOTA_ZONE = ZoneId.of("America/Bogota");
    private static final Collection<AuditCaseStatus> ACTIVE_STATUSES = List.of(
            AuditCaseStatus.OPEN,
            AuditCaseStatus.IN_REVIEW
    );

    private final AuditCaseRepository auditCaseRepository;
    private final AuditObservationRepository auditObservationRepository;
    private final InventoryClient inventoryClient;
    private final AuditRuleEngine auditRuleEngine;

    public AuditService(
            AuditCaseRepository auditCaseRepository,
            AuditObservationRepository auditObservationRepository,
            InventoryClient inventoryClient,
            AuditRuleEngine auditRuleEngine
    ) {
        this.auditCaseRepository = auditCaseRepository;
        this.auditObservationRepository = auditObservationRepository;
        this.inventoryClient = inventoryClient;
        this.auditRuleEngine = auditRuleEngine;
    }

    @Transactional
    public List<AuditHistoryResponse> listHistory(String authorizationHeader) {
        List<InventoryMovementResponse> movements = inventoryClient.listMovements(authorizationHeader);

        return movements.stream()
                .map(this::toHistoryResponse)
                .sorted(Comparator
                        .comparing((AuditHistoryResponse item) -> !"NORMAL".equals(item.auditStatus()))
                        .reversed()
                        .thenComparing(AuditHistoryResponse::movementId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Transactional
    public List<AuditInconsistencyResponse> listInconsistencies(String authorizationHeader) {
        return auditCaseRepository.findByStatusInOrderByPriorityDescCreatedAtDesc(ACTIVE_STATUSES)
                .stream()
                .map(this::toInconsistencyResponse)
                .toList();
    }

    public List<AuditObservationResponse> listObservations() {
        Map<Long, AuditCase> latestCases = new LinkedHashMap<>();
        latestCasesByMovement(auditCaseRepository.findAll())
                .forEach(auditCase -> latestCases.put(auditCase.getId(), auditCase));

        Map<Long, AuditObservation> latestObservations = new LinkedHashMap<>();
        for (AuditObservation observation : auditObservationRepository.findAllByOrderByCreatedAtDesc()) {
            AuditCase auditCase = observation.getAuditCase();
            if (auditCase != null && !latestCases.containsKey(auditCase.getId())) {
                continue;
            }

            Long movementKey = observation.getMovementId() == null ? observation.getId() : observation.getMovementId();
            latestObservations.putIfAbsent(movementKey, observation);
        }

        return latestObservations.values()
                .stream()
                .map(this::toObservationResponse)
                .toList();
    }

    @Transactional
    public AuditMetricsResponse getMetrics(String authorizationHeader) {
        List<InventoryMovementResponse> movements = inventoryClient.listMovements(authorizationHeader);
        List<AuditCase> cases = latestCasesByMovement(auditCaseRepository.findAll());

        long marked = cases.size();
        long observations = listObservations().size();
        long users = movements.stream().map(InventoryMovementResponse::getUserId).filter(id -> id != null).distinct().count();

        Map<String, Long> activityByUser = new LinkedHashMap<>();
        Map<String, Long> monthlyTrend = new LinkedHashMap<>();
        Map<String, Long> topMedicines = new LinkedHashMap<>();
        Map<String, Long> casesByUser = new LinkedHashMap<>();
        Map<String, Long> casesByMedicine = new LinkedHashMap<>();
        Map<String, Long> casesByPriority = new LinkedHashMap<>();
        Map<String, Long> casesBySource = new LinkedHashMap<>();
        Map<String, Long> caseMonthlyTrend = new LinkedHashMap<>();
        Map<String, BigDecimal> riskByMedicine = new LinkedHashMap<>();

        for (InventoryMovementResponse movement : movements) {
            String user = displayUserName(movement.getUserName());
            activityByUser.merge(user, 1L, Long::sum);

            if (movement.getDateTime() != null) {
                String month = movement.getDateTime().atZone(BOGOTA_ZONE).toLocalDate().withDayOfMonth(1).toString();
                monthlyTrend.merge(month, 1L, Long::sum);
            }

            String medicine = firstNotBlank(movement.getProductName(), "Sin medicamento");
            topMedicines.merge(medicine, 1L, Long::sum);
        }

        for (AuditCase auditCase : cases) {
            casesByUser.merge(displayUserName(auditCase.getMovementUserName()), 1L, Long::sum);
            casesByMedicine.merge(firstNotBlank(auditCase.getMedicineName(), "Sin medicamento"), 1L, Long::sum);
            casesByPriority.merge(auditCase.getPriority().name(), 1L, Long::sum);
            casesBySource.merge(auditCase.getSource().name(), 1L, Long::sum);
            if (auditCase.getCreatedAt() != null) {
                String month = auditCase.getCreatedAt().atZone(BOGOTA_ZONE).toLocalDate().withDayOfMonth(1).toString();
                caseMonthlyTrend.merge(month, 1L, Long::sum);
            }
            riskByMedicine.merge(
                    firstNotBlank(auditCase.getMedicineName(), "Sin medicamento"),
                    auditCase.getRiskScore() == null ? BigDecimal.ZERO : auditCase.getRiskScore(),
                    BigDecimal::add
            );
        }

        return new AuditMetricsResponse(
                new AuditMetricsResponse.Summary(
                        movements.size(),
                        marked,
                        observations,
                        users,
                        countBySource(cases, AuditCaseSource.AUTO),
                        countBySource(cases, AuditCaseSource.MANUAL),
                        countByStatus(cases, AuditCaseStatus.OPEN),
                        countByStatus(cases, AuditCaseStatus.IN_REVIEW),
                        countByStatus(cases, AuditCaseStatus.REVIEWED),
                        countByStatus(cases, AuditCaseStatus.CLOSED),
                        countByPriority(cases, AuditPriority.HIGH),
                        countByPriority(cases, AuditPriority.MEDIUM),
                        countByPriority(cases, AuditPriority.LOW)
                ),
                toMetricList(activityByUser, "user", "total"),
                toMetricList(monthlyTrend, "month", "total"),
                toMetricList(topMedicines, "medicine", "total"),
                toMetricList(casesByUser, "user", "total"),
                toMetricList(casesByMedicine, "medicine", "total"),
                toMetricList(casesByPriority, "priority", "total"),
                toMetricList(casesBySource, "source", "total"),
                toMetricList(caseMonthlyTrend, "month", "total"),
                toDecimalMetricList(riskByMedicine, "medicine", "riskScore")
        );
    }

    @Transactional
    public AuditRecalculateResponse recalculate(String authorizationHeader) {
        List<InventoryMovementResponse> movements = inventoryClient.listMovements(authorizationHeader);
        return ensureAutomaticCases(movements, authorizationHeader);
    }

    @Transactional
    public ManualAuditCaseResponse createManualCase(ManualAuditCaseRequest request, String authorizationHeader) {
        if (request.getNote() == null || request.getNote().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nota es obligatoria para marcar manualmente");
        }

        AuditPriority priority = parsePriority(request.getPriority());
        InventoryMovementResponse movement = inventoryClient.getMovement(request.getMovementId(), authorizationHeader);
        AuditActor actor = currentActor();

        AuditCase auditCase = auditCaseRepository
                .findFirstByMovementIdOrderByCreatedAtDesc(request.getMovementId())
                .orElseGet(() -> {
                    AuditCase created = buildCaseFromMovement(movement, AuditCaseSource.MANUAL);
                    created.setReason("Marcado manual");
                    created.setRiskScore(BigDecimal.valueOf(65));
                    created.setCreatedByUserId(actor.userId());
                    created.setCreatedByUserName(actor.userName());
                    return created;
        });

        auditCase.setSource(AuditCaseSource.MANUAL);
        auditCase.setReason("Marcado manual");
        auditCase.setPriority(priority);
        auditCase.setStatus(AuditCaseStatus.OPEN);
        auditCase.setClosedAt(null);
        auditCase.setClosedByUserId(null);
        auditCase.setClosedByUserName(null);

        upsertCaseObservation(auditCase, movement, request.getNote(), priority, actor);

        AuditCase saved = auditCaseRepository.save(auditCase);
        inventoryClient.updateAuditStatus(
                saved.getMovementId(),
                "MARKED",
                firstNotBlank(request.getNote(), saved.getReason()),
                authorizationHeader
        );
        return toManualResponse(saved);
    }

    @Transactional
    public AuditObservationResponse updateNote(Long caseId, AuditNoteRequest request, String authorizationHeader) {
        AuditCase auditCase = auditCaseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caso de auditoria no encontrado"));

        AuditObservation observation = auditObservationRepository.findFirstByAuditCaseIdOrderByCreatedAtDesc(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Observacion no encontrada"));

        observation.setNote(request.getNote().trim());
        observation.setPriority(parsePriority(request.getPriority()));
        auditCase.setPriority(observation.getPriority());

        auditCaseRepository.save(auditCase);
        AuditObservation savedObservation = auditObservationRepository.save(observation);
        inventoryClient.updateAuditStatus(
                auditCase.getMovementId(),
                "MARKED",
                firstNotBlank(savedObservation.getNote(), auditCase.getReason()),
                authorizationHeader
        );
        return toObservationResponse(savedObservation);
    }

    @Transactional
    public ManualAuditCaseResponse updateStatus(Long caseId, AuditStatusUpdateRequest request, String authorizationHeader) {
        AuditCase auditCase = auditCaseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caso de auditoria no encontrado"));
        AuditCaseStatus status = parseStatus(request.getStatus());
        auditCase.setStatus(status);

        if (status == AuditCaseStatus.CLOSED) {
            AuditActor actor = currentActor();
            auditCase.setClosedAt(Instant.now());
            auditCase.setClosedByUserId(actor.userId());
            auditCase.setClosedByUserName(actor.userName());
        }

        AuditCase saved = auditCaseRepository.save(auditCase);
        inventoryClient.updateAuditStatus(
                saved.getMovementId(),
                toInventoryStatus(status),
                saved.getReason(),
                authorizationHeader
        );
        return toManualResponse(saved);
    }

    @Transactional
    public void deleteManualFlag(Long caseId, String authorizationHeader) {
        AuditCase auditCase = auditCaseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caso de auditoria no encontrado"));
        if (auditCase.getSource() != AuditCaseSource.MANUAL) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Solo se puede quitar una marca manual");
        }
        Long movementId = auditCase.getMovementId();
        auditCaseRepository.delete(auditCase);
        inventoryClient.updateAuditStatus(movementId, "NORMAL", "", authorizationHeader);
    }

    private AuditRecalculateResponse ensureAutomaticCases(List<InventoryMovementResponse> movements, String authorizationHeader) {
        List<AuditCase> newCases = new ArrayList<>();
        for (InventoryMovementResponse movement : movements) {
            if (movement.getId() == null) {
                continue;
            }

            Optional<AuditCase> existingCase = auditCaseRepository.findFirstByMovementIdOrderByCreatedAtDesc(movement.getId());
            if (existingCase.isPresent()) {
                ensureAutomaticObservation(existingCase.get());
                continue;
            }

            List<AuditRuleEngine.RuleFinding> findings = auditRuleEngine.evaluate(movement, movements);
            Optional<AuditRuleEngine.RuleFinding> strongest = auditRuleEngine.strongest(findings);
            if (strongest.isEmpty()) {
                continue;
            }

            AuditRuleEngine.RuleFinding finding = strongest.get();
            AuditCase auditCase = buildCaseFromMovement(movement, AuditCaseSource.AUTO);
            auditCase.setReason(finding.reason());
            auditCase.setPriority(finding.priority());
            auditCase.setRiskScore(finding.score());

            AuditObservation observation = new AuditObservation();
            observation.setMovementId(movement.getId());
            observation.setNote(finding.reason());
            observation.setPriority(finding.priority());
            observation.setCreatedByUserName("Sistema");
            auditCase.addObservation(observation);

            for (AuditRuleEngine.RuleFinding item : findings) {
                AuditRuleResult ruleResult = new AuditRuleResult();
                ruleResult.setRuleCode(item.ruleCode());
                ruleResult.setRuleName(item.ruleName());
                ruleResult.setExpectedValue(item.expectedValue());
                ruleResult.setActualValue(item.actualValue());
                ruleResult.setScore(item.score());
                auditCase.addRuleResult(ruleResult);
            }
            newCases.add(auditCase);
        }

        int synchronizedMovements = 0;
        if (!newCases.isEmpty()) {
            List<AuditCase> savedCases = auditCaseRepository.saveAll(newCases);
            for (AuditCase auditCase : savedCases) {
                inventoryClient.updateAuditStatus(
                    auditCase.getMovementId(),
                    "MARKED",
                    auditCase.getReason(),
                    authorizationHeader
                );
                synchronizedMovements++;
            }
        }
        return new AuditRecalculateResponse(movements.size(), newCases.size(), synchronizedMovements);
    }

    private void ensureAutomaticObservation(AuditCase auditCase) {
        if (auditCase.getSource() != AuditCaseSource.AUTO) {
            return;
        }

        if (auditObservationRepository.findFirstByAuditCaseIdOrderByCreatedAtDesc(auditCase.getId()).isPresent()) {
            return;
        }

        AuditObservation observation = new AuditObservation();
        observation.setMovementId(auditCase.getMovementId());
        observation.setNote(firstNotBlank(auditCase.getReason(), "Caso automático detectado por auditoría"));
        observation.setPriority(auditCase.getPriority());
        observation.setCreatedByUserName("Sistema");
        auditCase.addObservation(observation);
        auditCaseRepository.save(auditCase);
    }

    private AuditCase buildCaseFromMovement(InventoryMovementResponse movement, AuditCaseSource source) {
        AuditCase auditCase = new AuditCase();
        auditCase.setMovementId(movement.getId());
        auditCase.setProductId(movement.getProductId());
        auditCase.setBatchId(movement.getBatchId());
        auditCase.setMovementType(movement.getType());
        auditCase.setMedicineName(movement.getProductName());
        auditCase.setQuantity(movement.getAmount());
        auditCase.setMovementReason(movement.getReason());
        auditCase.setMovementUserId(movement.getUserId());
        auditCase.setMovementUserName(displayUserName(movement.getUserName()));
        auditCase.setMovementUserRole(movement.getUserRole());
        auditCase.setMovementDateTime(movement.getDateTime());
        auditCase.setStatus(AuditCaseStatus.OPEN);
        auditCase.setSource(source);
        auditCase.setPriority(AuditPriority.MEDIUM);
        return auditCase;
    }

    private void upsertCaseObservation(
            AuditCase auditCase,
            InventoryMovementResponse movement,
            String note,
            AuditPriority priority,
            AuditActor actor
    ) {
        AuditObservation observation = auditCase.getId() == null
                ? null
                : auditObservationRepository.findFirstByAuditCaseIdOrderByCreatedAtDesc(auditCase.getId()).orElse(null);

        if (observation == null) {
            observation = new AuditObservation();
            observation.setMovementId(movement.getId());
            observation.setCreatedByUserId(actor.userId());
            observation.setCreatedByUserName(actor.userName());
            auditCase.addObservation(observation);
        }

        observation.setNote(note.trim());
        observation.setPriority(priority);
    }

    private AuditHistoryResponse toHistoryResponse(InventoryMovementResponse movement) {
        Optional<AuditCase> optionalCase = movement.getId() == null
                ? Optional.empty()
                : auditCaseRepository.findFirstByMovementIdOrderByCreatedAtDesc(movement.getId());
        AuditCase auditCase = optionalCase.orElse(null);
        AuditObservation observation = auditCase == null
                ? null
                : auditObservationRepository.findFirstByAuditCaseIdOrderByCreatedAtDesc(auditCase.getId()).orElse(null);

        String date = movement.getDateTime() == null
                ? null
                : movement.getDateTime().atZone(BOGOTA_ZONE).toLocalDate().toString();
        String time = movement.getDateTime() == null
                ? null
                : movement.getDateTime().atZone(BOGOTA_ZONE).toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        return new AuditHistoryResponse(
                auditCase == null ? movement.getId() : auditCase.getId(),
                movement.getId(),
                date,
                time,
                translateType(movement.getType()),
                firstNotBlank(movement.getProductName(), "Sin medicamento"),
                movement.getAmount(),
                movement.getAmount() == null ? null : Math.abs(movement.getAmount()),
                displayUserName(movement.getUserName()),
                firstNotBlank(movement.getReason(), movement.getObservation()),
                auditCase == null ? "NORMAL" : toHistoryAuditStatus(auditCase.getStatus()),
                auditCase == null ? null : auditCase.getSource().name(),
                auditCase == null ? null : auditCase.getPriority().name(),
                auditCase == null ? null : auditCase.getReason(),
                observation == null ? null : observation.getNote(),
                auditCase == null ? BigDecimal.ZERO : auditCase.getRiskScore()
        );
    }

    private AuditInconsistencyResponse toInconsistencyResponse(AuditCase auditCase) {
        return new AuditInconsistencyResponse(
                auditCase.getId(),
                auditCase.getMovementId(),
                auditCase.getMedicineName(),
                translateType(auditCase.getMovementType()),
                auditCase.getQuantity(),
                displayUserName(auditCase.getMovementUserName()),
                auditCase.getReason(),
                auditCase.getPriority().name(),
                auditCase.getSource().name(),
                auditCase.getRiskScore(),
                auditCase.getStatus().name()
        );
    }

    private AuditObservationResponse toObservationResponse(AuditObservation observation) {
        AuditCase auditCase = observation.getAuditCase();
        return new AuditObservationResponse(
                observation.getId(),
                auditCase == null ? null : auditCase.getId(),
                observation.getMovementId(),
                observation.getPriority().name(),
                observation.getNote(),
                auditCase == null ? null : displayUserName(auditCase.getMovementUserName()),
                observation.getCreatedByUserName(),
                observation.getCreatedAt()
        );
    }

    private ManualAuditCaseResponse toManualResponse(AuditCase auditCase) {
        return new ManualAuditCaseResponse(
                auditCase.getId(),
                auditCase.getMovementId(),
                auditCase.getStatus().name(),
                auditCase.getSource().name(),
                auditCase.getPriority().name(),
                auditCase.getReason()
        );
    }

    private List<Map<String, Object>> toMetricList(Map<String, Long> data, String keyName, String valueName) {
        return data.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put(keyName, entry.getKey());
                    row.put(valueName, entry.getValue());
                    return row;
                })
                .toList();
    }

    private List<Map<String, Object>> toDecimalMetricList(Map<String, BigDecimal> data, String keyName, String valueName) {
        return data.entrySet()
                .stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put(keyName, entry.getKey());
                    row.put(valueName, entry.getValue());
                    return row;
                })
                .toList();
    }

    private long countByPriority(List<AuditCase> cases, AuditPriority priority) {
        return cases.stream()
                .filter(auditCase -> auditCase.getPriority() == priority)
                .count();
    }

    private long countBySource(List<AuditCase> cases, AuditCaseSource source) {
        return cases.stream()
                .filter(auditCase -> auditCase.getSource() == source)
                .count();
    }

    private long countByStatus(List<AuditCase> cases, AuditCaseStatus status) {
        return cases.stream()
                .filter(auditCase -> auditCase.getStatus() == status)
                .count();
    }

    private List<AuditCase> latestCasesByMovement(List<AuditCase> cases) {
        Map<Long, AuditCase> latestCases = new LinkedHashMap<>();
        cases.stream()
                .sorted(Comparator.comparing(AuditCase::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .forEach(auditCase -> {
                    Long movementKey = auditCase.getMovementId() == null ? auditCase.getId() : auditCase.getMovementId();
                    latestCases.putIfAbsent(movementKey, auditCase);
                });
        return latestCases.values().stream().toList();
    }

    private String toInventoryStatus(AuditCaseStatus status) {
        return switch (status) {
            case OPEN, IN_REVIEW -> "MARKED";
            case REVIEWED, CLOSED -> "REVIEWED";
        };
    }

    private String toHistoryAuditStatus(AuditCaseStatus status) {
        return switch (status) {
            case OPEN -> "MARCADO";
            case IN_REVIEW -> "EN_REVISION";
            case REVIEWED -> "REVISADO";
            case CLOSED -> "CERRADO";
        };
    }

    private AuditPriority parsePriority(String rawPriority) {
        if (rawPriority == null || rawPriority.isBlank()) {
            return AuditPriority.MEDIUM;
        }
        try {
            return AuditPriority.valueOf(rawPriority.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "priority invalido. Valores permitidos: LOW, MEDIUM, HIGH");
        }
    }

    private AuditCaseStatus parseStatus(String rawStatus) {
        try {
            return AuditCaseStatus.valueOf(rawStatus.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status invalido. Valores permitidos: OPEN, IN_REVIEW, REVIEWED, CLOSED");
        }
    }

    private AuditActor currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return new AuditActor(null, "Auditor");
        }

        Long userId = null;
        String name = authentication.getName();
        if (authentication.getDetails() instanceof Claims claims) {
            userId = claims.get("userId", Long.class);
            name = firstNotBlank(claims.get("name", String.class), name);
        }

        return new AuditActor(userId, firstNotBlank(name, "Auditor"));
    }

    private String translateType(String type) {
        if (type == null) {
            return null;
        }
        return switch (type.toUpperCase(Locale.ROOT)) {
            case "ENTRANCE" -> "Entrada";
            case "EXIT" -> "Salida";
            case "UPDATED" -> "Actualizacion";
            case "DELETED" -> "Eliminacion";
            default -> type;
        };
    }

    private String firstNotBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String displayUserName(String value) {
        String normalizedValue = firstNotBlank(value, "Sistema");
        return normalizedValue.equalsIgnoreCase("SYSTEM_INIT") || normalizedValue.equalsIgnoreCase("SYSTEM")
                ? "Sistema"
                : normalizedValue;
    }

    private record AuditActor(Long userId, String userName) {
    }
}
