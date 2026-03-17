# # ADR-016: Unificar Configuracion JWT sin Secretos Hardcodeados

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
| **JWT Authentication** | `auth-service` e `inventory-service` | Validacion/autorizacion por token |
| **Configuracion por entorno** | `application.yaml` | Se define `jwt.secret` por variable de entorno |
| **Filtro de seguridad** | `JwtFilter` | Token revisado en cada request protegida |

### 1.2 Patrones Ausentes Criticos

| Patron Faltante | Justificacion |
|-----------------|---------------|
| **Configuration Consistency** | El secreto esta en YAML, pero el codigo usa un secreto hardcodeado |
| **Single Source of Truth** | La clave JWT no tiene una unica fuente de configuracion real |

---

## 2. Analisis del Proyecto y ADRs Potenciales

### 2.1 Estado Actual de la Arquitectura

```text
┌──────────────────────────────────────────────────────────────┐
│             CONFIGURACION JWT ACTUAL                         │
├──────────────────────────────────────────────────────────────┤
│ docker-compose: JWT_SECRET definido                          │
│ application.yaml: jwt.secret = ${JWT_SECRET}                │
│ JwtService: usa SECRET hardcodeado en codigo                │
│                                                              │
│ Resultado: configuracion duplicada e inconsistente           │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. Identificacion de Antipatrones

### 3.1 Antipatron: **Secreto Hardcodeado en Codigo**

**Ubicacion:** `JwtService` de auth e inventory

**Problema:** el secreto real no depende de configuracion del entorno, lo que complica rotacion y pruebas.

---

### 3.2 Antipatron: **Configuracion Duplicada sin Uso Real**

**Problema:** existe `jwt.secret` en YAML, pero no es el valor que procesa el servicio.

---

## 4. Segmentos de Codigo con Malas Practicas

### 4.1 Clave fija en auth-service

**Archivo:** `auth-service/src/main/java/co/edu/corhuila/auth_service/Service/JwtService.java`

**Problema:** constante `SECRET` hardcodeada.

---

### 4.2 Clave fija en inventory-service

**Archivo:** `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/JwtService.java`

**Problema:** misma estrategia de secreto fijo en codigo.

---

## 5. Diseno e Implementacion del ADR

### 5.1 Contexto

Se necesita una mejora simple, segura y sin impacto de arranque: usar el secreto JWT que ya existe en la configuracion del proyecto.

### 5.2 Decision

Leer `jwt.secret` desde `application.yaml` en ambos `JwtService` y eliminar secretos hardcodeados.

### 5.3 Diseno de la Solucion

```text
docker-compose (.env) -> JWT_SECRET
        │
application.yaml -> jwt.secret=${JWT_SECRET}
        │
JwtService (@Value jwt.secret)
        │
Firma y validacion de token
```

### 5.4 Implementacion Propuesta

#### 5.4.1 Inyeccion de propiedad en JwtService

```java
@Value("${jwt.secret}")
private String secret;
```

#### 5.4.2 Construccion de key con la propiedad

```java
private Key getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
}
```

### 5.5 Estructura de Archivos a Intervenir

```text
auth-service/src/main/java/co/edu/corhuila/auth_service/Service/JwtService.java
inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/JwtService.java
auth-service/src/main/resources/application.yaml
inventory-service/src/main/resources/application.yaml
docker-compose.yml
```

### 5.6 Plan de Implementacion

```text
Fase 1:
├── Reemplazar SECRET hardcodeado por @Value(jwt.secret)
├── Mantener JWT_SECRET actual de docker-compose
└── Verificar que auth e inventory arranquen sin cambios extra

Fase 2:
├── Probar login y acceso a endpoint protegido
├── Probar token invalido
└── Confirmar que no hubo impacto en startup

Fase 3:
└── Documentar variable JWT_SECRET en README
```

---

## 6. Impacto sobre el Sistema

### 6.1 Beneficios

| Beneficio | Descripcion | Impacto |
|-----------|-------------|---------|
| **Bajo riesgo** | Cambio puntual en dos clases | Alto |
| **Mejor seguridad operativa** | Facilita rotar clave por entorno | Alto |
| **Mantenibilidad** | Configuracion y codigo quedan alineados | Medio |

### 6.2 Riesgos y Mitigacion

| Riesgo | Probabilidad | Impacto | Mitigacion |
|--------|--------------|---------|------------|
| JWT_SECRET ausente en entorno | Baja | Medio | Mantener variable en docker-compose y .env |
| Token viejo deja de validar al rotar secreto | Media | Bajo | Coordinar cambio en ventana controlada |

### 6.3 Consecuencias

**Positivas:**
- Menos deuda tecnica en seguridad.
- Configuracion mas clara para backend y QA.
- Implementacion rapida y facil de probar.

**Negativas:**
- Requiere disciplina para no volver a hardcodear secretos.

---

*Documento generado para FarmaExpres*
