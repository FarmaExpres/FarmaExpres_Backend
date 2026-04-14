# # ADR-005: Estandarización de Respuestas de Autenticación y Manejo de Errores

**Fecha:** 2026-03-14  
**Estado:** Propuesto  
**Autor:** Rol Backend  
**Proyecto:** FarmaExpres

---

## 📋 Tabla de Contenidos

1. [Identificación de Patrones](#1-identificación-de-patrones)
2. [Análisis del Proyecto y ADRs Potenciales](#2-análisis-del-proyecto-y-adrs-potenciales)
3. [Identificación de Antipatrones](#3-identificación-de-antipatrones)
4. [Segmentos de Código con Malas Prácticas](#4-segmentos-de-código-con-malas-prácticas)
5. [Diseño e Implementación del ADR](#5-diseño-e-implementación-del-adr)
6. [Impacto sobre el Sistema](#6-impacto-sobre-el-sistema)

---

## 1. Identificación de Patrones

### 1.1 Patrones Actualmente Implementados

| Patrón | Ubicación | Descripción |
|--------|-----------|-------------|
| **DTO Pattern** | `DTO/` | Transferencia de datos entre capas |
| **Service Layer** | `AuthService`, servicios de inventario | Encapsulamiento de lógica de negocio |
| **REST API** | Controladores | Interacción HTTP |

### 1.2 Patrones Ausentes Críticos

| Patrón Faltante | Justificación |
|-----------------|---------------|
| **Standard Response Contract** | El login debe devolver una respuesta extensible |
| **Global Exception Handling** | Los errores deben seguir un formato uniforme |
| **API Consistency** | Auth e inventory deben responder de forma consistente |

---

## 2. Análisis del Proyecto y ADRs Potenciales

### 2.1 Estado Actual de la Arquitectura

```text
┌──────────────────────────────────────────────────────────────┐
│                    RESPUESTAS ACTUALES                       │
├──────────────────────────────────────────────────────────────┤
│ Auth                                                         │
│  └── Login devuelve String plano                             │
│                                                              │
│ Errores                                                      │
│  └── Sin formato homogéneo consolidado                       │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. Identificación de Antipatrones

### 3.1 Antipatrón: **Respuesta Minimalista No Escalable**

**Problema:** devolver solo un `String` limita la evolución del contrato.

---

### 3.2 Antipatrón: **Errores Inconsistentes**

**Problema:** el cliente no puede depender de una estructura uniforme al manejar fallos.

---

## 4. Segmentos de Código con Malas Prácticas

### 4.1 ❌ Login devuelve string plano

**Archivo:** `auth-service/.../Controllers/AuthController.java`

```java
@PostMapping("/login")
public String login(@RequestBody LoginRequestDto request) {
    return authService.login(request);
}
```

**Problema:** no permite añadir metadatos sin romper el contrato.

---

### 4.2 ❌ Ausencia de manejador global consolidado

**Problema:** las respuestas de error pueden variar entre módulos.

---

## 5. Diseño e Implementación del ADR

### 5.1 Contexto

El backend de FarmaExpres debe integrarse de forma estable con frontend o clientes externos. Para eso, las respuestas de autenticación y los errores necesitan un contrato predecible.

### 5.2 Decisión

Estandarizar la respuesta de login con DTO y homologar los errores con `@RestControllerAdvice`.

### 5.3 Diseño de la Solución

#### Respuesta de login propuesta

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `token` | `String` | JWT generado |
| `type` | `String` | Tipo de token, por ejemplo `Bearer` |
| `email` | `String` | Usuario autenticado |
| `role` | `String` | Rol asignado |

#### Error estándar

| Campo | Descripción |
|-------|-------------|
| `timestamp` | Fecha y hora del error |
| `status` | Código HTTP |
| `error` | Nombre del error |
| `message` | Mensaje descriptivo |
| `path` | Endpoint que falló |

### 5.4 Implementación Propuesta

#### 5.4.1 DTO de respuesta

```java
public class LoginResponseDto {
    private String token;
    private String type;
    private String email;
    private String role;
}
```

#### 5.4.2 Controlador

```java
@PostMapping("/login")
public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
    return ResponseEntity.ok(authService.login(request));
}
```

#### 5.4.3 Error estándar

```json
{
  "timestamp": "2026-03-14T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Credenciales inválidas",
  "path": "/api/auth/login"
}
```

### 5.5 Estructura de Archivos a Intervenir

```text
auth-service/src/main/java/.../Controllers/AuthController.java
auth-service/src/main/java/.../DTO/LoginResponseDto.java
auth-service/src/main/java/.../Service/AuthService.java
auth-service/src/main/java/.../exception/GlobalExceptionHandler.java
inventory-service/src/main/java/.../exception/GlobalExceptionHandler.java
```

### 5.6 Plan de Implementación

```text
Fase 1:
├── Crear LoginResponseDto
├── Ajustar AuthService
└── Ajustar AuthController

Fase 2:
├── Crear GlobalExceptionHandler
├── Homologar errores comunes
└── Probar respuestas desde Postman

Fase 3:
└── Actualizar documentación API
```

---

## 6. Impacto sobre el Sistema

### 6.1 Beneficios

| Beneficio | Descripción | Impacto |
|-----------|-------------|---------|
| **Escalabilidad del contrato** | Permite crecer sin romper diseño | 🟢 Alto |
| **Mejor integración** | El frontend recibe estructuras estables | 🟢 Alto |
| **Trazabilidad** | Errores más claros y consistentes | 🟡 Medio |

### 6.2 Riesgos y Mitigación

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Ruptura de clientes actuales | Media | Medio | Ajustar frontend en conjunto |
| Errores no homologados al inicio | Media | Bajo | Implementación incremental |

### 6.3 Consecuencias

**Positivas:**
- ✅ API más profesional.
- ✅ Menor ambigüedad para clientes.
- ✅ Más fácil documentar y probar.

**Negativas:**
- ⚠️ Cambia la respuesta esperada por clientes actuales.
- ⚠️ Requiere revisar ambos microservicios.


---

*Documento generado para FarmaExpres*
