# HU-012 - Reporte de lotes proximos a vencer (estado final HU-ACFE-03)

Fecha de actualizacion: 2026-04-04

## 1. Estado actual

Esta HU queda alineada al modelo batch-aware (por lote).  
Para frontend y reportes operativos, la fuente oficial es:

- `GET /api/alerts/expiring-batches/report`

Este endpoint devuelve lotes:
- vencidos y proximos a vencer segun ventana configurada
- con datos de lote (`batchId`, `batchCode`, `expirationDate`)
- con `daysUntilExpiration` calculado
- con `batchStock` y `operationalStock`
- filtrando registros sin utilidad operativa para reportes (stock de lote en 0)

## 2. Endpoints vigentes para vencimientos (batch-aware)

- `GET /api/alerts/expired-batches`
- `GET /api/alerts/expiring-batches`
- `GET /api/alerts/expiring-batches/report`

## 3. Regla funcional consolidada

Para la vista de reportes de vencimientos:

1. El frontend consume `expiring-batches/report`.
2. El chip/tab `Vencidos` se construye desde `daysUntilExpiration < 0` o estado equivalente.
3. No se deben mostrar vencidos con stock de lote en `0`.

## 4. Contrato minimo esperado por fila

- `productId`
- `productCode`
- `productName`
- `batchId`
- `batchCode`
- `expirationDate` (o `batchExpirationDate`)
- `daysUntilExpiration`
- `status`
- `batchStock` (o `availableStock`)
- `operationalStock`

## 5. Compatibilidad

La ruta historica:

- `GET /api/alerts/expiring-report`

se considera legacy y no debe usarse en frontend nuevo.

## 6. Resultado esperado en UI

En Reportes > Proximos a vencer:

- Los vencidos deben coincidir con backend batch-aware.
- No deben aparecer lotes vencidos con stock `0`.
- Debe mantenerse trazabilidad por lote (codigo y fecha del lote).

