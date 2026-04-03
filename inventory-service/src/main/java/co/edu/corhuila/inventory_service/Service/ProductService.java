package co.edu.corhuila.inventory_service.Service;

import co.edu.corhuila.inventory_service.Dto.AdjustmentDetailItem;
import co.edu.corhuila.inventory_service.Dto.ActiveInventorySummaryResponse;
import co.edu.corhuila.inventory_service.Dto.ProductOutOfStockResponse;
import co.edu.corhuila.inventory_service.Entity.MovementType;
import co.edu.corhuila.inventory_service.Entity.Motion;
import co.edu.corhuila.inventory_service.Entity.Product;
import co.edu.corhuila.inventory_service.Repository.MotionRepository;
import co.edu.corhuila.inventory_service.Repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class ProductService {
    private static final String SYSTEM_USER_NAME = "SYSTEM_INIT";
    private static final String SYSTEM_USER_EMAIL = "system@farmaexpres.local";
    private static final String SYSTEM_USER_ROLE = "SYSTEM";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

        String previousName = product.getName();
        BigDecimal previousUnitPrice = product.getUnitPrice();
        Integer previousStock = product.getStock();
        Integer previousMinimumStock = product.getMinimumStock();
        LocalDate previousExpirationDate = product.getExpirationDate();

        product.setName(updatedData.getName());
        product.setUnitPrice(updatedData.getUnitPrice());
        product.setStock(updatedData.getStock());
        product.setMinimumStock(updatedData.getMinimumStock());
        product.setExpirationDate(updatedData.getExpirationDate());

        Integer newStock = updatedData.getStock();
        boolean stockChanged = !Objects.equals(newStock, previousStock);
        boolean nonStockChanges = !Objects.equals(previousName, updatedData.getName())
                || !areBigDecimalValuesEqual(previousUnitPrice, updatedData.getUnitPrice())
                || !Objects.equals(previousMinimumStock, updatedData.getMinimumStock())
                || !Objects.equals(previousExpirationDate, updatedData.getExpirationDate());

        if (!stockChanged && !nonStockChanges) {
            return product;
        }

        Product productSaved = productRepository.save(product);
        MotionActorContext actor = extractMotionActor();

        if (stockChanged) {
            MovementType stockMovementType = newStock > previousStock ? MovementType.Entrance : MovementType.Exit;
            Integer stockMovementAmount = Math.abs(newStock - previousStock);
            String stockReason = newStock > previousStock
                    ? "Entrada por ajuste de inventario"
                    : "Salida por ajuste de inventario";

            Motion stockMotion = new Motion(
                    stockMovementType,
                    stockMovementAmount,
                    productSaved,
                    stockReason,
                    actor.userId(),
                    actor.userName(),
                    actor.userEmail(),
                    actor.userRole()
            );
            motionRepository.save(stockMotion);
        }

        if (nonStockChanges) {
            Motion updatedMotion = new Motion(
                    MovementType.Updated,
                    0,
                    productSaved,
                    "Ajuste de datos del producto",
                    actor.userId(),
                    actor.userName(),
                    actor.userEmail(),
                    actor.userRole()
            );

            List<AdjustmentDetailItem> adjustmentDetail = buildAdjustmentDetail(
                    previousName,
                    previousUnitPrice,
                    previousMinimumStock,
                    previousExpirationDate,
                    updatedData
            );
            updatedMotion.setAdjustmentSummary(buildAdjustmentSummary(adjustmentDetail));
            updatedMotion.setAdjustmentDetail(serializeAdjustmentDetail(adjustmentDetail));
            motionRepository.save(updatedMotion);
        }

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

    private List<AdjustmentDetailItem> buildAdjustmentDetail(
            String previousName,
            BigDecimal previousUnitPrice,
            Integer previousMinimumStock,
            LocalDate previousExpirationDate,
            Product updatedData
    ) {
        List<AdjustmentDetailItem> detail = new ArrayList<>();

        if (!Objects.equals(previousName, updatedData.getName())) {
            detail.add(new AdjustmentDetailItem(
                    "nombre",
                    "Nombre",
                    previousName,
                    updatedData.getName(),
                    "text"
            ));
        }

        if (!areBigDecimalValuesEqual(previousUnitPrice, updatedData.getUnitPrice())) {
            detail.add(new AdjustmentDetailItem(
                    "precio",
                    "Precio",
                    previousUnitPrice,
                    updatedData.getUnitPrice(),
                    "currency"
            ));
        }

        if (!Objects.equals(previousMinimumStock, updatedData.getMinimumStock())) {
            detail.add(new AdjustmentDetailItem(
                    "stockMinimo",
                    "Stock mínimo",
                    previousMinimumStock,
                    updatedData.getMinimumStock(),
                    "number"
            ));
        }

        if (!Objects.equals(previousExpirationDate, updatedData.getExpirationDate())) {
            detail.add(new AdjustmentDetailItem(
                    "fechaVencimiento",
                    "Fecha de vencimiento",
                    previousExpirationDate != null ? previousExpirationDate.toString() : null,
                    updatedData.getExpirationDate() != null ? updatedData.getExpirationDate().toString() : null,
                    "date"
            ));
        }

        return detail;
    }

    private String buildAdjustmentSummary(List<AdjustmentDetailItem> adjustmentDetail) {
        return adjustmentDetail.stream()
                .map(item -> item.getLabel() + ": "
                        + formatValueForSummary(item.getBefore(), item.getFormat())
                        + " -> "
                        + formatValueForSummary(item.getAfter(), item.getFormat()))
                .reduce((left, right) -> left + "; " + right)
                .orElse(null);
    }

    private String formatValueForSummary(Object value, String format) {
        if (value == null) {
            return "null";
        }

        if ("currency".equals(format)) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "CO"));
            symbols.setGroupingSeparator('.');
            DecimalFormat decimalFormat = new DecimalFormat("#,##0.##", symbols);
            return "$" + decimalFormat.format(value);
        }

        return String.valueOf(value);
    }

    private String serializeAdjustmentDetail(List<AdjustmentDetailItem> adjustmentDetail) {
        try {
            return OBJECT_MAPPER.writeValueAsString(adjustmentDetail);
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo serializar el detalle del ajuste"
            );
        }
    }

    private boolean areBigDecimalValuesEqual(BigDecimal left, BigDecimal right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.compareTo(right) == 0;
    }

    public ActiveInventorySummaryResponse getActiveInventorySummary() {
        List<Product> activeProducts = productRepository.findByActiveTrue();

        int totalStock = activeProducts.stream()
                .map(Product::getStock)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        BigDecimal totalInventoryValue = activeProducts.stream()
                .map(product -> {
                    BigDecimal unitPrice = product.getUnitPrice() != null
                            ? product.getUnitPrice()
                            : BigDecimal.ZERO;
                    int stock = product.getStock() != null
                            ? product.getStock()
                            : 0;
                    return unitPrice.multiply(BigDecimal.valueOf(stock));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ActiveInventorySummaryResponse(totalStock, totalInventoryValue);
    }

    private record MotionActorContext(Long userId, String userName, String userEmail, String userRole) {}
}
