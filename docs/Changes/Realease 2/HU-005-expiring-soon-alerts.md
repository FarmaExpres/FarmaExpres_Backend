# HU-005 - Alertas de productos proximos a vencer (alert-service)

## 1. Informacion general
- HU: `HU-005`
- Nombre: Consulta de alertas de productos proximos a vencer
- Microservicio: `alert-service`
- Estado: Implementado
- Rama de trabajo: `HU-AC-dev`

## 2. Objetivo de la HU
Exponer un endpoint que genere alertas de productos activos a los que les falten hasta 15 dias para vencer.

## 3. Endpoint funcional
### Consumo oficial (gateway)
- Metodo: `GET`
- URL: `http://localhost:8080/api/alerts/expiring-soon`

### Endpoint interno del microservicio
- Metodo: `GET`
- URL: `http://localhost:8083/api/alerts/expiring-soon`

## 4. Alcance implementado
- Se agrego endpoint `/api/alerts/expiring-soon`.
- Se implemento flujo por capas `router -> controller -> service -> repository`.
- Se agrego regla configurable de ventana de vencimiento (`EXPIRING_SOON_DAYS`, default `15`).
- Se agrego tipo de alerta `EXPIRING_SOON`.
- Se incorporo severidad segun dias restantes:
  - `HIGH` si faltan `<= 5` dias
  - `MEDIUM` si faltan `<= 10` dias
  - `LOW` si faltan `<= 15` dias

## 5. Logica de negocio aplicada
Se consideran solo productos que cumplan:
- `asset = TRUE`
- `expirationdate > CURRENT_DATE`
- `expirationdate <= CURRENT_DATE + 15 dias`

Orden de salida:
- `expirationdate ASC`, `name ASC`

## 6. Archivos modificados
- `alert-service/src/config/constants.js`
- `alert-service/src/config/env.js`
- `alert-service/.env.example`
- `alert-service/src/utils/dateUtils.js`
- `alert-service/src/utils/alertUtils.js`
- `alert-service/src/repositories/productRepository.js`
- `alert-service/src/controllers/expiringSoonAlertController.js`
- `alert-service/src/services/expiringSoonAlertService.js`
- `alert-service/src/routers/alertRoutes.js`
- `alert-service/tests/expiringSoonAlerts.test.js`
- `alert-service/package.json`

## 7. Contrato de respuesta
```json
{
  "generatedAt": "2026-03-31T18:22:10.100Z",
  "total": 2,
  "alerts": [
    {
      "type": "EXPIRING_SOON",
      "severity": "MEDIUM",
      "message": "Producto proximo a vencer (10 dias): Loratadina 10 mg",
      "product": {
        "id": "33",
        "code": "EXP-001",
        "name": "Loratadina 10 mg",
        "stock": 12,
        "minimumStock": 5,
        "expirationDate": "2026-04-10",
        "active": true
      }
    }
  ]
}
```

## 8. Criterios de aceptacion cubiertos
1. Existe `GET /api/alerts/expiring-soon`.
2. El endpoint responde `200 OK` cuando se procesa correctamente.
3. La respuesta incluye `generatedAt`, `total`, `alerts`.
4. Solo retorna productos activos proximos a vencer en ventana de 15 dias.
5. Cada alerta incluye `type`, `severity`, `message` y `product`.
6. `product` incluye `id`, `code`, `name`, `stock`, `minimumStock`, `expirationDate`, `active`.
7. El endpoint puede consumirse por gateway en `:8080`.

## 9. Evidencia de validacion
Se agrego prueba automatizada:
- `expiringSoonAlerts.test.js`

Adicionalmente, el script de test del microservicio fue actualizado para ejecutar tambien esta HU.
