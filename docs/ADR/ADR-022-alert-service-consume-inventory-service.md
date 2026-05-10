# ADR-022 - Alert-service consume contratos de inventory-service

## Estado
Aceptado

## Contexto
La base de datos fisica puede estar unificada, pero el schema `inventory` pertenece funcionalmente a `inventory-service`. Si `alert-service` consulta tablas de inventario de forma directa, queda acoplado a nombres de tablas, columnas y reglas internas de otro microservicio.

## Decision
`alert-service` no consulta directamente el schema `inventory`. Las alertas obtienen datos mediante un cliente HTTP interno hacia `inventory-service`.

`inventory-service` expone endpoints internos para bajo stock, agotados, vencidos, proximos a vencer y reportes de vencimiento. Estos endpoints mantienen oculto el modelo de datos y entregan DTOs estables para que `alert-service` construya severidades, mensajes y agrupaciones.

## Consecuencias
- `inventory-service` conserva la propiedad del schema `inventory`.
- `alert-service` ya no necesita credenciales de PostgreSQL ni permisos sobre tablas de inventario.
- Las pruebas de alertas mockean el cliente HTTP y no dependen de PostgreSQL real.
- Las rutas de negocio de `alert-service` requieren JWT.
- En Docker Compose, `alert-service` usa `expose` y queda accesible internamente para `api-gateway`, sin publicar puerto directo al host.

## Contratos internos
- `GET /api/inventory/alerts/low-stock`
- `GET /api/inventory/alerts/out-of-stock`
- `GET /api/inventory/alerts/expired`
- `GET /api/inventory/alerts/expiring-soon`
- `GET /api/inventory/alerts/expiring-range`
- `GET /api/inventory/reports/expiring-products`
- `GET /api/inventory/reports/batches`
