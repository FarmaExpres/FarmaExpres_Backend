# # ADR-010: Restricción de Acceso Directo a Microservicios y Uso Obligatorio del Gateway

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
| **API Gateway** | `api-gateway` | Punto de entrada central potencial |
| **Microservicios expuestos** | `docker-compose.yml` | Servicios accesibles directamente por puerto |
| **Ruteo centralizado** | Gateway | Reenvío de peticiones por dominio |

### 1.2 Patrones Ausentes Críticos

| Patrón Faltante | Justificación |
|-----------------|---------------|
| **Gateway-only Access** | El cliente debería entrar por un solo punto |
| **Internal Service Isolation** | Auth e inventory no deberían exponerse directamente |
| **Clear Network Boundary** | Se necesita delimitar acceso externo vs interno |

---

## 2. Análisis del Proyecto y ADRs Potenciales

### 2.1 Estado Actual

```text
┌──────────────────────────────────────────────────────────────┐
│                ACCESO ACTUAL AL SISTEMA                      │
├──────────────────────────────────────────────────────────────┤
│ Cliente                                                      │
│  ├── api-gateway:8080                                        │
│  ├── auth-service:8081                                       │
│  └── inventory-service:8082                                  │
│                                                              │
│ Resultado: el gateway existe, pero no es obligatorio         │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. Identificación de Antipatrones

### 3.1 Antipatrón: **Gateway Opcional**

**Problema:** el sistema sigue funcionando sin gateway porque los microservicios están expuestos.

### 3.2 Antipatrón: **Exposición Directa de Servicios Internos**

**Problema:** auth e inventory quedan accesibles directamente, debilitando la arquitectura.

---

## 4. Segmentos de Código con Malas Prácticas

### 4.1 Publicación externa de puertos internos

**Archivo objetivo:** `docker-compose.yml`

---

## 5. Diseño e Implementación del ADR

### 5.1 Contexto

El valor arquitectónico del gateway se consolida solo si los microservicios dejan de ser accesibles directamente por el cliente.

### 5.2 Decisión

Hacer del API Gateway el punto de entrada obligatorio y restringir el acceso externo directo a auth e inventory.

### 5.3 Diseño de la Solución

```text
Cliente
  │
  ▼
api-gateway:8080
  ├── auth-service:8081   (interno)
  └── inventory-service:8082 (interno)
```

---

## 6. Código a Modificar

### 6.1 Docker compose

**Archivo:** `docker-compose.yml`

**Antes**
```yaml
auth-service:
  build: ./auth-service
  ports:
    - "8081:8081"

inventory-service:
  build: ./inventory-service
  ports:
    - "8082:8082"

api-gateway:
  build: ./api-gateway
  ports:
    - "8080:8080"
```

**Después**
```yaml
auth-service:
  build: ./auth-service
  expose:
    - "8081"
  depends_on:
    - postgres

inventory-service:
  build: ./inventory-service
  expose:
    - "8082"
  depends_on:
    - postgres

api-gateway:
  build: ./api-gateway
  ports:
    - "8080:8080"
  depends_on:
    - auth-service
    - inventory-service
```

### 6.2 Gateway debe enrutar por nombre interno

**Archivo:** `api-gateway/src/main/resources/application.yaml`

**Código propuesto**
```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: http://auth-service:8081
          predicates:
            - Path=/api/auth/**,/api/users/**

        - id: inventory-service
          uri: http://inventory-service:8082
          predicates:
            - Path=/api/products/**,/api/movements/**
```

### 6.3 README

**Archivo:** `README.md`

**Código propuesto**
```md
## Acceso al sistema

El acceso oficial al backend se realiza únicamente por:

- `http://localhost:8080`

Los puertos internos de `auth-service` e `inventory-service`
no deben ser consumidos directamente por el frontend.
```

### 6.4 Paso a paso técnico

```text
1. Quitar puertos públicos 8081 y 8082 del docker-compose
2. Reemplazarlos por expose para comunicación interna
3. Verificar que el gateway enrute a auth-service e inventory-service
4. Probar login y productos desde localhost:8080
5. Ajustar frontend para consumir solo el gateway
```

---

## 7. Impacto sobre el Sistema

| Beneficio | Descripción | Impacto |
|-----------|-------------|---------|
| **Coherencia arquitectónica** | El gateway se vuelve obligatorio | 🟢 Alto |
| **Seguridad** | Menor exposición externa | 🟢 Alto |
| **Simplicidad para frontend** | Un solo endpoint base | 🟢 Alto |


---


