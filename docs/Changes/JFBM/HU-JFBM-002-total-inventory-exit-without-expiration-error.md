# HU-JFBM-002 - Salida total de inventario sin error de vencimiento

## 1. Informacion general
- HU: `HU-JFBM-002`
- Nombre: Salida total de inventario sin error de vencimiento
- Componente principal: `inventory-service`
- Componentes relacionados: `database`, `api-gateway`
- Estado: Implementado
- Rama de trabajo sugerida: `HU-JFBM-002-dev`

## 2. Objetivo
Permitir que el farmaceutico registre una salida de inventario que deje un medicamento sin stock, sin que el backend falle por no tener un proximo vencimiento disponible.

## 3. Por que se necesita
Cuando se registraba la salida de la ultima unidad de un medicamento, el sistema recalculaba el inventario del producto y dejaba:

```text
stock = 0
expirationdate = null
```

Esto es correcto desde negocio, porque un producto sin lotes activos no tiene proximo vencimiento operativo. Sin embargo, la base de datos tenia la columna `product.expirationdate` como obligatoria (`NOT NULL`), por lo que PostgreSQL rechazaba el cambio y el frontend recibia el mensaje generico:

```text
Ocurrio un error interno en inventory-service
```

## 4. Que se realizo
- Se permitio que `product.expirationdate` pueda quedar en `NULL`.
- Se agrego una migracion Liquibase para actualizar la estructura de la tabla `product`.
- Se agrego rollback para volver a marcar la columna como obligatoria si fuera necesario.
- Se ajusto la entidad `Product` para que el modelo Java coincida con la base de datos.
- Se aplico la migracion en la base de datos local de desarrollo.
- Se ejecutaron las pruebas del `inventory-service`.

## 5. Flujo corregido

```text
Antes:
Farmaceutico -> registra salida total -> stock queda en 0 -> expirationdate queda null -> error 500

Despues:
Farmaceutico -> registra salida total -> stock queda en 0 -> expirationdate queda null -> salida registrada correctamente
```

## 6. Cambios principales

### 6.1 Migracion de base de datos
Se agrego el archivo:

```text
database/inventory/01_ddl/00_tables/004_allow_nullable_product_expirationdate.sql
```

Contenido principal:

```sql
ALTER TABLE product
    ALTER COLUMN expirationdate DROP NOT NULL;
```

### 6.2 Rollback de la migracion
Se agrego el archivo:

```text
database/inventory/05_rollbacks/01_ddl/00_tables/004_allow_nullable_product_expirationdate.rollback.sql
```

El rollback asigna una fecha a los productos que tengan `expirationdate` en `NULL` y luego vuelve a activar la restriccion `NOT NULL`.

### 6.3 Changelog Liquibase
Se actualizo:

```text
database/inventory/01_ddl/00_tables/changelog.yaml
```

Se incluyo el changeset:

```text
inventory-004-allow-nullable-product-expirationdate
```

### 6.4 Entidad de producto
Se actualizo:

```text
inventory-service/src/main/java/co/edu/corhuila/inventory_service/Entity/Product.java
```

El campo `expirationDate` dejo de declararse como obligatorio en JPA:

```java
@Column(name = "expirationdate")
private LocalDate expirationDate;
```

## 7. Criterios de aceptacion
1. El sistema debe permitir registrar una salida de inventario que deje el stock del medicamento en `0`.
2. Si el producto queda sin lotes activos, `expirationdate` puede quedar en `NULL`.
3. La salida debe quedar registrada como movimiento tipo `Exit`.
4. El frontend no debe mostrar el mensaje generico `Ocurrio un error interno en inventory-service` para este caso.
5. La migracion debe poder aplicarse con Liquibase sin errores.
6. Las pruebas del `inventory-service` deben pasar.

## 8. Validacion realizada
Se aplico la migracion con Liquibase:

```bash
docker compose run --rm liquibase-inventory
```

Resultado:

```text
Liquibase: Update has been successful.
```

Se verifico la columna en PostgreSQL:

```sql
SELECT column_name, is_nullable
FROM information_schema.columns
WHERE table_name = 'product'
  AND column_name = 'expirationdate';
```

Resultado esperado:

```text
expirationdate | YES
```

Se ejecutaron las pruebas:

```bash
./mvnw test
```

Resultado:

```text
Tests run: 30, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 9. Resultado esperado
El farmaceutico puede registrar salidas de inventario completas sobre un medicamento. Si la salida consume la ultima unidad disponible, el producto queda con stock `0` y sin proximo vencimiento operativo, sin generar error interno en `inventory-service`.
