package co.edu.corhuila.auth_service.Controllers;

import co.edu.corhuila.auth_service.DTO.changePasswordRequest;
import co.edu.corhuila.auth_service.DTO.UpdateUserRequest;
import co.edu.corhuila.auth_service.DTO.UsurRequest;
import co.edu.corhuila.auth_service.DTO.UsurResponse;
import co.edu.corhuila.auth_service.Entity.User;
import co.edu.corhuila.auth_service.Service.userService;

import java.util.List;

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
                user.getRole().getName(),
                user.getState().name()
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

    @GetMapping
    public ResponseEntity<List<UsurResponse>> listUsers() {
    List<User> users = userService.listUsers();

    List<UsurResponse> response = users.stream()
            .map(user -> new UsurResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole().getName(),
                     user.getState().name()
            ))
            .toList();

    return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.ok("Usuario eliminado correctamente");
    }

    @PutMapping("/{id}/block")
    public ResponseEntity<UsurResponse> blockUser(@PathVariable Long id) {
    User user = userService.blockUser(id);

    UsurResponse response = new UsurResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().getName(),
             user.getState().name()
    );

    return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/unlock")
    public ResponseEntity<UsurResponse> unlockUser(@PathVariable Long id) {
    User user = userService.unlockUser(id);

    UsurResponse response = new UsurResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().getName(),
             user.getState().name()
    );

    return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}/update")
    public ResponseEntity<UsurResponse> updateUser(
        @PathVariable Long id,
        @RequestBody UpdateUserRequest request
    ) {
    User user = userService.updateUser(id, request);

    UsurResponse response = new UsurResponse(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getRole().getName(),
        user.getState().name()
    );

    return ResponseEntity.ok(response);
}

}
