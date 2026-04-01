# # ADR-019: Manejo Seguro de Token JWT Invalido (evitar 500)

**Fecha:** 2026-03-16  
**Estado:** Propuesto  
**Autor:** Rol Backend y QA  
**Proyecto:** FarmaExpres

---

## Tabla de Contenidos

1. [Identificacion de Patrones](#1-identificacion-de-patrones)
2. [Analisis del Proyecto y ADRs Potenciales](#2-analisis-del-proyecto-y-adrs-potenciales)
3. [Identificacion de Antipatrones](#3-identificacion-de-antipatrones)
4. [Segmentos de Codigo con Malas Practicas](#4-segmentos-de-codigo-con-malas-practicas)
5. [Diseno e Implementacion del ADR](#5-diseno-e-implementacion-del-adr)
6. [Impacto sobre el Sistema](#6-impacto-sobre-el-sistema)

---

## 1. Identificacion de Patrones

### 1.1 Patrones Actualmente Implementados

| Patron | Ubicacion | Descripcion |
|--------|-----------|-------------|
| **JWT Filter** | `JwtFilter` en auth e inventory | Extrae claims y construye autenticacion |
| **Security by Roles** | `SecurityConfig` | Reglas por endpoint y rol |
| **Manejo global de excepciones** | `GlobalExceptionHandler` | Estandar basico de errores |

### 1.2 Patrones Ausentes Criticos

| Patron Faltante | Justificacion |
|-----------------|---------------|
| **Token Failure Guard** | Parseo JWT no protegido ante token corrupto/expirado |
| **401 Uniform Response** | Error de token puede terminar como 500 inesperado |

---

## 2. Analisis del Proyecto y ADRs Potenciales

### 2.1 Estado Actual de la Arquitectura

```text
┌──────────────────────────────────────────────────────────────┐
│                  FLUJO JWT ACTUAL                            │
├──────────────────────────────────────────────────────────────┤
│ JwtFilter -> extraerClaims(token)                            │
│             -> parseClaimsJws                                │
│                                                              │
│ Si token esta mal formado/expirado, puede lanzar excepcion   │
│ sin manejo explicito en filtro                               │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. Identificacion de Antipatrones

### 3.1 Antipatron: **Excepcion JWT sin Control Local**

**Ubicacion:** `JwtFilter` en ambos servicios

**Problema:** ante token invalido, el filtro puede propagar excepcion tecnica.

---

### 3.2 Antipatron: **Respuesta de Error No Homogenea para Auth Failures**

**Problema:** cliente puede recibir error no uniforme en vez de `401 Unauthorized`.

---

## 4. Segmentos de Codigo con Malas Practicas

### 4.1 Parseo JWT sin try/catch en auth-service

**Archivo:** `auth-service/.../Service/JwtFilter.java`

**Problema:** `jwtService.extraerClaims(token)` no esta encapsulado en manejo de excepciones JWT.

---

### 4.2 Parseo JWT sin try/catch en inventory-service

**Archivo:** `inventory-service/.../Service/JwtFilter.java`

**Problema:** mismo comportamiento en servicio de inventario.

---

## 5. Diseno e Implementacion del ADR

### 5.1 Contexto

Se requiere una mejora pequena y segura para robustecer autenticacion sin tocar arquitectura ni arranque de aplicacion.

### 5.2 Decision

Agregar manejo explicito de excepciones JWT en filtros y responder `401` uniforme cuando el token sea invalido.

### 5.3 Diseno de la Solucion

```text
Request con token
  │
JwtFilter
  ├── token valido -> continua cadena
  └── token invalido -> 401 + mensaje estandar
```

### 5.4 Implementacion Propuesta

#### 5.4.1 Manejo en JwtFilter

```java
try {
    Claims claims = jwtService.extraerClaims(token);
    // auth context
} catch (JwtException ex) {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    return;
}
```

#### 5.4.2 Mensaje de error simple

- `"Token invalido o expirado"`

### 5.5 Estructura de Archivos a Intervenir

```text
auth-service/src/main/java/co/edu/corhuila/auth_service/Service/JwtFilter.java
inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/JwtFilter.java
auth-service/src/test/java/co/edu/corhuila/auth_service/*
inventory-service/src/test/java/co/edu/corhuila/inventory_service/*
```

### 5.6 Plan de Implementacion

```text
Fase 1:
├── Agregar try/catch en filtros JWT de ambos servicios
├── Definir respuesta HTTP 401 uniforme
└── Confirmar que flujo con token valido no cambia

Fase 2:
├── Probar token invalido, expirado y vacio
├── Validar que no aparezca 500 por parseo JWT
└── Ajustar pruebas automaticas basicas

Fase 3:
└── Documentar respuesta esperada para 401 en README
```

---

## 6. Impacto sobre el Sistema

### 6.1 Beneficios

| Beneficio | Descripcion | Impacto |
|-----------|-------------|---------|
| **Mayor estabilidad** | Evita errores 500 por token invalido | Alto |
| **Cambio pequeno** | Solo se tocan filtros JWT | Alto |
| **QA mas confiable** | Casos de autenticacion mas predecibles | Medio |

### 6.2 Riesgos y Mitigacion

| Riesgo | Probabilidad | Impacto | Mitigacion |
|--------|--------------|---------|------------|
| Mensaje de error no uniforme entre servicios | Media | Bajo | Reutilizar mismo texto y status 401 |
| Filtro corta request legitima por bug | Baja | Medio | Pruebas con token valido antes de merge |

### 6.3 Consecuencias

**Positivas:**
- Menos incidencias por tokens defectuosos.
- Error de autenticacion controlado y entendible.
- Cero impacto esperado en arranque.

**Negativas:**
- Requiere agregar/ajustar pruebas de seguridad.

---

*Documento generado para FarmaExpres*
