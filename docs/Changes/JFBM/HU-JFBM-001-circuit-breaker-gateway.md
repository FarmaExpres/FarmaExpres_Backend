# HU-JFBM-001 - Proteccion ante fallos en el Gateway

## 1. Informacion general
- HU: `HU-JFBM-001`
- Nombre: Proteccion ante fallos en el Gateway
- Componente principal: `api-gateway`
- Componentes relacionados: `auth-service`, `inventory-service`, `alert-service`
- Estado: Implementado
- Rama de trabajo sugerida: `HU-JFBM-001-dev`

## 2. Objetivo
Evitar que el sistema se caiga completo cuando un servicio interno responde lento o falla.

## 3. Por que se necesita
Si un servicio interno se queda lento, el gateway tambien se puede bloquear y el sistema deja de responder. Queremos que el gateway responda rapido con un mensaje claro y siga funcionando para el resto de usuarios.

## 4. Que se va a hacer
- Detectar fallos repetidos y cortar el paso temporalmente.
- Devolver un mensaje simple cuando un servicio no este disponible.
- Mantener la aplicacion estable para el frontend.

## 5. Ejemplo sencillo del flujo

```text
Antes:
Cliente -> Gateway -> Servicio lento -> todo se queda esperando

Despues:
Cliente -> Gateway -> Servicio lento -> respuesta clara y rapida
```

## 6. Cambios principales (resumidos)

### 6.1 Dependencia agregada
Se agrega una libreria para manejar fallos de forma controlada:

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
</dependency>
```

### 6.2 Respuesta cuando un servicio falla
Ejemplo de respuesta simple cuando un servicio no esta disponible:

```json
{
  "error": "service unavailable",
  "service": "auth-service",
  "path": "/api/auth/login",
  "requestId": "..."
}
```

### 6.3 Rutas de respuesta rapida
Se crean respuestas rapidas para:
- `/fallback/auth`
- `/fallback/inventory`
- `/fallback/alerts`

## 7. Criterios de aceptacion
1. Si un servicio interno falla, el gateway responde con un mensaje claro.

Ejemplo:

```bash
curl -i http://localhost:8080/fallback/auth
```

Respuesta esperada:

```json
{
  "error": "auth-service unavailable",
  "service": "auth-service",
  "path": "/fallback/auth",
  "requestId": "..."
}
```

2. El gateway sigue respondiendo aunque un servicio interno este caido.

Ejemplo:

```bash
curl -i http://localhost:8080/status
```

Respuesta esperada:

```json
{
  "status": "UP",
  "service": "api-gateway",
  "timestamp": "..."
}
```

3. El frontend recibe una respuesta rapida en vez de quedarse esperando.

Ejemplo:

```bash
curl -i http://localhost:8080/api/alerts
```

Respuesta esperada cuando el servicio no responde:

```json
{
  "error": "alert-service unavailable",
  "service": "alert-service",
  "path": "/api/alerts",
  "requestId": "..."
}
```

## 8. Resultado esperado
El gateway sigue funcionando aun cuando algun servicio interno falle, y el usuario recibe una respuesta clara sin largas esperas.

Ejemplo:

```bash
curl -i http://localhost:8080/api/products
```

Respuesta esperada cuando el servicio esta caido:

```json
{
  "error": "inventory-service unavailable",
  "service": "inventory-service",
  "path": "/api/products",
  "requestId": "..."
}
```