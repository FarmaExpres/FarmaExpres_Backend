package co.edu.corhuila.inventory_service.Config;

import co.edu.corhuila.inventory_service.Entity.Product;
import co.edu.corhuila.inventory_service.Repository.ProductRepository;
import co.edu.corhuila.inventory_service.Service.ProductService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private final ProductRepository productRepository;
    private final ProductService productService;

    public DataInitializer(ProductRepository productRepository,
                           ProductService productService) {
        this.productRepository = productRepository;
        this.productService = productService;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Product> seeds = List.of(
                new Product("Acetaminofen 500 mg", "MED-001", 120, BigDecimal.valueOf(8500), LocalDate.of(2027, 12, 31), 20),
                new Product("Ibuprofeno 400 mg", "MED-002", 95, BigDecimal.valueOf(11200), LocalDate.of(2028, 3, 15), 15),
                new Product("Loratadina 10 mg", "MED-003", 60, BigDecimal.valueOf(6400), LocalDate.of(2027, 11, 20), 10),
                new Product("Omeprazol 20 mg", "MED-004", 80, BigDecimal.valueOf(9800), LocalDate.of(2028, 1, 10), 12),
                new Product("Amoxicilina 500 mg", "MED-005", 45, BigDecimal.valueOf(15400), LocalDate.of(2027, 9, 5), 8),
                new Product("Diclofenaco 50 mg", "MED-006", 70, BigDecimal.valueOf(7600), LocalDate.of(2028, 2, 28), 10),
                new Product("Vitamina C 1 g", "MED-007", 110, BigDecimal.valueOf(6900), LocalDate.of(2028, 6, 30), 18),
                new Product("Salbutamol Inhalador", "MED-008", 35, BigDecimal.valueOf(28900), LocalDate.of(2027, 10, 18), 6),
                new Product("Metformina 850 mg", "MED-009", 90, BigDecimal.valueOf(13200), LocalDate.of(2028, 4, 22), 14),
                new Product("Losartan 50 mg", "MED-010", 75, BigDecimal.valueOf(14100), LocalDate.of(2028, 5, 12), 12)
        );

        for (Product product : seeds) {
            if (!productRepository.existsByCode(product.getCode())) {
                productService.createProduct(product);
            }
        }
    }
}
