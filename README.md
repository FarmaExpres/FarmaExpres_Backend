# FarmaExpres Backend

## Descripción General

Este repositorio contiene el backend de la aplicación FarmaExpres, una plataforma de microservicios diseñada para gestionar autenticación, inventario, alertas, auditoría y comunicación con servicios analíticos.

## Microservicios

El backend está compuesto por los siguientes microservicios:

-   **api-gateway**: Punto de entrada único para todas las peticiones de los clientes. Enruta las peticiones a los servicios correspondientes y maneja la autenticación y la autorización.
-   **auth-service**: Gestiona la autenticación de usuarios y la generación de tokens JWT.
-   **inventory-service**: Se encarga de la gestión del inventario de productos, incluyendo el control de stock, precios y caducidad.
-   **alert-service**: Genera y gestiona alertas relacionadas con el inventario, como bajo stock o productos a punto de caducar.
-   **audit-service**: Registra y revisa inconsistencias de movimientos de inventario.
-   **prediction-service**: Servicio externo del ecosistema, desarrollado en Python + FastAPI, que se enruta por el gateway en `/api/predictions` y consume datos desde `inventory-service`.

## Empezando

### Prerrequisitos

-   Docker
-   Docker Compose

### Ejecución

1.  Clona este repositorio.
2.  Crea los archivos de entorno necesarios (`.env.dev`, `.env.qa`, `.env.main`) si no existen. Puedes basarte en los puertos definidos más abajo.
3.  Levanta el entorno deseado con Docker Compose. Por ejemplo, para el entorno de desarrollo:

    ```bash
    docker compose --env-file .env.dev up --build
    ```

## Base de Datos

El proyecto utiliza Liquibase para gestionar las migraciones de la base de datos de forma versionada.

-   `database/bootstrap.sql`: Crea las bases de datos iniciales.
-   `database/auth/`: Contiene las migraciones para el servicio de autenticación.
-   `database/inventory/`: Contiene las migraciones para el servicio de inventario.
-   `database/audit/`: Contiene las migraciones para el servicio de auditoría.

Los microservicios están configurados con `ddl-auto: validate` para asegurar que los cambios en el esquema se gestionen exclusivamente a través de Liquibase.

## Configuración de Entornos

El proyecto utiliza diferentes puertos para los entornos de `dev`, `qa` y `main` a través de archivos de entorno.

### Puertos

-   **dev**:
    -   gateway: `8080`
    -   auth: `8081`
    -   inventory: `8082`
    -   alert: `8083`
    -   audit: `8084`
    -   prediction-service: `8085` directo desde su repositorio, o `/api/predictions` por gateway
    -   postgres: `5433`
-   **qa**:
    -   gateway: `9080`
    -   auth: `9081`
    -   inventory: `9082`
    -   alert: `9083`
    -   audit: `9084`
    -   prediction-service: `9085` directo desde su repositorio, o `/api/predictions` por gateway
    -   postgres: `6433`
-   **main**:
    -   gateway: `10080`
    -   auth: `10081`
    -   inventory: `10082`
    -   alert: `10083`
    -   audit: `10084`
    -   prediction-service: `10085` directo desde su repositorio, o `/api/predictions` por gateway
    -   postgres: `7433`

## Red Interna (Networking)

Dentro de la red de Docker, los servicios se comunican utilizando sus puertos internos, que son fijos e independientes de los puertos expuestos en el host.

-   `auth-service`: `8081`
-   `inventory-service`: `8082`
-   `alert-service`: `8083`
-   `audit-service`: `8084`
-   `prediction-service`: `8000` dentro de su contenedor Docker
-   `api-gateway`: `8080`
-   `postgres`: `5432`

El `api-gateway` siempre se comunicará con los demás servicios a través de estos puertos internos (ej: `http://auth-service:8081`).

Para el servicio predictivo, el repositorio `FarmaExpres-Micro-NoSQL` debe unirse a la red Docker del backend mediante `BACKEND_NETWORK=farmaexpres-dev_default` en desarrollo. El gateway usa `PREDICTION_SERVICE_URL`, por defecto `http://prediction-service:8000`.

## Decisiones de Arquitectura (ADRs)

Las decisiones de arquitectura importantes se documentan en la carpeta [docs/ADR](docs/ADR/).

## Documentación de la API

Los contratos de la API y otros documentos relevantes se encuentran en la carpeta `docs/`.

## Pruebas

Cada microservicio incluye su propio conjunto de pruebas. Por ejemplo, el `alert-service` tiene pruebas unitarias y de integración en su carpeta `tests/`. La estrategia de pruebas general se define en el [ADR-009](docs/ADR/ADR-009-estrategia-pruebas-microservicios.md).
