# HU-JVL-003 - Desacoplamiento de alert-service respecto al schema de inventario

## 1. Informacion general
- HU: `HU-JVL-003`
- Nombre: Desacoplar `alert-service` del schema `inventory` dentro de la base de datos unificada
- Componente principal: `alert-service`
- Componentes relacionados: `inventory-service`, `database`, `api-gateway`, `docker-compose`, archivos `.env`
- Estado: Propuesto
- Rama de trabajo sugerida: `HU-JVL-003-dev`

## 2. Objetivo de la HU
Modificar la arquitectura de `alert-service` para que deje de consultar directamente el schema `inventory` de la base de datos unificada y consuma informacion mediante endpoints expuestos por `inventory-service`.

La intencion es respetar la propiedad de datos de cada microservicio, reducir el acoplamiento al schema interno de inventario y fortalecer la seguridad del acceso a las alertas.

## 3. Justificacion funcional
`alert-service` entrega informacion importante para la operacion de la farmacia, como productos vencidos, productos por vencer, bajo stock y agotados.

Actualmente estas consultas dependen directamente de tablas del schema `inventory`. Aunque la base de datos fisica fue unificada como parte de la mejora de database, `inventory-service` sigue siendo el dueno funcional de ese schema.

Esto significa que `alert-service` no deberia leer tablas de `inventory` directamente, porque conoceria detalles internos del modelo de datos de otro microservicio. La separacion esperada no depende solamente de tener bases fisicas distintas, sino de respetar la propiedad de datos y los contratos entre servicios.

La mejora propuesta consiste en que `inventory-service`, como dueno del dominio de inventario, exponga consultas internas o endpoints controlados para alertas. `alert-service` los consumira y se encargara de construir las respuestas de alerta o reporte necesarias.

## 4. Problema que resuelve la HU
La situacion actual puede generar estos problemas:
- `alert-service` depende del schema interno `inventory`.
- cambios en tablas o columnas de inventario pueden romper alertas.
- se duplica logica de consulta fuera del servicio dueno del dominio.
- se debilita la separacion de responsabilidades.
- es mas dificil aplicar permisos de base de datos por microservicio.
- `alert-service` podria requerir permisos sobre tablas que no le pertenecen.
- algunas rutas pueden quedar expuestas si se llama directamente al puerto del servicio.
- las pruebas de alertas se vuelven fragiles al depender de consultas SQL directas.

## 5. Historia de usuario
Como equipo de desarrollo de `FarmaExpres`,  
queremos que `alert-service` consuma la informacion de inventario mediante endpoints de `inventory-service`,  
para eliminar el acceso directo al schema `inventory`, reducir el acoplamiento entre microservicios y mantener una arquitectura mas segura y mantenible.

## 6. Alcance funcional propuesto
Esta HU cubre:
- definir endpoints en `inventory-service` para datos necesarios por alertas.
- reemplazar repositorios SQL directos en `alert-service` por un cliente HTTP interno.
- proteger las rutas sensibles de `alert-service`.
- evitar exposicion externa directa del servicio en ambientes `qa` y `main`.
- ajustar pruebas unitarias para mockear el cliente HTTP.
- documentar el nuevo flujo de comunicacion.

Esta HU asume que la HU de database ya dejo una base de datos unificada con separacion logica por schemas. Por eso el objetivo no es separar bases fisicas, sino impedir que `alert-service` lea directamente el schema `inventory`.

## 7. Estado actual resumido
El estado actual puede representarse asi:

```text
Cliente
  |
  v
api-gateway
  |
  v
alert-service
  |
  v
Base de datos unificada
  |
  v
schema inventory
```

El problema principal es que `alert-service` conoce y consulta tablas del schema `inventory`, aunque ese schema pertenece funcionalmente al dominio de `inventory-service`.

## 8. Estado objetivo propuesto
El estado objetivo es:

```text
Cliente
  |
  v
api-gateway
  |
  v
alert-service
  |
  v
inventory-service
  |
  v
Base de datos unificada
  |
  v
schema inventory
```

`inventory-service` conserva la propiedad funcional del schema `inventory` y `alert-service` consume un contrato HTTP estable.

## 9. Responsabilidades esperadas

