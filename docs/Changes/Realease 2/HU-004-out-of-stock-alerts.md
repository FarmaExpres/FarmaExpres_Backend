# HU-004 - Alertas de productos sin stock (alert-service)

## 1. Informacion general
- HU: `HU-004`
- Nombre: Consulta de alertas por productos sin stock
- Microservicio: `alert-service`
- Estado: Implementado
- Rama de trabajo: `HU-AC-dev`

## 2. Objetivo de la HU
Exponer el endpoint de alertas de productos sin stock para su consumo desde API Gateway.

## 3. Endpoint funcional
### Consumo oficial (gateway)
- Metodo: `GET`
- URL: `http://localhost:8080/api/alerts/out-of-stock`

### Endpoint interno del microservicio
- Metodo: `GET`
- URL: `http://localhost:8083/api/alerts/out-of-stock`

## 4. Alcance implementado
- Se agrego ruta de alertas `out-of-stock` en `alert-service`.
- Se implemento flujo por capas: `router -> controller -> service -> repository`.
- Se agrego consulta SQL para productos activos con stock en cero.
- Se incorporo tipo de alerta `OUT_OF_STOCK` con severidad `HIGH`.
- Se habilito consumo via gateway con ruta `Path=/api/alerts/**`.

## 5. Logica de negocio aplicada
Se consideran solo productos que cumplan:
- `asset = TRUE`
- `stock = 0`

Orden de salida:
- `name ASC`

## 6. Archivos modificados
### alert-service
- `alert-service/src/routers/alertRoutes.js`
- `alert-service/src/controllers/outOfStockAlertController.js`
- `alert-service/src/services/outOfStockAlertService.js`
- `alert-service/src/repositories/productRepository.js`
- `alert-service/src/config/constants.js`
- `alert-service/src/models/Alert.js`
- `alert-service/src/models/AlertCollection.js`
- `alert-service/src/models/Product.js`

### Integracion en plataforma
- `api-gateway/src/main/resources/application.yaml`
- `docker-compose.yml`

## 7. Contrato de respuesta
```json
{
  "generatedAt": "2026-03-31T15:13:32.490Z",
  "total": 1,
  "alerts": [
    {
      "type": "OUT_OF_STOCK",
      "severity": "HIGH",
      "message": "Producto sin stock: Acetaminophen 500mg",
      "product": {
        "id": "16",
        "code": "MASD-001",
        "name": "Acetaminophen 500mg",
        "stock": 0,
        "minimumStock": 8,
        "expirationDate": "2026-03-31",
        "active": true
      }
    }
  ]
}
```

## 8. Criterios de aceptacion cubiertos
1. Existe `GET /api/alerts/out-of-stock`.
2. El endpoint responde `200 OK` cuando se procesa correctamente.
3. La respuesta incluye `generatedAt`, `total`, `alerts`.
4. Solo retorna productos activos sin stock (`stock = 0`).
5. Cada alerta incluye `type`, `severity`, `message` y `product`.
6. `product` incluye `id`, `code`, `name`, `stock`, `minimumStock`, `expirationDate`, `active`.
7. El endpoint es consumible desde gateway por `:8080`.

## 9. Evidencia de validacion
Pruebas automatizadas ejecutadas en `alert-service`:
- `status.test.js`
- `lowStockAlerts.test.js`
- `expiredAlerts.test.js`
- `outOfStockAlerts.test.js`

Resultado: todas en estado `pass`.

## 10. Revision general del avance
Estado actual de HUs en `alert-service`:
- HU-001: `status` implementado
- HU-002: `low-stock` implementado
- HU-003: `expired` implementado
- HU-004: `out-of-stock` implementado

El avance va consistente con la arquitectura y con integracion activa en gateway/compose.
