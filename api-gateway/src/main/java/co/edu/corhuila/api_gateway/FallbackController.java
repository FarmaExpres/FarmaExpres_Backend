package co.edu.corhuila.api_gateway;

import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// Start HU-JFBM-001-dev
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> authFallback(ServerHttpRequest request) {
        return ResponseEntity.status(503).body(buildBody("auth-service", request));
    }

    @GetMapping("/inventory")
    public ResponseEntity<Map<String, Object>> inventoryFallback(ServerHttpRequest request) {
        return ResponseEntity.status(503).body(buildBody("inventory-service", request));
    }

    @GetMapping("/alerts")
    public ResponseEntity<Map<String, Object>> alertsFallback(ServerHttpRequest request) {
        return ResponseEntity.status(503).body(buildBody("alert-service", request));
    }

    private Map<String, Object> buildBody(String service, ServerHttpRequest request) {
        String requestId = request.getHeaders().getFirst("X-Request-Id");
        return Map.of(
                "error", service + " unavailable",
                "service", service,
                "path", request.getPath().value(),
                "requestId", requestId == null ? "n/a" : requestId
        );
    }
}
// End-HU-JFBM-001-dev
