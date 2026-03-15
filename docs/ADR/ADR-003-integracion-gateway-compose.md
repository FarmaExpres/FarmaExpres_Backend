# # ADR-003: Integración Completa del API Gateway en el Despliegue

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
| **Microservices Architecture** | Repositorio completo | Separación por dominios |
| **Container Deployment** | `docker-compose.yml` | Orquestación local con Docker |
| **Gateway Module** | `api-gateway` | Módulo dedicado a entrada del sistema |

### 1.2 Patrones Ausentes Críticos

| Patrón Faltante | Justificación |
|-----------------|---------------|
| **Entry Point Consolidation** | El gateway debe formar parte real del despliegue |
| **Gateway-first Access** | El cliente no debería consumir microservicios directamente |

---

## 2. Análisis del Proyecto y ADRs Potenciales

### 2.1 Estado Actual de la Arquitectura

```text
┌──────────────────────────────────────────────────────────────┐
│                  DESPLIEGUE ACTUAL                           │
├──────────────────────────────────────────────────────────────┤
│ docker-compose                                               │
│  ├── postgres                                                │
│  ├── auth-service                                            │
│  └── inventory-service                                       │
│                                                              │
│ api-gateway existe en el repositorio pero no como entrada    │
│ principal consolidada del entorno                            │
└──────────────────────────────────────────────────────────────┘
```
---

## 3. Identificación de Antipatrones

### 3.1 Antipatrón: **Gateway Declarado pero No Consolidado**

**Ubicación:** arquitectura y `docker-compose.yml`

**Problema:** el sistema declara gateway, pero el entorno no lo usa como acceso principal.

---

### 3.2 Antipatrón: **Acceso Directo a Microservicios**

**Problema:** si los clientes entran directo a `auth-service` o `inventory-service`, se rompe la idea de punto único de entrada.

---

## 4. Segmentos de Código con Malas Prácticas

### 4.1 ❌ Falta de servicio gateway en compose

**Archivo:** `docker-compose.yml`

**Problema:** el gateway no participa del arranque coordinado.

---

### 4.2 ❌ Dockerfile del gateway incompleto o no consolidado

**Archivo:** `api-gateway/Dockerfile`

**Problema:** el módulo existe, pero requiere quedar listo para build y ejecución.

---

## 5. Diseño e Implementación del ADR

### 5.1 Contexto

FarmaExpres plantea una arquitectura con gateway, auth-service e inventory-service. Para que la arquitectura sea consistente, el gateway debe ser parte explícita del despliegue y el cliente debe consumir a través de él.

### 5.2 Decisión

Agregar `api-gateway` al `docker-compose.yml` y convertirlo en el punto de entrada oficial.

### 5.3 Diseño de la Solución

```text
Cliente
  │
  ▼
api-gateway:8080
  ├── auth-service:8081
  └── inventory-service:8082
```

### 5.4 Implementación Propuesta

#### 5.4.1 `docker-compose.yml`

```yaml
api-gateway:
  build: ./api-gateway
  container_name: api-gateway
  ports:
    - "8080:8080"
  depends_on:
    - auth-service
    - inventory-service
```

#### 5.4.2 Hostnames internos

El gateway debe usar:
- `http://auth-service:8081`
- `http://inventory-service:8082`

### 5.5 Estructura de Archivos a Intervenir

```text
docker-compose.yml
api-gateway/Dockerfile
api-gateway/pom.xml
api-gateway/src/main/resources/application.yaml
```

### 5.6 Plan de Implementación

```text
Fase 1:
├── Verificar build del gateway
├── Completar Dockerfile si hace falta
└── Validar puertos internos

Fase 2:
├── Agregar servicio a docker-compose
├── Levantar entorno completo
└── Verificar dependencia entre servicios

Fase 3:
└── Probar consumo total por localhost:8080
```

---

## 6. Impacto sobre el Sistema

### 6.1 Beneficios

| Beneficio | Descripción | Impacto |
|-----------|-------------|---------|
| **Coherencia arquitectónica** | El despliegue refleja la arquitectura | 🟢 Alto |
| **Centralización** | Entrada única para clientes | 🟢 Alto |
| **Escalabilidad futura** | Facilita políticas y observabilidad | 🟡 Medio |

### 6.2 Riesgos y Mitigación

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Fallas de arranque por dependencia | Media | Medio | Probar orden y salud de servicios |
| Configuración incorrecta de rutas | Media | Medio | Validar con ADR-002 |

### 6.3 Consecuencias

**Positivas:**
- ✅ Se consolida el gateway.
- ✅ Mejor control del tráfico.
- ✅ Base más sólida para seguridad y monitoreo.

**Negativas:**
- ⚠️ Se agrega un punto más al despliegue.
- ⚠️ Requiere pruebas de integración adicionales.


---

*Documento generado para FarmaExpres*
