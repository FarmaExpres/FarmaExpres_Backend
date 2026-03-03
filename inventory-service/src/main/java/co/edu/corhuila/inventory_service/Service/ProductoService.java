package co.edu.corhuila.inventory_service.Service;

import co.edu.corhuila.inventory_service.Entity.Movimiento;
import co.edu.corhuila.inventory_service.Entity.Producto;
import co.edu.corhuila.inventory_service.Entity.TipoMovimiento;
import co.edu.corhuila.inventory_service.Repository.MovimientoRepository;
import co.edu.corhuila.inventory_service.Repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {


    private final ProductoRepository productoRepository;
    private final MovimientoRepository movimientoRepository;

    public ProductoService(ProductoRepository productoRepository,
                           MovimientoRepository movimientoRepository) {
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
    }

    public Producto crearProducto(Producto producto) {

        if (productoRepository.existsByCodigo(producto.getCodigo())) {
            throw new RuntimeException("El código ya existe");
        }

        Producto productoGuardado = productoRepository.save(producto);

        Movimiento movimiento = new Movimiento(
                TipoMovimiento.ENTRADA,
                productoGuardado.getStock(),
                productoGuardado
        );

        movimientoRepository.save(movimiento);

        return productoGuardado;
    }

    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }
    public Producto actualizarProducto(Producto producto) {
        // 1. Guardamos el producto (como tiene ID, JPA hace un UPDATE)
        Producto productoGuardado = productoRepository.save(producto);

        // 2. Creamos el registro en la tabla de movimientos como ACTUALIZADO
        Movimiento movimiento = new Movimiento(
                TipoMovimiento.ACTUALIZADO,
                productoGuardado.getStock(),
                productoGuardado
        );

        movimientoRepository.save(movimiento);

        return productoGuardado;
    }


    public void eliminar(Long id) {
        productoRepository.deleteById(id);
    }
}

