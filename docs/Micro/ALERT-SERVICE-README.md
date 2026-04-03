# FarmaExpres_Micro_Alert

## Alert Service

### Descripcion
El servicio de alertas (`alert-service`) es un microservicio basado en Node.js y Express que centraliza la generacion y consulta de alertas del inventario. Este servicio toma la informacion de productos desde la base de datos PostgreSQL asociada al microservicio de inventario y construye alertas de bajo stock, sin stock, productos vencidos, productos proximos a vencer y un consolidado general.

### Tecnologias Utilizadas
- **Node.js**: 18 o superior
- **Express**: Framework para construir APIs REST
- **PostgreSQL**: Base de datos compartida con el microservicio de inventario
- **pg**: Driver de conexion a PostgreSQL
- **dotenv**: Manejo de variables de entorno
- **Nodemon**: Ejecucion en desarrollo
- **Docker**: Empaquetado y despliegue del microservicio

### Arquitectura
El servicio sigue una arquitectura en capas:
- **Controllers**: Exposicion de APIs REST
- **Services**: Logica de negocio de las alertas
- **Repositories**: Acceso a datos
- **Models**: Modelos de alertas, productos y estado del servicio
- **Config**: Configuracion de entorno, constantes y conexion
- **Utils**: Utilidades de fechas y severidades
- **Middlewares**: Manejo de errores y rutas no encontradas

### Endpoints Principales

#### Estado del Servicio
- `GET /status`
  - Verifica el estado del servicio
  - Response:
    ```json
    {
      "status": "UP",
      "service": "FarmaExpres_Micro_Alert",
      "timestamp": "2026-03-31T14:54:17.592Z",
      "database": "UP"
    }
    ```

#### Alertas de Inventario
- `GET /api/alerts/low-stock`
  - Retorna productos activos con stock menor al stock minimo

- `GET /api/alerts/expired`
  - Retorna productos activos cuya fecha de expiracion ya paso

- `GET /api/alerts/out-of-stock`
  - Retorna productos activos con stock igual a cero

- `GET /api/alerts/expiring-soon`
  - Retorna productos activos cuya fecha de expiracion esta dentro de la ventana configurada por `EXPIRING_SOON_DAYS`

- `GET /api/alerts/expiring-half-month`
  - Retorna productos activos cuya fecha de expiracion esta entre `16` y `30` dias

- `GET /api/alerts/expiring-month`
  - Retorna productos activos cuya fecha de expiracion esta entre `31` y `60` dias

- `GET /api/alerts`
  - Retorna el consolidado general de alertas
  - Response:
    ```json
    {
      "generatedAt": "2026-03-31T15:20:00.000Z",
      "summary": {
        "totalAlerts": 4,
        "outOfStock": 1,
        "expired": 1,
        "lowStock": 1,
        "expiringSoon": 1
      },
      "alerts": []
    }
    ```

### Configuracion

**Variables de entorno requeridas:**
- `PORT`: Puerto del microservicio. Ejemplo: `8083`
- `SERVICE_NAME`: Nombre del servicio expuesto en `/status`
- `INVENTORY_PRODUCTS_TABLE`: Tabla de productos del microservicio de inventario. Ejemplo: `product`
- `EXPIRING_SOON_DAYS`: Cantidad de dias para detectar productos proximos a vencer
- `DB_HOST`: Host de PostgreSQL
- `DB_PORT`: Puerto de PostgreSQL
- `DB_NAME`: Nombre de la base de datos
- `DB_USER`: Usuario de la base de datos
- `DB_PASSWORD`: Contrasena de la base de datos
- `DB_SSL`: `true` o `false` para conexion segura

### Estructura del Proyecto
```text
alert-service/
├── src/
│   ├── app/
│   │   └── index.js
│   ├── config/
│   │   ├── constants.js
│   │   ├── database.js
│   │   └── env.js
│   ├── controllers/
│   │   ├── allAlertsController.js
│   │   ├── expiredAlertController.js
│   │   ├── expiringSoonAlertController.js
│   │   ├── lowStockAlertController.js
│   │   ├── outOfStockAlertController.js
│   │   └── statusController.js
│   ├── middlewares/
│   │   ├── errorHandlerMiddleware.js
│   │   └── notFoundMiddleware.js
│   ├── models/
│   │   ├── Alert.js
│   │   ├── AlertCollection.js
│   │   ├── HealthStatus.js
│   │   └── Product.js
│   ├── repositories/
│   │   ├── healthRepository.js
│   │   └── productRepository.js
│   ├── routers/
│   │   ├── alertRoutes.js
│   │   ├── index.js
│   │   └── statusRoutes.js
│   ├── services/
│   │   ├── allAlertsService.js
│   │   ├── expiredAlertService.js
│   │   ├── expiringSoonAlertService.js
│   │   ├── lowStockAlertService.js
│   │   ├── outOfStockAlertService.js
│   │   └── statusService.js
│   ├── utils/
│   │   ├── alertUtils.js
│   │   └── dateUtils.js
│   └── server.js
├── tests/
│   ├── allAlerts.test.js
│   ├── expiredAlerts.test.js
│   ├── expiringSoonAlerts.test.js
│   ├── lowStockAlerts.test.js
│   ├── outOfStockAlerts.test.js
│   └── status.test.js
├── docs/
├── .env.example
├── .dockerignore
├── Dockerfile
├── package.json
└── README.md
```

### Modelo de Datos
- **Product**: id, code, name, stock, minimumStock, expirationDate, active
- **Alert**: type, severity, message, product
- **AlertCollection**: generatedAt, total, alerts
- **HealthStatus**: status, service, timestamp, database

### Ejecucion
1. **Requisitos previos**:
   - Node.js 18 o superior
   - npm
   - PostgreSQL

2. **Configurar variables de entorno**:
   ```bash
   cp .env.example .env
   ```

3. **Instalar dependencias**:
   ```bash
   npm install
   ```

4. **Ejecutar en desarrollo**:
   ```bash
   npm run dev
   ```

5. **Ejecutar en produccion**:
   ```bash
   npm start
   ```

6. **Con Docker**:
   ```bash
   docker build -t alert-service .
   docker run -p 8083:8083 --env-file .env alert-service
   ```

### Pruebas
El proyecto incluye pruebas de endpoints. Ejecutar con:
```bash
npm test
```
