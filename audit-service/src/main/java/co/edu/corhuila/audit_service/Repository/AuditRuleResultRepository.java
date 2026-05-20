package co.edu.corhuila.audit_service.Repository;

import co.edu.corhuila.audit_service.Entity.AuditRuleResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRuleResultRepository extends JpaRepository<AuditRuleResult, Long> {
}