### 9.1 Inventory-service
Debe encargarse de:
- consultar productos, lotes y movimientos.
- aplicar reglas propias del dominio de inventario.
- exponer endpoints internos para alertas y reportes.
- mantener oculto su schema de base de datos.

### 9.2 Alert-service
Debe encargarse de:
- consumir datos desde `inventory-service`.
- construir colecciones de alertas.
- definir severidad, mensajes y agrupaciones.
- aplicar reglas de autorizacion para reportes.
- responder al frontend mediante el gateway.

### 9.3 Api-gateway
Debe encargarse de:
- enrutar solicitudes externas.
- validar JWT antes de permitir acceso.
- evitar que clientes externos llamen directamente a servicios internos.

## 10. Endpoints internos propuestos en inventory-service
Se proponen endpoints controlados para que `alert-service` no consulte directamente el schema `inventory`:

```http
GET /api/inventory/alerts/low-stock
GET /api/inventory/alerts/out-of-stock
GET /api/inventory/alerts/expired
GET /api/inventory/alerts/expiring-soon
GET /api/inventory/alerts/expiring-range?from=YYYY-MM-DD&to=YYYY-MM-DD
GET /api/inventory/reports/batches
```

Los nombres pueden ajustarse a las convenciones actuales del proyecto, pero el principio debe mantenerse: la consulta al modelo de inventario vive en `inventory-service`, no en `alert-service`.

## 11. Estrategia tecnica recomendada

### 11.1 Cliente HTTP en alert-service
Crear una capa cliente para consultar `inventory-service`.

Ejemplo conceptual:

```text
alert-service
  src/clients/inventoryClient.js
```

Este cliente debe centralizar:
- URL base de `inventory-service`.
- headers necesarios.
- manejo de errores.
- timeouts.
- parseo de respuestas.

### 11.2 Eliminacion del acceso SQL directo
Los repositorios de `alert-service` que consultan tablas de inventario deben eliminarse o quedar fuera del flujo principal.

El objetivo es que `alert-service` no necesite credenciales con permisos sobre el schema `inventory`.

Si `alert-service` conserva variables como:

```env
DB_HOST
DB_NAME
DB_USER
DB_PASSWORD
```

estas no deben permitir lectura directa del schema `inventory`. Si su unica razon de existir es consultar datos de inventario, deben eliminarse y reemplazarse por configuracion del cliente HTTP hacia `inventory-service`.

### 11.3 Permisos esperados sobre database
Despues de la unificacion de base de datos, la regla de permisos debe ser:

```text
inventory-service -> puede acceder al schema inventory
alert-service     -> no debe acceder directamente al schema inventory
```

Si en una fase transitoria `alert-service` conserva acceso SQL, ese acceso debe considerarse deuda tecnica y quedar documentado para eliminacion.

### 11.4 Seguridad de rutas
Todas las rutas de alertas y reportes deben pasar por una politica clara:
- rutas publicas solo para salud o estado.
- rutas de negocio protegidas por JWT.
- reportes restringidos por rol cuando aplique.

Rutas como `/api/alerts/low-stock`, `/api/alerts/expired` y `/api/alerts/out-of-stock` no deben quedar accesibles directamente sin control.

### 11.5 Docker Compose
En ambientes `qa` y `main`, `alert-service` no deberia publicar puerto directo al host.

Debe quedar accesible para el gateway por red interna:

```yaml
expose:
  - "8083"
```

En desarrollo local puede publicarse temporalmente para depuracion, siempre que quede documentado.

## 12. Plan de implementacion propuesto

### Fase 1 - Preparacion
- Identificar todas las consultas SQL actuales de `alert-service`.
- Identificar que datos necesita cada endpoint de alertas.
- Definir contratos de respuesta desde `inventory-service`.
- Verificar que credenciales o usuarios de `alert-service` no requieran permisos sobre el schema `inventory`.

### Fase 2 - Endpoints en inventory-service
- Crear endpoints internos para bajo stock.
- Crear endpoints internos para agotados.
- Crear endpoints internos para vencidos.
- Crear endpoints internos para proximos a vencer.
- Agregar pruebas en `inventory-service`.

### Fase 3 - Cliente HTTP en alert-service
- Crear cliente HTTP hacia `inventory-service`.
- Reemplazar repositorios SQL directos.
- Centralizar manejo de errores y timeouts.
- Ajustar servicios de alertas para usar el cliente.
- Retirar configuracion de conexion directa al schema `inventory` cuando ya no sea necesaria.

