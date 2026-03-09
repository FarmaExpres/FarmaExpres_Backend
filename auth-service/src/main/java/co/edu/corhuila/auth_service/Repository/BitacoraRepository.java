package co.edu.corhuila.auth_service.Repository;

import co.edu.corhuila.auth_service.Entity.Bitacora;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BitacoraRepository extends JpaRepository<Bitacora, Long> {

    List<Bitacora> findByUsuarioId(Long usuarioId);
}
