package co.edu.corhuila.audit_service.Service;

import co.edu.corhuila.audit_service.Dto.InventoryMovementResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(
            @Value("${inventory.service-url}") String inventoryServiceUrl,
            @Value("${inventory.timeout-ms}") long timeoutMs
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(inventoryServiceUrl)
                .requestFactory(new org.springframework.http.client.JdkClientHttpRequestFactory(
                        java.net.http.HttpClient.newBuilder()
                                .connectTimeout(Duration.ofMillis(timeoutMs))
                                .build()
                ))
                .build();
    }

    public List<InventoryMovementResponse> listMovements(String authorizationHeader) {
        return restClient.get()
                .uri("/api/movements")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new ResponseStatusException(response.getStatusCode(), "No fue posible consultar movimientos");
                })
                .body(new ParameterizedTypeReference<>() {});
    }

    public InventoryMovementResponse getMovement(Long movementId, String authorizationHeader) {
        return restClient.get()
                .uri("/api/movements/{id}", movementId)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new ResponseStatusException(response.getStatusCode(), "Movimiento no encontrado");
                })
                .body(InventoryMovementResponse.class);
    }

    public void updateAuditStatus(Long movementId, String status, String observation, String authorizationHeader) {
        restClient.patch()
                .uri("/api/movements/{id}/audit-status", movementId)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .body(Map.of(
                        "status", status,
                        "observation", observation == null ? "" : observation
                ))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new ResponseStatusException(response.getStatusCode(), "No fue posible sincronizar estado de auditoria en movimientos");
                })
                .toBodilessEntity();
    }
}
