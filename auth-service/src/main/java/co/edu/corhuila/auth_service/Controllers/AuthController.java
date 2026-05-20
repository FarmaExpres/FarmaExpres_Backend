package co.edu.corhuila.auth_service.Controllers;


import co.edu.corhuila.auth_service.DTO.LoginRequest;
import co.edu.corhuila.auth_service.DTO.LoginResponseDto;
import co.edu.corhuila.auth_service.DTO.LogoutRequest;
import co.edu.corhuila.auth_service.DTO.RefreshTokenRequest;
import co.edu.corhuila.auth_service.Service.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;


    public AuthController(AuthService authService) {
        this.authService = authService;

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequest request) {
        LoginResponseDto response = authService.login(
                request.getEmail(),
                request.getPassword()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

}
