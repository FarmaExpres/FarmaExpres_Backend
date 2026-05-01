# HU-013 - Bajo stock por nivel (estado final HU-ACFE-03)

Fecha de actualizacion: 2026-04-04

## 1. Estado actual

La funcionalidad de bajo stock para reportes queda centralizada en `alert-service` con contrato por lote.

Endpoints operativos vigentes:

- `GET /api/alerts/low-stock-batches`
- `GET /api/alerts/low-stock-batches/critical`
- `GET /api/alerts/low-stock-batches/alert`

## 2. Regla de negocio vigente

1. Bajo stock se calcula en backend (no en frontend).
2. La severidad la define backend:
   - `CRITICO`
   - `ALERTA`
3. Frontend solo consume cada endpoint segun chip seleccionado.

## 3. Contrato minimo esperado por fila

- `productId`
- `productCode`
- `productName`
- `batchId`
- `batchCode`
- `batchExpirationDate` (o `expirationDate`)
- `batchStock` (o `availableStock`)
- `operationalStock`
- `minimumStock`
- `coverage`
- `coverageLabel`
- `status` / `severity`
- `suggestion`

## 4. Criterio de consistencia FE/BE

- Chip `Todos` -> `low-stock-batches`
- Chip `Critico` -> `low-stock-batches/critical`
- Chip `Alerta` -> `low-stock-batches/alert`

No se debe derivar criticidad en frontend como fallback principal.

## 5. Compatibilidad

Rutas historicas en `inventory-service` (`/api/products/low-stock-report*`) quedan como legacy tecnico.

Para reportes y alertas del flujo actual FE, la referencia oficial es `/api/alerts/low-stock-batches*`.

## 6. Resultado esperado en UI

- Coherencia entre chips y resultados.
- Sin clasificaciones distintas por calculo local.
- Trazabilidad por lote en tablas y exportaciones.

