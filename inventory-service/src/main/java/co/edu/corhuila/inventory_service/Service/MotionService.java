package co.edu.corhuila.inventory_service.Service;


import co.edu.corhuila.inventory_service.Dto.MotionResponse;
import co.edu.corhuila.inventory_service.Dto.UserActivityReportResponse;
import co.edu.corhuila.inventory_service.Entity.Motion;
import co.edu.corhuila.inventory_service.Entity.MovementType;
import co.edu.corhuila.inventory_service.Repository.MotionRepository;
import co.edu.corhuila.inventory_service.Repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MotionService {

    private static final String SYSTEM_USER_NAME = "Sistema";
    private static final String SYSTEM_USER_ROLE = "Automatico";

    private final MotionRepository motionRepository;

    public MotionService(MotionRepository motionRepository,
                         ProductRepository productRepository) {
        this.motionRepository = motionRepository;

    }



    public List<MotionResponse> listMotion() {
        return motionRepository.findAll()
                .stream()
                .map(MotionResponse::new)
                .toList();
    }

    public List<MotionResponse> listMotionByUser(Long userId) {
        List<Motion> motions = userId == null
                ? motionRepository.findAllByOrderByDateTimeDesc()
                : motionRepository.findByUserIdOrderByDateTimeDesc(userId);

        return motions.stream()
                .map(MotionResponse::new)
                .toList();
    }

    public List<MotionResponse> listEntranceMotion() {
        return motionRepository.findByType(MovementType.Entrance)
                .stream()
                .map(MotionResponse::new)
                .toList();
    }

    public List<MotionResponse> listExitMotion() {
        return motionRepository.findByType(MovementType.Exit)
                .stream()
                .map(MotionResponse::new)
                .toList();
    }

    public List<MotionResponse> listUpdatedMotion() {
        return motionRepository.findByType(MovementType.Updated)
                .stream()
                .map(MotionResponse::new)
                .toList();
    }

    public List<UserActivityReportResponse> listUsersActivityReport(String role) {
        Map<UserActivityKey, UserActivityAccumulator> activityByUser = new LinkedHashMap<>();
        String normalizedRoleFilter = normalizeRole(role);

        for (Motion motion : motionRepository.findAllByOrderByDateTimeDesc()) {
            String userRole = firstNotBlank(motion.getUserRole(), SYSTEM_USER_ROLE);
            if (normalizedRoleFilter != null && !userRole.equalsIgnoreCase(normalizedRoleFilter)) {
                continue;
            }

            UserActivityKey key = new UserActivityKey(
                    motion.getUserId(),
                    firstNotBlank(motion.getUserName(), SYSTEM_USER_NAME),
                    userRole
            );

            UserActivityAccumulator accumulator = activityByUser.computeIfAbsent(
                    key,
                    ignored -> new UserActivityAccumulator()
            );

            accumulator.totalMovements++;

            if (motion.getType() == MovementType.Entrance) {
                accumulator.totalEntrances++;
            }

            if (motion.getType() == MovementType.Exit) {
                accumulator.totalExits++;
            }
        }

        return activityByUser.entrySet()
                .stream()
                .map(entry -> toResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator
                        .comparing(UserActivityReportResponse::getTotalMovements, Comparator.reverseOrder())
                        .thenComparing(UserActivityReportResponse::getUserName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private String normalizeRole(String role) {
        return role == null || role.isBlank() ? null : role.trim();
    }

    private UserActivityReportResponse toResponse(UserActivityKey key, UserActivityAccumulator accumulator) {
        return new UserActivityReportResponse(
                key.userId(),
                key.userName(),
                key.userRole(),
                accumulator.totalMovements,
                accumulator.totalEntrances,
                accumulator.totalExits,
                resolveActivityLevel(accumulator.totalMovements)
        );
    }

    private String resolveActivityLevel(Long totalMovements) {
        if (totalMovements >= 10) {
            return "Alta";
        }

        if (totalMovements >= 4) {
            return "Media";
        }

        return "Baja";
    }

    private String firstNotBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private record UserActivityKey(Long userId, String userName, String userRole) {
    }

    private static class UserActivityAccumulator {
        private long totalMovements;
        private long totalEntrances;
        private long totalExits;
    }

}
