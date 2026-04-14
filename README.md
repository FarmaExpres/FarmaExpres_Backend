# FarmaExpres_Backend

## Base de datos versionada

El proyecto ahora maneja la base de datos con una estructura versionada en `database/`.

- `database/bootstrap.sql` crea las bases `farmaexpres_users` y `farmaexpres_inventory`
- `database/auth/` contiene las migraciones de autenticacion
- `database/inventory/` contiene las migraciones de inventario
- `docker-compose.yml` ejecuta Liquibase antes de levantar `auth-service`, `inventory-service` y `alert-service`

Los microservicios quedaron en modo `ddl-auto: validate` para evitar cambios automaticos sobre el esquema.

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

Para levantar un ambiente:

```bash
docker compose --env-file .env.dev up --build
docker compose --env-file .env.qa up --build
docker compose --env-file .env.main up --build
```

Si el frontend vive en otro repositorio, debe respetar la misma estrategia de puertos por ambiente en su propia configuracion.

## Gateway y puertos internos

La estrategia de puertos por ambiente cambia los puertos publicados hacia el host, pero no cambia los puertos internos entre contenedores.

Eso significa que dentro de Docker:

- `auth-service` sigue escuchando en `8081`
- `inventory-service` sigue escuchando en `8082`
- `alert-service` sigue escuchando en `8083`
- `api-gateway` sigue escuchando en `8080`
- `postgres` sigue escuchando en `5432`

Por lo tanto, el gateway no necesita consumir los puertos externos de `dev`, `qa` o `main` para hablar con los otros microservicios cuando todos viven en el mismo `docker compose`.

Ejemplo:
- el host puede exponer `auth-service` en `9081` para `qa`
- pero dentro de la red Docker el gateway sigue consumiendo `auth-service:8081`

Regla practica:
- puertos externos: acceso desde navegador, Postman, pgAdmin o clientes fuera de Docker
- puertos internos: comunicacion entre contenedores del mismo ambiente
