# FarmaExpres Backend

## Descripcion General

Este repositorio contiene el backend de la aplicacion FarmaExpres, una plataforma de microservicios disenada para gestionar autenticacion, inventario, alertas, auditoria y comunicacion con servicios analiticos.

## Microservicios

- `api-gateway`: punto de entrada unico para las peticiones del frontend. Enruta peticiones, centraliza seguridad y expone integraciones como `/api/predictions`.
- `auth-service`: autenticacion de usuarios y emision de tokens JWT.
- `inventory-service`: gestion de productos, lotes, entradas, salidas, movimientos y snapshots analiticos.
- `alert-service`: alertas operativas de inventario.
- `audit-service`: auditoria de movimientos e inconsistencias.
- `prediction-service`: servicio externo en Python y FastAPI. Se expone por el gateway en `/api/predictions` y consume el snapshot analitico de `inventory-service`.

## Empezando

### Prerrequisitos

- Docker
- Docker Compose v2

### Ejecucion

Para levantar solo el backend de un ambiente:

```bash
docker compose --env-file .env.dev up -d --build
docker compose --env-file .env.qa up -d --build
docker compose --env-file .env.main up -d --build
```

## Base de Datos

El proyecto utiliza Liquibase para gestionar las migraciones de la base de datos de forma versionada.

- `database/bootstrap.sql`: crea la base de datos inicial.
- `database/auth/`: migraciones del servicio de autenticacion.
- `database/inventory/`: migraciones del servicio de inventario.
- `database/audit/`: migraciones del servicio de auditoria.

Los microservicios estan configurados con `ddl-auto: validate` para asegurar que los cambios en el esquema se gestionen exclusivamente a traves de Liquibase.

## Despliegue completo con microservicio predictivo

El backend principal ya incluye el runtime del microservicio predictivo. Al levantar este repositorio con Docker Compose se crean también `mongo` y `prediction-service`; el frontend consume todo por el `api-gateway`.

### Dev

```bash
cd FarmaExpres_Backend
docker compose --env-file .env.dev up -d --build

cd ../FarmaExpres-Frontend/frontend
docker compose --env-file .env.dev up -d --build
```

### QA

```bash
cd FarmaExpres_Backend
docker compose --env-file .env.qa up -d --build

cd ../FarmaExpres-Frontend/frontend
docker compose --env-file .env.qa up -d --build
```

### Main

```bash
cd FarmaExpres_Backend
docker compose --env-file .env.main up -d --build

cd ../FarmaExpres-Frontend/frontend
docker compose --env-file .env.main up -d --build
```

El repositorio `FarmaExpres-Micro-NoSQL` queda como referencia histórica y documentación especializada, pero para ejecutar el sistema integrado basta con Backend + Frontend.

## Configuracion de Entornos

El proyecto utiliza diferentes puertos para los entornos de `dev`, `qa` y `main` a traves de archivos de entorno.

Los puertos internos son fijos para comunicacion entre contenedores:

- `api-gateway`: `8080`
- `auth-service`: `8081`
- `inventory-service`: `8082`
- `alert-service`: `8083`
- `audit-service`: `8084`
- `prediction-service`: `8000` dentro de su propio contenedor
- `postgres`: `5432`
- `mongo`: `27017`

Los puertos externos son para acceso desde navegador, Postman, pgAdmin o clientes fuera de Docker:

- **dev**:
  - gateway: `8080`
  - auth: `18081`
  - inventory: `8082`
  - alert: `8083`
  - audit: `8084`
  - prediction-service: `8085` directo desde este repositorio, o `/api/predictions` por gateway
  - mongo: `27017`
  - postgres: `5433`
- **qa**:
  - gateway: `9080`
  - auth: `9081`
  - inventory: `9082`
  - alert: `9083`
  - audit: `9084`
  - prediction-service: `9085` directo desde este repositorio, o `/api/predictions` por gateway
  - mongo: `37017`
  - postgres: `6433`
- **main**:
  - gateway: `10080`
  - auth: `10081`
  - inventory: `10082`
  - alert: `10083`
  - audit: `10084`
  - prediction-service: `10085` directo desde este repositorio, o `/api/predictions` por gateway
  - mongo: `47017`
  - postgres: `7433`

## Red Interna

Dentro de la red de Docker, los servicios se comunican usando nombres de servicio y puertos internos. El `api-gateway` se comunica con los demas servicios por direcciones como `http://auth-service:8081`.

Regla práctica:
- puertos externos: acceso desde navegador, Postman, pgAdmin o clientes fuera de Docker
- puertos internos: comunicación entre contenedores del mismo ambiente

El gateway usa `PREDICTION_SERVICE_URL`, por defecto `http://prediction-service:8000`.

## Decisiones de Arquitectura

Las decisiones de arquitectura importantes se documentan en la carpeta [docs/ADR](docs/ADR/).

## Documentacion de la API

Los contratos de la API y otros documentos relevantes se encuentran en la carpeta `docs/`.

## Pruebas

Cada microservicio incluye su propio conjunto de pruebas. Por ejemplo, el `alert-service` tiene pruebas unitarias y de integracion en su carpeta `tests/`. La estrategia de pruebas general se define en el [ADR-009](docs/ADR/ADR-009-estrategia-pruebas-microservicios.md).
