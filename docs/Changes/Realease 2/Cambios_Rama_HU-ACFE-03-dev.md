# Cambios de la Rama `HU-ACFE-03-dev`

Fecha de actualización: 2026-04-04  
Rama analizada: `HU-ACFE-03-dev`  
Alcance: Alineación FE/BE para modelo por lote, FEFO, alertas y reportes backend-driven.

## 1. Resumen ejecutivo

En esta rama se hizo una transición de modelo **producto-céntrico** a modelo **lote-céntrico operativo** para inventario, movimientos, alertas y reportes.

Se implementó:
- trazabilidad por lote
- movimientos con lote explícito y consumo FEFO
- alertas y reportes batch-aware
- reglas operativas de snapshot por lote activo
- endpoint agregado para optimizar frontend y evitar N+1 (`/api/products/fefo-snapshot`)

## 2. Qué se mejoró

1. Se eliminó ambigüedad de vencimientos/stock cuando existen múltiples lotes por producto.
2. Se reforzó FEFO real para salidas sin lote explícito.
3. Reportes y alertas dejaron de depender de derivaciones de frontend.
4. Se separaron claramente:
- estado del lote (`ACTIVE`, `OUT_OF_STOCK`, `EXPIRED`, `RETIRED`)
- severidad de bajo stock (`CRITICO`, `ALERTA`)
5. Se redujo carga de frontend al agregar snapshot FEFO consolidado por producto.

## 3. Qué cambió por microservicio

### 3.1 `inventory-service`

Cambios principales:
- nueva entidad `Batch` y enum `BatchStatus`
- nuevo `BatchRepository` y `BatchService`
- endpoints de lotes por producto
- movimientos batch-aware:
  - `POST /api/movements` (con `batchId`)
  - `POST /api/movements/consume-fefo` (asignación FEFO automática)
- reportes batch:
  - `GET /api/reports/inventory-batches`
  - `GET /api/reports/movements-batches`
- endpoint nuevo de optimización FE:
  - `GET /api/products/fefo-snapshot`

Reglas operativas incorporadas:
- producto en lectura usa snapshot de lotes consumibles activos:
  - `status = ACTIVE`
  - `availableStock > 0`
  - `expirationDate >= hoy`
- si no hay lote consumible activo:
  - `operationalStock = 0`
  - `nextExpirationDate = null`
  - `nextBatchCode = null`

Ajuste importante en edición:
- `PUT /api/products/{id}` no bloquea por `stock`/`expirationDate`
- esos campos quedan bajo dominio de lotes/movimientos

### 3.2 `alert-service`

Cambios principales:
- nuevos controladores/servicios/modelos batch-aware
- nuevas rutas:
  - `GET /api/alerts/expired-batches`
  - `GET /api/alerts/expiring-batches`
  - `GET /api/alerts/expiring-batches/report`
  - `GET /api/alerts/low-stock-batches`
  - `GET /api/alerts/low-stock-batches/critical`
  - `GET /api/alerts/low-stock-batches/alert`
  - `GET /api/alerts/out-of-stock-batches`
  - `GET /api/reports/alerts-batches`

Ajustes de comportamiento:
- centro de alertas alineado a lote (evitar mezclas producto vs lote)
- separación de `status` (estado de lote) y `severity` (nivel bajo stock)
- reporte de próximos a vencer parametrizado para casos de reportes

### 3.3 `api-gateway`

Cambios principales:
- enrutamiento actualizado para rutas batch de alertas/reportes
- preservación de consumo centralizado vía `:8080`

### 3.4 `database/init.sql`

Cambios principales:
- tabla `batch` con restricciones y unicidad por producto+lote
- relación `motion.batch_id`
- seed inicial alineado a escenarios reales:
  - vencidos
  - próximos
  - bajo stock
  - agotados

## 4. Qué afectó y qué no afectó

### 4.1 Afectó (intencional)

1. Contratos de reportes/alertas migrados a batch-aware.
2. Interpretación de stock/vencimiento de producto en pantallas:
- ahora reflejan snapshot operativo por lote activo.
3. Rutas legacy quedan como compatibilidad temporal en varias HUs previas.

### 4.2 No afectó

1. Autenticación JWT y flujo base de login (`auth-service`) no cambió en comportamiento.
2. Estructura general de microservicios y docker-compose se mantiene.
3. Endpoints de creación/consulta base de producto se conservan.

## 5. Endpoints clave vigentes para FE (alineados)

### Productos / Inventario
- `GET /api/products/active-table`
- `GET /api/products/active-summary`
- `GET /api/products/fefo-snapshot`
- `GET /api/products/{id}/batches`
- `POST /api/products/{id}/batches`

### Movimientos
- `GET /api/movements`
- `GET /api/movements/entrance`
- `GET /api/movements/exit`
- `GET /api/movements/updated`
- `GET /api/movements/report/users-activity`
- `POST /api/movements`
- `POST /api/movements/consume-fefo`

