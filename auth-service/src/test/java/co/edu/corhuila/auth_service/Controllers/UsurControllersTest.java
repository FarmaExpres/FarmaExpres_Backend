package co.edu.corhuila.auth_service.Controllers;

import co.edu.corhuila.auth_service.DTO.UsurRequest;
import co.edu.corhuila.auth_service.DTO.UsurResponse;
import co.edu.corhuila.auth_service.Entity.Role;
import co.edu.corhuila.auth_service.Entity.User;
import co.edu.corhuila.auth_service.Service.userService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsurControllersTest {

    @Mock
    private userService userService;

    @InjectMocks
    private UsurControllers usurControllers;

    @Test
    void createUserReturns201() {
        UsurRequest request = mock(UsurRequest.class);
        when(request.getName()).thenReturn("Admin");
        when(request.getEmail()).thenReturn("admin@farma.com");
        when(request.getPassword()).thenReturn("123456");
        when(request.getRole()).thenReturn("ADMIN");

        Role rol = new Role(1, "ADMIN", "Administrador");
        User user = new User("Admin", "admin@farma.com", "123456", rol);
        user.setId(10L);

        when(userService.CreateUser(
                eq("Admin"),
                eq("admin@farma.com"),
                eq("123456"),
                eq("ADMIN")
        )).thenReturn(user);

        ResponseEntity<UsurResponse> response = usurControllers.CreateUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(10L, response.getBody().getId());
        assertEquals("ADMIN", response.getBody().getRole());
        verify(userService).CreateUser("Admin", "admin@farma.com", "123456", "ADMIN");
    }
}
