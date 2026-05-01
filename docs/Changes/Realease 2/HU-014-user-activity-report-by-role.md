# HU-014 - Reporte de actividad de usuarios por rol (inventory-service)

## 1. Informacion general
- HU: `HU-014`
- Nombre: Reporte de actividad de usuarios con filtro por rol
- Microservicio: `inventory-service`
- Estado: Implementada en backend
- Rama de trabajo sugerida: `HU-014-dev`

## 2. Objetivo de la HU
Implementar en `inventory-service` un metodo que permita consultar un reporte de actividad de usuarios como el mostrado en la interfaz de referencia.

El reporte debe permitir:
- ver todos los usuarios con su actividad consolidada
- filtrar el reporte por rol
- mostrar metricas agregadas por usuario

La informacion visible en la tabla incluye:
- usuario
- rol
- movimientos
- entradas
- salidas
- actividad

## 3. Justificacion del microservicio seleccionado
Se propone implementar esta HU en `inventory-service` porque el reporte depende directamente de los movimientos de inventario.

Actualmente la entidad `Motion` ya almacena:
- `userId`
- `userName`
- `userEmail`
- `userRole`
- `type`
- `dateTime`

Esto permite construir el reporte directamente desde los movimientos registrados, sin necesidad de consultar a `auth-service` para cada fila del consolidado.

`auth-service` sigue siendo el dueño funcional de usuarios y roles, pero no es el mejor lugar para calcular estadisticas de entradas, salidas y cantidad total de movimientos.

## 4. Contexto actual del microservicio
Actualmente `inventory-service` ya dispone de:
- entidad `Motion`
- controlador `MotionController`
- servicio `MotionService`
- repositorio `MotionRepository`
- DTO `MotionResponse`

Ademas, la entidad `Motion` ya contiene los datos base necesarios para esta HU:
- identificacion del usuario que realizo el movimiento
- nombre del usuario
- rol del usuario
- tipo de movimiento
- fecha del movimiento

Con esto, la HU puede resolverse creando una consulta agregada por usuario y rol.

## 5. Necesidad funcional observada en la interfaz
La imagen muestra una tabla resumen donde cada fila representa un usuario y su actividad en inventario.

El backend debe entregar, por cada usuario:
- `userId`
- `userName`
- `userRole`
- `totalMovements`
- `totalEntrances`
- `totalExits`
- `activityLevel`

Interpretacion funcional sugerida:
- `totalMovements`: total de movimientos realizados por el usuario
- `totalEntrances`: cantidad de movimientos tipo `Entrance`
- `totalExits`: cantidad de movimientos tipo `Exit`
- `activityLevel`: clasificacion visual de actividad, por ejemplo `Alta`, `Media` o `Baja`

## 6. Alcance funcional propuesto
Se propone crear un metodo especializado para consultar un reporte consolidado de actividad de usuarios.

El metodo debe soportar:
- consulta de todos los usuarios con movimientos
- filtro opcional por rol

Comportamiento esperado:
- si no se envia `role`, se retornan todos los usuarios con actividad
- si se envia `role`, solo se retornan usuarios cuyo rol coincida
- el resultado debe devolverse agrupado por usuario
- los usuarios deben ordenarse de mayor a menor actividad

## 7. Endpoint implementado
### Consumo oficial por gateway
- Metodo: `GET`
- URL propuesta: `http://localhost:8080/api/movements/report/users-activity`

### Endpoint interno del microservicio
- Metodo: `GET`
- URL propuesta: `http://localhost:8082/api/movements/report/users-activity`

### Query params propuestos
- `role`: nombre del rol a filtrar

Ejemplos propuestos:
```http
GET /api/movements/report/users-activity
GET /api/movements/report/users-activity?role=Admin
```

Nota:
Se usa query params porque el requerimiento corresponde a un filtro sobre un listado consolidado.

## 8. DTO implementado
Nombre usado:
- `UserActivityReportResponse`

Campos sugeridos:
```json
{
  "userId": 5,
  "userName": "Jose Leonardo Vargas",
  "userRole": "Administrador",
  "totalMovements": 2,
  "totalEntrances": 0,
  "totalExits": 2,
  "activityLevel": "Baja"
}
```

