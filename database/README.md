# FarmaExpres Database

Este directorio centraliza la versionacion de la base de datos de `FarmaExpres`.

## Estructura

- `bootstrap.sql`: crea la base `farmaexpres` y los schemas `auth` e `inventory`
- `auth/`: changelogs y scripts del schema de autenticacion
- `inventory/`: changelogs y scripts del schema de inventario

Cada schema sigue la misma organizacion:

- `01_ddl`: cambios estructurales
- `02_dml`: datos iniciales y parches de datos
- `03_dcl`: roles, grants y seguridad
- `04_tcl`: operaciones transaccionales excepcionales
- `05_rollbacks`: scripts de reversa

## Flujo de trabajo

1. PostgreSQL crea la base `farmaexpres` con `bootstrap.sql`.
2. El mismo bootstrap crea los schemas `auth` e `inventory`.
3. Liquibase aplica migraciones de autenticacion usando `--default-schema-name=auth`.
4. Liquibase aplica migraciones de inventario usando `--default-schema-name=inventory`.
5. Los microservicios arrancan con `ddl-auto: validate` para verificar el schema correspondiente y no mutarlo.

## Usuarios de prueba DCL

Se crean usuarios `LOGIN` para validar permisos desde pgAdmin o clientes SQL:

- `farmaexpres_auth_app_user` / variable `AUTH_DB_PASSWORD`
- `farmaexpres_auth_read_user` / variable `AUTH_READ_DB_PASSWORD`
- `farmaexpres_inventory_app_user` / variable `INVENTORY_DB_PASSWORD`
- `farmaexpres_inventory_read_user` / variable `INVENTORY_READ_DB_PASSWORD`

Los valores presentes en `.env.*` son de ejemplo para ambientes locales o academicos. No se deben versionar secretos reales.

## Permisos por usuario de prueba

### Schema `auth`

- `farmaexpres_auth_app_user`
  - hereda del rol `farmaexpres_auth_app`
  - puede `SELECT`, `INSERT`, `UPDATE` y `DELETE` sobre:
    - `auth.role`
    - `auth.users`
    - `auth.binnacle`
    - `auth.refresh_token`
  - puede usar las secuencias:
    - `auth.role_id_role_seq`
    - `auth.users_id_seq`
    - `auth.binnacle_id_seq`
    - `auth.refresh_token_id_seq`

- `farmaexpres_auth_read_user`
  - hereda del rol `farmaexpres_auth_readonly`
  - puede solo `SELECT` sobre:
    - `auth.role`
    - `auth.users`
    - `auth.binnacle`
    - `auth.refresh_token`
  - puede consultar las secuencias:
    - `auth.role_id_role_seq`
    - `auth.users_id_seq`
    - `auth.binnacle_id_seq`
    - `auth.refresh_token_id_seq`

### Schema `inventory`

- `farmaexpres_inventory_app_user`
  - hereda del rol `farmaexpres_inventory_app`
  - puede `SELECT`, `INSERT`, `UPDATE` y `DELETE` sobre:
    - `inventory.product`
    - `inventory.batch`
    - `inventory.motion`
  - puede usar las secuencias:
    - `inventory.product_id_seq`
    - `inventory.batch_id_seq`
    - `inventory.motion_id_seq`

- `farmaexpres_inventory_read_user`
  - hereda del rol `farmaexpres_inventory_readonly`
  - puede solo `SELECT` sobre:
    - `inventory.product`
    - `inventory.batch`
    - `inventory.motion`
  - puede consultar las secuencias:
    - `inventory.product_id_seq`
    - `inventory.batch_id_seq`
    - `inventory.motion_id_seq`

## Separacion esperada

- `auth-service` se conecta a `farmaexpres` con `farmaexpres_auth_app_user` y `currentSchema=auth`.
- `inventory-service` se conecta a `farmaexpres` con `farmaexpres_inventory_app_user` y `currentSchema=inventory`.
- `alert-service` se conecta con `farmaexpres_inventory_read_user` y `search_path=inventory`, porque solo consulta informacion de inventario.
- Ningun microservicio debe usar `postgres` como usuario de aplicacion.

## Nota importante para pruebas en pgAdmin

Si se quieren probar permisos con estos usuarios, se recomienda crear conexiones nuevas en pgAdmin en lugar de reutilizar la conexion de `postgres`.

Esto evita que pgAdmin mezcle credenciales anteriores en una misma conexion guardada.

## Regla del proyecto

Los cambios de base de datos ya no deben agregarse en `init.sql`.
Todo cambio nuevo debe entrar como migracion versionada dentro de `auth/` o `inventory/`.
