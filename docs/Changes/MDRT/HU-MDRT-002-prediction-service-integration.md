# HU-MDRT-002 - Integración de prediction-service

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
- La trazabilidad de este módulo inicia en `HU-MDRT-002` porque `HU-MDRT-001` ya corresponde a control de concurrencia de inventario.

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
- `docker compose --env-file .env.qa config --quiet`
- `docker compose --env-file .env.main config --quiet`
- pruebas unitarias de `inventory-service`;
- verificación de ruta por gateway con token válido;
- fallback del gateway si `prediction-service` no está disponible.

## Ejecución integrada por ambiente

El backend no levanta MongoDB ni `prediction-service` por sí solo, porque ese módulo vive en el repositorio `FarmaExpres-Micro-NoSQL`. Para probar la integración completa se deben levantar los tres repositorios en orden.

### Dev

```bash
cd FarmaExpres_Backend
docker compose --env-file .env.dev up -d --build

cd ../FarmaExpres-Micro-NoSQL
docker compose --env-file .env.dev up -d --build

cd ../FarmaExpres-Frontend/frontend
docker compose --env-file .env.dev up -d --build
```

### QA

```bash
cd FarmaExpres_Backend
docker compose --env-file .env.qa up -d --build

cd ../FarmaExpres-Micro-NoSQL
docker compose --env-file .env.qa up -d --build

cd ../FarmaExpres-Frontend/frontend
docker compose --env-file .env.qa up -d --build
```

### Main

```bash
cd FarmaExpres_Backend
docker compose --env-file .env.main up -d --build

cd ../FarmaExpres-Micro-NoSQL
docker compose --env-file .env.main up -d --build

cd ../FarmaExpres-Frontend/frontend
docker compose --env-file .env.main up -d --build
```

La variable clave del microservicio es `BACKEND_NETWORK`, que debe coincidir con la red del backend del mismo ambiente: `farmaexpres-dev_default`, `farmaexpres-qa_default` o `farmaexpres-main_default`.

## Validación local realizada

- `GET /api/inventory/analytics/snapshot` respondió productos, lotes y movimientos desde `inventory-service`.
- `POST /api/predictions/ingest` respondió 200 por el gateway.
- `POST /api/predictions/recalculate` respondió 200 por el gateway.
- La sincronización integrada dejó 130 registros crudos, 130 registros limpios, 11 predicciones, 10 productos en riesgo alto y 1 producto agotado.
