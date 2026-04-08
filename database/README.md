# FarmaExpres Database

Este directorio centraliza la versionacion de la base de datos de `FarmaExpres`.

## Estructura

- `bootstrap.sql`: crea las bases `farmaexpres_users` y `farmaexpres_inventory`
- `auth/`: changelogs y scripts de la base de autenticacion
- `inventory/`: changelogs y scripts de la base de inventario

Cada base sigue la misma organizacion:

- `01_ddl`: cambios estructurales
- `02_dml`: datos iniciales y parches de datos
- `03_dcl`: roles, grants y seguridad
- `04_tcl`: operaciones transaccionales excepcionales
- `05_rollbacks`: scripts de reversa

## Flujo de trabajo

1. PostgreSQL crea las bases con `bootstrap.sql`.
2. Liquibase aplica migraciones sobre `farmaexpres_users`.
3. Liquibase aplica migraciones sobre `farmaexpres_inventory`.
4. Los microservicios arrancan con `ddl-auto: validate` para verificar el esquema y no mutarlo.

## Usuarios de prueba DCL

Se dejaron usuarios `LOGIN` temporales para validar permisos desde pgAdmin o clientes SQL:

- `farmaexpres_auth_app_user` / `AuthApp123!`
- `farmaexpres_auth_read_user` / `AuthRead123!`
- `farmaexpres_inventory_app_user` / `InventoryApp123!`
- `farmaexpres_inventory_read_user` / `InventoryRead123!`

Estos usuarios son de desarrollo y deben cambiarse o eliminarse antes de un uso productivo.

## Permisos por usuario de prueba

### Base `farmaexpres_users`

- `farmaexpres_auth_app_user`
  - hereda del rol `farmaexpres_auth_app`
  - puede `SELECT`, `INSERT`, `UPDATE` y `DELETE` sobre:
    - `role`
    - `users`
    - `binnacle`
  - puede usar las secuencias:
    - `role_id_role_seq`
    - `users_id_seq`
    - `binnacle_id_seq`

- `farmaexpres_auth_read_user`
  - hereda del rol `farmaexpres_auth_readonly`
  - puede solo `SELECT` sobre:
    - `role`
    - `users`
    - `binnacle`
  - puede consultar las secuencias:
    - `role_id_role_seq`
    - `users_id_seq`
    - `binnacle_id_seq`

### Base `farmaexpres_inventory`

- `farmaexpres_inventory_app_user`
  - hereda del rol `farmaexpres_inventory_app`
  - puede `SELECT`, `INSERT`, `UPDATE` y `DELETE` sobre:
    - `product`
    - `batch`
    - `motion`
  - puede usar las secuencias:
    - `product_id_seq`
    - `batch_id_seq`
    - `motion_id_seq`

- `farmaexpres_inventory_read_user`
  - hereda del rol `farmaexpres_inventory_readonly`
  - puede solo `SELECT` sobre:
    - `product`
    - `batch`
    - `motion`
  - puede consultar las secuencias:
    - `product_id_seq`
    - `batch_id_seq`
    - `motion_id_seq`

## Nota importante para pruebas en pgAdmin

Si se quieren probar permisos con estos usuarios, se recomienda crear conexiones nuevas en pgAdmin en lugar de reutilizar la conexion de `postgres`.

Esto evita que pgAdmin mezcle credenciales anteriores en una misma conexion guardada.

## Regla del proyecto

Los cambios de base de datos ya no deben agregarse en `init.sql`.
Todo cambio nuevo debe entrar como migracion versionada dentro de `auth/` o `inventory/`.
