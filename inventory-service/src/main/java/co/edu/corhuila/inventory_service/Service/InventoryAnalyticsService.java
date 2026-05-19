package co.edu.corhuila.inventory_service.Service;

import co.edu.corhuila.inventory_service.Dto.InventoryAnalyticsBatchResponse;
import co.edu.corhuila.inventory_service.Dto.InventoryAnalyticsMovementResponse;
import co.edu.corhuila.inventory_service.Dto.InventoryAnalyticsProductResponse;
import co.edu.corhuila.inventory_service.Dto.InventoryAnalyticsSnapshotResponse;
import co.edu.corhuila.inventory_service.Entity.Batch;
import co.edu.corhuila.inventory_service.Entity.Motion;
import co.edu.corhuila.inventory_service.Entity.Product;
import co.edu.corhuila.inventory_service.Repository.BatchRepository;
import co.edu.corhuila.inventory_service.Repository.MotionRepository;
import co.edu.corhuila.inventory_service.Repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InventoryAnalyticsService {
    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final MotionRepository motionRepository;

    public InventoryAnalyticsService(
            ProductRepository productRepository,
            BatchRepository batchRepository,
            MotionRepository motionRepository
    ) {
        this.productRepository = productRepository;
        this.batchRepository = batchRepository;
        this.motionRepository = motionRepository;
    }

    @Transactional(readOnly = true)
    public InventoryAnalyticsSnapshotResponse buildSnapshot() {
        List<Product> products = productRepository.findByActiveTrue();
        Set<Long> activeProductIds = products.stream()
                .map(Product::getId)
                .collect(Collectors.toSet());

        List<InventoryAnalyticsBatchResponse> batches = batchRepository.findAll().stream()
                .filter(batch -> batch.getProduct() != null && activeProductIds.contains(batch.getProduct().getId()))
                .map(this::toBatchResponse)
                .toList();

        List<InventoryAnalyticsMovementResponse> movements = motionRepository.findAllByOrderByDateTimeDesc().stream()
                .filter(movement -> movement.getProduct() != null && activeProductIds.contains(movement.getProduct().getId()))
                .map(this::toMovementResponse)
                .toList();

        return new InventoryAnalyticsSnapshotResponse(
                Instant.now(),
                products.stream().map(this::toProductResponse).toList(),
                batches,
                movements
        );
    }

    private InventoryAnalyticsProductResponse toProductResponse(Product product) {
        return new InventoryAnalyticsProductResponse(
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getNombreGenerico(),
                product.getConcentracion(),
                product.getFormaFarmaceutica(),
                product.getPresentacion(),
                product.getFormaFarmaceutica(),
                product.getStock(),
                product.getMinimumStock(),
                product.getStockMaximo(),
                product.getUnitPrice(),
                product.getPrecioCompra(),
                product.getPrecioVenta(),
                product.getExpirationDate(),
                product.getActive()
        );
    }

    private InventoryAnalyticsBatchResponse toBatchResponse(Batch batch) {
        return new InventoryAnalyticsBatchResponse(
                batch.getId(),
                batch.getProduct().getId(),
                batch.getBatchCode(),
                batch.getExpirationDate(),
                batch.getInitialStock(),
                batch.getAvailableStock(),
                batch.getStatus() == null ? null : batch.getStatus().name()
        );
    }

    private InventoryAnalyticsMovementResponse toMovementResponse(Motion movement) {
        Product product = movement.getProduct();
        Batch batch = movement.getBatch();

        return new InventoryAnalyticsMovementResponse(
                movement.getId(),
                product.getId(),
                product.getCode(),
                product.getName(),
                batch == null ? null : batch.getId(),
                batch == null ? null : batch.getBatchCode(),
                movement.getType() == null ? null : movement.getType().name(),
                movement.getAmount(),
                movement.getDateTime(),
                movement.getReason(),
                movement.getUserId(),
                movement.getUserName(),
                movement.getUserEmail(),
                movement.getUserRole(),
                product.getStock(),
                product.getMinimumStock(),
                product.getExpirationDate(),
                batch == null ? null : batch.getExpirationDate()
        );
    }
}
