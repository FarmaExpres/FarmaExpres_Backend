# Cambios de la Rama `HU-ACFE-01-dev`

Fecha de generacion: 2026-04-01  
Rama analizada: `HU-ACFE-01-dev`  
Base de comparacion: `develop` (`08be781`)

## 1. Resumen General

Esta rama contiene 2 commits sobre `develop` y concentra ajustes de:
- trazabilidad de movimientos para frontend,
- estandarizacion internacional de fecha/hora en backend.

Metricas del diff (`develop..HU-ACFE-01-dev`):
- Archivos cambiados: 19
- Inserciones: 400
- Eliminaciones: 55

## 2. Commits Incluidos en la Rama

| Commit | Fecha | Autor | Mensaje |
|---|---|---|---|
| `8b03029` | 2026-04-01 | Nicolas Tello Mendez | `feat(timezone): standardize backend timestamps to UTC with explicit zone` |
| `716470b` | 2026-03-31 | Nicolas Tello Mendez | `feat(movements): add actor traceability fields and status support` |

## 3. Cambios Funcionales y Tecnicos

### 3.1 `inventory-service` - Trazabilidad de movimientos

Se amplió el modelo de `Motion` y su respuesta para soportar trazabilidad completa por registro:
- Campos agregados/normalizados:
  - `reason`
  - `userId`
  - `userName`
  - `userEmail`
  - `userRole`
  - `status` (`NORMAL` por defecto)
  - `markedByUserId`
  - `markedByUserName`
  - `markedAt`
  - `observation`
- Se introdujo `MotionStatus` (`NORMAL`, `MARKED`).
- Se mantuvo compatibilidad de lectura para frontend:
  - `GET /api/movements`
  - `GET /api/motions`
  - `GET /api/Motion`
- Se ajusto seguridad para lectura de movimientos por roles requeridos.
- Se ajusto el flujo de negocio para persistir actor del movimiento desde claims JWT.

### 3.2 `auth-service` - Claims JWT para trazabilidad

Se fortalecio la emision de token para soportar trazabilidad cross-service:
- Inclusión de `userId` en claims del JWT.
- Ajustes en `AuthService` para emitir token con datos necesarios del actor.

## 4. Estandarizacion de Fecha/Hora (UTC + Zona Explicita)

### 4.1 Modelo temporal backend

Se migro de `LocalDateTime` a `Instant` en componentes criticos:
- `inventory-service`:
  - `Motion.dateTime`
  - `Motion.markedAt`
  - `MotionResponse.dateTime`
  - `MotionResponse.markedAt`
  - `ApiErrorResponse.timestamp`
  - `GlobalExceptionHandler` (timestamps de error)
- `auth-service`:
  - `Binnacle.dateTime`
  - `ApiErrorResponse.timestamp`
  - `GlobalExceptionHandler` (timestamps de error)

Impacto:
- Respuestas JSON en ISO-8601 con zona explicita (ejemplo: `2026-04-01T04:57:43.780294Z`).
- Frontend puede convertir correctamente a zona local del navegador/OS.

### 4.2 Configuracion de servicios

Se definio timezone tecnico en UTC:
- `spring.jackson.time-zone: UTC`
- `hibernate.jdbc.time_zone: UTC`

Aplicado en:
- `auth-service/src/main/resources/application.yaml`
- `inventory-service/src/main/resources/application.yaml`

### 4.3 Base de datos

`database/init.sql` actualizado para persistir tiempos zonificados:
- `binnacle.date_time`: `TIMESTAMPTZ`
- `motion.date_time`: `TIMESTAMPTZ`
- `motion.marked_at`: `TIMESTAMPTZ`

## 5. Contratos API Relevantes

### 5.1 Login (auth)
Endpoint:
- `POST /api/auth/login`

Respuesta compatible con frontend (incluye identidad):
- `token`
- `type`
- `email`
- `role`
- `name`

### 5.2 Movimientos (inventory)
Endpoint de lectura:
- `GET /api/movements`

Campos relevantes en respuesta por movimiento:
- `id`, `dateTime`, `type`, `amount`
- `productId`, `productName`, `reason`
- `userId`, `userName`, `userEmail`, `userRole`
- `status`, `markedByUserId`, `markedByUserName`, `markedAt`, `observation`

## 6. Validaciones Ejecutadas

- Build/compilacion validada en:
  - `auth-service`
  - `inventory-service`
  - `api-gateway` (solo validacion de build; sin cambios finales en este ciclo)
- Validacion funcional de endpoints:
  - `POST /api/auth/login` (OK en auth-service)
  - `GET /api/movements` (OK, timestamps con `Z`)
  - `GET /api/binnacle` (OK, timestamps con `Z`)

## 7. Alcance Final de la Rama

Se confirmo que los cambios efectivos quedaron en:
- `auth-service`
- `inventory-service`
- `database/init.sql`

`api-gateway` no conserva cambios finales en esta rama por decision de alcance del equipo.

## 8. Listado de Archivos Modificados (`develop..HU-ACFE-01-dev`)

Formato: `Estado<TAB>Ruta` (`A`=Added, `M`=Modified)
```text
M	auth-service/src/main/java/co/edu/corhuila/auth_service/DTO/ApiErrorResponse.java
M	auth-service/src/main/java/co/edu/corhuila/auth_service/Entity/Binnacle.java
M	auth-service/src/main/java/co/edu/corhuila/auth_service/Service/AuthService.java
M	auth-service/src/main/java/co/edu/corhuila/auth_service/Service/JwtService.java
M	auth-service/src/main/java/co/edu/corhuila/auth_service/exception/GlobalExceptionHandler.java
M	auth-service/src/main/resources/application.yaml
M	auth-service/src/test/java/co/edu/corhuila/auth_service/Service/JwtFilterTest.java
M	database/init.sql
M	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Config/SecurityConfig.java
M	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Controllers/MotionController.java
M	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Dto/ApiErrorResponse.java
M	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Dto/MotionResponse.java
M	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Entity/Motion.java
A	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Entity/MotionStatus.java
M	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/JwtFilter.java
M	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/ProductService.java
M	inventory-service/src/main/java/co/edu/corhuila/inventory_service/exception/GlobalExceptionHandler.java
M	inventory-service/src/main/resources/application.yaml
M	inventory-service/src/test/java/co/edu/corhuila/inventory_service/Service/JwtFilterTest.java
```
