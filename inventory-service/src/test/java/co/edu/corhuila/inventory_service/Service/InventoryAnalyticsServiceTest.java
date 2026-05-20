package co.edu.corhuila.inventory_service.Service;

import co.edu.corhuila.inventory_service.Dto.InventoryAnalyticsSnapshotResponse;
import co.edu.corhuila.inventory_service.Entity.Batch;
import co.edu.corhuila.inventory_service.Entity.BatchStatus;
import co.edu.corhuila.inventory_service.Entity.Motion;
import co.edu.corhuila.inventory_service.Entity.MovementType;
import co.edu.corhuila.inventory_service.Entity.Product;
import co.edu.corhuila.inventory_service.Repository.BatchRepository;
import co.edu.corhuila.inventory_service.Repository.MotionRepository;
import co.edu.corhuila.inventory_service.Repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryAnalyticsServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private MotionRepository motionRepository;

    @InjectMocks
    private InventoryAnalyticsService inventoryAnalyticsService;

    @Test
    void buildSnapshotReturnsOnlyActiveProductData() {
        Product activeProduct = product(1L, "FX-001", "Acetaminofen 500 mg", true);
        Product inactiveProduct = product(2L, "FX-002", "Producto inactivo", false);
        Batch activeBatch = batch(10L, activeProduct, "LOT-001");
        Batch inactiveBatch = batch(11L, inactiveProduct, "LOT-002");
        Motion exit = movement(20L, activeProduct, activeBatch, MovementType.Exit, 6);
        Motion inactiveMovement = movement(21L, inactiveProduct, inactiveBatch, MovementType.Exit, 4);

        when(productRepository.findByActiveTrue()).thenReturn(List.of(activeProduct));
        when(batchRepository.findAll()).thenReturn(List.of(activeBatch, inactiveBatch));
        when(motionRepository.findAllByOrderByDateTimeDesc()).thenReturn(List.of(exit, inactiveMovement));

        InventoryAnalyticsSnapshotResponse snapshot = inventoryAnalyticsService.buildSnapshot();

        assertThat(snapshot.products()).hasSize(1);
        assertThat(snapshot.products().get(0).productCode()).isEqualTo("FX-001");
        assertThat(snapshot.batches()).hasSize(1);
        assertThat(snapshot.batches().get(0).batchCode()).isEqualTo("LOT-001");
        assertThat(snapshot.movements()).hasSize(1);
        assertThat(snapshot.movements().get(0).amount()).isEqualTo(6);
    }

    private Product product(Long id, String code, String name, Boolean active) {
        Product product = new Product();
        product.setId(id);
        product.setCode(code);
        product.setName(name);
        product.setStock(42);
        product.setMinimumStock(10);
        product.setStockMaximo(100);
        product.setUnitPrice(BigDecimal.valueOf(2500));
        product.setFormaFarmaceutica("TABLETA");
        product.setExpirationDate(LocalDate.now().plusDays(90));
        product.setActive(active);
        return product;
    }

    private Batch batch(Long id, Product product, String batchCode) {
        Batch batch = new Batch();
        batch.setId(id);
        batch.setProduct(product);
        batch.setBatchCode(batchCode);
        batch.setInitialStock(80);
        batch.setAvailableStock(38);
        batch.setExpirationDate(LocalDate.now().plusDays(120));
        batch.setStatus(BatchStatus.ACTIVE);
        return batch;
    }

    private Motion movement(Long id, Product product, Batch batch, MovementType type, Integer amount) {
        Motion movement = new Motion();
        movement.setId(id);
        movement.setProduct(product);
        movement.setBatch(batch);
        movement.setType(type);
        movement.setAmount(amount);
        movement.setDateTime(Instant.now());
        movement.setReason("Venta local");
        movement.setUserRole("FARMACEUTICO");
        return movement;
    }
}
