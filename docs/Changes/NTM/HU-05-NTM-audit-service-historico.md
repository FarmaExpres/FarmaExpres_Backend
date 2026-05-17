# Historico de cambios HU-05-NTM - Audit Service

## Contexto

Este documento resume los cambios realizados en backend para habilitar el modulo de auditoria solicitado por frontend en la historia de usuario `HU-05-NTM-dev`.

El objetivo fue crear un microservicio nuevo de auditoria, integrarlo con los demas servicios existentes, conectarlo a base de datos con Liquibase, exponerlo por el gateway y dejarlo funcionando en Docker con una estructura similar al resto del backend.

## Resultado general

Se agrego el microservicio `audit-service` y quedo integrado con:

- `api-gateway`, para exponer los endpoints bajo `/api/audit`.
- `inventory-service`, para leer movimientos y sincronizar estados de auditoria.
- PostgreSQL, usando el schema `audit`.
- Liquibase, para crear tablas, indices, roles, permisos y rollbacks.
- Docker Compose, para levantar el servicio junto con sus migraciones.
- Archivos de ambiente `.env.dev`, `.env.qa` y `.env.main`.

Tambien se realizaron ajustes posteriores para fortalecer el modulo:

- Endpoint explicito de recalculo automatico.
- Estado `REVIEWED` en inventario.
- Seguridad con rol `AUDITOR`.
- Metricas enriquecidas.
- Pruebas unitarias del flujo principal.
- Creacion de observaciones para casos automaticos.

## Nuevo microservicio audit-service

Se creo la carpeta `audit-service` como un microservicio Spring Boot independiente, siguiendo una estructura parecida a los servicios existentes.

Componentes principales agregados:

- Aplicacion principal `AuditServiceApplication`.
- Controladores REST para auditoria y estado.
- DTOs de entrada y salida.
- Entidades JPA para casos, observaciones y resultados de reglas.
- Repositorios Spring Data JPA.
- Servicios de negocio.
- Cliente HTTP hacia `inventory-service`.
- Seguridad JWT.
- Dockerfile.
- Maven wrapper.
- Configuracion `application.yaml`.

El servicio corre internamente en el puerto `8084`.

## Endpoints principales de auditoria

Se implementaron los endpoints consumidos por frontend:

- `GET /api/audit/history`
- `GET /api/audit/inconsistencies`
- `GET /api/audit/observations`
- `GET /api/audit/metrics`
- `POST /api/audit/cases/manual`
- `PATCH /api/audit/cases/{id}/note`
- `PATCH /api/audit/cases/{id}/status`
- `DELETE /api/audit/cases/{id}/manual-flag`
- `POST /api/audit/recalculate`

Estos endpoints permiten:

- Listar historial auditado.
- Listar inconsistencias detectadas.
- Listar observaciones de auditoria.
- Calcular metricas del modulo.
- Crear marcas manuales.
- Editar notas.
- Cambiar estado del caso.
- Eliminar una marca manual.
- Ejecutar recalculo automatico de reglas.

## Modelo de datos de auditoria

Se agregaron tres entidades principales:

### AuditCase

Representa un caso de auditoria asociado a un movimiento de inventario.

Contiene informacion como:

- `movementId`
- medicamento
- cantidad
- tipo de movimiento
- usuario responsable del movimiento
- prioridad
- origen del caso
- estado
- razon de auditoria
- puntaje de riesgo
- metadatos de apertura, revision y cierre

Estados manejados:

- `OPEN`
- `IN_REVIEW`
- `REVIEWED`
- `CLOSED`

Origenes manejados:

- `AUTO`
- `MANUAL`

Prioridades manejadas:

- `LOW`
- `MEDIUM`
- `HIGH`

### AuditObservation

Representa una observacion o nota asociada a un caso.

Se usa para guardar:

- notas manuales del auditor
- razon automatica detectada por reglas
- prioridad de la observacion
- usuario creador
- fecha de creacion

### AuditRuleResult

Representa el resultado tecnico de una regla automatica ejecutada sobre un movimiento.

Permite dejar trazabilidad de:

- regla aplicada
- descripcion
- prioridad
- puntaje de riesgo
- fecha de ejecucion

## Liquibase para audit-service

Se agrego la carpeta `database/audit` con estructura propia de Liquibase.

Incluye:

