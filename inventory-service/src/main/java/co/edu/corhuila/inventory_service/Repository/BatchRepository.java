package co.edu.corhuila.inventory_service.Repository;

import co.edu.corhuila.inventory_service.Entity.Batch;
import co.edu.corhuila.inventory_service.Entity.BatchStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    interface FefoSnapshotProjection {
        Long getProductId();
        String getProductCode();
        String getProductName();
        Integer getOperationalStock();
        String getNextBatchCode();
        java.time.LocalDate getNextExpirationDate();
        Integer getActiveBatchesCount();
    }

    interface InventoryAlertBatchProjection {
        Long getProductId();
        String getProductCode();
        String getProductName();
        Integer getMinimumStock();
        Long getBatchId();
        String getBatchCode();
        java.time.LocalDate getExpirationDate();
        Integer getAvailableStock();
        Integer getBatchStock();
        Integer getExpiredBatchStock();
        Integer getOperationalStock();
        String getStatus();
    }

    List<Batch> findByProductIdOrderByExpirationDateAsc(Long productId);

    @Query("""
            SELECT b
            FROM Batch b
            WHERE b.product.id = :productId
              AND b.product.active = true
              AND b.status IN :statuses
              AND b.availableStock > 0
              AND b.expirationDate >= CURRENT_DATE
            ORDER BY b.createdAt ASC, b.id ASC
            """)
    List<Batch> findConsumableBatchesByProductIdOrderByCreatedAtAsc(Long productId, Collection<BatchStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT b
            FROM Batch b
            WHERE b.product.id = :productId
              AND b.product.active = true
              AND b.status IN :statuses
              AND b.availableStock > 0
              AND b.expirationDate >= CURRENT_DATE
            ORDER BY b.createdAt ASC, b.id ASC
            """)
    List<Batch> findConsumableBatchesByProductIdOrderByCreatedAtAscForUpdate(
            Long productId,
            Collection<BatchStatus> statuses
    );

    boolean existsByProductIdAndBatchCodeIgnoreCase(Long productId, String batchCode);

    Optional<Batch> findByIdAndProductId(Long batchId, Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT b
            FROM Batch b
            WHERE b.id = :batchId
              AND b.product.id = :productId
            """)
    Optional<Batch> findByIdAndProductIdForUpdate(Long batchId, Long productId);

    @Query("""
            SELECT b
            FROM Batch b
            WHERE b.product.id = :productId
              AND b.product.active = true
              AND b.status IN :statuses
              AND b.availableStock > 0
              AND b.expirationDate >= CURRENT_DATE
            ORDER BY b.expirationDate ASC, b.id ASC
            """)
    List<Batch> findConsumableBatchesByProductId(Long productId, Collection<BatchStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT b
            FROM Batch b
            WHERE b.product.id = :productId
              AND b.product.active = true
              AND b.status IN :statuses
              AND b.availableStock > 0
              AND b.expirationDate >= CURRENT_DATE
            ORDER BY b.expirationDate ASC, b.id ASC
            """)
    List<Batch> findConsumableBatchesByProductIdForUpdate(Long productId, Collection<BatchStatus> statuses);

    @Query(value = """
            WITH consumable_batches AS (
              SELECT
                b.product_id,
                b.batch_code,
                b.expiration_date::date AS expiration_date,
                b.available_stock,
                b.id AS batch_id
              FROM batch b
              JOIN product p
                ON p.id = b.product_id
              WHERE b.status = 'ACTIVE'
                AND p.asset = TRUE
                AND b.available_stock > 0
                AND b.expiration_date::date >= (CURRENT_TIMESTAMP AT TIME ZONE 'America/Bogota')::date
            ),
            ranked_batches AS (
              SELECT
                cb.*,
                ROW_NUMBER() OVER (
                  PARTITION BY cb.product_id
                  ORDER BY cb.expiration_date ASC, cb.batch_id ASC
                ) AS rn
              FROM consumable_batches cb
            ),
            agg AS (
              SELECT
                cb.product_id,
                SUM(cb.available_stock)::int AS operational_stock,
                COUNT(*)::int AS active_batches_count
              FROM consumable_batches cb
              GROUP BY cb.product_id
            ),
            next_batch AS (
              SELECT
                rb.product_id,
                rb.batch_code AS next_batch_code,
                rb.expiration_date AS next_expiration_date
              FROM ranked_batches rb
              WHERE rb.rn = 1
            )
            SELECT
              p.id AS productId,
              p.code AS productCode,
              p.name AS productName,
              COALESCE(a.operational_stock, 0) AS operationalStock,
              nb.next_batch_code AS nextBatchCode,
              nb.next_expiration_date AS nextExpirationDate,
              COALESCE(a.active_batches_count, 0) AS activeBatchesCount
            FROM product p
            LEFT JOIN agg a
              ON a.product_id = p.id
            LEFT JOIN next_batch nb
              ON nb.product_id = p.id
            WHERE p.asset = TRUE
            ORDER BY p.name ASC
            """, nativeQuery = true)
    List<FefoSnapshotProjection> findFefoSnapshot();

    @Query(value = """
            WITH operational AS (
              SELECT
                b2.product_id,
                COALESCE(SUM(b2.available_stock), 0)::int AS operational_stock
              FROM batch b2
              WHERE b2.status = 'ACTIVE'
                AND b2.available_stock > 0
                AND b2.expiration_date >= CURRENT_DATE
              GROUP BY b2.product_id
            )
            SELECT
              p.id AS productId,
              p.code AS productCode,
              p.name AS productName,
              p.minimumstock AS minimumStock,
              b.id AS batchId,
              b.batch_code AS batchCode,
              b.expiration_date AS expirationDate,
              b.available_stock AS availableStock,
              b.available_stock AS batchStock,
              CASE WHEN b.expiration_date < CURRENT_DATE THEN b.available_stock ELSE NULL END AS expiredBatchStock,
              COALESCE(o.operational_stock, 0) AS operationalStock,
              b.status AS status
            FROM batch b
            JOIN product p ON p.id = b.product_id
            LEFT JOIN operational o ON o.product_id = p.id
            WHERE p.asset = TRUE
              AND b.status <> 'RETIRED'
              AND b.available_stock > 0
              AND b.expiration_date < CURRENT_DATE
            ORDER BY b.expiration_date ASC, p.name ASC
            """, nativeQuery = true)
    List<InventoryAlertBatchProjection> findExpiredAlertBatches();

    @Query(value = """
            WITH operational AS (
              SELECT
                b2.product_id,
                COALESCE(SUM(b2.available_stock), 0)::int AS operational_stock
              FROM batch b2
              WHERE b2.status = 'ACTIVE'
                AND b2.available_stock > 0
                AND b2.expiration_date >= CURRENT_DATE
              GROUP BY b2.product_id
            )
            SELECT
              p.id AS productId,
              p.code AS productCode,
              p.name AS productName,
              p.minimumstock AS minimumStock,
              b.id AS batchId,
              b.batch_code AS batchCode,
              b.expiration_date AS expirationDate,
              b.available_stock AS availableStock,
              b.available_stock AS batchStock,
              NULL AS expiredBatchStock,
              COALESCE(o.operational_stock, 0) AS operationalStock,
              b.status AS status
            FROM batch b
            JOIN product p ON p.id = b.product_id
            LEFT JOIN operational o ON o.product_id = p.id
            WHERE p.asset = TRUE
              AND b.status <> 'RETIRED'
              AND (:includeExpired = TRUE OR b.expiration_date >= CURRENT_DATE)
              AND (:onlyWithStock = FALSE OR b.available_stock > 0)
              AND b.expiration_date <= CURRENT_DATE + (:daysWindow * INTERVAL '1 day')
            ORDER BY b.expiration_date ASC, p.name ASC
            """, nativeQuery = true)
    List<InventoryAlertBatchProjection> findExpiringAlertBatches(Integer daysWindow, Boolean includeExpired, Boolean onlyWithStock);

    @Query(value = """
            WITH operational AS (
              SELECT
                b2.product_id,
                COALESCE(SUM(b2.available_stock), 0)::int AS operational_stock
              FROM batch b2
              WHERE b2.status = 'ACTIVE'
                AND b2.available_stock > 0
                AND b2.expiration_date >= CURRENT_DATE
              GROUP BY b2.product_id
            )
            SELECT
              p.id AS productId,
              p.code AS productCode,
              p.name AS productName,
              p.minimumstock AS minimumStock,
              b.id AS batchId,
              b.batch_code AS batchCode,
              b.expiration_date AS expirationDate,
              b.available_stock AS availableStock,
              b.available_stock AS batchStock,
              NULL AS expiredBatchStock,
              COALESCE(o.operational_stock, 0) AS operationalStock,
              b.status AS status
            FROM batch b
            JOIN product p ON p.id = b.product_id
            LEFT JOIN operational o ON o.product_id = p.id
            WHERE p.asset = TRUE
              AND b.status <> 'RETIRED'
              AND b.available_stock > 0
              AND b.available_stock <= p.minimumstock
            ORDER BY b.available_stock ASC, p.name ASC
            """, nativeQuery = true)
    List<InventoryAlertBatchProjection> findLowStockAlertBatches();

    @Query(value = """
            WITH operational AS (
              SELECT
                b2.product_id,
                COALESCE(SUM(b2.available_stock), 0)::int AS operational_stock
              FROM batch b2
              WHERE b2.status = 'ACTIVE'
                AND b2.available_stock > 0
                AND b2.expiration_date >= CURRENT_DATE
              GROUP BY b2.product_id
            )
            SELECT
              p.id AS productId,
              p.code AS productCode,
              p.name AS productName,
              p.minimumstock AS minimumStock,
              b.id AS batchId,
              b.batch_code AS batchCode,
              b.expiration_date AS expirationDate,
              b.available_stock AS availableStock,
              b.available_stock AS batchStock,
              NULL AS expiredBatchStock,
              COALESCE(o.operational_stock, 0) AS operationalStock,
              b.status AS status
            FROM batch b
            JOIN product p ON p.id = b.product_id
            LEFT JOIN operational o ON o.product_id = p.id
            WHERE p.asset = TRUE
              AND b.status <> 'RETIRED'
              AND b.available_stock = 0
            ORDER BY b.expiration_date ASC, p.name ASC
            """, nativeQuery = true)
    List<InventoryAlertBatchProjection> findOutOfStockAlertBatches();
}
