package co.edu.corhuila.audit_service.Repository;

import co.edu.corhuila.audit_service.Entity.AuditCase;
import co.edu.corhuila.audit_service.Entity.AuditCaseSource;
import co.edu.corhuila.audit_service.Entity.AuditCaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AuditCaseRepository extends JpaRepository<AuditCase, Long> {

    Optional<AuditCase> findFirstByMovementIdAndStatusIn(Long movementId, Collection<AuditCaseStatus> statuses);

    Optional<AuditCase> findFirstByMovementIdOrderByCreatedAtDesc(Long movementId);

    List<AuditCase> findByStatusInOrderByPriorityDescCreatedAtDesc(Collection<AuditCaseStatus> statuses);

    long countBySource(AuditCaseSource source);

    long countByStatus(AuditCaseStatus status);
}
