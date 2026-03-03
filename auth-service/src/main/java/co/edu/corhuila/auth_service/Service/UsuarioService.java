package co.edu.corhuila.auth_service.Service;


import co.edu.corhuila.auth_service.Entity.Bitacora;
import co.edu.corhuila.auth_service.Entity.EstadoUsuario;
import co.edu.corhuila.auth_service.Entity.Rol;
import co.edu.corhuila.auth_service.Entity.Usuario;
import co.edu.corhuila.auth_service.Repository.BitacoraRepository;
import co.edu.corhuila.auth_service.Repository.RolRepository;
import co.edu.corhuila.auth_service.Repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final BitacoraRepository bitacoraRepository;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          BCryptPasswordEncoder passwordEncoder,
                          BitacoraRepository bitacoraRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.bitacoraRepository = bitacoraRepository;
    }

    // =========================
    // Crear Usuario
    // =========================


    public Usuario crearUsuario(String nombre,
                                String email,
                                String password,
                                String nombreRol) {

        if (usuarioRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya está registrado");
        }

        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        String passwordEncriptado = passwordEncoder.encode(password);

        Usuario usuario = new Usuario(
                nombre,
                email,
                passwordEncriptado,
                rol
        );

        usuario.setEstado(EstadoUsuario.ACTIVO);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        bitacoraRepository.save(
                new Bitacora(usuarioGuardado.getId(), "CREACION_USUARIO")
        );


        return usuarioGuardado;
    }

    // =========================
    // Cambiar Contraseña
    // =========================


    public void cambiarPassword(Long usuarioId,
                                String passwordActual,
                                String nuevaPassword
    ) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new RuntimeException("Contraseña actual incorrecta");
        }

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        bitacoraRepository.save(
                new Bitacora(usuario.getId(), "CAMBIO_PASSWORD")
        );
    }


}
