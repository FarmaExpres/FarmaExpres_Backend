package co.edu.corhuila.auth_service.Service;


import co.edu.corhuila.auth_service.Entity.Binnacle;
import co.edu.corhuila.auth_service.Entity.Role;
import co.edu.corhuila.auth_service.Entity.UserStatus;
import co.edu.corhuila.auth_service.Entity.User;
import co.edu.corhuila.auth_service.Repository.BinnacleRepository;
import co.edu.corhuila.auth_service.Repository.RoleRepository;
import co.edu.corhuila.auth_service.Repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class userService {

    private final UserRepository userRepository;
    private final RoleRepository rolRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final BinnacleRepository binnacleRepository;

    public userService(UserRepository userRepository,
                       RoleRepository rolRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       BinnacleRepository binnacleRepository) {
        this.userRepository = userRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.binnacleRepository = binnacleRepository;
    }

    // =========================
    // Crear Usuario
    // =========================


    public User CreateUser(String name,
                             String email,
                             String password,
                             String nameRole) {

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya está registrado");
        }

        Role role = rolRepository.findByName(nameRole)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        String Encryptedpassword = passwordEncoder.encode(password);

        User user = new User(
                name,
                email,
                Encryptedpassword,
                role
        );

        user.setState(UserStatus.Asset);

        User userSaved = userRepository.save(user);

        binnacleRepository.save(
                new Binnacle(userSaved.getId(), "CREACION_USUARIO")
        );


        return userSaved;
    }

    // =========================
    // Cambiar Contraseña
    // =========================


    public void changePassword(Long usurId,
                                String currentpassword,
                                String newPassword
    ) {

        User user = userRepository.findById(usurId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(currentpassword, user.getPassword())) {
            throw new RuntimeException("Contraseña actual incorrecta");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        binnacleRepository.save(
                new Binnacle(user.getId(), "CAMBIO_PASSWORD")
        );
    }


}
