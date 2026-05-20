# # ADR-006: Comunicación y Manejo de Errores entre Microservicios

**Fecha:** 2026-03-15  
**Estado:** Propuesto  
**Autor:** Rol Frontend  
**Proyecto:** FarmaExpres

---

## 📋 Tabla de Contenidos

1. [Identificación de Patrones](#1-identificación-de-patrones)
2. [Análisis del Proyecto y ADRs Potenciales](#2-análisis-del-proyecto-y-adrs-potenciales)
3. [Identificación de Antipatrones](#3-identificación-de-antipatrones)
4. [Segmentos de Código con Malas Prácticas](#4-segmentos-de-código-con-malas-prácticas)
5. [Diseño e Implementación del ADR](#5-diseño-e-implementación-del-adr)
6. [Código a Modificar](#6-código-a-modificar)
7. [Impacto sobre el Sistema](#7-impacto-sobre-el-sistema)

---

## 1. Identificación de Patrones

### 1.1 Patrones Actualmente Implementados

| Patrón | Ubicación | Descripción |
|--------|-----------|-------------|
| **Microservicios desacoplados** | `auth-service`, `inventory-service` | Separación de dominios funcionales |
| **REST API** | Controladores y endpoints | Comunicación vía HTTP |
| **DTO Pattern** | Paquetes `DTO` | Transferencia de datos entre capas |

### 1.2 Patrones Ausentes Críticos

| Patrón Faltante | Justificación |
|-----------------|---------------|
| **Standard Error Contract** | Los servicios deben responder errores de manera homogénea |
| **Global Exception Handling** | Se necesita centralizar errores técnicos y funcionales |
| **Traceable Error Handling** | Los errores deben ser comprensibles para frontend y equipo técnico |

---

## 2. Análisis del Proyecto y ADRs Potenciales

### 2.1 Estado Actual de la Arquitectura

```text
┌──────────────────────────────────────────────────────────────┐
│          COMUNICACIÓN Y ERRORES EN ESTADO ACTUAL            │
├──────────────────────────────────────────────────────────────┤
│ auth-service                                                 │
│  └── Respuestas no completamente estandarizadas             │
│                                                              │
│ inventory-service                                            │
│  └── Manejo de errores local y poco homogéneo               │
│                                                              │
│ Resultado: cada servicio puede responder distinto            │
│ ante errores funcionales o técnicos                         │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. Identificación de Antipatrones

### 3.1 Antipatrón: **Errores Inconsistentes entre Servicios**

**Problema:** cada microservicio puede responder con estructuras distintas, dificultando el consumo desde frontend.

### 3.2 Antipatrón: **Mensajes Técnicos Poco Controlados**

**Problema:** errores internos pueden llegar al cliente sin un contrato uniforme.

---

## 4. Segmentos de Código con Malas Prácticas

### 4.1 Ausencia de contrato uniforme de error

**Archivos objetivo:**
- `auth-service/src/main/java/.../exception/`
- `inventory-service/src/main/java/.../exception/`

### 4.2 Manejo local no centralizado

**Problema:** si cada controlador maneja errores por separado, aumenta duplicación e inconsistencia.

---

## 5. Diseño e Implementación del ADR

### 5.1 Contexto

FarmaExpres requiere un contrato de error común para que frontend y otros consumidores manejen respuestas de fallo de forma predecible.

### 5.2 Decisión

Crear una estructura de error estándar y centralizar el manejo con `@RestControllerAdvice`.

### 5.3 Diseño de la Solución

| Campo | Descripción |
|-------|-------------|
| `timestamp` | Fecha y hora del error |
| `status` | Código HTTP |
| `error` | Nombre del error |
| `message` | Descripción legible |
| `path` | Endpoint afectado |
| `service` | Servicio que generó el error |

---

## 6. Código a Modificar

### 6.1 Crear DTO de error estándar

**Archivo nuevo:** `inventory-service/src/main/java/.../dto/ApiErrorResponse.java`  
**Archivo nuevo:** `auth-service/src/main/java/.../dto/ApiErrorResponse.java`

**Código propuesto**
```java
package co.edu.corhuila.inventory_service.dto;

import java.time.LocalDateTime;

public class ApiErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String service;

    public ApiErrorResponse(LocalDateTime timestamp, int status, String error,
                            String message, String path, String service) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.service = service;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public String getService() { return service; }
}
```

### 6.2 Crear manejador global de excepciones

**Archivo nuevo:** `inventory-service/src/main/java/.../exception/GlobalExceptionHandler.java`

**Código propuesto**
```java
package co.edu.corhuila.inventory_service.exception;

import co.edu.corhuila.inventory_service.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException ex, HttpServletRequest request) {

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getReason(),
                request.getRequestURI(),
                "inventory-service"
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                500,
                "Internal Server Error",
                "Ocurrió un error interno en el servicio",
                request.getRequestURI(),
                "inventory-service"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

### 6.3 Homologar auth-service

**Archivo nuevo:** `auth-service/src/main/java/.../exception/GlobalExceptionHandler.java`

**Código propuesto**
```java
package co.edu.corhuila.auth_service.exception;

import co.edu.corhuila.auth_service.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException ex, HttpServletRequest request) {

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getReason(),
                request.getRequestURI(),
                "auth-service"
        );

        return ResponseEntity.status(status).body(response);
    }
}
```

### 6.4 Resultado esperado en frontend

```json
{
  "timestamp": "2026-03-15T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Producto no encontrado",
  "path": "/api/products/10",
  "service": "inventory-service"
}
```

### 6.5 Paso a paso técnico

```text
1. Crear ApiErrorResponse en ambos servicios
2. Crear GlobalExceptionHandler en ambos servicios
3. Reemplazar errores locales o dispersos por ResponseStatusException
4. Probar errores de login, productos inexistentes y errores 500
5. Ajustar frontend para leer message, status y service
```

---

## 7. Impacto sobre el Sistema

### 7.1 Beneficios

| Beneficio | Descripción | Impacto |
|-----------|-------------|---------|
| **Consistencia** | Respuestas uniformes entre servicios | 🟢 Alto |
| **Integración frontend** | Más fácil manejar errores en cliente | 🟢 Alto |
| **Trazabilidad** | Diagnóstico más claro | 🟡 Medio |

### 7.2 Riesgos y Mitigación

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Inconsistencias en implementación inicial | Media | Medio | Aplicar plantilla común |
| Cambios en clientes | Media | Bajo | Alinear frontend con contrato |


---