### Alertas / Reportes batch
- `GET /api/alerts/expired-batches`
- `GET /api/alerts/expiring-batches`
- `GET /api/alerts/expiring-batches/report`
- `GET /api/alerts/low-stock-batches`
- `GET /api/alerts/low-stock-batches/critical`
- `GET /api/alerts/low-stock-batches/alert`
- `GET /api/alerts/out-of-stock-batches`
- `GET /api/reports/alerts-batches`
- `GET /api/reports/inventory-batches`
- `GET /api/reports/movements-batches`

## 6. Legacy detectado (deprecación recomendada, no remover sin validación FE)

1. Alertas legacy por producto:
- `/api/alerts/expired`
- `/api/alerts/expiring-soon`
- `/api/alerts/expiring-half-month`
- `/api/alerts/expiring-month`
- `/api/alerts/expiring-report`

2. Alias legacy de productos/movimientos:
- `/api/products/Assets`
- `/api/motions`
- `/api/Motion`
- `/api/products/low-stock-report*`

## 7. Archivos principales incorporados en esta rama (núcleo funcional)

### Nuevos
- `inventory-service/.../Entity/Batch.java`
- `inventory-service/.../Entity/BatchStatus.java`
- `inventory-service/.../Repository/BatchRepository.java`
- `inventory-service/.../Service/BatchService.java`
- `inventory-service/.../Controllers/ReportController.java`
- `inventory-service/.../Dto/BatchResponse.java`
- `inventory-service/.../Dto/CreateBatchRequest.java`
- `inventory-service/.../Dto/MovementRequest.java`
- `inventory-service/.../Dto/MovementExecutionResponse.java`
- `inventory-service/.../Dto/FefoConsumeRequest.java`
- `inventory-service/.../Dto/FefoConsumptionItemResponse.java`
- `inventory-service/.../Dto/InventoryBatchReportItemResponse.java`
- `inventory-service/.../Dto/MovementBatchReportItemResponse.java`
- `inventory-service/.../Dto/FefoSnapshotItemResponse.java`
- `inventory-service/.../Dto/FefoSnapshotResponse.java`
- `alert-service/src/controllers/batchAlertsController.js`
- `alert-service/src/controllers/batchReportsController.js`
- `alert-service/src/services/batchAlertsService.js`
- `alert-service/src/models/BatchAlertItem.js`
- `alert-service/src/middlewares/reportsAccessMiddleware.js`
- `alert-service/src/routers/reportRoutes.js`

### Modificados (alta relevancia)
- `inventory-service/.../Service/ProductService.java`
- `inventory-service/.../Service/MotionService.java`
- `inventory-service/.../Controllers/ProductController.java`
- `inventory-service/.../Controllers/MotionController.java`
- `inventory-service/.../Config/SecurityConfig.java`
- `alert-service/src/repositories/productRepository.js`
- `alert-service/src/routers/alertRoutes.js`
- `api-gateway/src/main/resources/application.yaml`
- `database/init.sql`

## 8. Limpieza técnica aplicada

Cleanup confirmado:
- eliminación de DTO sin uso: `CriticalLowStockResponse`
- eliminación de método no referenciado en `ProductRepository`
- poda de funciones muertas en `alert-service/src/repositories/productRepository.js`

## 9. Verificación realizada

1. Compilación de `inventory-service` exitosa (`mvn -DskipTests package`).
2. Rebuild de contenedores:
- `inventory-service`
- `alert-service`
3. Validación funcional con datos seed y revisiones en UI:
- lote activo vs sin lote activo
- vencidos/próximos
- bajo stock crítico/alerta

## 10. Estado final

La rama queda funcionalmente alineada con HU-ACFE-03 para backend.  
Queda pendiente de cierre final:
- consolidar commits por bloques funcionales
- cierre deprecaciones legacy en HU técnica posterior

## 11. Ajustes finales de consistencia aplicados

### 11.1 Regla de vencidos operativos

Se consolidó la regla para alertas y reportes:

- lotes vencidos con `availableStock = 0` no se muestran en vistas operativas.

Ajuste backend aplicado:

- `alert-service/src/repositories/productRepository.js`
  - `findExpiredBatches()` ahora filtra `b.available_stock > 0`.

### 11.2 Alineación frontend confirmada

Frontend quedó alineado a contratos nuevos:

- Reportes de vencimientos consume `GET /api/alerts/expiring-batches/report`.
- Alertas y reportes usan mapeo de stock por lote (`batchStock`) y stock operativo (`operationalStock`).
- Se eliminó consumo de rutas legacy en módulos principales de reportes/alertas.

### 11.3 Resultado funcional esperado

1. Si un lote está vencido y tiene stock `0`, no aparece en vencidos.
2. Si un lote está vencido y tiene stock `> 0`, sí aparece en vencidos.
3. Reportes y Alertas muestran la misma lógica de negocio para vencimientos.
