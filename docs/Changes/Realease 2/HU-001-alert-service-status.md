# HU-001 - Creacion del microservicio `alert-service` y endpoint de estado

## Informacion general

- **HU:** HU-001
- **Nombre:** Creacion base del microservicio `alert-service`
- **Microservicio:** `alert-service`
- **Prioridad:** Alta
- **Rama sugerida:** `HU-001-alert-service-status`
- **Estado:** Pendiente

## Observacion previa

Se registra una definicion tecnica que requiere aclaracion antes de construir:

- Se solicita **framework Node.js** y **lenguaje Java**.
- Esta combinacion no es consistente, porque **Node.js** trabaja normalmente con **JavaScript** o **TypeScript**, mientras que **Java** requiere otro stack como **Spring Boot**, **Micronaut** o similar.

Para esta HU se deja documentado el requerimiento tal como fue indicado, pero la implementacion debe confirmar uno de estos caminos:

1. `Node.js + JavaScript`
2. `Node.js + TypeScript`
3. `Java + Spring Boot`

## Historia de usuario

Como equipo de FarmaExpres,  
quiero crear el microservicio `alert-service` con una estructura base organizada,  
para disponer de un servicio inicial de alertas que permita validar su disponibilidad mediante un endpoint de estado.

## Objetivo

Construir la base del microservicio `alert-service`, dejando definida su arquitectura inicial por capas y exponiendo el endpoint `GET /status` en el puerto `8083`, con una respuesta JSON que confirme que el servicio se encuentra activo.

## Alcance funcional de esta HU

Esta HU cubre unicamente:

- Creacion del microservicio `alert-service`.
- Definicion de estructura de carpetas por responsabilidades.
- Configuracion del puerto `8083`.
- Exposicion del endpoint `GET /status`.
- Respuesta JSON de salud del servicio.
- Configuracion inicial para futura conexion a PostgreSQL.
- Referencia de integracion con la base de datos del microservicio `Inventory-service`.

## Alcance no incluido en esta HU

Esta HU no cubre todavia:

- CRUD de alertas.
- Logica de negocio completa sobre inventario.
- Eventos, colas o mensajeria.
- Persistencia de alertas.
- Autenticacion y autorizacion.
- Dockerizacion.
- Pruebas automatizadas avanzadas.
- Integracion real con la base de datos compartida del `Inventory-service`.

## Endpoint requerido

### Endpoint de estado

- **Metodo:** `GET`
- **Ruta:** `/status`
- **Puerto:** `8083`
- **URL esperada:** `http://localhost:8083/status`

### Respuesta esperada

```json
{
  "status": "UP",
  "service": "FarmaExpres_Micro_Alert",
  "timestamp": "2026-03-31T14:54:17.592Z"
}
```

## Criterios de aceptacion

1. El microservicio `alert-service` debe iniciar correctamente en el puerto `8083`.
2. Debe existir el endpoint `GET /status`.
3. Al consumir `GET /status`, el sistema debe responder con codigo HTTP `200 OK`.
4. La respuesta debe estar en formato JSON.
5. La respuesta debe incluir los campos `status`, `service` y `timestamp`.
6. El campo `status` debe retornar el valor `UP`.
7. El campo `service` debe identificar el microservicio como `FarmaExpres_Micro_Alert`.
8. El campo `timestamp` debe generarse en formato de fecha y hora valido tipo ISO 8601.
9. El proyecto debe quedar organizado por capas para facilitar escalabilidad y mantenimiento.
10. La configuracion de conexion a PostgreSQL debe quedar preparada para uso futuro.

## Estructura base propuesta

```text
alert-service/
  src/
    config/
    controllers/
    models/
    routers/
    services/
    utils/
    middlewares/
    repositories/
    app/
  tests/
  docs/
  .env.example
  package.json
  README.md
```

## Responsabilidad esperada por carpeta

- `config/`: configuracion del entorno, puerto, base de datos y variables globales.
- `controllers/`: manejo de solicitudes HTTP y respuestas.
- `models/`: definicion de entidades o modelos de datos.
- `routers/`: declaracion de rutas y vinculacion con controladores.
- `services/`: logica de negocio.
- `utils/`: utilidades comunes, helpers y formateadores.
- `middlewares/`: manejo de errores, logs y validaciones transversales.
- `repositories/`: acceso a datos y consultas a PostgreSQL.
- `app/`: inicializacion de la aplicacion y composicion de modulos.
- `tests/`: pruebas unitarias e integracion.

## Reglas tecnicas iniciales

- El servicio debe seguir una arquitectura modular y mantenible.
- La configuracion debe manejar variables de entorno.
- La conexion a PostgreSQL no debe quedar acoplada a la logica del endpoint `/status`.
- El endpoint `/status` debe responder aun cuando la conexion a base de datos no este implementada por completo.

## Dependencia externa identificada

- La base de datos es **PostgreSQL**.
- La base de datos pertenece a otro microservicio llamado `Inventory-service`.
- Esta HU solo deja preparada la configuracion de acceso; la validacion de uso compartido de esa base debe definirse con el equipo.

## Riesgos o puntos por definir

1. Definir si el stack real sera `Node.js` o `Java`.
2. Confirmar si `alert-service` compartira base de datos con `Inventory-service` o si solo consumira informacion desde ese microservicio.
3. Definir convencion oficial de nombres para carpetas y servicio: `alert-service`, `AlertService` o `FarmaExpres_Micro_Alert`.
4. Confirmar si el endpoint `/status` debe validar solo disponibilidad del servicio o tambien estado de dependencias como base de datos.

## Definicion de terminado

La HU se considera terminada cuando:

- Exista el microservicio `alert-service` en el repositorio.
- El proyecto tenga la estructura base definida en esta HU.
- El servicio levante en el puerto `8083`.
- El endpoint `GET /status` responda correctamente con `200 OK`.
- Se documente la configuracion minima de ejecucion local.

## Evidencia esperada

- Captura o prueba de `GET http://localhost:8083/status`.
- Respuesta JSON con `status`, `service` y `timestamp`.
- Estructura de carpetas creada en el proyecto.

