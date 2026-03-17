# # ADR-017: Estandarizar Codigos HTTP en Controladores REST

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
| **REST API** | Controladores de auth e inventory | Endpoints CRUD y autenticacion |
| **ResponseEntity** | Varios metodos de controllers | Permite controlar cuerpo y status |
| **DTO Responses** | `UsuarioResponse`, `LoginResponseDto` | Contratos de salida definidos |

### 1.2 Patrones Ausentes Criticos

| Patron Faltante | Justificacion |
|-----------------|---------------|
| **HTTP Semantics Consistency** | POST de creacion retorna 200 en lugar de 201 |
| **Delete Contract Consistency** | DELETE retorna mensaje con 200, no 204 |

---

## 2. Analisis del Proyecto y ADRs Potenciales

### 2.1 Estado Actual de la Arquitectura

```text
┌──────────────────────────────────────────────────────────────┐
│            RESPUESTAS HTTP ACTUALES                          │
├──────────────────────────────────────────────────────────────┤
│ POST /api/products -> 200 OK                                 │
│ POST /api/users    -> 200 OK (por defecto)                  │
│ DELETE /api/products/{id} -> 200 OK + mensaje               │
│                                                              │
│ Funciona, pero no sigue convención REST esperada             │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. Identificacion de Antipatrones

### 3.1 Antipatron: **Creacion con 200 en lugar de 201**

**Ubicacion:** `ProductoController` y `UsuarioControllers`

**Problema:** dificulta validaciones automatizadas y no expresa semantica REST completa.

---

### 3.2 Antipatron: **Delete con respuesta textual variable**

**Problema:** mensaje libre en DELETE complica contratos de cliente y pruebas QA.

---

## 4. Segmentos de Codigo con Malas Practicas

### 4.1 Creacion de producto con status no semantico

**Archivo:** `inventory-service/.../Controllers/ProductoController.java`

**Problema:** `crear()` responde `ResponseEntity.ok(...)`.

---

### 4.2 Eliminacion con cuerpo innecesario

**Archivo:** `inventory-service/.../Controllers/ProductoController.java`

**Problema:** `eliminarProducto()` responde texto, no `204 No Content`.

---

## 5. Diseno e Implementacion del ADR

### 5.1 Contexto

Se busca una mejora simple, de bajo riesgo y facil de validar por QA para estandarizar comportamiento HTTP sin tocar logica de negocio.

### 5.2 Decision

Aplicar codigos HTTP semanticos:

- `POST` de creacion -> `201 Created`
- `DELETE` exitoso -> `204 No Content`
- Mantener `GET` y `PUT` como estan (`200 OK`)

### 5.3 Diseno de la Solucion

```text
Cliente -> POST /api/products -> 201 Created
Cliente -> POST /api/users    -> 201 Created
Cliente -> DELETE /api/products/{id} -> 204 No Content
```

### 5.4 Implementacion Propuesta

#### 5.4.1 Ajuste en controladores

```java
return ResponseEntity.status(HttpStatus.CREATED).body(objetoCreado);
```

```java
return ResponseEntity.noContent().build();
```

#### 5.4.2 Validacion QA

- Verificar status code correcto con tests MockMvc.
- Confirmar que payload de creacion no cambia.

### 5.5 Estructura de Archivos a Intervenir

```text
inventory-service/src/main/java/co/edu/corhuila/inventory_service/Controllers/ProductoController.java
auth-service/src/main/java/co/edu/corhuila/auth_service/Controllers/UsuarioControllers.java
inventory-service/src/test/java/co/edu/corhuila/inventory_service/*
auth-service/src/test/java/co/edu/corhuila/auth_service/*
```

### 5.6 Plan de Implementacion

```text
Fase 1:
├── Cambiar codigos HTTP en controladores de creacion/delete
├── Mantener DTO actuales
└── Ejecutar pruebas unitarias basicas

Fase 2:
├── Agregar pruebas de status code esperados
└── Validar endpoints manualmente con Postman/curl

Fase 3:
└── Actualizar README con ejemplos de respuestas
```

---

## 6. Impacto sobre el Sistema

### 6.1 Beneficios

| Beneficio | Descripcion | Impacto |
|-----------|-------------|---------|
| **Implementacion rapida** | Son cambios puntuales de controllers | Alto |
| **Mejor contrato API** | Semantica HTTP alineada a REST | Alto |
| **QA mas simple** | Casos de prueba mas claros por status | Medio |

### 6.2 Riesgos y Mitigacion

| Riesgo | Probabilidad | Impacto | Mitigacion |
|--------|--------------|---------|------------|
| Cliente espera 200 en POST | Media | Bajo | Comunicar cambio y mantener payload igual |
| Cliente espera mensaje en DELETE | Media | Bajo | Documentar 204 sin body |

### 6.3 Consecuencias

**Positivas:**
- API mas consistente.
- Mejor trazabilidad en pruebas.
- Sin impacto en arranque de aplicacion.

**Negativas:**
- Ajuste pequeno en consumidores que validan status fijo.

---

*Documento generado para FarmaExpres*
