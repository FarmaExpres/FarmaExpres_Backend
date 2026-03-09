package co.edu.corhuila.auth_service.Controllers;

import co.edu.corhuila.auth_service.DTO.CambiarPasswordRequest;
import co.edu.corhuila.auth_service.DTO.UsuarioRequest;
import co.edu.corhuila.auth_service.DTO.UsuarioResponse;
import co.edu.corhuila.auth_service.Entity.Usuario;
import co.edu.corhuila.auth_service.Service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioControllers {

    private final UsuarioService usuarioService;


    public UsuarioControllers(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public UsuarioResponse crearUsuario(@RequestBody UsuarioRequest request) {

        Usuario usuario = usuarioService.crearUsuario(
                request.getNombre(),
                request.getEmail(),
                request.getPassword(),
                request.getRol()
        );

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol().getNombre()
        );
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<String> cambiarPassword(
            @PathVariable Long id,
            @RequestBody CambiarPasswordRequest request) {

        usuarioService.cambiarPassword(
                id,
                request.getPasswordActual(),
                request.getNuevaPassword()
        );

        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }


}
