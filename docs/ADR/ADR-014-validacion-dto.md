
# ADR-014: Validación Mínima en DTO

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
| **DTO sin validación** | `DTO/` | No hay anotaciones de validación |

### 1.2 Patrones Ausentes Críticos

| Patrón Faltante | Justificación |
|-----------------|---------------|
| **Validación mínima** | Evita errores por datos nulos o vacíos |

---

## 2. Análisis del Proyecto y ADRs Potenciales

Los DTO no tienen validaciones básicas, lo que puede provocar errores en la lógica de negocio y datos inconsistentes.

---

## 3. Identificación de Antipatrones

- Datos nulos o vacíos aceptados por el sistema.
- Validaciones manuales en controladores.

---

## 4. Segmentos de Código con Malas Prácticas

- Falta de anotaciones como `@NotNull` o `@NotBlank`.
- Validaciones dispersas en el código.

---

## 5. Diseño e Implementación del ADR

### 5.1 Contexto
FarmaExpres usa DTOs para transferencia de datos entre capas. La validación mínima es necesaria para asegurar la calidad de datos.

### 5.2 Decisión
Implementar anotaciones básicas de validación en los DTOs.

### 5.3 Diseño de la Solución
Anotación | Uso
--- | ---
@NotNull | Evita valores nulos
@NotBlank | Evita cadenas vacías

### 5.4 Implementación Propuesta
Ejemplo en un DTO:

```java
public class UserDTO {
    @NotNull
    @NotBlank
    private String username;
}
```

### 5.5 Estructura de Archivos Esperada
FarmaExpres_Backend/
├── auth-service/
│   └── src/main/java/.../dto/UserDTO.java
├── inventory-service/
│   └── src/main/java/.../dto/ProductDTO.java

### 5.6 Plan de Implementación
Fase 1:
├── Añadir anotaciones en los DTOs

Fase 2:
├── Probar validaciones en endpoints

Fase 3:
└── Documentar validaciones en README

---

## 6. Impacto sobre el Sistema

### 6.1 Beneficios
Beneficio | Descripción | Impacto
--- | --- | ---
Calidad | Mejora la calidad de datos | 🟢 Alto
Reducción de errores | Menos fallos en lógica | 🟢 Medio

### 6.2 Riesgos y Mitigación
Riesgo | Probabilidad | Impacto | Mitigación
--- | --- | --- | ---
Validaciones excesivas | Baja | Bajo | Revisar requisitos de negocio

### 6.3 Consecuencias
Positivas:
✅ Datos consistentes.
✅ Menos errores.
Negativas:
⚠️ Validaciones mal configuradas pueden bloquear datos válidos.
