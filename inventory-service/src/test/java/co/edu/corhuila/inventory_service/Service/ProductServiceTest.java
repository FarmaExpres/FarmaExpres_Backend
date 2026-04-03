package co.edu.corhuila.inventory_service.Service;

import co.edu.corhuila.inventory_service.Dto.ActiveInventorySummaryResponse;
import co.edu.corhuila.inventory_service.Dto.ActiveInventoryTableItemResponse;
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

    @Test
    void shouldReturnActiveInventoryTable() {
        Product productOne = new Product(
                "Ibuprofeno 400mg",
                "IBU-001",
                80,
                new BigDecimal("3200"),
                LocalDate.of(2027, 6, 30),
                15
        );

        Product productTwo = new Product(
                "Losartan 50 mg",
                "LOS-001",
                75,
                new BigDecimal("14100"),
                LocalDate.of(2027, 8, 10),
                20
        );

        when(productRepository.findByActiveTrue())
                .thenReturn(List.of(productOne, productTwo));

        List<ActiveInventoryTableItemResponse> response = productService.getActiveInventoryTable();

        assertEquals(2, response.size());
        assertEquals("IBU-001", response.get(0).getCode());
        assertEquals("Ibuprofeno 400mg", response.get(0).getName());
        assertEquals(80, response.get(0).getStock());
        assertEquals(new BigDecimal("3200"), response.get(0).getUnitPrice());
        assertEquals(new BigDecimal("256000"), response.get(0).getTotalValue());
        assertEquals("LOS-001", response.get(1).getCode());
        assertEquals(new BigDecimal("1057500"), response.get(1).getTotalValue());
        verify(productRepository).findByActiveTrue();
    }

    @Test
    void shouldReturnEmptyActiveInventoryTableWhenThereAreNoProducts() {
        when(productRepository.findByActiveTrue()).thenReturn(List.of());

        List<ActiveInventoryTableItemResponse> response = productService.getActiveInventoryTable();

        assertEquals(List.of(), response);
        verify(productRepository).findByActiveTrue();
    }
}
