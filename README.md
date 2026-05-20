# FarmaExpres_Backend

## Base de datos versionada

El proyecto ahora maneja la base de datos con una estructura versionada en `database/`.

- `database/bootstrap.sql` crea las bases `farmaexpres_users` y `farmaexpres_inventory`
- `database/auth/` contiene las migraciones de autenticacion
- `database/inventory/` contiene las migraciones de inventario
- `docker-compose.yml` ejecuta Liquibase antes de levantar `auth-service`, `inventory-service` y `alert-service`

Los microservicios quedaron en modo `ddl-auto: validate` para evitar cambios automaticos sobre el esquema.

## Microservicios

- `api-gateway`: punto de entrada para las peticiones del frontend.
- `auth-service`: autenticacion y emision de tokens JWT.
- `inventory-service`: gestion de productos, lotes, entradas, salidas y movimientos.
- `alert-service`: alertas operativas de inventario.
- `audit-service`: auditoria de movimientos e inconsistencias.
- `prediction-service`: servicio externo en Python y FastAPI. Se expone por el gateway en `/api/predictions` y consume el snapshot analitico de `inventory-service`.

## Puertos por ambiente

El proyecto ahora soporta puertos distintos para `dev`, `qa` y `main` usando archivos de entorno:

- `.env.dev`
- `.env.qa`
- `.env.main`

Puertos definidos en este repositorio:

- `dev`
  - gateway: `8080`
  - auth: `8081`
  - inventory: `8082`
  - alert: `8083`
  - postgres: `5433`

- `qa`
  - gateway: `9080`
  - auth: `9081`
  - inventory: `9082`
  - alert: `9083`
  - postgres: `6433`

- `main`
  - gateway: `10080`
  - auth: `10081`
  - inventory: `10082`
  - alert: `10083`
  - postgres: `7433`

Para levantar solo el backend de un ambiente:

```bash
docker compose --env-file .env.dev up -d --build
docker compose --env-file .env.qa up -d --build
docker compose --env-file .env.main up -d --build
```

## Despliegue completo con microservicio predictivo

El backend principal, el microservicio NoSQL y el frontend viven en repositorios separados. Para una ejecución integrada se debe respetar este orden, porque `prediction-service` se conecta a la red Docker del backend y el frontend consume todo por el `api-gateway`.

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

Si el frontend o el microservicio viven en otro repositorio local, deben respetar la misma estrategia de puertos, nombre de proyecto Docker y red por ambiente.

## Gateway y puertos internos

La estrategia de puertos por ambiente cambia los puertos publicados hacia el host, pero no cambia los puertos internos entre contenedores.

Eso significa que dentro de Docker:

- `auth-service` sigue escuchando en `8081`
- `inventory-service` sigue escuchando en `8082`
- `alert-service` sigue escuchando en `8083`
- `api-gateway` sigue escuchando en `8080`
- `postgres` sigue escuchando en `5432`
- `prediction-service` escucha en `8000` dentro de su propio contenedor

Por lo tanto, el gateway no necesita consumir los puertos externos de `dev`, `qa` o `main` para hablar con los otros microservicios cuando todos viven en el mismo `docker compose`.

Ejemplo:
- el host puede exponer `auth-service` en `9081` para `qa`
- pero dentro de la red Docker el gateway sigue consumiendo `auth-service:8081`

Regla práctica:
- puertos externos: acceso desde navegador, Postman, pgAdmin o clientes fuera de Docker
- puertos internos: comunicación entre contenedores del mismo ambiente

Para el servicio predictivo, el repositorio `FarmaExpres-Micro-NoSQL` debe unirse a la red Docker del backend:

- `dev`: `BACKEND_NETWORK=farmaexpres-dev_default`
- `qa`: `BACKEND_NETWORK=farmaexpres-qa_default`
- `main`: `BACKEND_NETWORK=farmaexpres-main_default`

El gateway usa `PREDICTION_SERVICE_URL`, por defecto `http://prediction-service:8000`.
