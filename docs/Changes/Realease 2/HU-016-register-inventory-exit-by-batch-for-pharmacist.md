# HU-016 - Registrar salida de inventario por lote para FARMACEUTICO

## 1. Informacion general
- HU: `HU-016`
- Nombre: Registrar salida de inventario por lote para el rol FARMACEUTICO
- Microservicio: `inventory-service`
- Estado: Propuesta funcional para implementacion en backend
- Rama de trabajo sugerida: `HU-016-dev`

## 2. Objetivo de la HU
Implementar en `inventory-service` un metodo que permita al rol `FARMACEUTICO` registrar salidas de inventario sobre medicamentos ya existentes.

La vista de referencia muestra que el usuario debe poder diligenciar:
- medicamento existente
- cantidad
- motivo
- observaciones opcionales

La operacion debe respetar el modelo actual del backend basado en lotes y en trazabilidad de movimientos.

## 3. Justificacion funcional
Actualmente el backend no cuenta con un metodo especializado para que el rol `FARMACEUTICO` registre salidas de inventario desde la pantalla de `Salidas`.

Esto genera un vacio funcional porque:
- el farmaceutico si necesita descontar existencias de medicamentos ya creados
- el descuento no puede hacerse solo contra el stock general del producto
- el backend ya trabaja con trazabilidad por lote
- toda salida debe quedar reflejada en `movimientos`
- la salida debe respetar el orden de consumo de los lotes disponibles

Por lo tanto, no es suficiente disminuir el stock del producto de forma directa.  
La salida debe consumir existencias desde los lotes registrados y dejar trazabilidad de cada afectacion.

## 4. Justificacion del microservicio seleccionado
La HU debe implementarse en `inventory-service` porque alli ya existen las entidades y reglas de negocio relacionadas con:
- productos
- lotes
- movimientos
- sincronizacion de stock operativo
- seguridad por rol

El microservicio ya dispone de:
- entidad `Product`
- entidad `Batch`
- entidad `Motion`
- `BatchService`
- `MotionService`
- `SecurityConfig`

Esto permite resolver la HU respetando el modelo batch-aware ya implementado.

## 5. Necesidad funcional observada en la interfaz
La interfaz de `Registrar Salida de Inventario` evidencia un formulario para registrar egresos de medicamentos existentes.

Los campos visibles actualmente son:
- `medicamento`
- `cantidad`
- `motivo`
- `observaciones`

La regla principal observada es que el farmaceutico selecciona un medicamento ya existente y solicita una cantidad de salida.

Sin embargo, para backend es necesario definir explicitamente:
- como se distribuye la salida entre lotes
- que no se aceptan cantidades mayores al stock total disponible del producto
- que el descuento debe avanzar lote por lote hasta completar la salida

## 6. Regla funcional principal
Cada vez que el `FARMACEUTICO` registre una salida:

1. se selecciona un medicamento ya existente
2. se ingresa una cantidad mayor a cero
3. se selecciona un motivo
4. se registra una observacion opcional
5. el backend valida el stock total disponible por lotes
6. si la cantidad solicitada supera el total disponible, la operacion se rechaza
7. si la cantidad es valida, el backend descuenta primero del lote mas antiguo disponible
8. si ese lote no alcanza, continua con el siguiente lote disponible
9. el proceso se repite hasta completar la salida solicitada
10. el stock operativo del producto se recalcula con base en los lotes activos
11. la operacion queda registrada en `movimientos`

## 7. Comportamiento esperado del backend
El metodo propuesto no debe descontar manualmente un unico lote fijo ni modificar el producto sin considerar los lotes.

En su lugar debe:
- validar que el producto exista
- validar que el producto este activo
- validar que la cantidad solicitada sea mayor a `0`
- validar que exista stock suficiente sumando los lotes disponibles del producto
- consumir existencias lote por lote
- descontar primero del primer lote registrado disponible
- continuar con el segundo lote cuando el primero ya no tenga unidades suficientes
- registrar la trazabilidad del motivo y observacion
- registrar el movimiento tipo `Exit`

