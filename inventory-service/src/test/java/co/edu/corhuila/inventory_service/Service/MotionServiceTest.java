package co.edu.corhuila.inventory_service.Service;

import co.edu.corhuila.inventory_service.Dto.MotionResponse;
import co.edu.corhuila.inventory_service.Dto.UserActivityReportResponse;
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

    @Test
    void shouldBuildUsersActivityReportForAllUsers() {
        Product product = new Product();
        product.setId(6L);
        product.setName("Loratadina");

        Motion systemEntranceOne = new Motion();
        systemEntranceOne.setId(60L);
        systemEntranceOne.setType(MovementType.Entrance);
        systemEntranceOne.setAmount(10);
        systemEntranceOne.setProduct(product);

        Motion systemEntranceTwo = new Motion();
        systemEntranceTwo.setId(61L);
        systemEntranceTwo.setType(MovementType.Entrance);
        systemEntranceTwo.setAmount(5);
        systemEntranceTwo.setProduct(product);

        Motion adminExitOne = new Motion();
        adminExitOne.setId(62L);
        adminExitOne.setType(MovementType.Exit);
        adminExitOne.setAmount(2);
        adminExitOne.setProduct(product);
        adminExitOne.setUserId(8L);
        adminExitOne.setUserName("Jose Leonardo Vargas");
        adminExitOne.setUserRole("Administrador");

        Motion adminExitTwo = new Motion();
        adminExitTwo.setId(63L);
        adminExitTwo.setType(MovementType.Exit);
        adminExitTwo.setAmount(1);
        adminExitTwo.setProduct(product);
        adminExitTwo.setUserId(8L);
        adminExitTwo.setUserName("Jose Leonardo Vargas");
        adminExitTwo.setUserRole("Administrador");

        Motion adminUpdate = new Motion();
        adminUpdate.setId(64L);
        adminUpdate.setType(MovementType.Updated);
        adminUpdate.setAmount(0);
        adminUpdate.setProduct(product);
        adminUpdate.setUserId(8L);
        adminUpdate.setUserName("Jose Leonardo Vargas");
        adminUpdate.setUserRole("Administrador");

        Motion pharmacistEntrance = new Motion();
        pharmacistEntrance.setId(65L);
        pharmacistEntrance.setType(MovementType.Entrance);
        pharmacistEntrance.setAmount(3);
        pharmacistEntrance.setProduct(product);
        pharmacistEntrance.setUserId(9L);
        pharmacistEntrance.setUserName("Maria Perez");
        pharmacistEntrance.setUserRole("Farmaceutico");

        when(motionRepository.findAllByOrderByDateTimeDesc())
                .thenReturn(List.of(
                        systemEntranceOne,
                        systemEntranceTwo,
                        adminExitOne,
                        adminExitTwo,
                        adminUpdate,
                        pharmacistEntrance
                ));

        List<UserActivityReportResponse> response = motionService.listUsersActivityReport(null);

        assertEquals(3, response.size());
        assertEquals(8L, response.get(0).getUserId());
        assertEquals("Jose Leonardo Vargas", response.get(0).getUserName());
        assertEquals("Administrador", response.get(0).getUserRole());
        assertEquals(3L, response.get(0).getTotalMovements());
        assertEquals(0L, response.get(0).getTotalEntrances());
        assertEquals(2L, response.get(0).getTotalExits());
        assertEquals("Baja", response.get(0).getActivityLevel());

        assertEquals(null, response.get(1).getUserId());
        assertEquals("Sistema", response.get(1).getUserName());
        assertEquals("Automatico", response.get(1).getUserRole());
        assertEquals(2L, response.get(1).getTotalMovements());
        assertEquals(2L, response.get(1).getTotalEntrances());
        assertEquals(0L, response.get(1).getTotalExits());

        assertEquals(9L, response.get(2).getUserId());
        assertEquals("Maria Perez", response.get(2).getUserName());
        assertEquals("Farmaceutico", response.get(2).getUserRole());
        assertEquals(1L, response.get(2).getTotalMovements());
        assertEquals(1L, response.get(2).getTotalEntrances());
        assertEquals(0L, response.get(2).getTotalExits());

        verify(motionRepository).findAllByOrderByDateTimeDesc();
    }

    @Test
    void shouldFilterUsersActivityReportByRole() {
        Product product = new Product();
        product.setId(7L);
        product.setName("Omeprazol");

        Motion adminExitOne = new Motion();
        adminExitOne.setId(70L);
        adminExitOne.setType(MovementType.Exit);
        adminExitOne.setAmount(2);
        adminExitOne.setProduct(product);
        adminExitOne.setUserId(8L);
        adminExitOne.setUserName("Jose Leonardo Vargas");
        adminExitOne.setUserRole("Administrador");

        Motion adminExitTwo = new Motion();
        adminExitTwo.setId(71L);
        adminExitTwo.setType(MovementType.Exit);
        adminExitTwo.setAmount(4);
        adminExitTwo.setProduct(product);
        adminExitTwo.setUserId(8L);
        adminExitTwo.setUserName("Jose Leonardo Vargas");
        adminExitTwo.setUserRole("Administrador");

        Motion pharmacistEntrance = new Motion();
        pharmacistEntrance.setId(72L);
        pharmacistEntrance.setType(MovementType.Entrance);
        pharmacistEntrance.setAmount(3);
        pharmacistEntrance.setProduct(product);
        pharmacistEntrance.setUserId(9L);
        pharmacistEntrance.setUserName("Maria Perez");
        pharmacistEntrance.setUserRole("Farmaceutico");

        when(motionRepository.findAllByOrderByDateTimeDesc())
                .thenReturn(List.of(adminExitOne, adminExitTwo, pharmacistEntrance));

        List<UserActivityReportResponse> response = motionService.listUsersActivityReport("Administrador");

        assertEquals(1, response.size());
        assertEquals(8L, response.get(0).getUserId());
        assertEquals("Jose Leonardo Vargas", response.get(0).getUserName());
        assertEquals("Administrador", response.get(0).getUserRole());
        assertEquals(2L, response.get(0).getTotalMovements());
        assertEquals(0L, response.get(0).getTotalEntrances());
        assertEquals(2L, response.get(0).getTotalExits());

        verify(motionRepository).findAllByOrderByDateTimeDesc();
    }
}
