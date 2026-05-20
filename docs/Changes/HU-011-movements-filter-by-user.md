# HU-011 - Filtro de movimientos por usuario (inventory-service)

## 1. Informacion general
- HU: `HU-011`
- Nombre: Consulta de movimientos con filtro por usuario
- Microservicio: `inventory-service`
- Estado: Planeada
- Rama de trabajo sugerida: `HU-011-dev`

## 2. Objetivo de la HU
Implementar en `inventory-service` un metodo que permita consultar movimientos de inventario filtrando unicamente por usuario, tal como se observa en la interfaz de referencia.

Ademas, la respuesta debe exponer los campos necesarios para pintar la tabla de movimientos del frontend, incluyendo la informacion visible en la columna de usuario y las demas columnas relacionadas.

## 3. Contexto actual del microservicio
Actualmente `inventory-service` ya dispone de:
- entidad `Motion`
- controlador `MotionController`
- servicio `MotionService`
- repositorio `MotionRepository`
- DTO `MotionResponse`
- endpoint general:
  - `GET /api/movements`
- endpoints por categoria:
  - `GET /api/movements/entrance`
  - `GET /api/movements/exit`
  - `GET /api/movements/updated`

La entidad `Motion` y el DTO `MotionResponse` ya contienen informacion relevante para esta HU:
- `dateTime`
- `type`
- `amount`
- `productId`
- `productName`
- `reason`
- `userId`
- `userName`
- `userEmail`
- `userRole`
- `status`
- `observation`
- `adjustmentSummary`
- `adjustmentDetail`

Esto significa que la HU puede resolverse ampliando la consulta actual y definiendo un endpoint filtrado por usuario, sin rehacer la estructura principal de movimientos.

## 4. Necesidad funcional observada en la interfaz
La imagen muestra una casilla de filtro de usuario que permite:
- seleccionar un usuario especifico
- seleccionar la opcion `Todos`

La tabla visible necesita, como minimo, mostrar:
- fecha
- hora
- tipo
- medicamento
- cantidad
- motivo
- usuario
- rol
- estado

Para el caso de movimientos de ajuste, tambien puede requerirse:
- `adjustmentSummary`
- `adjustmentDetail`
- `observation`

## 5. Alcance funcional propuesto
Se propone crear un metodo especializado para consultar movimientos filtrados solo por usuario, con soporte para el siguiente criterio opcional:
- `userId`

Comportamiento esperado:
- si no se envia `userId`, se retornan todos los movimientos
- si se envia `userId`, solo se retornan movimientos del usuario indicado
- la respuesta debe seguir devolviendo la informacion suficiente para construir la tabla completa del frontend

## 6. Propuesta de endpoint
### Consumo oficial por gateway
- Metodo: `GET`
- URL propuesta: `http://localhost:8080/api/movements/filter-by-user`

### Endpoint interno del microservicio
- Metodo: `GET`
- URL propuesta: `http://localhost:8082/api/movements/filter-by-user`

### Query params propuestos
- `userId`: identificador del usuario

Ejemplo propuesto:
```http
GET /api/movements/filter-by-user?userId=5
```

Nota:
Se propone usar query params porque el comportamiento de la interfaz corresponde a un filtro sobre un listado existente.

## 7. Contrato de respuesta esperado
Se recomienda reutilizar `MotionResponse`, ya que actualmente ya incluye casi toda la informacion requerida por la tabla.

Ejemplo de respuesta:
```json
[
  {
    "id": 15,
    "dateTime": "2026-04-03T13:43:00Z",
    "type": "Exit",
    "amount": -20,
    "productId": 8,
    "productName": "Diclofenaco 50 mg",
    "reason": "Eliminacion logica del producto",
    "userId": 5,
    "userName": "Marlon Romero",
    "userEmail": "marlon@farmaexpres.com",
    "userRole": "Farmaceutico",
    "status": "NORMAL",
    "markedByUserId": null,
    "markedByUserName": null,
    "markedAt": null,
    "observation": null,
    "adjustmentSummary": null,
    "adjustmentDetail": null
  }
]
```

