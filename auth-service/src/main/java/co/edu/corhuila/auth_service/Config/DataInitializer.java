package co.edu.corhuila.auth_service.Config;

import co.edu.corhuila.auth_service.Entity.Role;
import co.edu.corhuila.auth_service.Entity.User;
import co.edu.corhuila.auth_service.Entity.UserStatus;
import co.edu.corhuila.auth_service.Repository.RoleRepository;
import co.edu.corhuila.auth_service.Repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        Role adminRole = findOrCreateRole("ADMIN", "Administrador del sistema");
        Role auditorRole = findOrCreateRole("AUDITOR", "Auditoria y consulta");
        Role farmaceuticoRole = findOrCreateRole("FARMACEUTICO", "Operacion de farmacia");

        findOrCreateUser(
                "Nicolas Tello",
                "temenico5@gmail.com",
                "$2a$10$JdJ5yd1.UKKaum5ssGK6Tu/SXA/SEDKBOK313eAvkeL2tyOYaObq6",
                adminRole
        );
        findOrCreateUser(
                "Jose Leonardo Vargas",
                "leonardojv@gmail.com",
                "$2a$10$PhkfSmC/7YOGJFcvennQ5Oz9Mi43ga7wrpl.r8BXwoOCfIPJAvCoC",
                adminRole
        );
        findOrCreateUser(
                "Jersson Fabian Buitrago",
                "jerssson@gmail.com",
                "$2a$10$OrsSJt0u3dsDXtpJroCh.OTkHfJH7y5wvmKLjocJ0zsfDFKSXGge6",
                auditorRole
        );
        findOrCreateUser(
                "Marlon Romero",
                "marlon@gmail.com",
                "$2a$10$hVcBu5n6uuwsdRfoN.QRne5a79A5urgq6G1Ckrz0peBgKFhPmQodS",
                farmaceuticoRole
        );
    }

    private Role findOrCreateRole(String name, String description) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    role.setDescription(description);
                    return roleRepository.save(role);
                });
    }

    private void findOrCreateUser(String name, String email, String encodedPassword, Role role) {
        userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User user = new User(name, email, encodedPassword, role);
                    user.setState(UserStatus.Asset);
                    return userRepository.save(user);
                });
    }
}
