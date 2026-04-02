package co.edu.corhuila.inventory_service.Service;

import co.edu.corhuila.inventory_service.Dto.ProductOutOfStockResponse;
import co.edu.corhuila.inventory_service.Entity.Motion;
import co.edu.corhuila.inventory_service.Entity.Product;
import co.edu.corhuila.inventory_service.Entity.MovementType;
import co.edu.corhuila.inventory_service.Repository.MotionRepository;
import co.edu.corhuila.inventory_service.Repository.ProductRepository;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {
    private static final String SYSTEM_USER_NAME = "SYSTEM_INIT";
    private static final String SYSTEM_USER_EMAIL = "system@farmaexpres.local";
    private static final String SYSTEM_USER_ROLE = "SYSTEM";

    private final ProductRepository productRepository;
    private final MotionRepository motionRepository;

    public ProductService(ProductRepository productRepository,
                          MotionRepository motionRepository) {
        this.productRepository = productRepository;
        this.motionRepository = motionRepository;
    }

        // Método para crear un nuevo producto
        public Product createProduct(Product product) {
        validateProductData(product);

        if (productRepository.existsByCode(product.getCode())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El código del producto ya existe"
            );
        }

        Product productSaved = productRepository.save(product);
        MotionActorContext actor = extractMotionActor();

        Motion motion = new Motion(
                MovementType.Entrance,
                productSaved.getStock(),
                productSaved,
                "Creación de producto",
                actor.userId(),
                actor.userName(),
                actor.userEmail(),
                actor.userRole()
        );
        motionRepository.save(motion);

        return productSaved;
    }

    // Método para actualizar un producto existente
    @Transactional
    public Product updateProduct(Long id, Product updatedData) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));

        if (!product.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede modificar un producto eliminado"
            );
        }

        validateProductData(updatedData);

        Integer previousStock = product.getStock();

        product.setName(updatedData.getName());
        product.setUnitPrice(updatedData.getUnitPrice());
        product.setStock(updatedData.getStock());
        product.setMinimumStock(updatedData.getMinimumStock());
        product.setExpirationDate(updatedData.getExpirationDate());

        Product productSaved = productRepository.save(product);
        MotionActorContext actor = extractMotionActor();

        Integer newStock = updatedData.getStock();
        MovementType movementType;
        Integer quantityMovement;
        String reason;

        if (newStock > previousStock) {
            movementType = MovementType.Entrance;
            quantityMovement = newStock - previousStock;
            reason = "Entrada por ajuste de inventario";
        } else if (newStock < previousStock) {
            movementType = MovementType.Exit;
            quantityMovement = previousStock - newStock;
            reason = "Salida por ajuste de inventario";
        } else {
            movementType = MovementType.Updated;
            quantityMovement = 0;
            reason = "Actualización de datos del producto";
        }

        Motion motion = new Motion(
                movementType,
                quantityMovement,
                productSaved,
                reason,
                actor.userId(),
                actor.userName(),
                actor.userEmail(),
                actor.userRole()
        );
        motionRepository.save(motion);

        return productSaved;
    }

    // Método para eliminar un producto 
    @Transactional
    public void removeProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));

        product.setActive(false);
        productRepository.save(product);
        MotionActorContext actor = extractMotionActor();

        Motion motion = new Motion(
                MovementType.Deleted,
                product.getStock(),
                product,
                "Eliminación lógica del producto",
                actor.userId(),
                actor.userName(),
                actor.userEmail(),
                actor.userRole()
        );
        motionRepository.save(motion);
    }

    // Método para listar todos los productos
    public List<Product> listProducts() {
        return productRepository.findAll();
    }
    // Método para listar solo los productos activos

    public List<Product> listActiveProducts() {
        return productRepository.findByActiveTrue();
    }
    // Método para listar productos que estan vacios
    public List<ProductOutOfStockResponse> outOfStockProducts() {
        return productRepository.findByStockAndActiveTrue(0)
                .stream()
                .map(ProductOutOfStockResponse::new)
                .toList();
    }

        // Método para validar los datos del producto
    private void validateProductData(Product product) {
        if (product.getStock() == null || product.getStock() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El stock debe ser mayor o igual a 0"
            );
        }

        if (product.getMinimumStock() == null || product.getMinimumStock() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El stock mínimo debe ser mayor o igual a 0"
            );
        }
    }

    private MotionActorContext extractMotionActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new MotionActorContext(null, SYSTEM_USER_NAME, SYSTEM_USER_EMAIL, SYSTEM_USER_ROLE);
        }

        String email = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .map(authority -> authority.replace("ROLE_", ""))
                .orElse(SYSTEM_USER_ROLE);

        String name = null;
        Long userId = null;
        Object details = authentication.getDetails();
        if (details instanceof Claims claims) {
            name = claims.get("name", String.class);
            userId = claims.get("userId", Long.class);
        }

        return new MotionActorContext(
                userId,
                name == null || name.isBlank() ? SYSTEM_USER_NAME : name,
                email == null || email.isBlank() ? SYSTEM_USER_EMAIL : email,
                role == null || role.isBlank() ? SYSTEM_USER_ROLE : role
        );
    }

    private record MotionActorContext(Long userId, String userName, String userEmail, String userRole) {}
}
