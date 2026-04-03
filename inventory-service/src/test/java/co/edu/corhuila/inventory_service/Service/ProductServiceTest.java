package co.edu.corhuila.inventory_service.Service;

import co.edu.corhuila.inventory_service.Dto.ActiveInventorySummaryResponse;
import co.edu.corhuila.inventory_service.Entity.Product;
import co.edu.corhuila.inventory_service.Repository.MotionRepository;
import co.edu.corhuila.inventory_service.Repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MotionRepository motionRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldReturnActiveInventorySummary() {
        Product productOne = new Product(
                "Acetaminofen 500mg",
                "ACM-001",
                100,
                new BigDecimal("2500"),
                LocalDate.of(2027, 12, 31),
                20
        );

        Product productTwo = new Product(
                "Ibuprofeno 400mg",
                "IBU-001",
                80,
                new BigDecimal("3200"),
                LocalDate.of(2027, 6, 30),
                15
        );

        Product productThree = new Product(
                "Loratadina 10mg",
                "LOR-001",
                60,
                new BigDecimal("6400"),
                LocalDate.of(2027, 11, 20),
                10
        );

        when(productRepository.findByActiveTrue())
                .thenReturn(List.of(productOne, productTwo, productThree));

        ActiveInventorySummaryResponse response = productService.getActiveInventorySummary();

        assertEquals(240, response.getTotalStock());
        assertEquals(new BigDecimal("890000"), response.getTotalInventoryValue());
        verify(productRepository).findByActiveTrue();
    }

    @Test
    void shouldReturnZeroSummaryWhenThereAreNoActiveProducts() {
        when(productRepository.findByActiveTrue()).thenReturn(List.of());

        ActiveInventorySummaryResponse response = productService.getActiveInventorySummary();

        assertEquals(0, response.getTotalStock());
        assertEquals(BigDecimal.ZERO, response.getTotalInventoryValue());
        verify(productRepository).findByActiveTrue();
    }
}
