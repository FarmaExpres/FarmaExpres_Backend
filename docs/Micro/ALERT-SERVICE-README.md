# FarmaExpres_Micro_Alert

## Alert Service

### Descripcion
`alert-service` es un microservicio Node.js/Express que construye alertas operativas de inventario: bajo stock, sin stock, vencidos, proximos a vencer y reportes consolidados.

Desde la HU-JVL-003 este servicio no consulta directamente PostgreSQL ni el schema `inventory`. Toda la informacion de productos y lotes se obtiene mediante contratos HTTP internos expuestos por `inventory-service`.

### Flujo
```text
Cliente
  -> api-gateway
  -> alert-service
  -> inventory-service
  -> schema inventory
```

`inventory-service` conserva la propiedad funcional del schema `inventory`; `alert-service` solo transforma los datos recibidos en alertas y reportes.

### Tecnologias
- Node.js 18 o superior
- Express
- dotenv
- Nodemon para desarrollo
- Docker

### Capas principales
- `controllers`: Exposicion de endpoints REST.
- `services`: Reglas de alertas, severidades y reportes.
- `clients`: Cliente HTTP interno hacia `inventory-service`.
- `middlewares`: Autenticacion, autorizacion, errores y rutas no encontradas.
- `models`: Modelos de respuesta.
- `utils`: Fechas y reglas auxiliares.

### Endpoints publicos de salud
- `GET /status`

Ejemplo:
```json
{
  "status": "UP",
  "service": "FarmaExpres_Micro_Alert",
  "timestamp": "2026-05-10T13:20:00.000Z",
  "inventoryService": "UP"
}
```

### Endpoints de negocio
Todas las rutas `/api/alerts/**` requieren JWT.

- `GET /api/alerts`
- `GET /api/alerts/low-stock`
- `GET /api/alerts/expired`
- `GET /api/alerts/out-of-stock`
- `GET /api/alerts/expiring-soon`
- `GET /api/alerts/expiring-half-month`
- `GET /api/alerts/expiring-month`

Rutas de reporte restringidas por rol (`ADMIN`, `AUDITOR`, `FARMACEUTICO`):

- `GET /api/alerts/expiring-report`
- `GET /api/alerts/expired-batches`
- `GET /api/alerts/expiring-batches`
- `GET /api/alerts/expiring-batches/report`
- `GET /api/alerts/low-stock-batches`
- `GET /api/alerts/low-stock-batches/critical`
- `GET /api/alerts/low-stock-batches/alert`
- `GET /api/alerts/out-of-stock-batches`

### Configuracion
Variables principales:

- `PORT`: Puerto interno del servicio. Ejemplo: `8083`.
- `SERVICE_NAME`: Nombre expuesto en `/status`.
- `EXPIRING_SOON_DAYS`: Ventana para proximos a vencer. Por defecto `15`.
- `INVENTORY_SERVICE_URL`: URL base interna de `inventory-service`. En Docker: `http://inventory-service:8082`.
- `INVENTORY_SERVICE_TIMEOUT_MS`: Timeout de llamadas HTTP internas.
- `JWT_SECRET`: Secreto para validar JWT recibidos en rutas de negocio.

`alert-service` no debe recibir `DB_HOST`, `DB_USER`, `DB_PASSWORD` ni permisos directos sobre el schema `inventory`.

### Contratos internos consumidos
`alert-service` consume:

- `GET /api/inventory/alerts/low-stock`
- `GET /api/inventory/alerts/out-of-stock`
- `GET /api/inventory/alerts/expired`
- `GET /api/inventory/alerts/expiring-soon`
- `GET /api/inventory/alerts/expiring-range`
- `GET /api/inventory/reports/expiring-products`
- `GET /api/inventory/reports/batches`

### Ejecucion
```bash
npm install
npm run dev
```

En Docker, el servicio queda expuesto solo dentro de la red de Compose mediante `expose: 8083`; el acceso externo debe hacerse por `api-gateway`.

### Pruebas
```bash
npm test
```

Las pruebas unitarias mockean `src/clients/inventoryClient.js`, por lo que no requieren PostgreSQL real.
