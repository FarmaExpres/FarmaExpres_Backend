package co.edu.corhuila.inventory_service.Service;

import co.edu.corhuila.inventory_service.Entity.Batch;
import co.edu.corhuila.inventory_service.Entity.BatchStatus;
import co.edu.corhuila.inventory_service.Entity.Product;
import co.edu.corhuila.inventory_service.Repository.BatchRepository;
import co.edu.corhuila.inventory_service.Repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class InventoryConcurrencyGuard {

    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;

    public InventoryConcurrencyGuard(ProductRepository productRepository, BatchRepository batchRepository) {
        this.productRepository = productRepository;
        this.batchRepository = batchRepository;
    }

    public Product lockOperableProduct(Long productId, String inactiveProductMessage) {
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, inactiveProductMessage);
        }

        return product;
    }

    public Batch lockBatchForProduct(Long productId, Long batchId) {
        return batchRepository.findByIdAndProductIdForUpdate(batchId, productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lote no encontrado para el producto"
                ));
    }

    public List<Batch> lockConsumableBatchesByRegistrationOrder(Long productId) {
        return batchRepository.findConsumableBatchesByProductIdOrderByCreatedAtAscForUpdate(
                productId,
                List.of(BatchStatus.ACTIVE)
        );
    }

    public List<Batch> lockConsumableBatchesByFefo(Long productId) {
        return batchRepository.findConsumableBatchesByProductIdForUpdate(
                productId,
                List.of(BatchStatus.ACTIVE)
        );
    }
}
