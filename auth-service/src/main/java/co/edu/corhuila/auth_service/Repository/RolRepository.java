package co.edu.corhuila.auth_service.Repository;

import co.edu.corhuila.auth_service.Entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(String nombre);
}
