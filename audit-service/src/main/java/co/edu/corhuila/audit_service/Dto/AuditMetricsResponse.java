package co.edu.corhuila.audit_service.Dto;

import java.util.List;
import java.util.Map;

public record AuditMetricsResponse(
        Summary summary,
        List<Map<String, Object>> activityByUser,
        List<Map<String, Object>> monthlyTrend,
        List<Map<String, Object>> topMedicines,
        List<Map<String, Object>> casesByUser,
        List<Map<String, Object>> casesByMedicine,
        List<Map<String, Object>> casesByPriority,
        List<Map<String, Object>> casesBySource,
        List<Map<String, Object>> caseMonthlyTrend,
        List<Map<String, Object>> topRiskMedicines
) {
    public record Summary(
            long totalMovements,
            long marked,
            long observations,
            long users,
            long automaticCases,
            long manualCases,
            long openCases,
            long inReviewCases,
            long reviewedCases,
            long closedCases,
            long highPriorityCases,
            long mediumPriorityCases,
            long lowPriorityCases
    ) {
    }
}
