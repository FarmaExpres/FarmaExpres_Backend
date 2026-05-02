# HU-015 - Registrar entrada de inventario por lote para FARMACEUTICO

## 1. Informacion general
- HU: `HU-015`
- Nombre: Registrar entrada de inventario por lote para el rol FARMACEUTICO
- Microservicio: `inventory-service`
- Estado: Propuesta funcional para implementacion en backend
- Rama de trabajo sugerida: `HU-015-dev`

## 2. Objetivo de la HU
Implementar en `inventory-service` un metodo que permita al rol `FARMACEUTICO` registrar nuevas entradas de inventario sobre medicamentos ya existentes.

La vista de referencia muestra que el usuario debe poder diligenciar:
- medicamento existente
- cantidad
- motivo
- observaciones opcionales

Adicionalmente, para que la operacion sea consistente con el modelo actual del backend, se debe incluir:
- fecha de vencimiento obligatoria

## 3. Justificacion funcional
Actualmente el backend no cuenta con un metodo especializado para que el rol `FARMACEUTICO` registre entradas de inventario desde la pantalla de `Entradas`.

Esto genera un vacio funcional porque:
- el farmaceutico si necesita ingresar nuevas existencias de medicamentos ya creados
- cada ingreso nuevo no pertenece necesariamente al lote actual del producto
- el backend ya trabaja con trazabilidad por lote
- toda entrada debe quedar reflejada en `movimientos`

Por lo tanto, no es suficiente aumentar el stock del producto de forma directa.  
La entrada debe crear un nuevo lote y registrar el movimiento asociado.

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
La interfaz de `Registrar Entrada de Inventario` evidencia un formulario para registrar ingresos de medicamentos existentes.

Los campos visibles actualmente son:
- `medicamento`
- `cantidad`
- `motivo`
- `observaciones`

Los motivos visibles en la referencia son:
- `Compra proveedor`
- `Devolucion`
- `Donacion`
- `Ajuste inventario`

Sin embargo, hace falta un dato obligatorio para backend:
- `fechaVencimiento`

Sin esta fecha no es posible crear correctamente el nuevo lote ni mantener la trazabilidad del inventario.

## 6. Regla funcional principal
Cada vez que el `FARMACEUTICO` registre una entrada:

1. se selecciona un medicamento ya existente
2. se ingresa una cantidad mayor a cero
3. se selecciona un motivo
4. se registra una observacion opcional
5. se registra una fecha de vencimiento obligatoria
6. el backend crea un nuevo lote para ese ingreso
7. el producto sigue siendo el mismo
8. el lote ya no sera el lote anterior sino uno nuevo
9. el stock operativo del producto se recalcula con base en los lotes activos
10. la operacion queda registrada en `movimientos`

## 7. Comportamiento esperado del backend
El metodo propuesto no debe modificar manualmente un lote anterior.

En su lugar debe:
- crear un lote nuevo asociado al producto seleccionado
- asignar la cantidad ingresada como `initialStock`
- asignar la misma cantidad como `availableStock`
- guardar la fecha de vencimiento enviada
- dejar trazabilidad del motivo y observacion
- registrar un movimiento tipo `Entrance`

Resultado esperado:
- un mismo medicamento puede tener multiples lotes
- cada nueva entrada representa un nuevo lote
- la consulta de movimientos debe mostrar:
  - tipo `Entrada`
  - medicamento
  - lote creado
  - cantidad ingresada
  - motivo
  - usuario que realizo la accion

## 8. Alcance funcional propuesto
Se propone crear un endpoint especializado para registrar entradas de inventario desde la UI de `Entradas`.

El endpoint debe permitir:
- registrar entradas solo para productos existentes
- crear un lote nuevo por cada entrada
- registrar el movimiento asociado
- recalcular el stock del producto
- restringir el acceso al rol `FARMACEUTICO`

No hace parte de esta HU:
- crear productos nuevos
- editar entradas historicas
- reutilizar un lote existente para sumar unidades

## 9. Endpoint propuesto
### Consumo oficial por gateway
- Metodo: `POST`
- URL propuesta: `http://localhost:8080/api/movements/entries`

### Endpoint interno del microservicio
- Metodo: `POST`
- URL propuesta: `http://localhost:8082/api/movements/entries`

Se propone una ruta especifica para entradas porque funcionalmente el formulario representa un caso de uso de negocio concreto y no solo un movimiento generico.

## 10. Payload propuesto
```json
{
  "productId": 8,
  "quantity": 25,
  "reason": "Compra proveedor",
  "detail": "Ingreso correspondiente a factura FP-2031",
  "expirationDate": "2027-02-15"
}
```

## 11. Validaciones de negocio propuestas
El backend debe validar como minimo:

1. `productId` es obligatorio y debe existir
2. el producto debe estar activo
3. `quantity` es obligatoria y debe ser mayor a `0`
4. `reason` es obligatorio
5. `reason` debe pertenecer al catalogo permitido:
   - `Compra proveedor`
   - `Devolucion`
   - `Donacion`
   - `Ajuste inventario`