Descripcion funcional de campos:
- `userId`: identificador del usuario
- `userName`: nombre visible del usuario
- `userRole`: rol visible del usuario
- `totalMovements`: total consolidado de movimientos
- `totalEntrances`: cantidad de movimientos de entrada
- `totalExits`: cantidad de movimientos de salida
- `activityLevel`: etiqueta calculada en backend para representar nivel de actividad

## 9. Contrato de respuesta esperado
La respuesta esperada del endpoint es una lista de usuarios resumidos:

```json
[
  {
    "userId": null,
    "userName": "Sistema",
    "userRole": "Automatico",
    "totalMovements": 11,
    "totalEntrances": 11,
    "totalExits": 0,
    "activityLevel": "Media"
  },
  {
    "userId": 5,
    "userName": "Jose Leonardo Vargas",
    "userRole": "Administrador",
    "totalMovements": 2,
    "totalEntrances": 0,
    "totalExits": 2,
    "activityLevel": "Baja"
  }
]
```

## 10. Logica de negocio implementada
El metodo actualmente:
- consultar los movimientos registrados en base de datos
- agruparlos por `userId`, `userName` y `userRole`
- contar el total de movimientos por usuario
- contar cuantas entradas tiene cada usuario
- contar cuantas salidas tiene cada usuario
- calcular un nivel de actividad
- permitir filtrar por rol de forma opcional

Comportamiento actual:
- si no existen movimientos, retornar lista vacia
- si el rol enviado no tiene resultados, retornar lista vacia
- incluir registros del sistema si existen movimientos sin usuario autenticado y ya fueron guardados como `Sistema` o `Automatico`
- ordenar el resultado de mayor a menor por `totalMovements`

## 11. Regla sugerida para `activityLevel`
Como primera aproximacion, puede definirse una clasificacion simple basada en `totalMovements`.

Ejemplo sugerido:
- `Alta`: 10 o mas movimientos
- `Media`: entre 4 y 9 movimientos
- `Baja`: entre 1 y 3 movimientos

Nota:
Esta regla puede ajustarse luego si el frontend o negocio necesita otros umbrales.

## 12. Implementacion tecnica realizada
- `MotionService`
  - se implemento `listUsersActivityReport(String role)`
  - se centralizo la regla de calculo de `activityLevel`
- `MotionController`
  - se expuso el endpoint `GET /api/movements/report/users-activity`
  - se habilito `role` como query param opcional
- `Dto`
  - se creo `UserActivityReportResponse`
- `Pruebas`
  - se agregaron pruebas unitarias para todos y filtro por rol

Nota tecnica:
La implementacion actual realiza la agregacion en memoria a partir de `findAllByOrderByDateTimeDesc()`. Mas adelante, si el volumen de movimientos crece, se puede optimizar con una consulta agregada en `MotionRepository`.

## 13. Criterios de aceptacion propuestos
1. Debe existir un endpoint en `inventory-service` para consultar el reporte de actividad de usuarios.
2. El endpoint debe permitir obtener todos los usuarios con actividad registrada.
3. El endpoint debe permitir filtrar por rol.
4. La respuesta debe incluir `userName`, `userRole`, `totalMovements`, `totalEntrances`, `totalExits` y `activityLevel`.
5. Los resultados deben entregarse agrupados por usuario.
6. La respuesta debe retornar lista vacia cuando no existan datos.
7. El reporte debe poder consumirse a traves de `api-gateway`.
8. La logica principal del reporte debe implementarse en `inventory-service`.

## 14. Archivos candidatos a modificacion
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Controllers/MotionController.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/MotionService.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Repository/MotionRepository.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Dto/`
- `inventory-service/src/test/java/...`
- `api-gateway/src/main/resources/application.yaml`

## 15. Riesgos o validaciones previas
- Confirmar si `movimientos` debe contar solo `Entrance` y `Exit` o tambien otros tipos como `Updated` y `Deleted`.
- Confirmar si `activityLevel` debe calcularse en backend o si solo se enviara `totalMovements` para que el frontend pinte la etiqueta.
- Validar si el filtro por rol sera exacto por nombre, por identificador o por catalogo controlado.
- Confirmar si deben mostrarse usuarios sin movimientos; con el enfoque actual solo saldran usuarios con actividad registrada.

## 16. Estado de este documento
Este documento refleja la implementacion actual de `HU-014` en backend para consultar el reporte de actividad de usuarios, incluyendo consulta general y filtro opcional por rol en `inventory-service`.
