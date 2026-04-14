# # ADR-007: Observabilidad y Monitoreo del Sistema

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
| **Actuator básico** | Servicios Spring Boot | Exposición de endpoints operativos |
| **Logs de aplicación** | Microservicios | Registro de eventos del sistema |
| **Docker deployment** | `docker-compose.yml` | Ejecución distribuida local |

### 1.2 Patrones Ausentes Críticos

| Patrón Faltante | Justificación |
|-----------------|---------------|
| **Health Monitoring Strategy** | Se necesita observar el estado real de cada componente |
| **Operational Visibility** | El sistema debe facilitar diagnóstico |
| **Controlled Logging** | Logs útiles, pero no excesivos |

---

## 2. Análisis del Proyecto y ADRs Potenciales

### 2.1 Estado Actual de la Arquitectura

```text
┌──────────────────────────────────────────────────────────────┐
│               OBSERVABILIDAD ACTUAL                          │
├──────────────────────────────────────────────────────────────┤
│ gateway, auth e inventory                                    │
│  ├── logs disponibles                                        │
│  ├── health checks parciales                                 │
│  └── monitoreo no formalizado                                │
└──────────────────────────────────────────────────────────────┘
```


---

## 3. Identificación de Antipatrones

### 3.1 Antipatrón: **Monitoreo Implícito**

**Problema:** revisar el estado del sistema depende demasiado de inspección manual.

### 3.2 Antipatrón: **Logging sin Estrategia Formal**

**Problema:** los logs pueden ser excesivos o insuficientes según el entorno.

---

## 4. Segmentos de Código con Malas Prácticas

### 4.1 Exposición de monitoreo no formalizada

**Archivos objetivo:**
- `api-gateway/src/main/resources/application.yaml`
- `auth-service/src/main/resources/application.yaml`
- `inventory-service/src/main/resources/application.yaml`

### 4.2 Logs no estandarizados por entorno

**Problema:** configuraciones de desarrollo pueden mezclarse con necesidades operativas.

---

## 5. Diseño e Implementación del ADR

### 5.1 Contexto

FarmaExpres necesita una estrategia mínima de observabilidad para detectar fallos y revisar disponibilidad de servicios.

### 5.2 Decisión

Exponer solo `health` e `info`, y normalizar niveles de log.

### 5.3 Diseño de la Solución

| Servicio | Endpoints |
|----------|-----------|
| Gateway | `/actuator/health`, `/actuator/info` |
| Auth | `/actuator/health`, `/actuator/info` |
| Inventory | `/actuator/health`, `/actuator/info` |

---

## 6. Código a Modificar

### 6.1 Gateway

**Archivo:** `api-gateway/src/main/resources/application.yaml`

**Antes**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

**Después**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info

logging:
  level:
    root: INFO
    org.springframework.cloud.gateway: INFO
```

### 6.2 Auth service

**Archivo:** `auth-service/src/main/resources/application.yaml`

**Antes**
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

**Después**
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

management:
  endpoints:
    web:
      exposure:
        include: health,info

logging:
  level:
    root: INFO
    org.springframework.security: WARN
```

### 6.3 Inventory service

**Archivo:** `inventory-service/src/main/resources/application.yaml`

**Antes**
```yaml
logging:
  level:
    org.springframework.security: DEBUG
```

**Después**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info

logging:
  level:
    root: INFO
    org.springframework.security: WARN
```

### 6.4 Docker compose para healthcheck

**Archivo:** `docker-compose.yml`

**Código propuesto**
```yaml
api-gateway:
  build: ./api-gateway
  ports:
    - "8080:8080"
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
    interval: 30s
    timeout: 10s
    retries: 5
```

### 6.5 Paso a paso técnico

```text
1. Restringir Actuator a health,info
2. Bajar niveles de log a INFO/WARN
3. Desactivar show-sql en release
4. Agregar healthchecks en Docker para gateway y servicios
5. Verificar /actuator/health en 8080, 8081 y 8082
```

---

## 7. Impacto sobre el Sistema

| Beneficio | Descripción | Impacto |
|-----------|-------------|---------|
| **Visibilidad operativa** | Facilita diagnóstico | 🟢 Alto |
| **Mantenibilidad** | Detecta fallos más rápido | 🟢 Alto |
| **Control** | Ayuda a evaluar estabilidad | 🟡 Medio |


---

