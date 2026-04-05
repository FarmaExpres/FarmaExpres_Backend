# HU-010 - DTO para tabla de inventario activo (inventory-service)

## Actualizacion HU-ACFE-03 (2026-04-04)

Esta HU sigue vigente para tabla de inventario activo.  
Se recomienda complementar la vista con `GET /api/products/fefo-snapshot` para evitar consultas por producto al cargar:

- proximo lote FEFO
- proximo vencimiento operativo
- stock operativo real por lotes activos

Nota:
- si un producto no tiene lote activo consumible, backend retorna `nextBatchCode = null` y `nextExpirationDate = null`.

## 1. Informacion general
- HU: `HU-010`
- Nombre: Consulta de productos activos para tabla de inventario
- Microservicio: `inventory-service`
- Estado: Planeada
- Rama de trabajo: `HU-010-dev`

## 2. Objetivo de la HU
Implementar un DTO de respuesta en `inventory-service` que permita exponer la informacion necesaria para construir una tabla de inventario como la mostrada en la imagen de referencia.

La idea es que el frontend reciba una lista de productos activos con los datos ya organizados para pintar columnas de:
- codigo
- nombre
- stock
- precio
- valor total

## 3. Contexto actual del microservicio
Actualmente `inventory-service` ya dispone de:
- la entidad `Product`
- el controlador `ProductController`
- el servicio `ProductService`
- el repositorio `ProductRepository`
- el endpoint `GET /api/products/Assets` para listar productos activos

La entidad `Product` ya contiene los campos base necesarios para esta HU:
- `code`
- `name`
- `stock`
- `unitPrice`
- `active`

Con esto, no es necesario modificar la estructura de datos principal; basta con crear un DTO de salida y mapear los productos activos a ese nuevo contrato.

## 4. Alcance funcional propuesto
Se propone crear un DTO orientado a la tabla de inventario activo, donde cada fila represente un producto activo y retorne:
- `code`
- `name`
- `stock`
- `unitPrice`
- `totalValue`

El campo `totalValue` debe calcularse en backend con la formula:
- `stock * unitPrice`

Este DTO no reemplaza la entidad `Product`; solo define una vista de salida mas controlada para consumo de frontend.

## 5. Endpoint propuesto
### Consumo oficial por gateway
- Metodo: `GET`
- URL propuesta: `http://localhost:8080/api/products/active-table`

### Endpoint interno del microservicio
- Metodo: `GET`
- URL propuesta: `http://localhost:8082/api/products/active-table`

Nota:
Se propone una ruta nueva para no acoplar el frontend al objeto completo `Product` y mantener separada la respuesta de listado general frente a la respuesta especializada para tabla.

## 6. DTO propuesto
Nombre sugerido:
- `ActiveInventoryTableItemResponse`

Campos sugeridos:
```json
{
  "code": "IBU-001",
  "name": "Ibuprofen 400m",
  "stock": 80,
  "unitPrice": 3200.00,
  "totalValue": 256000.00
}
```

Descripcion de campos:
- `code`: codigo unico del producto
- `name`: nombre comercial o nombre visible del producto
- `stock`: unidades disponibles en inventario
- `unitPrice`: precio unitario del producto
- `totalValue`: resultado de `stock * unitPrice`

## 7. Contrato de respuesta esperado
La respuesta esperada del endpoint es una lista de filas:

```json
[
  {
    "code": "IBU-001",
    "name": "Ibuprofen 400m",
    "stock": 80,
    "unitPrice": 3200.00,
    "totalValue": 256000.00
  },
  {
    "code": "LOS-001",
    "name": "Losartan 50 mg",
    "stock": 75,
    "unitPrice": 14100.00,
    "totalValue": 1057500.00
  },
  {
    "code": "LOT-001",
    "name": "Loratadina 10 mg",
    "stock": 60,
    "unitPrice": 6400.00,
    "totalValue": 384000.00
  }
]
```

## 8. Logica de negocio esperada
El metodo debe:
- consultar unicamente productos con `active = true`
- transformar cada producto a un DTO especializado
- calcular `totalValue` por cada fila
- retornar la lista completa para consumo de frontend

Comportamiento esperado:
- si no hay productos activos, debe retornar una lista vacia
- no debe formatear valores monetarios como texto con simbolos o separadores
- los valores monetarios deben salir como numeros JSON

## 9. Consideraciones de formato
Aunque la imagen muestra valores como:
- `$ 3.200`
- `$ 256.000`

ese formato visual debe resolverse en frontend.

Se recomienda que el backend entregue:
- `unitPrice` como numero
- `totalValue` como numero

Esto evita acoplar el contrato a un formato visual especifico y facilita reutilizar la respuesta en otros clientes.

## 10. Cambios tecnicos esperados
- `Dto`
  - crear el DTO `ActiveInventoryTableItemResponse`
- `ProductService`
  - agregar un metodo que consulte productos activos y los transforme al DTO
- `ProductController`
  - agregar un endpoint GET para retornar la lista DTO
- `ProductRepository`
  - reutilizar `findByActiveTrue()` si ya cubre la necesidad
- `Pruebas`
  - agregar validaciones del mapeo y del calculo de `totalValue`

## 11. Criterios de aceptacion propuestos
1. Debe existir un DTO de salida para representar cada fila de la tabla de inventario activo.
2. El DTO debe incluir `code`, `name`, `stock`, `unitPrice` y `totalValue`.
3. `totalValue` debe calcularse como `stock * unitPrice`.
4. El endpoint debe retornar unicamente productos con `active = true`.
5. La respuesta debe ser una lista de DTOs y no la entidad completa `Product`.
6. Si no existen productos activos, la respuesta debe ser una lista vacia.
7. El endpoint debe poder consumirse a traves de `api-gateway`.

## 12. Archivos candidatos a modificacion
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Dto/`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Controllers/ProductController.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/ProductService.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Repository/ProductRepository.java`
- `inventory-service/src/test/java/...`
- `api-gateway/src/main/resources/application.yaml`

## 13. Estado de este documento
Este documento deja definida la propuesta funcional y tecnica inicial de `HU-010` para posteriormente implementar el DTO y su endpoint asociado en `inventory-service`.
