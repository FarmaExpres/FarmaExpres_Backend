# # ADR-018: Normalizar Ruta de Productos Activos sin Romper Clientes

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
| **Routing REST** | `ProductoController` | Endpoints de productos bajo `/api/products` |
| **Soft-delete funcional** | `ProductoService.listarProductosActivos()` | Listado de productos activos |
| **Seguridad por rol** | `SecurityConfig` de inventory | Control de acceso por metodo HTTP |

### 1.2 Patrones Ausentes Criticos

| Patron Faltante | Justificacion |
|-----------------|---------------|
| **Naming Consistency en rutas** | Existe endpoint `/Assets` con mayuscula y termino mixto |
| **Backward-compatible endpoint evolution** | Falta estrategia de transicion sin romper cliente |

---

## 2. Analisis del Proyecto y ADRs Potenciales

### 2.1 Estado Actual de la Arquitectura

```text
┌──────────────────────────────────────────────────────────────┐
│            RUTAS DE INVENTARIO ACTUALES                      │
├──────────────────────────────────────────────────────────────┤
│ /api/products            -> listado general                  │
│ /api/products/Assets     -> listado activos                 │
│ /api/products/sin-stock  -> agotados                         │
│                                                              │
│ El endpoint /Assets no sigue convencion uniforme            │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. Identificacion de Antipatrones

### 3.1 Antipatron: **Ruta con Estilo Inconsistente**

**Ubicacion:** `ProductoController`

**Problema:** uso de `/Assets` (mayuscula e ingles) rompe estandar de naming esperado en API.

---

### 3.2 Antipatron: **Cambio de Ruta sin Plan de Transicion**

**Problema:** corregir directamente la ruta podria romper clientes existentes.

---

## 4. Segmentos de Codigo con Malas Practicas

### 4.1 Endpoint no estandar

**Archivo:** `inventory-service/.../Controllers/ProductoController.java`

**Problema:** `@GetMapping("/Assets")`.

---

### 4.2 Falta de deprecacion explicita

**Archivo:** `inventory-service/.../Controllers/ProductoController.java`

**Problema:** no hay ruta canonical ni alias de compatibilidad.

---

## 5. Diseno e Implementacion del ADR

### 5.1 Contexto

Se necesita mejorar consistencia de API con un cambio sencillo y seguro para no afectar despliegue ni clientes actuales.

### 5.2 Decision

Crear ruta canonical `GET /api/products/activos` y mantener `GET /api/products/Assets` como alias temporal deprecado.

### 5.3 Diseno de la Solucion

```text
Nueva ruta principal:
/api/products/activos
       │
Alias temporal:
/api/products/Assets
       │
Ambas ejecutan listarProductosActivos()
```

### 5.4 Implementacion Propuesta

#### 5.4.1 Mapeos en controlador

```java
@GetMapping({"/activos", "/Assets"})
public ResponseEntity<List<Producto>> listarProductosActivos() { ... }
```

#### 5.4.2 Transicion controlada

- Documentar `"/Assets"` como deprecado.
- Mantener alias por una ventana corta (por ejemplo 1 sprint).

### 5.5 Estructura de Archivos a Intervenir

```text
inventory-service/src/main/java/co/edu/corhuila/inventory_service/Controllers/ProductoController.java
inventory-service/src/main/resources/application.yaml
README.md
```

### 5.6 Plan de Implementacion

```text
Fase 1:
├── Agregar ruta canonical /activos
├── Mantener /Assets como alias temporal
└── Verificar respuesta identica en ambas rutas

Fase 2:
├── Actualizar documentacion y ejemplos de consumo
├── Ejecutar pruebas de endpoint con QA
└── Notificar deprecacion a consumidores

Fase 3:
└── Retirar alias legado cuando no existan clientes dependientes
```

---

## 6. Impacto sobre el Sistema

### 6.1 Beneficios

| Beneficio | Descripcion | Impacto |
|-----------|-------------|---------|
| **Bajo riesgo** | Cambio puntual en un endpoint | Alto |
| **Mejor legibilidad API** | Ruta en minuscula y termino claro | Alto |
| **Compatibilidad** | Se evita romper clientes actuales | Alto |

### 6.2 Riesgos y Mitigacion

| Riesgo | Probabilidad | Impacto | Mitigacion |
|--------|--------------|---------|------------|
| Clientes sigan usando ruta vieja | Alta | Bajo | Mantener alias temporal y comunicar deprecacion |
| Olvido de retirar alias luego | Media | Bajo | Agendar tarea de limpieza en backlog |

### 6.3 Consecuencias

**Positivas:**
- API mas consistente y facil de mantener.
- Mejora rapida sin impacto en arranque.
- Facil de probar para QA.

**Negativas:**
- Durante transicion existen dos rutas para mismo recurso.

---

*Documento generado para FarmaExpres*
