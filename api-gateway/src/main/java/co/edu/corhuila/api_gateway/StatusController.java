package co.edu.corhuila.api_gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.time.OffsetDateTime;

@RestController
public class StatusController {
    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of(
                "status", "UP",
                "service", "api-gateway",
                "timestamp", OffsetDateTime.now().toString()
        );
    }
}
