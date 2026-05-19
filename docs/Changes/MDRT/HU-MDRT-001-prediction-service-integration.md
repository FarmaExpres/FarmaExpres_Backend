# HU-MDRT-001 - Integración de prediction-service

## Contexto

FarmaExpres incorporó un microservicio Python con MongoDB para análisis predictivo de inventario. Para integrarlo de forma coherente con la arquitectura existente, el acceso debe pasar por `api-gateway` y la extracción de datos debe respetar la propiedad del dominio `inventory-service`.

## Cambios implementados

- Se agregó la ruta de gateway:
  - `/api/predictions`
  - `/api/predictions/**`
- Se agregó circuit breaker `prediction-service`.
- Se agregó fallback `/fallback/predictions`.
- Se agregó la variable `PREDICTION_SERVICE_URL`.
- Se agregó un contrato interno en `inventory-service`:
  - `GET /api/inventory/analytics/snapshot`
- El contrato entrega productos activos, lotes asociados y movimientos históricos para análisis.
- El endpoint de snapshot queda protegido para roles `ADMIN` y `AUDITOR`.

## Flujo esperado

```text
Frontend React
  -> api-gateway
  -> prediction-service
  -> inventory-service
  -> PostgreSQL inventory
  -> prediction-service
  -> MongoDB
```

## Razón técnica

`prediction-service` no debe leer directamente tablas del esquema `inventory` como integración oficial. La base relacional puede seguir siendo PostgreSQL, pero el dueño funcional de esos datos es `inventory-service`.

## Archivos principales

- `api-gateway/src/main/resources/application.yaml`
- `api-gateway/src/main/java/co/edu/corhuila/api_gateway/FallbackController.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Controllers/InventoryAnalyticsController.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/InventoryAnalyticsService.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Dto/InventoryAnalytics*`
- `.env.dev`
- `.env.qa`
- `.env.main`
- `docker-compose.yml`

## Validaciones esperadas

- `docker compose --env-file .env.dev config --quiet`
- pruebas unitarias de `inventory-service`;
- verificación de ruta por gateway con token válido;
- fallback del gateway si `prediction-service` no está disponible.