- `changelog-master.yaml`
- scripts DDL para crear tablas.
- scripts DCL para usuarios, roles y permisos.
- rollbacks.

Tablas creadas:

- `audit.audit_case`
- `audit.audit_observation`
- `audit.audit_rule_result`

Tambien se agrego una restriccion para evitar duplicar casos abiertos del mismo movimiento:

```sql
CREATE UNIQUE INDEX uq_audit_case_open_movement
ON audit.audit_case(movement_id)
WHERE status IN ('OPEN', 'IN_REVIEW');
```

Esto asegura que no existan multiples casos abiertos para el mismo movimiento.

## Integracion con base de datos

Se actualizo `database/bootstrap.sql` para crear el schema:

```sql
CREATE SCHEMA IF NOT EXISTS audit;
```

Tambien se agregaron variables de conexion para auditoria en:

- `.env.dev`
- `.env.qa`
- `.env.main`

Variables agregadas:

- `AUDIT_DB_USERNAME`
- `AUDIT_DB_PASSWORD`
- `AUDIT_READ_DB_USERNAME`
- `AUDIT_READ_DB_PASSWORD`
- `AUDIT_PORT`

## Integracion con Docker

Se actualizo `docker-compose.yml` para incluir:

- `liquibase-audit`
- `audit-service`

El nuevo servicio quedo expuesto asi:

```yaml
${AUDIT_PORT:-8084}:8084
```

Tambien se agrego dependencia desde `api-gateway` hacia `audit-service`, para que el gateway espere a que auditoria este disponible.

## Integracion con api-gateway

Se agrego una nueva ruta en `api-gateway`:

```text
/api/audit
/api/audit/**
```

La ruta apunta a:

```text
http://audit-service:8084
```

Tambien se agrego:

- circuit breaker para `audit-service`.
- fallback `/fallback/audit`.
- soporte para metodos diferentes de GET en fallbacks, evitando errores `405` cuando el fallback se activa en POST, PATCH o DELETE.

## Integracion con inventory-service

Se agrego soporte para que auditoria pueda consultar y sincronizar movimientos.

Cambios principales:

- Endpoint para obtener un movimiento por id:

```text
GET /api/movements/{id}
```

- Endpoint para actualizar el estado de auditoria del movimiento:

```text
PATCH /api/movements/{id}/audit-status
```

Este endpoint permite sincronizar el estado resumido del movimiento en inventario.

## Estados de auditoria en inventario

Antes `MotionStatus` manejaba:

```text
NORMAL
MARKED
```

Se amplio a:

```text
NORMAL
MARKED
REVIEWED
```

La regla final quedo asi:

- Caso abierto o en revision: movimiento `MARKED`.
- Caso revisado o cerrado: movimiento `REVIEWED`.
- Marca manual eliminada: movimiento `NORMAL`.

Esto permite que el frontend muestre claramente:

- Normal
- Marcado
- Revisado

## Liquibase en inventory-service

Se agrego una migracion para actualizar el constraint del estado del movimiento:

Archivo:

```text
database/inventory/01_ddl/00_tables/005_add_reviewed_motion_status.sql
```

La migracion permite:

```text
NORMAL
MARKED
REVIEWED
```

Tambien se agrego rollback:

```text
database/inventory/05_rollbacks/01_ddl/00_tables/005_add_reviewed_motion_status.rollback.sql
```

En rollback, los movimientos `REVIEWED` vuelven a `MARKED` antes de restaurar el constraint anterior.

## Seguridad

Se configuro seguridad JWT en `audit-service`.

Los endpoints de auditoria quedan protegidos para usuarios con rol:

```text
AUDITOR
```

Tambien se protegio en `inventory-service` el endpoint:

```text
PATCH /api/movements/{id}/audit-status
```

Este endpoint tambien requiere rol `AUDITOR`.

## Recalculo automatico

Se agrego el endpoint:

```text
POST /api/audit/recalculate
```

Este endpoint ejecuta las reglas automaticas sobre los movimientos existentes.

El objetivo es evitar que una consulta como historial, inconsistencias o metricas cree datos nuevos de forma implicita.

Ahora el recalculo:

- consulta movimientos desde `inventory-service`.
- aplica reglas de auditoria.
- crea casos automaticos faltantes.
- evita duplicados abiertos.
- sincroniza movimientos marcados con `inventory-service`.
- devuelve resumen de procesamiento.

