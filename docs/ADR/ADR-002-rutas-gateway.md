# # ADR-002: Unificación de Rutas entre API Gateway y Microservicios

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
| **API Gateway** | `api-gateway` | Punto de entrada central de la arquitectura |
| **REST Controllers** | `auth-service`, `inventory-service` | Exposición de endpoints HTTP |
| **Role-based Security** | `SecurityConfig` | Control de acceso por rol |

### 1.2 Patrones Ausentes Críticos

| Patrón Faltante | Justificación |
|-----------------|---------------|
| **Uniform API Contract** | Las rutas públicas deben seguir una convención uniforme |
| **Consistent Route Naming** | Auth e inventory deben exponer endpoints coherentes |

---

## 2. Análisis del Proyecto y ADRs Potenciales

### 2.1 Estado Actual de la Arquitectura

```text
┌──────────────────────────────────────────────────────────────┐
│                    INTEGRACIÓN ACTUAL                        │
├──────────────────────────────────────────────────────────────┤
│ api-gateway                                                  │
│  ├── /api/users/**                                           │
│  └── /api/products/**                                        │
│                                                              │
│ auth-service                                                 │
│  ├── /auth/login                                             │
│  └── /usuarios/**                                            │
│                                                              │
│ inventory-service                                            │
│  ├── /productos/**                                           │
│  └── /movimientos/**                                         │
│                                                              │
│ Resultado: contratos HTTP desalineados                       │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. Identificación de Antipatrones

### 3.1 Antipatrón: **Contrato HTTP Inconsistente**

**Ubicación:** gateway y controladores de los microservicios.

**Problema:** las rutas públicas no coinciden con lo que realmente exponen los servicios.

---

### 3.2 Antipatrón: **Naming Inconsistente**

**Problema:** mezcla de nombres como `/auth`, `/usuarios`, `/productos`, lo que dificulta el mantenimiento y la documentación.

---

## 4. Segmentos de Código con Malas Prácticas

### 4.1 ❌ Predicados del gateway desalineados

**Archivo:** `api-gateway/src/main/resources/application.yaml`

```yaml
routes:
  - id: auth-service
    predicates:
      - Path=/api/users/**
```

**Problema:** no contempla de manera consistente rutas reales como login o usuarios.

---

### 4.2 ❌ Controladores con prefijos diferentes

**Archivos:**
- `AuthController.java`
- `ProductoController.java`
- `MovimientoController.java`

**Problema:** cada módulo define convenciones distintas.

---

## 5. Diseño e Implementación del ADR

### 5.1 Contexto

La arquitectura de FarmaExpres depende de un gateway como punto de entrada. Para que esto funcione, los contratos HTTP deben ser consistentes entre gateway, controladores y reglas de seguridad.

### 5.2 Decisión

Adoptar una convención única de rutas públicas con prefijo `/api`.

### 5.3 Diseño de la Solución

| Dominio | Ruta base propuesta |
|---------|---------------------|
| Auth | `/api/auth` |
| Users | `/api/users` |
| Products | `/api/products` |
| Movements | `/api/movements` |

### 5.4 Implementación Propuesta

#### 5.4.1 Gateway

```yaml
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

#### 5.4.2 Controladores

```java
@RestController
@RequestMapping("/api/products")
public class ProductoController {
}
```

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
}
```

#### 5.4.3 Seguridad

Actualizar `requestMatchers` para coincidir con las nuevas rutas públicas.

### 5.5 Estructura de Archivos a Intervenir

```text
api-gateway/src/main/resources/application.yaml
auth-service/src/main/java/.../Controllers/AuthController.java
auth-service/src/main/java/.../Config/SecurityConfig.java
inventory-service/src/main/java/.../Controllers/ProductoController.java
inventory-service/src/main/java/.../Controllers/MovimientoController.java
inventory-service/src/main/java/.../Config/SecurityConfig.java
```

### 5.6 Plan de Implementación

```text
Fase 1:
├── Inventariar endpoints actuales
├── Definir convención final
└── Ajustar gateway

Fase 2:
├── Cambiar @RequestMapping en controladores
├── Actualizar SecurityConfig
└── Validar acceso por roles

Fase 3:
└── Ajustar clientes o frontend
```

---

## 6. Impacto sobre el Sistema

### 6.1 Beneficios

| Beneficio | Descripción | Impacto |
|-----------|-------------|---------|
| **Claridad** | Rutas uniformes y legibles | 🟢 Alto |
| **Integración** | El gateway enruta correctamente | 🟢 Alto |
| **Mantenibilidad** | Menor ambigüedad en documentación y código | 🟢 Alto |

### 6.2 Riesgos y Mitigación

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Ruptura de clientes actuales | Media | Alto | Versionar o comunicar el cambio |
| Errores de autorización | Media | Medio | Probar rutas y roles por caso |

### 6.3 Consecuencias

**Positivas:**
- ✅ Gateway y servicios quedan alineados.
- ✅ La arquitectura gana coherencia.
- ✅ Simplifica pruebas y documentación.

**Negativas:**
- ⚠️ Requiere cambios en clientes existentes.
- ⚠️ Obliga a revisar seguridad de cada ruta.


---

*Documento generado para FarmaExpres*
