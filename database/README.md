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

## Regla del proyecto

Los cambios de base de datos ya no deben agregarse en `init.sql`.
Todo cambio nuevo debe entrar como migracion versionada dentro de `auth/` o `inventory/`.
