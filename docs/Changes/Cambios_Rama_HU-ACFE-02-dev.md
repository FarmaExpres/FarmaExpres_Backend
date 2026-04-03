# Cambios de la Rama `HU-ACFE-02-dev`

Fecha de generacion: 2026-04-02  
Rama analizada: `HU-ACFE-02-dev`  
Base de comparacion: `origin/Develop` (`a503a50`)

## 1. Resumen General

Esta rama contiene 3 commits sobre `origin/Develop` y concentra ajustes de:
- trazabilidad detallada para movimientos `UPDATED` en inventario,
- validacion estricta de correo en backend (dominio y sintaxis canonica),
- validacion y normalizacion de nombre completo en backend,
- estabilizacion de ejecucion de pruebas de `auth-service` con perfil `test`.

Metricas del diff (`a503a50..HU-ACFE-02-dev`):
- Archivos cambiados: 14
- Inserciones: 575
- Eliminaciones: 32

## 2. Commits Incluidos en la Rama

| Commit | Fecha | Autor | Mensaje |
|---|---|---|---|
| `6ddb465` | 2026-04-02 | Nicolas Tello Mendez | `feat(auth): enforce full-name validation and stabilize test profile execution` |
| `02c192f` | 2026-04-02 | Nicolas Tello Mendez | `feat(auth): enforce strict email domain validation on user create/update and login` |
| `66d31bc` | 2026-04-02 | Nicolas Tello Mendez | `feat(inventory): add detailed UPDATED audit trail and dual movement handling on product update` |

## 3. Cambios Funcionales y Tecnicos

### 3.1 `inventory-service` - Detalle de ajustes en movimientos `UPDATED`

Se fortalecio la trazabilidad de auditoria para actualizaciones de productos:
- Persistencia de nuevos campos en `motion`:
  - `adjustment_summary` (`TEXT`)
  - `adjustment_detail` (`JSONB`)
- Modelo `Motion` extendido para exponer:
  - `adjustmentSummary`
  - `adjustmentDetail`
- Respuesta de `GET /api/movements` enriquecida con:
  - resumen corto del ajuste,
  - detalle campo a campo (`field`, `label`, `before`, `after`, `format`).
- Nuevo DTO tipado:
  - `AdjustmentDetailItem`.
- Logica de diff real en `ProductService.updateProduct` para:
  - `name`, `unitPrice`, `minimumStock`, `expirationDate`.
- Si no hay cambios reales, no se genera movimiento `UPDATED`.
- Comparacion de precio robusta con `BigDecimal.compareTo(...)` para evitar falsos positivos por escala.
- Cuando en una sola actualizacion cambian stock y datos no-stock:
  - se crean dos movimientos en la misma transaccion:
    - `Entrance`/`Exit` por stock,
    - `Updated` por auditoria de campos.

### 3.2 `auth-service` - Validacion estricta de correo

Se aplico validacion canonica de email en backend como fuente de verdad:
- Aplicada en:
  - `POST /api/users`
  - `PUT /api/users/{id}/update`
  - `POST /api/auth/login`
- Reglas implementadas:
  - estructura valida con un solo `@`,
  - dominio con etiquetas validas (sin iniciar/terminar en `-`),
  - TLD solo letras y minimo 2 caracteres,
  - bloqueo de punycode (`xn--`),
  - rechazo de casos como `marlon-.@gmail-.com`.
- Se agrega `EmailValidator` + pruebas unitarias dedicadas.

### 3.3 `auth-service` - Validacion y normalizacion de nombre completo

Se alinea backend con frontend para el campo `name`:
- Reglas permitidas:
  - letras (`A-Z`, `a-z`),
  - tildes (`ÁÉÍÓÚáéíóú`),
  - `Ññ`,
  - espacios.
- Rechaza:
  - numeros,
  - simbolos especiales,
  - vacio o solo espacios.
- Normalizacion previa:
  - `trim()` de extremos,
  - colapso de espacios internos multiples a uno.
- Se agrega `NameValidator` + pruebas unitarias dedicadas.

### 3.4 Estabilidad de pruebas en `auth-service`

Se estabiliza la suite de pruebas con perfil dedicado:
- `AuthServiceApplicationTests` ahora usa `@ActiveProfiles("test")`.
- Nuevo archivo:
  - `src/test/resources/application-test.yaml`
- El perfil `test` desacopla el `contextLoads` de variables de entorno productivas.

## 4. Validaciones Ejecutadas

### 4.1 Compilacion y pruebas
- `inventory-service`:
  - `mvn -DskipTests compile` (OK).
- `auth-service`:
  - `mvn -q -Dtest=EmailValidatorTest test` (OK).
  - `mvn -q -Dtest=NameValidatorTest test` (OK).
  - `mvn -q test` (OK, suite completa con perfil `test`).

### 4.2 Pruebas funcionales API relevantes
- `POST /api/users`:
  - acepta nombre valido con normalizacion de espacios.
  - rechaza nombre invalido con `400`.
- `PUT /api/users/{id}/update`:
  - rechaza nombre invalido con `400`.
  - acepta nombre valido y normaliza espacios.
- `POST /api/users` y `PUT /api/users/{id}/update`:
  - rechazan dominios de correo invalidos con `400`.
- `POST /api/auth/login`:
  - valida sintaxis de correo en backend.
- `PUT /api/products/{id}` + `GET /api/movements`:
  - al cambiar fecha de vencimiento, se registra `UPDATED` con detalle antes/despues.
  - al cambiar stock + datos, aparecen `Entrance/Exit` y `Updated`.

## 5. Alcance y Notas Operativas

- No se implementaron cambios en `api-gateway` por alcance acordado.
- La integracion operativa se mantiene directa desde frontend hacia cada microservicio.
- Se conservaron contratos existentes para `ENTRANCE` y `EXIT`, extendiendo solo el caso `UPDATED`.

## 6. Listado de Archivos Modificados (`a503a50..HU-ACFE-02-dev`)

Formato: `Estado<TAB>Ruta` (`A`=Added, `M`=Modified)
```text
M	auth-service/src/main/java/co/edu/corhuila/auth_service/Service/AuthService.java
M	auth-service/src/main/java/co/edu/corhuila/auth_service/Service/userService.java
A	auth-service/src/main/java/co/edu/corhuila/auth_service/Validation/EmailValidator.java
A	auth-service/src/main/java/co/edu/corhuila/auth_service/Validation/NameValidator.java
M	auth-service/src/test/java/co/edu/corhuila/auth_service/AuthServiceApplicationTests.java
A	auth-service/src/test/java/co/edu/corhuila/auth_service/Validation/EmailValidatorTest.java
A	auth-service/src/test/java/co/edu/corhuila/auth_service/Validation/NameValidatorTest.java
A	auth-service/src/test/resources/application-test.yaml
M	database/init.sql
M	inventory-service/pom.xml
A	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Dto/AdjustmentDetailItem.java
M	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Dto/MotionResponse.java
M	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Entity/Motion.java
M	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/ProductService.java
```
