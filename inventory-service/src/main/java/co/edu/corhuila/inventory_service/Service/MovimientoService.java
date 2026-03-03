package co.edu.corhuila.inventory_service.Service;


import co.edu.corhuila.inventory_service.Entity.Movimiento;
import co.edu.corhuila.inventory_service.Entity.Producto;
import co.edu.corhuila.inventory_service.Entity.TipoMovimiento;
import co.edu.corhuila.inventory_service.Repository.MovimientoRepository;
import co.edu.corhuila.inventory_service.Repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final ProductoRepository productoRepository;

    public MovimientoService(MovimientoRepository movimientoRepository,
                             ProductoRepository productoRepository) {
        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository;
    }

    public List<Movimiento> listarMovimientos() {
        return movimientoRepository.findAll();
    }

    public Producto actualizarConMovimiento(Producto producto) {
        // Guardamos el producto actualizado
        Producto productoGuardado = productoRepository.save(producto);

        // Creamos el registro en la tabla de movimientos
        Movimiento movimiento = new Movimiento(
                TipoMovimiento.ACTUALIZADO, // El tipo que querías
                productoGuardado.getStock(),
                productoGuardado
        );

        movimientoRepository.save(movimiento);

        return productoGuardado;
    }

}