Resultado esperado:
- un mismo medicamento puede tener multiples lotes
- una salida puede afectar uno o varios lotes
- la consulta de movimientos debe mostrar:
  - tipo `Salida`
  - medicamento
  - lote afectado
  - cantidad descontada por lote
  - motivo
  - usuario que realizo la accion

## 8. Alcance funcional propuesto
Se propone crear un endpoint especializado para registrar salidas de inventario desde la UI de `Salidas`.

El endpoint debe permitir:
- registrar salidas solo para productos existentes
- validar que la cantidad no supere el stock total disponible
- distribuir la salida entre los lotes del producto
- registrar el movimiento asociado
- recalcular el stock del producto
- restringir el acceso al rol `FARMACEUTICO`

No hace parte de esta HU:
- crear productos nuevos
- editar salidas historicas
- permitir salidas con stock insuficiente
- descontar cantidades ignorando los lotes

## 9. Endpoint propuesto
### Consumo oficial por gateway
- Metodo: `POST`
- URL propuesta: `http://localhost:8080/api/movements/exits`

### Endpoint interno del microservicio
- Metodo: `POST`
- URL propuesta: `http://localhost:8082/api/movements/exits`

Se propone una ruta especifica para salidas porque funcionalmente el formulario representa un caso de uso de negocio concreto y no solo un movimiento generico.

## 10. Payload propuesto
```json
{
  "productId": 8,
  "quantity": 30,
  "reason": "Dispensacion",
  "detail": "Salida registrada por entrega interna"
}
```

## 11. Validaciones de negocio propuestas
El backend debe validar como minimo:

1. `productId` es obligatorio y debe existir
2. el producto debe estar activo
3. `quantity` es obligatoria y debe ser mayor a `0`
4. `reason` es obligatorio
5. `reason` debe pertenecer al catalogo permitido definido para salidas
6. `detail` es opcional
7. la suma de `availableStock` de los lotes disponibles debe ser suficiente para cubrir la salida
8. si la cantidad solicitada supera el total disponible, la solicitud debe rechazarse
9. no se deben afectar lotes retirados
10. no se deben afectar lotes vencidos

## 12. Regla de consumo por lote
Para cada salida registrada:
- se debe buscar el conjunto de lotes disponibles del producto
- el consumo debe iniciar sobre el primer lote registrado disponible
- si ese lote no alcanza, se continua con el siguiente lote
- la salida se completa usando tantos lotes como sean necesarios
- ningun lote puede quedar con stock negativo

Ejemplo funcional:
- lote 1: `15`
- lote 2: `20`
- lote 3: `5`
- total disponible: `40`

Si el farmaceutico registra una salida de `30`:
- el lote 1 queda en `0`
- el lote 2 queda en `5`
- el lote 3 queda en `5`

Si el farmaceutico registra una salida de `42`:
- la operacion debe rechazarse
- no se debe descontar parcialmente ningun lote
- el backend debe responder que la cantidad solicitada supera el stock total disponible

## 13. Regla de trazabilidad en movimientos
Toda salida realizada por el `FARMACEUTICO` debe quedar registrada en `Motion`.

La trazabilidad debe incluir:
- tipo de movimiento: `Exit`
- cantidad solicitada
- producto asociado
- lote o lotes afectados
- cantidad descontada en cada lote
- motivo
- observacion si fue enviada
- usuario autenticado
- rol autenticado
- fecha y hora del registro

Esto garantiza coherencia con la tabla de `Movimientos` mostrada en la interfaz.

## 14. Restriccion de seguridad
El acceso principal de esta HU debe quedar controlado de forma exclusiva para el rol `FARMACEUTICO`.

Regla esperada:
- `FARMACEUTICO` puede registrar salidas por este metodo
- `FARMACEUTICO` tambien puede consultar movimientos de salida
- otros roles no deben poder usar este endpoint especializado de registro

