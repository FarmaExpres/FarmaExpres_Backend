package co.edu.corhuila.inventory_service.Service;

import co.edu.corhuila.inventory_service.Entity.Batch;
import co.edu.corhuila.inventory_service.Entity.BatchStatus;
import co.edu.corhuila.inventory_service.Entity.Product;
import co.edu.corhuila.inventory_service.Repository.BatchRepository;
import co.edu.corhuila.inventory_service.Repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryConcurrencyGuardTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BatchRepository batchRepository;

    @InjectMocks
    private InventoryConcurrencyGuard inventoryConcurrencyGuard;

    @Test
    void shouldLockOperableProductBeforeInventoryMutation() {
        Product product = new Product();
        product.setId(8L);
        product.setActive(true);

        when(productRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(product));

        Product lockedProduct = inventoryConcurrencyGuard.lockOperableProduct(
                8L,
                "No se pueden registrar movimientos sobre un medicamento inactivo."
        );

        assertSame(product, lockedProduct);
        verify(productRepository).findByIdForUpdate(8L);
    }

    @Test
    void shouldRejectInactiveProductAfterLockingRow() {
        Product product = new Product();
        product.setId(8L);
        product.setActive(false);

        when(productRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(product));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> inventoryConcurrencyGuard.lockOperableProduct(
                        8L,
                        "No se pueden registrar salidas sobre un medicamento inactivo."
                )
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(productRepository).findByIdForUpdate(8L);
    }

    @Test
    void shouldLockBatchForDirectMovement() {
        Batch batch = new Batch();
        batch.setId(3L);

        when(batchRepository.findByIdAndProductIdForUpdate(3L, 8L)).thenReturn(Optional.of(batch));

        Batch lockedBatch = inventoryConcurrencyGuard.lockBatchForProduct(8L, 3L);

        assertSame(batch, lockedBatch);
        verify(batchRepository).findByIdAndProductIdForUpdate(3L, 8L);
    }

    @Test
    void shouldLockConsumableBatchesForFefoExit() {
        Batch batch = new Batch();
        batch.setId(10L);

        when(batchRepository.findConsumableBatchesByProductIdForUpdate(
                eq(8L),
                org.mockito.ArgumentMatchers.<Collection<BatchStatus>>any()
        )).thenReturn(List.of(batch));

        List<Batch> lockedBatches = inventoryConcurrencyGuard.lockConsumableBatchesByFefo(8L);

        assertEquals(1, lockedBatches.size());
        assertSame(batch, lockedBatches.get(0));
        verify(batchRepository).findConsumableBatchesByProductIdForUpdate(
                eq(8L),
                org.mockito.ArgumentMatchers.<Collection<BatchStatus>>any()
        );
    }
}
