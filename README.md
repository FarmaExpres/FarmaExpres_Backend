# FarmaExpres_Backend

## Base de datos versionada

El proyecto ahora maneja la base de datos con una estructura versionada en `database/`.

- `database/bootstrap.sql` crea las bases `farmaexpres_users` y `farmaexpres_inventory`
- `database/auth/` contiene las migraciones de autenticacion
- `database/inventory/` contiene las migraciones de inventario
- `docker-compose.yml` ejecuta Liquibase antes de levantar `auth-service`, `inventory-service` y `alert-service`

Los microservicios quedaron en modo `ddl-auto: validate` para evitar cambios automaticos sobre el esquema.