Para la parte de consulta, se debe habilitar acceso del rol `FARMACEUTICO` al endpoint o vista backend que expone movimientos tipo `Exit`.

Estas restricciones deben aplicarse en `SecurityConfig` del `inventory-service`.

## 15. Contrato de respuesta propuesto
Respuesta sugerida:

```json
{
  "type": "Exit",
  "productId": 8,
  "requestedQuantity": 30,
  "reason": "Dispensacion",
  "detail": "Salida registrada por entrega interna",
  "allocations": [
    {
      "batchId": 10,
      "batchCode": "LOT-MET-20260401-001",
      "expirationDate": "2027-01-10",
      "quantity": 15
    },
    {
      "batchId": 11,
      "batchCode": "LOT-MET-20260403-001",
      "expirationDate": "2027-03-15",
      "quantity": 15
    }
  ]
}
```

La respuesta debe permitir al frontend conocer:
- que la salida fue registrada
- cuales lotes fueron afectados
- que cantidad fue descontada en cada lote

## 16. Implementacion tecnica sugerida
- `MotionController`
  - crear endpoint `POST /api/movements/exits`
  - exponer o habilitar consulta de movimientos de salida para `FARMACEUTICO`
- `MotionService` o servicio especializado
  - centralizar validaciones
  - validar stock suficiente por lotes
  - consumir existencias lote por lote
  - registrar `Motion` tipo `Exit`
  - mantener o adaptar la consulta de salidas para que pueda ser consumida por `FARMACEUTICO`
- `BatchService`
  - apoyar la obtencion de lotes disponibles
  - recalcular stock del producto
- `Dto`
  - crear request especializado para salida de inventario
- `SecurityConfig`
  - permitir acceso exclusivo a `FARMACEUTICO`
- `api-gateway`
  - exponer la nueva ruta

## 17. Criterios de aceptacion propuestos
1. Debe existir un endpoint backend para registrar salidas de inventario de medicamentos ya existentes.
2. El endpoint debe recibir `productId`, `quantity`, `reason` y `detail` opcional.
3. La salida no debe poder ser mayor al stock total disponible del producto.
4. La salida debe descontarse desde los lotes disponibles del producto.
5. Si un lote no alcanza, el backend debe continuar con el siguiente lote hasta completar la cantidad solicitada.
6. Ningun lote debe quedar con stock negativo.
7. Si la cantidad solicitada supera el total disponible, el backend debe rechazar la solicitud sin realizar descuentos parciales.
8. El stock operativo del producto debe actualizarse despues de registrar la salida.
9. La operacion debe quedar registrada en `movimientos`.
10. El movimiento debe quedar asociado al lote o lotes afectados.
11. El rol `FARMACEUTICO` debe poder consultar los movimientos de salida.
12. Solo el rol `FARMACEUTICO` debe poder consumir este metodo de registro.

## 18. Archivos candidatos a modificacion
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Controllers/MotionController.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/MotionService.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/BatchService.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Config/SecurityConfig.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Dto/`
- `inventory-service/src/test/java/...`
- `api-gateway/src/main/resources/application.yaml`

## 19. Riesgos o validaciones previas
- Definir si el orden de consumo sera estrictamente por primer lote registrado o por lote mas proximo a vencer.
- Confirmar el catalogo exacto de motivos permitido para salidas.
- Validar si el rol `ADMIN` podra consultar el historico resultante, aunque no pueda registrar salidas por este metodo.
- Confirmar si los lotes con estado `OUT_OF_STOCK`, `EXPIRED` o `RETIRED` quedan excluidos del proceso de consumo.

## 20. Estado de este documento
Este documento justifica la necesidad de implementar una HU para registrar salidas de inventario por lote para el rol `FARMACEUTICO`, alineada con el modelo actual de productos, lotes y movimientos existente en el backend.
