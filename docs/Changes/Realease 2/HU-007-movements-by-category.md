# HU-007 - Consulta de movimientos por categoria (inventory-service)

## 1. Informacion general
- HU: `HU-007`
- Nombre: Consulta separada de movimientos de inventario
- Microservicio: `inventory-service`
- Estado: Planeada
- Rama de trabajo: `HU-007-dev`

## 2. Objetivo de la HU
Permitir que el frontend consulte los movimientos de inventario separados por categoria para mostrar vistas independientes de:
- `Entrance`
- `Exit`
- `Updated`

La necesidad funcional responde al diseno del modulo mostrado en interfaz, donde cada pestana debe consumir un metodo especifico y retornar solo la informacion correspondiente a esa categoria.

## 3. Contexto actual del microservicio
Actualmente `inventory-service` ya dispone de:
- Un endpoint general para listar movimientos:
  - `GET /api/movements`
- Una entidad `Motion` con informacion suficiente para clasificar movimientos.
- Un repositorio `MotionRepository` con una base inicial para filtrar por tipo:
  - `findByType(MovementType type)`

Esto permite extender la HU sin rehacer la estructura principal del modulo.

## 4. Alcance funcional propuesto
Para esta HU se implementaran metodos de consulta separados, uno por uno, dentro de `inventory-service`, con el siguiente enfoque:

### Metodo 1. Ver movimientos de entradas
- Exponer un endpoint dedicado para listar solo movimientos de entrada.
- Retornar exclusivamente registros cuyo tipo corresponda a `Entrance`.
- Mantener el mismo contrato base de `MotionResponse`, salvo que luego se acuerde un ajuste puntual.

### Metodo 2. Ver movimientos de salidas
- Exponer un endpoint dedicado para listar solo movimientos de salida.
- Retornar exclusivamente registros cuyo tipo corresponda a `Exit`.
- Reutilizar la misma capa de servicio y DTO ya existentes.

### Metodo 3. Ver movimientos de ajustes
- Exponer un endpoint dedicado para listar solo movimientos de ajuste.
- Retornar exclusivamente registros cuyo tipo corresponda a `Updated`.
- Incluir en la respuesta los campos de ajuste ya existentes cuando apliquen:
  - `adjustmentSummary`
  - `adjustmentDetail`
  - `observation`

## 5. Propuesta inicial de endpoints
### Consumo oficial por gateway
- `GET /api/movements/entries`
- `GET /api/movements/exits`
- `GET /api/movements/adjustments`

### Endpoint interno del microservicio
- `GET http://localhost:8082/api/movements/entries`
- `GET http://localhost:8082/api/movements/exits`
- `GET http://localhost:8082/api/movements/adjustments`

Nota:
Antes de exponerlos por `api-gateway`, se debe validar si la ruta actual del gateway ya cubre automaticamente estas subrutas o si requiere ajuste explicito.

## 6. Cambios tecnicos esperados
- `MotionController`
  - Agregar metodos GET independientes por categoria.
- `MotionService`
  - Agregar metodos de consulta por tipo de movimiento.
- `MotionRepository`
  - Reutilizar o ampliar consultas por `MovementType`.
- `MovementType`
  - Validar si los valores actuales cubren claramente:
    - `Entrance`
    - `Exit`
    - `Updated`
- Pruebas
  - Agregar pruebas unitarias o de integracion para cada nuevo metodo.

## 7. Orden de implementacion acordado
La HU se desarrollara paso a paso, implementando los metodos uno por uno y validando cada uno antes de pasar al siguiente.

Orden tentativo:
1. Metodo de `Entradas`
2. Metodo de `Salidas`
3. Metodo de `Ajustes`

## 8. Criterios de aceptacion esperados
1. Debe existir un metodo independiente para consultar entradas.
2. Debe existir un metodo independiente para consultar salidas.
3. Debe existir un metodo independiente para consultar ajustes.
4. Cada endpoint debe responder solo con movimientos de su categoria.
5. La respuesta debe ser compatible con la visualizacion por pestanas del frontend.
6. La implementacion debe hacerse en `inventory-service`.

## 9. Riesgos o validaciones previas
- Para esta HU, los nombres funcionales del documento deben mantenerse iguales a los del enum `MovementType` actual:
  - `Entrance`
  - `Exit`
  - `Updated`
- En frontend o documentacion visual se puede hablar de entradas, salidas y ajustes, pero en backend la referencia oficial de esta HU sera con los nombres del enum actual.
- Debe confirmarse si el frontend necesita ordenamiento especifico, por ejemplo de mas reciente a mas antiguo.

## 10. Archivos candidatos a modificacion
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Controllers/MotionController.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/MotionService.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Repository/MotionRepository.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Entity/MovementType.java`
- `inventory-service/src/test/java/...`

## 11. Estado de este documento
Este documento describe el alcance y la estrategia de implementacion de `HU-007` antes de iniciar el desarrollo del primer metodo.