6. `detail` es opcional
7. `expirationDate` es obligatoria
8. `expirationDate` no debe corresponder a una fecha vencida

## 12. Regla de creacion del lote
Para cada entrada registrada:
- se debe crear un lote nuevo
- el lote debe quedar asociado al `productId`
- el lote debe tener su propio identificador y codigo de lote
- el lote no debe sobrescribir ni fusionarse automaticamente con otro lote existente

Consideracion importante:
si el medicamento ya tiene lotes anteriores, estos deben conservarse sin alteracion.  
La nueva entrada representa una existencia distinta, por lo que corresponde a un lote distinto.

## 13. Regla de trazabilidad en movimientos
Toda entrada realizada por el `FARMACEUTICO` debe quedar registrada en `Motion`.

La trazabilidad debe incluir:
- tipo de movimiento: `Entrance`
- cantidad ingresada
- producto asociado
- lote creado
- motivo
- observacion si fue enviada
- usuario autenticado
- rol autenticado
- fecha y hora del registro

Esto garantiza coherencia con la tabla de `Movimientos` mostrada en la interfaz.

## 14. Restriccion de seguridad
El acceso principal de esta HU debe quedar controlado para el rol `FARMACEUTICO`.

Regla esperada:
- `FARMACEUTICO` puede registrar entradas por este metodo
- `FARMACEUTICO` tambien puede consultar movimientos de entrada
- otros roles no deben poder usar este endpoint especializado de registro

Para la parte de consulta, se debe habilitar acceso del rol `FARMACEUTICO` al endpoint o vista backend que expone movimientos tipo `Entrance`.

Estas restricciones deben aplicarse en `SecurityConfig` del `inventory-service`.

## 15. Contrato de respuesta propuesto
Respuesta sugerida:

```json
{
  "type": "Entrance",
  "productId": 8,
  "requestedQuantity": 25,
  "reason": "Compra proveedor",
  "detail": "Ingreso correspondiente a factura FP-2031",
  "allocations": [
    {
      "batchId": 31,
      "batchCode": "LOT-MET-20260405-001",
      "expirationDate": "2027-02-15",
      "quantity": 25
    }
  ]
}
```

La respuesta debe permitir al frontend conocer:
- que la entrada fue registrada
- cual fue el lote creado
- que cantidad quedo asociada a ese lote

## 16. Implementacion tecnica sugerida
- `MotionController`
  - crear endpoint `POST /api/movements/entries`
  - exponer o habilitar consulta de movimientos de entrada para `FARMACEUTICO`
- `MotionService` o servicio especializado
  - centralizar validaciones
  - crear lote nuevo
  - registrar `Motion` tipo `Entrance`
  - mantener o adaptar la consulta de entradas para que pueda ser consumida por `FARMACEUTICO`
- `BatchService`
  - apoyar la creacion del lote
  - recalcular stock del producto
- `Dto`
  - crear request especializado para entrada de inventario
- `SecurityConfig`
  - permitir acceso exclusivo a `FARMACEUTICO`
- `api-gateway`
  - exponer la nueva ruta

## 17. Criterios de aceptacion propuestos
1. Debe existir un endpoint backend para registrar entradas de inventario de medicamentos ya existentes.
2. El endpoint debe recibir `productId`, `quantity`, `reason`, `detail` opcional y `expirationDate`.
3. Cada entrada debe crear un lote nuevo asociado al medicamento seleccionado.
4. La entrada no debe reutilizar ni sobrescribir un lote anterior.
5. El stock operativo del producto debe actualizarse despues de crear el lote.
6. La operacion debe quedar registrada en `movimientos`.
7. El movimiento debe quedar asociado al lote creado.
8. El rol `FARMACEUTICO` debe poder consultar los movimientos de entrada.
9. Solo el rol `FARMACEUTICO` debe poder consumir este metodo de registro.
10. Si la fecha de vencimiento es invalida o vencida, el backend debe rechazar la solicitud.
11. Si el producto no existe o esta inactivo, el backend debe rechazar la solicitud.

## 18. Archivos candidatos a modificacion
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Controllers/MotionController.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/MotionService.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/BatchService.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Config/SecurityConfig.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Dto/`
- `inventory-service/src/test/java/...`
- `api-gateway/src/main/resources/application.yaml`

## 19. Riesgos o validaciones previas
- Definir si el codigo del nuevo lote sera generado automaticamente por backend o enviado desde frontend.
- Confirmar si la fecha de vencimiento puede ser igual a la fecha actual o debe ser estrictamente futura.
- Confirmar si el catalogo de motivos sera fijo en backend o parametrizable a futuro.
- Validar si el rol `ADMIN` podra consultar el historico resultante, aunque no pueda registrar entradas por este metodo.

## 20. Estado de este documento
Este documento justifica la necesidad de implementar una HU para registrar entradas de inventario por lote para el rol `FARMACEUTICO`, alineada con el modelo actual de productos, lotes y movimientos existente en el backend.