### Fase 4 - Seguridad
- Aplicar middleware JWT a rutas de negocio.
- Mantener autorizacion por rol para reportes.
- Revisar rutas publicas permitidas.
- Ajustar exposicion de puertos en Docker.

### Fase 5 - Pruebas
- Corregir pruebas actuales de `alert-service`.
- Mockear el cliente HTTP de inventario.
- Probar respuestas exitosas.
- Probar errores cuando `inventory-service` no responde.
- Probar acceso sin token y con token invalido.
- Probar restricciones por rol.

### Fase 6 - Documentacion
- Documentar el nuevo flujo de comunicacion.
- Actualizar README si aplica.
- Crear o actualizar ADR sobre propiedad de datos entre microservicios.

## 13. Criterios de aceptacion propuestos
1. `alert-service` no debe consultar directamente el schema `inventory`.
2. `alert-service` debe obtener datos mediante `inventory-service`.
3. Debe existir un cliente HTTP interno o equivalente para consumir inventario.
4. Las variables de conexion directa al schema de inventario deben eliminarse de `alert-service` si ya no se usan.
5. Los endpoints de alertas deben seguir respondiendo con el contrato esperado.
6. Los errores de comunicacion con `inventory-service` deben manejarse con respuestas claras.
7. Las rutas de negocio de `alert-service` deben requerir autenticacion.
8. Las rutas de reportes deben validar roles permitidos.
9. En `qa` y `main`, `alert-service` no debe estar expuesto directamente al cliente externo.
10. Las pruebas de `alert-service` deben pasar sin depender de PostgreSQL real.
11. Las pruebas deben mockear la respuesta de `inventory-service`.
12. El nuevo flujo debe quedar documentado.
13. El usuario tecnico de `alert-service`, si existe, no debe tener permisos directos sobre tablas del schema `inventory`.

## 14. Fuera de alcance de esta HU
No hace parte de esta HU:
- cambiar el mecanismo JWT del gateway.
- redisenar todo el modelo de inventario.
- implementar mensajeria asincrona con colas o eventos.
- crear notificaciones por correo.
- crear historico persistente de alertas.
- migrar `alert-service` a Java o Spring Boot.

## 15. Riesgos y mitigaciones

| Riesgo | Impacto | Mitigacion |
|--------|---------|------------|
| Mayor latencia por llamada HTTP entre servicios | Medio | Usar timeouts, respuestas compactas y circuit breaker en gateway |
| `alert-service` queda dependiente de disponibilidad de `inventory-service` | Alto | Manejar errores claros y considerar cache futura |
| Contratos HTTP mal definidos | Medio | Documentar request/response y agregar pruebas |
| Duplicacion de rutas de alertas entre servicios | Medio | Dejar claro que inventario entrega datos y alertas construye respuesta final |
| Rutas siguen accesibles por puerto directo | Alto | Ajustar Docker y mantener gateway como entrada externa |
| Tests quedan acoplados a infraestructura real | Medio | Mockear cliente HTTP y probar servicios de forma aislada |
| Base de datos unificada se interpreta como permiso para compartir tablas | Alto | Documentar propiedad de schemas y restringir permisos |

## 16. Beneficios esperados
Implementar esta HU aportara:
- mejor separacion de responsabilidades.
- menor acoplamiento entre servicios.
- mayor seguridad sobre el schema `inventory`.
- pruebas mas faciles de mantener.
- contratos mas claros entre microservicios.
- arquitectura mas defendible para el proyecto.
- base preparada para futuras notificaciones o procesamiento asincrono.

## 17. Resultado esperado
Al completar esta HU, `alert-service` dejara de depender del schema interno de inventario y consumira datos mediante contratos HTTP ofrecidos por `inventory-service`.

El resultado esperado es:

```text
alert-service no conoce tablas del schema inventory
alert-service consume endpoints internos
inventory-service conserva propiedad del dominio de inventario
api-gateway sigue siendo la entrada externa
```

Con esto, `alert-service` pasara de ser un lector directo del schema `inventory` a un microservicio mejor delimitado dentro de la arquitectura de `FarmaExpres`.
