# HU-002 - Alertas de bajo stock (alert-service)

## 1. Informacion general
- HU: `HU-002`
- Nombre: Consulta de alertas por bajo stock
- Microservicio: `alert-service`
- Estado: Implementado (ajustado a esquema real de BD)
- Rama de trabajo: `HU-AC-dev`

## 2. Objetivo de la HU
Exponer el endpoint `GET /api/alerts/low-stock` para listar productos activos con stock por debajo del stock minimo configurado.

## 3. Alcance funcional implementado
- Endpoint disponible: `GET /api/alerts/low-stock`
- Capa aplicada: `router -> controller -> service -> repository`
- Fuente de datos: PostgreSQL `farmaexpres_inventory`, tabla `product`
- Regla de negocio: solo productos activos (`asset = true`) y con `stock < minimumstock`

## 4. Cambios realizados
### 4.1 Endpoint y flujo de negocio
- Se agrego ruta para bajo stock en el router de alertas.
- Se implemento controlador para responder la consulta.
- Se implemento servicio para construir la coleccion de alertas.
- Se definio severidad de alerta segun deficit de stock.

### 4.2 Ajuste tecnico para que funcione con el esquema real
Se corrigieron nombres de tabla y columnas para que coincidan con `database/init.sql`:
- Tabla: `product` (no `products`)
- Columnas: `minimumstock`, `expirationdate`, `asset`

## 5. Archivos modificados para HU-002
- `alert-service/src/routers/alertRoutes.js`
- `alert-service/src/controllers/lowStockAlertController.js`
- `alert-service/src/services/lowStockAlertService.js`
- `alert-service/src/repositories/productRepository.js`
- `alert-service/src/models/Alert.js`
- `alert-service/src/models/AlertCollection.js`
- `alert-service/src/models/Product.js`
- `alert-service/src/config/constants.js`
- `alert-service/src/utils/alertUtils.js`
- `alert-service/src/config/env.js`
- `alert-service/.env.example`

## 6. Contrato de respuesta esperado
```json
{
  "generatedAt": "2026-03-31T15:12:43.021Z",
  "total": 2,
  "alerts": [
    {
      "type": "LOW_STOCK",
      "severity": "MEDIUM",
      "message": "Producto bajo stock minimo: Acetaminophen 500mg",
      "product": {
        "id": "1",
        "code": "ACM-001",
        "name": "Acetaminophen 500mg",
        "stock": 7,
        "minimumStock": 10,
        "expirationDate": "2026-12-31",
        "active": true
      }
    }
  ]
}
```

## 7. Criterios de aceptacion cubiertos
1. Existe `GET /api/alerts/low-stock`.
2. Responde `200 OK` en ejecucion correcta.
3. Devuelve `generatedAt`, `total`, `alerts`.
4. Filtra solo productos activos bajo minimo.
5. Incluye `type`, `severity`, `message` y `product`.
6. Incluye en `product`: `id`, `code`, `name`, `stock`, `minimumStock`, `expirationDate`, `active`.

## 8. Evidencia tecnica de correccion
Se ajusto configuracion para evitar error por tabla/columnas inexistentes:
- Default de `INVENTORY_PRODUCTS_TABLE` a `product`.
- Query SQL alineada con columnas reales (`minimumstock`, `expirationdate`, `asset`).

## 9. Pendiente fuera de esta HU
- Integrar endpoint de alertas en gateway para consumo unificado por `:8080` (si aplica en la siguiente HU).
- Agregar pruebas automatizadas especificas de bajo stock.