Respuesta esperada:

```json
{
  "processedMovements": 0,
  "createdAutomaticCases": 0,
  "synchronizedMovements": 0
}
```

## Reglas automaticas

Se implemento un motor de reglas para detectar inconsistencias de forma mas contextual.

El enfoque evita reglas globales simples como:

```text
cantidad > 40
```

En su lugar, se consideran patrones del medicamento y del historial disponible.

Reglas contempladas:

- cantidad fuera del patron historico del medicamento.
- salida superior al comportamiento historico.
- actividad inusual.
- movimientos fuera de horario.
- escenarios de riesgo por tipo de movimiento.

## Observaciones para casos automaticos

Se realizo un ajuste importante en la logica de creacion de casos automaticos.

Cuando el sistema detecta una inconsistencia automatica, ademas de crear el `AuditCase`, tambien crea una `AuditObservation` asociada al mismo caso.

La observacion queda con:

- `movementId`: id del movimiento auditado.
- `note`: razon automatica detectada.
- `priority`: la misma prioridad del caso automatico.
- `createdByUserName`: `Sistema`.

Ejemplo de nota:

```text
Cantidad superior al patron historico del medicamento
```

Motivo del cambio:

- La tabla del frontend debe mostrar solo el origen del caso: `Automatico` o `Manual`.
- La razon tecnica no debe mostrarse como texto largo en la tabla.
- El detalle debe quedar como nota u observacion del caso.

Tambien se agrego proteccion para casos automaticos existentes que no tengan observacion.

Si existe un `AuditCase` automatico sin observacion registrada, el servicio crea una observacion con la razon del caso.

Con esto se garantiza que los casos automaticos no queden sin detalle explicativo.

## Metricas de auditoria

Se enriquecio el endpoint:

```text
GET /api/audit/metrics
```

Resumen incluido:

- total de movimientos
- total marcados
- observaciones
- usuarios
- casos automaticos
- casos manuales
- casos abiertos
- casos en revision
- casos revisados
- casos cerrados
- casos de prioridad alta
- casos de prioridad media
- casos de prioridad baja

Tambien se agregaron agrupaciones:

- actividad por usuario
- tendencia mensual de movimientos
- medicamentos con mas movimientos
- casos por usuario
- casos por medicamento
- casos por prioridad
- casos por origen
- tendencia mensual de casos
- medicamentos con mayor riesgo

## Flujo de marca manual

Se implemento el flujo para que un auditor cree una marca manual sobre un movimiento.

Reglas:

- La nota es obligatoria.
- Se crea un `AuditCase` con origen `MANUAL`.
- Se crea o actualiza la observacion asociada.
- Se sincroniza el movimiento en inventario como `MARKED`.
- Si ya existe un caso abierto para el movimiento, se reutiliza y no se duplica.

## Flujo de cierre y revision

Cuando cambia el estado de un caso:

- `OPEN` o `IN_REVIEW` sincroniza inventario como `MARKED`.
- `REVIEWED` o `CLOSED` sincroniza inventario como `REVIEWED`.

Cuando un caso se cierra, se guardan metadatos de cierre.

Esto permite conservar visibilidad de que el movimiento ya fue auditado, en lugar de volverlo inmediatamente a `NORMAL`.

## Eliminacion de marca manual

Se agrego soporte para eliminar una marca manual:

```text
DELETE /api/audit/cases/{id}/manual-flag
```

Cuando se elimina:

- se borra el caso manual.
- se sincroniza el movimiento en inventario como `NORMAL`.

## Contrato esperado con frontend

El frontend puede consumir los endpoints estables de auditoria:

```text
GET /api/audit/history
GET /api/audit/inconsistencies
GET /api/audit/observations
GET /api/audit/metrics
POST /api/audit/cases/manual
PATCH /api/audit/cases/{id}/note
PATCH /api/audit/cases/{id}/status
DELETE /api/audit/cases/{id}/manual-flag
POST /api/audit/recalculate
```

Campos relevantes para frontend:

- `movementId`
- `auditCaseId`
- `id`
- `auditStatus`
- `auditSource`
- `auditPriority`
- `auditReason`
- `auditNote`
- `riskScore`
- `priority`
- `source`
- `status`
- `description`
- `createdBy`
- `createdAt`

Para inconsistencias:

