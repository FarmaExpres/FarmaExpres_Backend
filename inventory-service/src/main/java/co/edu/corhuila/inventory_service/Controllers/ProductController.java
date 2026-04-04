package co.edu.corhuila.inventory_service.Controllers;

import co.edu.corhuila.inventory_service.Dto.ActiveInventorySummaryResponse;
import co.edu.corhuila.inventory_service.Dto.ActiveInventoryTableItemResponse;
import co.edu.corhuila.inventory_service.Dto.CriticalLowStockResponse;
import co.edu.corhuila.inventory_service.Dto.ProductOutOfStockResponse;
import co.edu.corhuila.inventory_service.Entity.Product;
import co.edu.corhuila.inventory_service.Service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product) {

        Product updated = productService.updateProduct(id, product);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeProduct(@PathVariable Long id) {
        productService.removeProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<Product> listProducts() {
        return productService.listProducts();
    }

    @GetMapping("/Assets")
    public ResponseEntity<List<Product>> listActiveProducts() {
        return ResponseEntity.ok(productService.listActiveProducts());
    }

    @GetMapping("/active-table")
    public ResponseEntity<List<ActiveInventoryTableItemResponse>> getActiveInventoryTable() {
        return ResponseEntity.ok(productService.getActiveInventoryTable());
    }

    @GetMapping("/active-summary")
    public ResponseEntity<ActiveInventorySummaryResponse> getActiveInventorySummary() {
        return ResponseEntity.ok(productService.getActiveInventorySummary());
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<ProductOutOfStockResponse>> outOfStockProducts() {
        return ResponseEntity.ok(productService.outOfStockProducts());
    }

    @GetMapping({"/low-stock/critical", "/low-stock-report/critical"})
    public ResponseEntity<List<CriticalLowStockResponse>> getCriticalLowStockProducts() {
        return ResponseEntity.ok(productService.getCriticalLowStockProducts());
    }
}
