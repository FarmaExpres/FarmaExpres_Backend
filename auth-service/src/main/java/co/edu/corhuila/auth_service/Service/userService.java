package co.edu.corhuila.auth_service.Service;


import co.edu.corhuila.auth_service.DTO.UpdateUserRequest;
import co.edu.corhuila.auth_service.Entity.Binnacle;
import co.edu.corhuila.auth_service.Entity.Role;
import co.edu.corhuila.auth_service.Entity.UserStatus;
import co.edu.corhuila.auth_service.Entity.User;
import co.edu.corhuila.auth_service.Repository.BinnacleRepository;
import co.edu.corhuila.auth_service.Repository.RoleRepository;
import co.edu.corhuila.auth_service.Repository.UserRepository;

import java.util.List;

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

    // =========================
    // Listar Usuarios
    // =========================
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    // =========================
    // Eliminar Usuario
    // =========================
    public void deleteUser(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    userRepository.delete(user);

    binnacleRepository.save(
            new Binnacle(userId, "ELIMINACION_USUARIO")
    );
    }

    // =========================
    // Bloquear Usuario
    // =========================
    public User blockUser(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    user.BlockUser();
    User updatedUser = userRepository.save(user);

    binnacleRepository.save(
            new Binnacle(userId, "BLOQUEO_USUARIO")
    );

    return updatedUser;
    }

    // =========================
    // Desbloquear Usuario
    // =========================
    public User unlockUser(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    user.unlock();
    User updatedUser = userRepository.save(user);

    binnacleRepository.save(
            new Binnacle(userId, "DESBLOQUEO_USUARIO")
    );

    return updatedUser;
    }


    // =========================
    // actualizar usuario
    // =========================


    public User updateUser(Long userId, UpdateUserRequest request) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
        userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
            throw new RuntimeException("El email ya está registrado");
        });
        user.setEmail(request.getEmail());
    }

    if (request.getName() != null && !request.getName().isBlank()) {
        user.setName(request.getName());
    }

    if (request.getRole() != null && !request.getRole().isBlank()) {
        Role role = rolRepository.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        user.setRole(role);
    }

    User updatedUser = userRepository.save(user);

    binnacleRepository.save(
            new Binnacle(userId, "ACTUALIZACION_USUARIO")
    );

    return updatedUser;
}
}