- La tabla debe mostrar el origen: `Automatico` o `Manual`.
- La razon automatica debe verse en observaciones/notas.

## Ambientes

Se actualizaron los tres archivos de ambiente:

- `.env.dev`
- `.env.qa`
- `.env.main`

Cada ambiente tiene puertos separados:

| Ambiente | Gateway | Auth | Inventory | Alert | Audit | PostgreSQL |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Dev | 8080 | 8081 | 8082 | 8083 | 8084 | 5433 |
| QA | 9080 | 9081 | 9082 | 9083 | 9084 | 6433 |
| Main | 10080 | 10081 | 10082 | 10083 | 10084 | 7433 |

Se valido que los tres ambientes renderizan correctamente con Docker Compose:

```powershell
docker compose --env-file .env.dev config --quiet
docker compose --env-file .env.qa config --quiet
docker compose --env-file .env.main config --quiet
```

Tambien se valido el ambiente `dev` en ejecucion con Docker.

## Validaciones realizadas

Se realizaron validaciones de build, pruebas y ejecucion.

### Pruebas de audit-service

Comando:

```powershell
.\mvnw.cmd test
```

Resultado:

```text
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Pruebas cubiertas:

- deteccion de cantidad fuera de patron historico.
- no marcar cantidades normales con historial pequeno.
- rechazar caso manual sin nota.
- reutilizar caso abierto existente.
- sincronizar `MARKED`.
- sincronizar `REVIEWED` al cerrar.
- recalcular y crear casos automaticos faltantes.

### Build de inventory-service

Comando:

```powershell
.\mvnw.cmd test -DskipTests
```

Resultado:

```text
BUILD SUCCESS
```

### Build de api-gateway

Comando:

```powershell
.\mvnw.cmd test -DskipTests
```

Resultado:

```text
BUILD SUCCESS
```

### Docker

Comando usado:

```powershell
docker compose up -d --build inventory-service audit-service api-gateway
```

Servicios verificados como `healthy`:

- `postgres`
- `auth-service`
- `inventory-service`
- `alert-service`
- `audit-service`
- `api-gateway`

### Prueba funcional por gateway

Se valido login con usuario auditor y consumo de auditoria por gateway.

Endpoints probados:

```text
POST /api/auth/login
GET /api/audit/metrics
POST /api/audit/recalculate
```

Resultado del recalculo en ambiente dev:

```json
{
  "processedMovements": 24,
  "createdAutomaticCases": 0,
  "synchronizedMovements": 0
}
```

## Notas operativas

- `audit-service` queda disponible directo en `http://localhost:8084` en ambiente dev.
- El acceso recomendado para frontend es mediante gateway: `http://localhost:8080/api/audit`.
- `alert-service` puede aparecer como puerto interno en Docker; si no esta publicado al host, debe consumirse por gateway.
- Si un circuit breaker queda abierto tras reiniciar servicios, reiniciar `api-gateway` limpia el estado y permite revalidar rutas.

## Archivos principales modificados o agregados

### Agregados

- `audit-service/`
- `database/audit/`
- `database/inventory/01_ddl/00_tables/005_add_reviewed_motion_status.sql`
- `database/inventory/05_rollbacks/01_ddl/00_tables/005_add_reviewed_motion_status.rollback.sql`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Dto/InventoryAuditStatusRequest.java`

### Modificados

- `.env.dev`
- `.env.qa`
- `.env.main`
- `docker-compose.yml`
- `database/bootstrap.sql`
- `database/inventory/01_ddl/00_tables/changelog.yaml`
- `api-gateway/src/main/resources/application.yaml`
- `api-gateway/src/main/java/co/edu/corhuila/api_gateway/FallbackController.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Config/SecurityConfig.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Controllers/MotionController.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Entity/MotionStatus.java`
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/MotionService.java`

## Estado final

El backend queda preparado para que el frontend use el modulo de auditoria desde el gateway.

El modulo permite:

- detectar inconsistencias automaticas.
- crear casos manuales.
- guardar observaciones.
- consultar historial.
- consultar inconsistencias.
- consultar metricas.
- recalcular reglas bajo demanda.
- sincronizar estado resumido en inventario.
- distinguir movimientos normales, marcados y revisados.

Con esto, la historia `HU-05-NTM-dev` queda documentada con los cambios tecnicos necesarios para el funcionamiento del modulo de auditoria.
