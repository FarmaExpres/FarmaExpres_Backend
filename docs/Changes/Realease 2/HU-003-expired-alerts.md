# HU-003 - Alertas de productos vencidos (alert-service)

## 1. Informacion general
- HU: `HU-003`
- Nombre: Consulta de alertas por productos vencidos
- Microservicio: `alert-service`
- Estado: Implementado
- Rama de trabajo: `HU-AC-dev`

## 2. Objetivo de la HU
Exponer el endpoint `GET /api/alerts/expired` para retornar alertas de productos activos cuya fecha de vencimiento ya paso.

## 3. Alcance funcional implementado
- Endpoint disponible: `GET /api/alerts/expired`
- Capa aplicada: `router -> controller -> service -> repository`
- Fuente de datos: PostgreSQL `farmaexpres_inventory`, tabla `product`
- Regla de negocio:
  - Producto activo (`asset = true`)
  - Producto vencido (`expirationdate < CURRENT_DATE`)

## 4. Cambios realizados
### 4.1 Endpoint y flujo
- Se agrego ruta `/api/alerts/expired`.
- Se implemento controlador dedicado de alertas vencidas.
- Se implemento servicio para transformar productos vencidos en alertas de tipo `EXPIRED`.
- Se agrego soporte del tipo de alerta `EXPIRED` en constantes.

### 4.2 Consulta SQL
Se implemento consulta en repositorio de productos con filtro por vencimiento:
- Tabla: `product`
- Condicion: `asset = TRUE AND expirationdate < CURRENT_DATE`
- Orden: `expirationdate ASC, name ASC`

## 5. Archivos modificados para HU-003
- `alert-service/src/routers/alertRoutes.js`
- `alert-service/src/controllers/expiredAlertController.js`
- `alert-service/src/services/expiredAlertService.js`
- `alert-service/src/repositories/productRepository.js`
- `alert-service/src/config/constants.js`
- `alert-service/src/models/Alert.js`
- `alert-service/src/models/AlertCollection.js`
- `alert-service/src/models/Product.js`

## 6. Contrato de respuesta esperado
```json
{
  "generatedAt": "2026-03-31T15:13:16.316Z",
  "total": 2,
  "alerts": [
    {
      "type": "EXPIRED",
      "severity": "HIGH",
      "message": "Producto vencido: Acetaminophen 500mg",
      "product": {
        "id": "13",
        "code": "ACM-001",
        "name": "Acetaminophen 500mg",
        "stock": 7,
        "minimumStock": 8,
        "expirationDate": "2026-01-15",
        "active": true
      }
    }
  ]
}
```

## 7. Criterios de aceptacion cubiertos
1. Existe `GET /api/alerts/expired`.
2. Responde `200 OK` en ejecucion correcta.
3. Devuelve `generatedAt`, `total`, `alerts`.
4. Incluye solo productos activos vencidos.
5. Cada alerta incluye `type`, `severity`, `message` y `product`.
6. `product` incluye `id`, `code`, `name`, `stock`, `minimumStock`, `expirationDate`, `active`.

## 8. Evidencia tecnica
- Tipo de alerta `EXPIRED` definido en constantes.
- Endpoint registrado en `alertRoutes`.
- Query de vencidos implementada en `productRepository`.

## 9. Pendientes fuera de esta HU
- Agregar pruebas automatizadas especificas para `/api/alerts/expired`.
- Integrar visualizacion consolidada de alertas (low-stock + expired) en endpoint agregado, si se define en HU posterior.
