package co.edu.corhuila.inventory_service.Service;

import co.edu.corhuila.inventory_service.Dto.MotionResponse;
import co.edu.corhuila.inventory_service.Entity.Motion;
import co.edu.corhuila.inventory_service.Entity.MovementType;
import co.edu.corhuila.inventory_service.Entity.Product;
import co.edu.corhuila.inventory_service.Repository.MotionRepository;
import co.edu.corhuila.inventory_service.Repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MotionServiceTest {

    @Mock
    private MotionRepository motionRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private MotionService motionService;

    @Test
    void shouldListOnlyEntranceMotion() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Acetaminofen");

        Motion entranceMotion = new Motion();
        entranceMotion.setId(10L);
        entranceMotion.setType(MovementType.Entrance);
        entranceMotion.setAmount(25);
        entranceMotion.setProduct(product);

        when(motionRepository.findByType(MovementType.Entrance))
                .thenReturn(List.of(entranceMotion));

        List<MotionResponse> response = motionService.listEntranceMotion();

        assertEquals(1, response.size());
        assertEquals("Entrance", response.get(0).getType());
        assertEquals(1L, response.get(0).getProductId());
        assertEquals("Acetaminofen", response.get(0).getProductName());
        assertNotNull(response.get(0));
        verify(motionRepository).findByType(MovementType.Entrance);
    }

    @Test
    void shouldListOnlyExitMotion() {
        Product product = new Product();
        product.setId(2L);
        product.setName("Ibuprofeno");

        Motion exitMotion = new Motion();
        exitMotion.setId(20L);
        exitMotion.setType(MovementType.Exit);
        exitMotion.setAmount(8);
        exitMotion.setProduct(product);

        when(motionRepository.findByType(MovementType.Exit))
                .thenReturn(List.of(exitMotion));

        List<MotionResponse> response = motionService.listExitMotion();

        assertEquals(1, response.size());
        assertEquals("Exit", response.get(0).getType());
        assertEquals(2L, response.get(0).getProductId());
        assertEquals("Ibuprofeno", response.get(0).getProductName());
        assertNotNull(response.get(0));
        verify(motionRepository).findByType(MovementType.Exit);
    }
}
