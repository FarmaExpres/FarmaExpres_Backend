package co.edu.corhuila.audit_service.Repository;

import co.edu.corhuila.audit_service.Entity.AuditObservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuditObservationRepository extends JpaRepository<AuditObservation, Long> {

    List<AuditObservation> findAllByOrderByCreatedAtDesc();

    Optional<AuditObservation> findFirstByAuditCaseIdOrderByCreatedAtDesc(Long auditCaseId);

    long countByCreatedByUserNameIsNotNull();
}
