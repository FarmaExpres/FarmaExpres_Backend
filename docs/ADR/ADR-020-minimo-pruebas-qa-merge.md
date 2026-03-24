# # ADR-020: Minimo de Pruebas QA Obligatorias antes de Merge

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
| **Pruebas de contexto** | tests en auth, inventory y gateway | Validan arranque basico de Spring |
| **Actuator health** | `/actuator/health` | Punto de verificacion operativa |
| **Estructura por microservicios** | repo backend | Cada servicio puede probarse de forma independiente |

### 1.2 Patrones Ausentes Criticos

| Patron Faltante | Justificacion |
|-----------------|---------------|
| **Smoke Test Coverage Minima** | Falta set minimo de pruebas funcionales por endpoint clave |
| **Definition of Done QA-Backend** | No hay criterio claro de "listo para merge" |

---

## 2. Analisis del Proyecto y ADRs Potenciales

### 2.1 Estado Actual de la Arquitectura

```text
┌──────────────────────────────────────────────────────────────┐
│                  PRUEBAS ACTUALES                            │
├──────────────────────────────────────────────────────────────┤
│ Predominan tests contextLoads                                │
│ Pocas validaciones de endpoint/status code                   │
│                                                              │
│ Riesgo: cambios pequenos pueden romper flujo sin detectarse  │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. Identificacion de Antipatrones

### 3.1 Antipatron: **Cobertura Minima No Definida**

**Ubicacion:** pipeline local/equipo

**Problema:** cada integrante valida distinto, sin base comun para merge.

---

### 3.2 Antipatron: **Dependencia de Prueba Manual Tardia**

**Problema:** errores simples de endpoint se detectan tarde, despues de integrar.

---

## 4. Segmentos de Codigo con Malas Practicas

### 4.1 Tests con alcance muy basico

**Archivo:** `inventory-service/src/test/java/co/edu/corhuila/inventory_service/ProductoControllerIntegrationTest.java`

**Problema:** solo valida carga de contexto.

---

### 4.2 Ausencia de verificacion de codigos HTTP clave

**Archivo:** pruebas de `auth-service` y `api-gateway`

**Problema:** faltan asserts directos sobre status esperados por endpoint.

---

## 5. Diseno e Implementacion del ADR

### 5.1 Contexto

Se necesita una mejora practica de QA que no afecte runtime: definir un minimo de pruebas rapidas para detectar regresiones antes del merge.

### 5.2 Decision

Definir suite minima obligatoria (smoke QA) ejecutable en menos de 2 minutos por servicio.

### 5.3 Diseno de la Solucion

```text
Checklist por servicio:
1) contextLoads
2) /status o /actuator/health responde OK
3) endpoint protegido sin token -> 401/403 esperado
```

### 5.4 Implementacion Propuesta

#### 5.4.1 Cobertura minima por microservicio

- `auth-service`: login invalido y endpoint protegido sin token.
- `inventory-service`: `GET /api/products` sin token retorna 401/403 segun seguridad.
- `api-gateway`: `GET /status` retorna 200.

#### 5.4.2 Ejecucion estandar

- Comando recomendado en raiz de cada modulo: `./mvnw test`.

### 5.5 Estructura de Archivos a Intervenir

```text
auth-service/src/test/java/co/edu/corhuila/auth_service/*
inventory-service/src/test/java/co/edu/corhuila/inventory_service/*
api-gateway/src/test/java/co/edu/corhuila/api_gateway/*
README.md
```

### 5.6 Plan de Implementacion

```text
Fase 1:
├── Crear pruebas smoke faltantes en auth, inventory y gateway
├── Asegurar asserts de status code
└── Ejecutar tests localmente

Fase 2:
├── Ajustar datos/mock necesarios para estabilidad
├── Medir tiempo total de ejecucion
└── Mantener suite por debajo de 2 minutos

Fase 3:
└── Documentar checklist QA-Backend obligatorio para merge
```

---

## 6. Impacto sobre el Sistema

### 6.1 Beneficios

| Beneficio | Descripcion | Impacto |
|-----------|-------------|---------|
| **Bajo riesgo tecnico** | No toca runtime de produccion | Alto |
| **Deteccion temprana** | Regresiones simples se detectan antes | Alto |
| **Mejor colaboracion backend-QA** | Criterio unico de validacion | Alto |

### 6.2 Riesgos y Mitigacion

| Riesgo | Probabilidad | Impacto | Mitigacion |
|--------|--------------|---------|------------|
| Tests inestables por entorno | Media | Medio | Aislar dependencias y usar datos controlados |
| Tiempo de test crece demasiado | Media | Bajo | Mantener alcance smoke y evitar pruebas pesadas |

### 6.3 Consecuencias

**Positivas:**
- Mayor confianza al integrar cambios.
- Menos bugs simples en despliegues.
- Implementacion sencilla y progresiva.

**Negativas:**
- Requiere mantener pruebas cuando cambian endpoints.

---

*Documento generado para FarmaExpres*
