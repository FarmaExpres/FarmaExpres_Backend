# Cambios de la Rama `HU-ACFE-04-dev`

Fecha de generacion: 2026-04-25  
Rama analizada: `HU-ACFE-04-dev`  
Alcance: bloqueo operativo de medicamentos inactivos y listado administrativo completo para Gestion de Medicamentos.

## 1. Resumen General

Esta rama atiende ajustes solicitados por frontend para separar dos comportamientos:

- Listados operativos: solo medicamentos activos y utilizables.
- Gestion administrativa de medicamentos: medicamentos activos e inactivos con estado visible.

Tambien se refuerza backend como barrera definitiva para impedir entradas, salidas, lotes y consumo FEFO sobre medicamentos inactivos, aun cuando el endpoint se consuma directamente.

## 2. Cambios Funcionales y Tecnicos

### 2.1 `inventory-service` - Productos operativos vs administrativos

Se separaron los listados de productos:

- `GET /api/products`
  - queda como listado operativo,
  - devuelve solo medicamentos activos,
  - se mantiene como fuente para selects de entradas/salidas y vistas operativas.
- `GET /api/products/all`
  - nuevo endpoint administrativo,
  - devuelve medicamentos activos e inactivos,
  - conserva el campo `active` para que frontend pinte estado `Activo` o `Inactivo`,
  - pensado para la tabla de Gestion de Medicamentos.

### 2.2 `inventory-service` - Bloqueo de medicamentos inactivos

Se centralizo la validacion de producto operable en `BatchService`:

- `findOperableProductOrThrow(...)`
- `validateProductIsOperable(...)`
- `isProductOperable(...)`

La validacion se aplica antes de operaciones de inventario:

- `POST /api/movements/entries`
- `POST /api/movements/exits`
- `POST /api/movements`
- `POST /api/movements/consume-fefo`
- `POST /api/products/{productId}/batches`

Mensajes principales:

- `No se pueden registrar movimientos sobre un medicamento inactivo.`
- `No se pueden registrar salidas sobre un medicamento inactivo.`
- `No se pueden crear lotes sobre un medicamento inactivo.`

HTTP usado para producto inactivo:

- `409 Conflict`

### 2.3 `inventory-service` - FEFO y lotes consumibles

Se ajustaron queries de lotes para excluir productos inactivos:

- lotes consumibles por producto,
- lotes consumibles por orden de registro,
- snapshot FEFO.

Regla consolidada:

- si el producto esta inactivo, su stock operativo se considera `0`.
- un lote de producto inactivo no se considera consumible aunque el lote tenga estado `ACTIVE`.

### 2.4 Seguridad

Se agrego regla especifica en `SecurityConfig`:

- `GET /api/products/all`
  - roles permitidos: `ADMIN`, `AUDITOR`.

El matcher general de lectura de productos queda despues, para no exponer el listado administrativo a `FARMACEUTICO`.

### 2.5 Correccion de correo seed/documentacion

Se corrigio el correo de Jersson Fabian Buitrago:

- Antes: `jerssson@gmail.com`
- Ahora: `jersson@gmail.com`

Aplicado en:

- seed de usuarios,
- rollback del seed,
- guia de inicializacion,
- documento historico de cambios.

Tambien se actualizo la base activa en Docker:

```text
Jersson Fabian Buitrago | jersson@gmail.com | Asset
```

## 3. Contratos API Relevantes

### 3.1 Listado operativo

Endpoint:

- `GET /api/products`

Uso esperado:

- selects de entradas,
- selects de salidas,
- vistas operativas de inventario.

Respuesta esperada:

- solo productos con `active = true`.

### 3.2 Listado administrativo completo

Endpoint:

- `GET /api/products/all`

Uso esperado:

- tabla de Gestion de Medicamentos,
- visualizacion de productos desactivados con badge `Inactivo`.

Respuesta esperada:

- productos con `active = true`,
- productos con `active = false`,
- inactivos con stock operativo `0`.

### 3.3 Historicos

Los movimientos historicos no se ocultan:

- `GET /api/movements`
- `GET /api/movements/entrance`
- `GET /api/movements/exit`
- reportes historicos existentes.

Esto mantiene trazabilidad de movimientos previos y del evento de eliminacion logica.

## 4. Validaciones Ejecutadas

### 4.1 Pruebas unitarias

Comando ejecutado:

```bash
mvn "-Dtest=MotionServiceTest,ProductServiceTest" test
```

Resultado:

- 23 tests ejecutados.
- 0 fallos.
- 0 errores.

### 4.2 Docker

Se reconstruyo y reinicio `inventory-service`:

```bash
docker compose up -d --build inventory-service
```

Estado verificado:

- `inventory-service` saludable (`healthy`).
- Puerto expuesto: `8082`.

### 4.3 Base de datos activa

Se actualizo el correo en la base `farmaexpres_users`:

```sql
UPDATE users
SET email = 'jersson@gmail.com'
WHERE email = 'jerssson@gmail.com';
```

Resultado:

- 1 registro actualizado.

## 5. Alcance y Notas Operativas

- No se eliminan medicamentos inactivos: se conservan como historicos/desactivados.
- No se ocultan movimientos historicos.
- El frontend debe usar:
  - `GET /api/products` para operacion,
  - `GET /api/products/all` para Gestion de Medicamentos.
- Alertas, dashboard, FEFO y stock operativo se mantienen excluyendo productos inactivos.

## 6. Listado de Archivos Modificados

Formato: `Estado<TAB>Ruta` (`A`=Added, `M`=Modified)

```text
M	database/auth/02_dml/00_seed/002_seed_users.sql
M	database/auth/05_rollbacks/02_dml/00_seed/002_seed_users.rollback.sql
M	docs/Changes/Cambios_Backend_Hasta_Ahora_2026-03-29.md
A	docs/Changes/Cambios_Rama_HU-ACFE-04-dev.md
M	docs/Guia_Inicializacion_Backend_Frontend.md
M	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Config/SecurityConfig.java
M	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Controllers/ProductController.java
M	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Entity/Product.java
M	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Repository/BatchRepository.java
M	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/BatchService.java
M	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/MotionService.java
M	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/ProductService.java
M	inventory-service/src/test/java/co/edu/corhuila/inventory_service/Service/MotionServiceTest.java
M	inventory-service/src/test/java/co/edu/corhuila/inventory_service/Service/ProductServiceTest.java
```
