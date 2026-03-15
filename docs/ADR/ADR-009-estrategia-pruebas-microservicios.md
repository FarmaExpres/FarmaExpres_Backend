# # ADR-009: Estrategia de Pruebas para Microservicios

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
| **Servicios por capas** | `Service`, `Repository`, `Controllers` | Lógica distribuida en capas |
| **Spring Boot Test** | Carpetas `src/test` | Soporte para pruebas |
| **Dockerized Environment** | `docker-compose.yml` | Entorno reproducible para integración |

### 1.2 Patrones Ausentes Críticos

| Patrón Faltante | Justificación |
|-----------------|---------------|
| **Testing Strategy** | El proyecto necesita criterios formales de validación |
| **Integration Testing** | Microservicios deben probarse también como sistema |
| **Regression Safety** | Cada cambio por ADR debe poder verificarse |

---

## 2. Análisis del Proyecto y ADRs Potenciales

### 2.1 Estado Actual

```text
┌──────────────────────────────────────────────────────────────┐
│                   ESTRATEGIA DE PRUEBAS                      │
├──────────────────────────────────────────────────────────────┤
│ Hay soporte técnico para pruebas, pero no una estrategia     │
│ formal ni casos mínimos definidos                             │
└──────────────────────────────────────────────────────────────┘
```


---

## 3. Identificación de Antipatrones

### 3.1 Antipatrón: **Cambios sin Verificación Formal**

**Problema:** las mejoras dependen demasiado de pruebas manuales.

### 3.2 Antipatrón: **Cobertura No Priorizada**

**Problema:** no está claro qué partes deben probarse primero.

---

## 4. Segmentos de Código con Malas Prácticas

### 4.1 Dependencia excesiva de validación manual

**Archivos objetivo:**
- `auth-service/src/test/`
- `inventory-service/src/test/`
- `api-gateway/src/test/`

---

## 5. Diseño e Implementación del ADR

### 5.1 Contexto

FarmaExpres necesita una estrategia mínima de pruebas para sostener la evolución del sistema.

### 5.2 Decisión

Definir pruebas unitarias, integración y funcionales para flujos críticos.

### 5.3 Diseño de la Solución

| Nivel | Objetivo |
|-------|----------|
| Unitarias | Validar lógica aislada |
| Integración | Validar interacción con BD y seguridad |
| Funcionales | Validar flujos vía HTTP/gateway |

---

## 6. Código a Modificar

### 6.1 Prueba unitaria de AuthService

**Archivo nuevo:** `auth-service/src/test/java/.../service/AuthServiceTest.java`

**Código propuesto**
```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void deberiaLanzarErrorSiUsuarioNoExiste() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("noexiste@correo.com");
        request.setPassword("123456");

        when(usuarioRepository.findByEmail("noexiste@correo.com"))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> authService.login(request));
    }
}
```

### 6.2 Prueba de integración de productos

**Archivo nuevo:** `inventory-service/src/test/java/.../controller/ProductoControllerIntegrationTest.java`

**Código propuesto**
```java
@SpringBootTest
@AutoConfigureMockMvc
class ProductoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deberiaResponderOkAlListarProductos() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }
}
```

### 6.3 Prueba básica del gateway

**Archivo nuevo:** `api-gateway/src/test/java/.../GatewaySmokeTest.java`

**Código propuesto**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewaySmokeTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void healthDebeResponder() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/actuator/health", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
```

### 6.4 README con estrategia

**Archivo:** `README.md`

**Código propuesto**
```md
## Estrategia de pruebas

- Unitarias: reglas de negocio por servicio
- Integración: controladores, seguridad y base de datos
- Funcionales: flujos vía gateway
```

### 6.5 Paso a paso técnico

```text
1. Crear casos críticos por microservicio
2. Agregar unit tests a AuthService y ProductoService
3. Agregar integration tests con MockMvc
4. Agregar smoke test al gateway
5. Ejecutar mvn test en cada módulo
```

---

## 7. Impacto sobre el Sistema

| Beneficio | Descripción | Impacto |
|-----------|-------------|---------|
| **Calidad** | Reduce regresiones | 🟢 Alto |
| **Confianza** | Permite cambiar con seguridad | 🟢 Alto |
| **Trazabilidad** | Cada ADR puede validarse mejor | 🟡 Medio |


---


