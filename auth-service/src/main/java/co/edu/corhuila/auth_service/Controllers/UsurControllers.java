package co.edu.corhuila.auth_service.Controllers;

import co.edu.corhuila.auth_service.DTO.changePasswordRequest;
import co.edu.corhuila.auth_service.DTO.UsurRequest;
import co.edu.corhuila.auth_service.DTO.UsurResponse;
import co.edu.corhuila.auth_service.Entity.User;
import co.edu.corhuila.auth_service.Service.userService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UsurControllers {

    private final userService userService;


    public UsurControllers(userService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UsurResponse> CreateUser(@RequestBody UsurRequest request) {

        User user = userService.CreateUser(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getRole()
        );

        UsurResponse response = new UsurResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<String> changePassword(
            @PathVariable Long id,
            @RequestBody changePasswordRequest request) {

        userService.changePassword(
                id,
                request.getCurrentpassword(),
                request.getNewPassword()
        );

        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }


}
