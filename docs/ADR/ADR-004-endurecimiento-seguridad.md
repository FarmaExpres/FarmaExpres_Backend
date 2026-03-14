# # ADR-004: Endurecimiento de Seguridad y Exposición Controlada

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
| **JWT Security** | `auth-service`, `inventory-service` | Autenticación y autorización con token |
| **Spring Security** | `SecurityConfig` | Reglas de acceso por endpoint |
| **Gateway CORS Config** | `api-gateway/application.yaml` | Política de acceso entre dominios |

### 1.2 Patrones Ausentes Críticos

| Patrón Faltante | Justificación |
|-----------------|---------------|
| **Security Hardening** | El sistema usa parámetros propios de desarrollo |
| **Least Exposure Principle** | Actuator y CORS deben exponerse solo lo necesario |
| **Controlled Observability** | Monitoreo sí, sobreexposición no |

---

## 2. Análisis del Proyecto y ADRs Potenciales

### 2.1 Estado Actual de la Arquitectura

```text
┌──────────────────────────────────────────────────────────────┐
│                  SEGURIDAD ACTUAL                            │
├──────────────────────────────────────────────────────────────┤
│ api-gateway                                                  │
│  ├── CORS abierto con *                                      │
│  └── Actuator expuesto ampliamente                           │
│                                                              │
│ auth-service / inventory-service                             │
│  ├── show-sql activo                                         │
│  └── logging de desarrollo                                   │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. Identificación de Antipatrones

### 3.1 Antipatrón: **Configuración de Desarrollo en Release**

**Problema:** parámetros útiles en desarrollo permanecen activos en una rama de release.

---

### 3.2 Antipatrón: **Exposición Amplia por Defecto**

**Problema:** CORS abierto y Actuator con exposición total aumentan el riesgo operativo.

---

## 4. Segmentos de Código con Malas Prácticas

### 4.1 ❌ CORS demasiado permisivo

**Archivo:** `api-gateway/src/main/resources/application.yaml`

```yaml
allowedOrigins: "*"
allowedHeaders: "*"
```

**Problema:** cualquier origen puede intentar consumir el backend.

---

### 4.2 ❌ Actuator sobreexpuesto

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

**Problema:** expone información operativa innecesaria.

---

### 4.3 ❌ Configuración ruidosa de desarrollo

**Archivos:** YAMLs de `auth-service` e `inventory-service`

**Problema:** `show-sql` y logs muy verbosos no deberían quedar activos para release.

---

## 5. Diseño e Implementación del ADR

### 5.1 Contexto

FarmaExpres ya dispone de una base funcional de seguridad con JWT. El siguiente paso es endurecer la exposición del sistema para reducir riesgos de integración y operación.

### 5.2 Decisión

Restringir CORS, limitar Actuator, reducir exposición de logs y revisar manejo de tokens inválidos.

### 5.3 Diseño de la Solución

| Elemento | Estado actual | Estado objetivo |
|----------|---------------|-----------------|
| CORS | `*` | Orígenes explícitos |
| Actuator | `*` | `health,info` |
| Logs | Verbosos | Controlados |
| Seguridad JWT | Básica | Más robusta ante errores |

### 5.4 Implementación Propuesta

#### 5.4.1 CORS

```yaml
allowedOrigins: "http://localhost:3000"
```

#### 5.4.2 Actuator

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "health,info"
```

#### 5.4.3 Configuración adicional

```yaml
spring:
  jpa:
    show-sql: false
```

### 5.5 Estructura de Archivos a Intervenir

```text
api-gateway/src/main/resources/application.yaml
auth-service/src/main/resources/application.yaml
inventory-service/src/main/resources/application.yaml
auth-service/src/main/java/.../Config/SecurityConfig.java
inventory-service/src/main/java/.../Config/SecurityConfig.java
auth-service/src/main/java/.../Service/JwtFilter.java
```

### 5.6 Plan de Implementación

```text
Fase 1:
├── Restringir CORS
├── Limitar Actuator
└── Ajustar logs

Fase 2:
├── Revisar respuestas ante token inválido
├── Probar acceso no autenticado
└── Probar acceso autenticado

Fase 3:
└── Documentar políticas de acceso
```

---

## 6. Impacto sobre el Sistema

### 6.1 Beneficios

| Beneficio | Descripción | Impacto |
|-----------|-------------|---------|
| **Seguridad** | Menor superficie de ataque | 🟢 Alto |
| **Control operativo** | Menos exposición de internals | 🟢 Alto |
| **Mejor release** | Configuración más cercana a producción | 🟢 Alto |

### 6.2 Riesgos y Mitigación

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Frontend bloqueado por CORS | Media | Medio | Configurar origen correcto |
| Pérdida de visibilidad | Baja | Bajo | Mantener `health` e `info` |

### 6.3 Consecuencias

**Positivas:**
- ✅ Release más segura.
- ✅ Menor exposición operacional.
- ✅ Mejor control del acceso.

**Negativas:**
- ⚠️ Requiere configuración explícita del frontend.
- ⚠️ Deben revisarse pruebas de integración.


---

*Documento generado para FarmaExpres*