## 8. Campos que el frontend podra derivar de la respuesta
Con `MotionResponse`, el frontend puede construir la tabla asi:
- `fecha`: derivada de `dateTime`
- `hora`: derivada de `dateTime`
- `tipo`: derivada de `type`
- `medicamento`: derivada de `productName`
- `cantidad`: derivada de `amount`
- `motivo`: derivada de `reason`
- `usuario`: derivada de `userName`
- `rol`: derivada de `userRole`
- `estado`: derivada de `status`

Y para ajustes:
- resumen del ajuste: `adjustmentSummary`
- detalle del ajuste: `adjustmentDetail`
- observacion adicional: `observation`

## 9. Logica de negocio esperada
El metodo debe:
- consultar movimientos desde base de datos aplicando el filtro opcional de usuario
- permitir filtrar por `userId`
- retornar resultados ordenados de mas reciente a mas antiguo
- mapear el resultado a `MotionResponse`

Comportamiento sugerido:
- si no hay resultados, retornar lista vacia
- si el `userId` no tiene movimientos, retornar lista vacia

## 10. Propuesta tecnica de implementacion
- `MotionController`
  - agregar un endpoint GET para consulta filtrada por usuario
- `MotionService`
  - agregar un metodo que reciba `userId` como filtro opcional
- `MotionRepository`
  - crear o reutilizar una consulta por `userId`
- `MotionResponse`
  - reutilizar el DTO actual, salvo que luego se requiera un campo adicional solo para UI

Opcion tecnica viable para repositorio:
- usar un metodo derivado como `findByUserId(...)`
- o usar una consulta con `userId` opcional si se quiere soportar `Todos` desde el mismo endpoint

Para esta HU no es necesario construir una consulta compleja, porque el alcance queda enfocado solo en usuario.

## 11. Consideraciones sobre el filtro de usuario
La interfaz muestra un selector de usuario con opciones como:
- `Todos`
- nombre del usuario
- rol del usuario entre parentesis

Por esto, en backend se recomienda:
- filtrar oficialmente por `userId`
- seguir retornando `userName` y `userRole` para que el frontend arme la etiqueta visible

Etiqueta visual esperada en frontend:
- `userName (userRole)`

Ejemplo:
- `Marlon Romero (Farmaceutico)`

## 12. Criterios de aceptacion propuestos
1. Debe existir un endpoint en `inventory-service` para consultar movimientos con filtros.
2. El endpoint debe permitir filtrar por usuario.
3. Si no se envia `userId`, el metodo debe permitir obtener todos los movimientos.
4. La respuesta debe incluir los datos necesarios para construir la tabla de movimientos.
5. El endpoint debe retornar una lista vacia cuando no existan resultados.
6. Los resultados deben entregarse ordenados del mas reciente al mas antiguo.
7. El filtro de usuario debe permitir mostrar en frontend nombre y rol del usuario.
8. La implementacion debe realizarse en `inventory-service`.

## 13. Archivos candidatos a modificacion
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Controllers/MotionController.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/MotionService.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Repository/MotionRepository.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Dto/MotionResponse.java`
- `inventory-service/src/test/java/...`
- `api-gateway/src/main/resources/application.yaml`

## 14. Riesgos o validaciones previas
- Confirmar si el frontend enviara `userId` o un valor textual del usuario.
- Validar si el filtro por usuario debe excluir movimientos del sistema como `SYSTEM_INIT`.
- Validar si tambien se necesitara un endpoint adicional para poblar el selector de usuarios.

## 15. Estado de este documento
Este documento deja definida la propuesta funcional y tecnica inicial de `HU-011` para posteriormente implementar el metodo de consulta de movimientos filtrados unicamente por usuario en `inventory-service`.
