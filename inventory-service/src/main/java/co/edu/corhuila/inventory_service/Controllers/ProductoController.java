package co.edu.corhuila.inventory_service.Controllers;



import co.edu.corhuila.inventory_service.Dto.ProductoSinStockResponse;
import co.edu.corhuila.inventory_service.Entity.Producto;
import co.edu.corhuila.inventory_service.Service.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }
    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody Producto producto) {
        return ResponseEntity.ok(productoService.crearProducto(producto));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(
            @PathVariable Long id,
            @RequestBody Producto producto) {

        Producto actualizado = productoService.actualizarProducto(id, producto);

        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.ok("Producto eliminado correctamente");
    }

    @GetMapping
    public List<Producto> listarProductos() {
        return productoService.listarProductos();
    }

    @GetMapping("/Activos")
    public ResponseEntity<List<Producto>> listarProductosActivos() {
        return ResponseEntity.ok(productoService.listarProductosActivos());
    }

    @GetMapping("/sin-stock")
    public ResponseEntity<List<ProductoSinStockResponse>> obtenerProductosSinStock() {
        return ResponseEntity.ok(productoService.productosSinStock());
    }


}
