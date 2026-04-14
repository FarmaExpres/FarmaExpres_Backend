# # ADR-001: Externalización de Secretos y Configuración Sensible

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
| **Microservicios por dominio** | `auth-service`, `inventory-service` | Separación funcional entre autenticación e inventario |
| **Configuration as Code** | `application.yaml`, `docker-compose.yml` | Configuración declarativa por servicio |
| **JWT Authentication** | `auth-service` | Autenticación basada en token |
| **Containerización** | `Dockerfile`, `docker-compose.yml` | Despliegue homogéneo en contenedores |

### 1.2 Patrones Ausentes Críticos

| Patrón Faltante | Justificación |
|-----------------|---------------|
| **Externalized Configuration** | Las credenciales y secretos no deben vivir en el código versionado |
| **Environment-based Deployment** | El proyecto necesita configuración por ambiente |
| **Secret Management** | El secreto JWT y contraseñas deben aislarse del repositorio |

---

## 2. Análisis del Proyecto y ADRs Potenciales

### 2.1 Estado Actual de la Arquitectura

```text
┌──────────────────────────────────────────────────────────────┐
│                  CONFIGURACIÓN ACTUAL                        │
├──────────────────────────────────────────────────────────────┤
│ auth-service/application.yaml                                │
│  ├── DB username hardcodeado                                 │
│  ├── DB password hardcodeado                                 │
│  └── JWT secret hardcodeado                                  │
│                                                              │
│ inventory-service/application.yaml                           │
│  ├── DB username hardcodeado                                 │
│  └── DB password hardcodeado                                 │
│                                                              │
│ docker-compose.yml                                           │
│  └── Sin estrategia clara de variables externas              │
└──────────────────────────────────────────────────────────────┘
```
---

## 3. Identificación de Antipatrones

### 3.1 Antipatrón: **Secretos Embebidos en Repositorio**

**Ubicación:** `auth-service/src/main/resources/application.yaml` e `inventory-service/src/main/resources/application.yaml`

```yaml
spring:
  datasource:
    username: postgres
    password: 1234

jwt:
  secret: clave-super-secreta
```

**Problema:** información sensible expuesta en archivos versionados.

---

### 3.2 Antipatrón: **Configuración Acoplada al Entorno Local**

**Ubicación:** archivos YAML de los microservicios

**Problema:** dificulta migrar entre desarrollo, pruebas y producción sin editar código.

---

## 4. Segmentos de Código con Malas Prácticas

### 4.1 ❌ Variables sensibles fijas

**Archivo:** `auth-service/src/main/resources/application.yaml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/farmaexpres_users
    username: postgres
    password: 1234
```

**Problema:**
- Expone credenciales.
- No es portable.
- Requiere cambios manuales por ambiente.

---

### 4.2 ❌ Secreto JWT acoplado al código

**Archivo:** `auth-service/src/main/resources/application.yaml`

```yaml
jwt:
  secret: clave-super-secreta
```

**Problema:**
- Riesgo de compromiso del sistema.
- Cambiar la clave exige modificar archivos del repositorio.

---

## 5. Diseño e Implementación del ADR

### 5.1 Contexto

FarmaExpres usa microservicios con Spring Boot y PostgreSQL. Actualmente hay secretos y credenciales definidas directamente en archivos YAML, lo que representa un riesgo de seguridad y una mala práctica de despliegue.

### 5.2 Decisión

Adoptar configuración externalizada con variables de entorno para credenciales de base de datos, secreto JWT y parámetros sensibles.

### 5.3 Diseño de la Solución

| Variable | Uso |
|----------|-----|
| `DB_URL` | URL JDBC de la base de datos |
| `DB_USERNAME` | Usuario de base de datos |
| `DB_PASSWORD` | Contraseña de base de datos |
| `JWT_SECRET` | Secreto para firma de tokens |

### 5.4 Implementación Propuesta

#### 5.4.1 `auth-service/src/main/resources/application.yaml`

```yaml
server:
  port: 8081

spring:
  application:
    name: auth-service

  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

jwt:
  secret: ${JWT_SECRET}
  expiration: 3600000
```

#### 5.4.2 `inventory-service/src/main/resources/application.yaml`

```yaml
server:
  port: 8082

spring:
  application:
    name: inventory-service

  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

jwt:
  secret: ${JWT_SECRET}

```

#### 5.4.3 `docker-compose.yml`

```yaml
services:
  postgres:
    image: postgres:16
    container_name: farmaexpres_postgres
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: 1234
    ports:
      - "5433:5432"
    volumes:
      - ./database/init.sql:/docker-entrypoint-initdb.d/init.sql
  auth-service:
    build: ./auth-service
    container_name: auth_service
    depends_on:
      - postgres
    ports:
      - "8081:8081"
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/farmaexpres_users
      DB_USERNAME: postgres
      DB_PASSWORD: 1234
      JWT_SECRET: mi_clave_secreta_larga

  inventory-service:
    build: ./inventory-service
    container_name: inventory_service
    depends_on:
      - postgres
    ports:
      - "8082:8082"
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/farmaexpres_inventory
      DB_USERNAME: postgres
      DB_PASSWORD: 1234
      JWT_SECRET: mi_clave_secreta_larga
```

### 5.5 Estructura de Archivos Esperada

```text
FarmaExpres_Backend/
├── auth-service/
│   └── src/main/resources/application.yaml
├── inventory-service/
│   └── src/main/resources/application.yaml
├── docker-compose.yml
└── .env.example
```

### 5.6 Plan de Implementación

```text
Fase 1:
├── Reemplazar valores fijos por placeholders ${...}
├── Definir variables en docker-compose
└── Crear .env.example

Fase 2:
├── Probar levantamiento local
├── Validar auth-service
└── Validar inventory-service

Fase 3:
└── Documentar variables requeridas en README
```

---

## 6. Impacto sobre el Sistema

### 6.1 Beneficios

| Beneficio | Descripción | Impacto |
|-----------|-------------|---------|
| **Seguridad** | Reduce exposición de secretos | 🟢 Alto |
| **Portabilidad** | Facilita despliegues por ambiente | 🟢 Alto |
| **Mantenibilidad** | Cambios de configuración sin tocar código | 🟢 Alto |

### 6.2 Riesgos y Mitigación

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Variables faltantes | Media | Medio | `.env.example` y validación al arranque |
| Error de configuración | Media | Bajo | Probar por ambiente antes de release |

### 6.3 Consecuencias

**Positivas:**
- ✅ Menor riesgo de exposición.
- ✅ Mejor práctica DevOps.
- ✅ Base sólida para producción.

**Negativas:**
- ⚠️ Requiere gestión adecuada de variables.
- ⚠️ Puede fallar el arranque si faltan valores.



