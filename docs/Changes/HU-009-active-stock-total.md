# HU-009 - Resumen de stock y valor total de productos activos (inventory-service)

## Actualizacion HU-ACFE-03 (2026-04-04)

Se mantiene vigente `GET /api/products/active-summary`.  
Adicionalmente se incorporo endpoint optimizado para snapshot FEFO por producto:

- `GET /api/products/fefo-snapshot`

Este endpoint reduce N+1 en frontend y devuelve por producto:
- `operationalStock`
- `nextBatchCode`
- `nextExpirationDate`
- `activeBatchesCount`

Regla operativa:
- solo lotes `ACTIVE`, con `availableStock > 0` y no vencidos.

## 1. Informacion general
- HU: `HU-009`
- Nombre: Consulta del total de stock y valor total de productos activos
- Microservicio: `inventory-service`
- Estado: Planeada
- Rama de trabajo: `HU-009-dev`

## 2. Objetivo de la HU
Implementar un nuevo metodo en `inventory-service` que permita consultar un resumen consolidado de los productos activos, retornando:
- el total de unidades en stock de los productos activos
- el valor total monetario del inventario activo

Esta HU busca ofrecer al frontend una vista rapida del inventario disponible sin necesidad de recorrer producto por producto para hacer sumatorias manuales.

## 3. Contexto actual del microservicio
Actualmente `inventory-service` ya dispone de:
- `GET /api/products` para listar todos los productos
- `GET /api/products/Assets` para listar productos activos
- `ProductController`, `ProductService` y `ProductRepository` como base del modulo
- la entidad `Product` con los campos necesarios para esta HU:
  - `stock`
  - `unitPrice`
  - `active`

Con esta base, la HU puede implementarse como una consulta agregada sobre productos activos sin modificar la estructura principal del microservicio.

## 4. Alcance funcional propuesto
Se agregara un nuevo metodo que:
- considere solo productos con `active = true`
- sume el `stock` de todos los productos activos
- calcule el valor total del inventario activo con la formula:
  - `stock * unitPrice` por cada producto
- retorne ambos valores en una sola respuesta

Este metodo no reemplaza los endpoints actuales de listado; los complementa con una consulta de resumen.

El calculo del valor total debe hacerse producto por producto:
- `stock * unitPrice`
- luego sumar el resultado de todos los productos activos

## 5. Endpoint propuesto
### Consumo oficial por gateway
- Metodo: `GET`
- URL propuesta: `http://localhost:8080/api/products/active-summary`

### Endpoint interno del microservicio
- Metodo: `GET`
- URL propuesta: `http://localhost:8082/api/products/active-summary`

Nota:
Se propone usar una ruta nueva y en minusculas para mantener claridad funcional. No depende del endpoint actual `GET /api/products/Assets`, que ya existe para listado de productos activos.

## 6. Logica de negocio esperada
El metodo debe:
- consultar solo registros con `active = true`
- calcular `totalStock` como la suma de `stock`
- calcular `totalInventoryValue` como la suma de `stock * unitPrice`
- responder correctamente incluso si no existen productos activos

Comportamiento esperado cuando no haya productos activos:
- `totalStock = 0`
- `totalInventoryValue = 0`

## 7. Contrato de respuesta esperado
```json
{
  "totalStock": 240,
  "totalInventoryValue": 786000.00
}
```

## 8. Cambios tecnicos esperados
- `ProductController`
  - agregar un nuevo endpoint GET para el resumen
- `ProductService`
  - agregar un metodo que calcule la suma de stock y valor total
- `ProductRepository`
  - reutilizar consultas existentes o agregar una consulta agregada especifica para productos activos
- DTO
  - crear un DTO de respuesta para el resumen, con nombres claros y estables
- Pruebas
  - agregar pruebas unitarias o de integracion para validar el calculo

## 9. Criterios de aceptacion propuestos
1. Debe existir un endpoint en `inventory-service` para consultar el resumen de productos activos.
2. El endpoint debe considerar unicamente productos con `active = true`.
3. La respuesta debe incluir el total de stock de productos activos.
4. La respuesta debe incluir el valor total monetario del inventario activo.
5. Si no existen productos activos, el endpoint debe responder valores en `0` y no fallar.
6. El valor total debe calcularse multiplicando `stock * unitPrice` para cada producto activo y sumando todos los resultados.
7. El endpoint debe poder consumirse a traves de `api-gateway`.

## 10. Riesgos o validaciones previas
- Debe definirse si `totalInventoryValue` se retornara como `number` JSON simple o con formato monetario; se recomienda devolverlo como valor numerico sin formato.
- El formato visual tipo `$ 5.199.000` debe resolverse en frontend; backend debe responder el valor numerico limpio.
- Si el equipo prefiere evitar calculos en memoria, puede resolverse con agregaciones desde `ProductRepository`.

## 11. Archivos candidatos a modificacion
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Controllers/ProductController.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/ProductService.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Repository/ProductRepository.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Dto/`
- `inventory-service/src/test/java/...`
- `api-gateway/src/main/resources/application.yaml`

## 12. Estado de este documento
Este documento deja definida la propuesta funcional y tecnica inicial de `HU-009` antes de iniciar la implementacion del metodo en `inventory-service`.
