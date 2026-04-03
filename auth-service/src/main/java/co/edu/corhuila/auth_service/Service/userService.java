package co.edu.corhuila.auth_service.Service;


import co.edu.corhuila.auth_service.DTO.UpdateUserRequest;
import co.edu.corhuila.auth_service.Entity.Binnacle;
import co.edu.corhuila.auth_service.Entity.Role;
import co.edu.corhuila.auth_service.Entity.UserStatus;
import co.edu.corhuila.auth_service.Entity.User;
import co.edu.corhuila.auth_service.Repository.BinnacleRepository;
import co.edu.corhuila.auth_service.Repository.RoleRepository;
import co.edu.corhuila.auth_service.Repository.UserRepository;
import co.edu.corhuila.auth_service.Validation.EmailValidator;
import co.edu.corhuila.auth_service.Validation.NameValidator;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
         String normalizedName = NameValidator.normalizeAndValidateOrThrow(name);
         EmailValidator.validateOrThrow(email);

         if (userRepository.existsByEmail(email)) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El email ya está registrado"
                );
        }
        Role role = rolRepository.findByName(nameRole)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Rol no encontrado"
                ));

        String Encryptedpassword = passwordEncoder.encode(password);

        User user = new User(
                normalizedName,
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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

           

        if (currentpassword == null || currentpassword.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña actual es obligatoria"
            );
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La nueva contraseña es obligatoria"
            );
        }

        if (newPassword.length() < 8) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La nueva contraseña debe tener al menos 8 caracteres"
            );
        }

        if (!passwordEncoder.matches(currentpassword, user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Contraseña actual incorrecta"
            );
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La nueva contraseña debe ser diferente de la contraseña actual"
            );
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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));
                
    if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
        EmailValidator.validateOrThrow(request.getEmail());
        userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El email ya está registrado"
            );
        });
        user.setEmail(request.getEmail());
    }

    if (request.getName() != null) {
        user.setName(NameValidator.normalizeAndValidateOrThrow(request.getName()));
    }

    if (request.getRole() != null && !request.getRole().isBlank()) {
        Role role = rolRepository.findByName(request.getRole())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Rol no encontrado"
                ));
        user.setRole(role);
    }

    User updatedUser = userRepository.save(user);

    binnacleRepository.save(
            new Binnacle(userId, "ACTUALIZACION_USUARIO")
    );

    return updatedUser;
}
}
