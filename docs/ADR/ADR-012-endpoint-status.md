
# ADR-012: Endpoint de Estado Simple

**Fecha:** 2026-03-15  
**Estado:** Propuesto  
**Autor:** Rol QA  
**Proyecto:** FarmaExpres

---

## 📋 Tabla de Contenidos

1. Identificación de Patrones
2. Análisis del Proyecto y ADRs Potenciales
3. Identificación de Antipatrones
4. Segmentos de Código con Malas Prácticas
5. Diseño e Implementación del ADR
6. Impacto sobre el Sistema
7. Contexto
8. Decisión
9. Diseño de la Solución
10. Implementación Propuesta
11. Estructura de Archivos Esperada
12. Plan de Implementación
13. Beneficios
14. Riesgos y Mitigación
15. Consecuencias

---

## 1. Identificación de Patrones

### 1.1 Patrones Actualmente Implementados

| Patrón | Ubicación | Descripción |
|--------|-----------|-------------|
| **Actuator** | Algunos servicios | Endpoints operativos |

### 1.2 Patrones Ausentes Críticos

| Patrón Faltante | Justificación |
|-----------------|---------------|
| **Endpoint /status** | No existe endpoint simple de estado |

---

## 2. Análisis del Proyecto y ADRs Potenciales

No hay endpoint `/status` que devuelva "OK" para monitoreo básico. Esto dificulta la integración con herramientas externas y la verificación rápida del estado del servicio.

---

## 3. Identificación de Antipatrones

- Falta de endpoint de estado.
- Monitoreo manual o indirecto.

---

## 4. Segmentos de Código con Malas Prácticas

- No hay método simple para verificar si el servicio está activo.
- Dependencia de actuator para monitoreo básico.

---

## 5. Diseño e Implementación del ADR

### 5.1 Contexto
FarmaExpres usa microservicios Spring Boot. El monitoreo básico es necesario para saber si los servicios están activos.

### 5.2 Decisión
Agregar un endpoint `/status` en cada microservicio que devuelva "OK".

### 5.3 Diseño de la Solución
Endpoint | Respuesta
--- | ---
/status | "OK"

### 5.4 Implementación Propuesta
Agregar en el controlador principal:

```java
@GetMapping("/status")
public String status() {
    return "OK";
}
```

### 5.5 Estructura de Archivos Esperada
FarmaExpres_Backend/
├── auth-service/
│   └── src/main/java/.../controller/AuthController.java
├── inventory-service/
│   └── src/main/java/.../controller/InventoryController.java

### 5.6 Plan de Implementación
Fase 1:
├── Añadir método status() en cada controlador principal

Fase 2:

# ADR-012: Endpoint de Estado Simple

**Fecha:** 2026-03-15  
**Estado:** Propuesto  
**Autor:** Rol QA  
**Proyecto:** FarmaExpres

---

## 📋 Tabla de Contenidos

1. Identificación de Patrones
2. Análisis del Proyecto y ADRs Potenciales
3. Identificación de Antipatrones
4. Segmentos de Código con Malas Prácticas
5. Diseño e Implementación del ADR
6. Impacto sobre el Sistema
7. Contexto
8. Decisión
9. Diseño de la Solución
10. Implementación Propuesta
11. Estructura de Archivos Esperada
12. Plan de Implementación
13. Beneficios
14. Riesgos y Mitigación
15. Consecuencias

---

## 1. Identificación de Patrones

### 1.1 Patrones Actualmente Implementados

| Patrón | Ubicación | Descripción |
|--------|-----------|-------------|
| **Actuator** | Algunos servicios | Endpoints operativos |

### 1.2 Patrones Ausentes Críticos

| Patrón Faltante | Justificación |
|-----------------|---------------|
| **Endpoint /status** | No existe endpoint simple de estado |

---

## 2. Análisis del Proyecto y ADRs Potenciales

No hay endpoint `/status` que devuelva "OK" para monitoreo básico. Esto dificulta la integración con herramientas externas y la verificación rápida del estado del servicio.

---

## 3. Identificación de Antipatrones

- Falta de endpoint de estado.
- Monitoreo manual o indirecto.

---

## 4. Segmentos de Código con Malas Prácticas

- No hay método simple para verificar si el servicio está activo.
- Dependencia de actuator para monitoreo básico.

---

## 5. Diseño e Implementación del ADR

### 5.1 Contexto
FarmaExpres usa microservicios Spring Boot. El monitoreo básico es necesario para saber si los servicios están activos.

### 5.2 Decisión
Agregar un endpoint `/status` en cada microservicio que devuelva "OK".

### 5.3 Diseño de la Solución
Endpoint | Respuesta
--- | ---
/status | "OK"

### 5.4 Implementación Propuesta
Agregar en el controlador principal:

```java
@GetMapping("/status")
public String status() {
    return "OK";
}
```

### 5.5 Estructura de Archivos Esperada
FarmaExpres_Backend/
├── auth-service/
│   └── src/main/java/.../controller/AuthController.java
├── inventory-service/
│   └── src/main/java/.../controller/InventoryController.java

### 5.6 Plan de Implementación
Fase 1:
├── Añadir método status() en cada controlador principal

Fase 2:
├── Probar endpoint en local

Fase 3:
└── Documentar endpoint en README

---

## 6. Impacto sobre el Sistema

### 6.1 Beneficios
Beneficio | Descripción | Impacto
--- | --- | ---
Monitoreo | Facilita verificación rápida | 🟢 Alto
Integración | Permite integración con herramientas externas | 🟢 Medio

### 6.2 Riesgos y Mitigación
Riesgo | Probabilidad | Impacto | Mitigación
--- | --- | --- | ---
Endpoint no implementado | Baja | Bajo | Validar en pruebas

### 6.3 Consecuencias
Positivas:
✅ Monitoreo sencillo.
✅ Integración rápida.
Negativas:
⚠️ Si el endpoint no está, monitoreo será manual.
