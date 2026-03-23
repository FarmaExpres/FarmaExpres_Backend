package co.edu.corhuila.auth_service.Repository;

import co.edu.corhuila.auth_service.Entity.Binnacle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BinnacleRepository extends JpaRepository<Binnacle, Long> {

    List<Binnacle> findByuserId(Long userId);
}
