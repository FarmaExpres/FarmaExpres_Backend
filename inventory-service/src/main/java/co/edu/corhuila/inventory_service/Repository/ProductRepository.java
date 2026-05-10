package co.edu.corhuila.inventory_service.Repository;

import co.edu.corhuila.inventory_service.Entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface ProductRepository extends JpaRepository<Product, Long> {

    interface InventoryExpiringProductProjection {
        Long getId();
        String getCode();
        String getName();
        Integer getStock();
        Integer getMinimumStock();
        java.time.LocalDate getExpirationDate();
        Boolean getActive();
    }

    boolean existsByCode(String code);

    List<Product> findAll();
    
    List<Product> findByActiveTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :productId")
    Optional<Product> findByIdForUpdate(Long productId);

    @Query(value = """
            SELECT
              p.id AS id,
              p.code AS code,
              p.name AS name,
              p.stock AS stock,
              p.minimumstock AS minimumStock,
              p.expirationdate AS expirationDate,
              p.asset AS active
            FROM product p
            WHERE p.asset = TRUE
              AND p.expirationdate >= CURRENT_DATE + (:minDays * INTERVAL '1 day')
              AND p.expirationdate <= CURRENT_DATE + (:maxDays * INTERVAL '1 day')
            ORDER BY p.expirationdate ASC, p.name ASC
            """, nativeQuery = true)
    List<InventoryExpiringProductProjection> findProductsExpiringBetweenDays(Integer minDays, Integer maxDays);

    @Query(value = """
            SELECT
              p.id AS id,
              p.code AS code,
              p.name AS name,
              p.stock AS stock,
              p.minimumstock AS minimumStock,
              p.expirationdate AS expirationDate,
              p.asset AS active
            FROM product p
            WHERE p.asset = TRUE
              AND p.expirationdate <= CURRENT_DATE + (:maxDaysWindow * INTERVAL '1 day')
            ORDER BY p.expirationdate ASC, p.name ASC
            """, nativeQuery = true)
    List<InventoryExpiringProductProjection> findExpiringReportProducts(Integer maxDaysWindow);
}
