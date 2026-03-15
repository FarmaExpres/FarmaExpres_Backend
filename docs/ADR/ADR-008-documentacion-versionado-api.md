# # ADR-008: Estrategia de Documentación y Versionado de API

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
| **REST API** | Microservicios | Endpoints HTTP por dominio |
| **Rutas unificadas** | Gateway y controladores | Prefijo `/api` para acceso público |
| **Documentación parcial** | `README.md` | Información básica del proyecto |

### 1.2 Patrones Ausentes Críticos

| Patrón Faltante | Justificación |
|-----------------|---------------|
| **API Documentation Strategy** | El equipo necesita una guía clara del contrato |
| **API Versioning Strategy** | Los cambios futuros no deben romper clientes |
| **Discoverable API** | Frontend y consumidores deben poder explorar endpoints |

---

## 2. Análisis del Proyecto y ADRs Potenciales

### 2.1 Estado Actual

```text
┌──────────────────────────────────────────────────────────────┐
│                  DOCUMENTACIÓN API ACTUAL                    │
├──────────────────────────────────────────────────────────────┤
│ Endpoints funcionales sin documentación navegable            │
│ y sin estrategia formal de versionado                        │
└──────────────────────────────────────────────────────────────┘
```


---

## 3. Identificación de Antipatrones

### 3.1 Antipatrón: **API Funcional pero Poco Descubrible**

**Problema:** el frontend debe conocer manualmente los endpoints.

### 3.2 Antipatrón: **Cambios sin Política de Versionado**

**Problema:** un cambio futuro puede romper clientes.

---

## 4. Segmentos de Código con Malas Prácticas

### 4.1 Ausencia de documentación API integrada

**Archivos objetivo:**
- `auth-service/pom.xml`
- `inventory-service/pom.xml`
- controladores

---

## 5. Diseño e Implementación del ADR

### 5.1 Contexto

FarmaExpres necesita una forma clara de documentar su API y evolucionarla sin romper frontend.

### 5.2 Decisión

Agregar OpenAPI/Swagger y definir una política base de versionado.

### 5.3 Diseño de la Solución

| Elemento | Decisión |
|----------|----------|
| Documentación | OpenAPI / Swagger |
| Ruta base actual | `/api/...` |
| Evolución futura | Evaluar `/api/v1/...` |

---

## 6. Código a Modificar

### 6.1 Dependencia Swagger en auth-service

**Archivo:** `auth-service/pom.xml`

**Código propuesto**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.5</version>
</dependency>
```

### 6.2 Dependencia Swagger en inventory-service

**Archivo:** `inventory-service/pom.xml`

**Código propuesto**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.5</version>
</dependency>
```

### 6.3 Documentar controlador de auth

**Archivo:** `auth-service/src/main/java/.../Controllers/AuthController.java`

**Antes**
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
}
```

**Después**
```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Auth", description = "Operaciones de autenticación")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Operation(summary = "Iniciar sesión")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
```

### 6.4 Documentar controlador de productos

**Archivo:** `inventory-service/src/main/java/.../Controllers/ProductoController.java`

**Después**
```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Productos", description = "Gestión de productos")
@RestController
@RequestMapping("/api/products")
public class ProductoController {

    @Operation(summary = "Listar productos")
    @GetMapping
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(productoService.listarProductos());
    }
}
```

### 6.5 README

**Archivo:** `README.md`

**Código propuesto**
```md
## Documentación API

- Auth: `http://localhost:8081/swagger-ui.html`
- Inventory: `http://localhost:8082/swagger-ui.html`

## Estrategia de versionado

Actualmente se usa `/api/...`.
Si existen cambios incompatibles, se migrará a `/api/v1/...`.
```

### 6.6 Paso a paso técnico

```text
1. Agregar dependencia springdoc en auth e inventory
2. Anotar controladores con @Tag y @Operation
3. Levantar servicios y validar swagger-ui.html
4. Documentar endpoints base en README
5. Definir regla de migración futura a /api/v1
```

---

## 7. Impacto sobre el Sistema

| Beneficio | Descripción | Impacto |
|-----------|-------------|---------|
| **Claridad** | API navegable y entendible | 🟢 Alto |
| **Integración frontend** | Facilita consumo y pruebas | 🟢 Alto |
| **Evolución controlada** | Reduce riesgo ante cambios | 🟡 Medio |


---

