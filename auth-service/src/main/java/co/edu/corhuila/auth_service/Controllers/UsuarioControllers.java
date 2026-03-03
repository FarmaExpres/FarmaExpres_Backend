package co.edu.corhuila.auth_service.Controllers;

import co.edu.corhuila.auth_service.DTO.UsuarioRequest;
import co.edu.corhuila.auth_service.DTO.UsuarioResponse;
import co.edu.corhuila.auth_service.Entity.Usuario;
import co.edu.corhuila.auth_service.Service.UsuarioService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
