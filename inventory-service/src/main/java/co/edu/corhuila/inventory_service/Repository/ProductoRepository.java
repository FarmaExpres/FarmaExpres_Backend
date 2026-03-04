package co.edu.corhuila.inventory_service.Repository;

import co.edu.corhuila.inventory_service.Entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Producto> findAll();

    List<Producto> findByActivoTrue();

}
