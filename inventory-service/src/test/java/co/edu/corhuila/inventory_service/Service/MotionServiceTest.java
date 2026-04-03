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

import java.time.Instant;
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

    @Test
    void shouldListOnlyUpdatedMotion() {
        Product product = new Product();
        product.setId(3L);
        product.setName("Amoxicilina");

        Motion updatedMotion = new Motion();
        updatedMotion.setId(30L);
        updatedMotion.setType(MovementType.Updated);
        updatedMotion.setAmount(5);
        updatedMotion.setProduct(product);
        updatedMotion.setAdjustmentSummary("Ajuste por conciliacion");

        when(motionRepository.findByType(MovementType.Updated))
                .thenReturn(List.of(updatedMotion));

        List<MotionResponse> response = motionService.listUpdatedMotion();

        assertEquals(1, response.size());
        assertEquals("Updated", response.get(0).getType());
        assertEquals(3L, response.get(0).getProductId());
        assertEquals("Amoxicilina", response.get(0).getProductName());
        assertEquals("Ajuste por conciliacion", response.get(0).getAdjustmentSummary());
        assertNotNull(response.get(0));
        verify(motionRepository).findByType(MovementType.Updated);
    }

    @Test
    void shouldListAllMotionWhenUserFilterIsNotProvided() {
        Product product = new Product();
        product.setId(4L);
        product.setName("Diclofenaco");

        Motion motion = new Motion();
        motion.setId(40L);
        motion.setType(MovementType.Exit);
        motion.setAmount(-10);
        motion.setProduct(product);
        motion.setUserId(7L);
        motion.setUserName("Marlon Romero");
        motion.setUserRole("Farmaceutico");
        motion.setDateTime(Instant.parse("2026-04-03T13:43:00Z"));

        when(motionRepository.findAllByOrderByDateTimeDesc())
                .thenReturn(List.of(motion));

        List<MotionResponse> response = motionService.listMotionByUser(null);

        assertEquals(1, response.size());
        assertEquals(7L, response.get(0).getUserId());
        assertEquals("Marlon Romero", response.get(0).getUserName());
        assertEquals("Farmaceutico", response.get(0).getUserRole());
        verify(motionRepository).findAllByOrderByDateTimeDesc();
    }

    @Test
    void shouldListOnlyMotionForRequestedUser() {
        Product product = new Product();
        product.setId(5L);
        product.setName("Amoxicilina");

        Motion motion = new Motion();
        motion.setId(50L);
        motion.setType(MovementType.Updated);
        motion.setAmount(0);
        motion.setProduct(product);
        motion.setUserId(9L);
        motion.setUserName("Jose Gregorio Cangrejo");
        motion.setUserRole("Farmaceutico");
        motion.setDateTime(Instant.parse("2026-04-03T00:16:07Z"));

        when(motionRepository.findByUserIdOrderByDateTimeDesc(9L))
                .thenReturn(List.of(motion));

        List<MotionResponse> response = motionService.listMotionByUser(9L);

        assertEquals(1, response.size());
        assertEquals(9L, response.get(0).getUserId());
        assertEquals("Jose Gregorio Cangrejo", response.get(0).getUserName());
        assertEquals("Farmaceutico", response.get(0).getUserRole());
        verify(motionRepository).findByUserIdOrderByDateTimeDesc(9L);
    }
}
