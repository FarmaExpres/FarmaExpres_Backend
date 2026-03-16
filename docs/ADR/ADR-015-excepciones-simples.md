
# ADR-015: Manejo de Excepciones Simple

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
| **Manejo de excepciones básico** | Controladores | Captura de errores en métodos |

### 1.2 Patrones Ausentes Críticos

| Patrón Faltante | Justificación |
|-----------------|---------------|
| **Manejador global** | Uniformidad en respuestas de error |

---

## 2. Análisis del Proyecto y ADRs Potenciales

No hay clase global para manejar excepciones, lo que provoca respuestas inconsistentes y dificulta la depuración.

---

## 3. Identificación de Antipatrones

- Respuestas de error inconsistentes.
- Manejo de errores disperso en controladores.

---

## 4. Segmentos de Código con Malas Prácticas

- Manejo de errores disperso en controladores.
- Falta de clase global para excepciones.

---

## 5. Diseño e Implementación del ADR

### 5.1 Contexto
FarmaExpres usa microservicios Spring Boot. El manejo global de excepciones mejora la uniformidad y facilita la depuración.

### 5.2 Decisión
Crear una clase @RestControllerAdvice para capturar y responder errores de forma uniforme.

### 5.3 Diseño de la Solución
Clase | Uso
--- | ---
GlobalExceptionHandler | Captura todas las excepciones

### 5.4 Implementación Propuesta
Ejemplo de clase global:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handle(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno");
    }
}
```

### 5.5 Estructura de Archivos Esperada
FarmaExpres_Backend/
├── auth-service/
│   └── src/main/java/.../exception/GlobalExceptionHandler.java
├── inventory-service/
│   └── src/main/java/.../exception/GlobalExceptionHandler.java

### 5.6 Plan de Implementación
Fase 1:
├── Crear clase GlobalExceptionHandler en cada servicio

Fase 2:
├── Probar manejo de errores en endpoints

Fase 3:
└── Documentar manejo de excepciones en README

---

## 6. Impacto sobre el Sistema

### 6.1 Beneficios
Beneficio | Descripción | Impacto
--- | --- | ---
Uniformidad | Respuestas de error uniformes | 🟢 Alto
Depuración | Facilita troubleshooting | 🟢 Medio

### 6.2 Riesgos y Mitigación
Riesgo | Probabilidad | Impacto | Mitigación
--- | --- | --- | ---
Manejo excesivo | Baja | Bajo | Revisar excepciones específicas

### 6.3 Consecuencias
Positivas:
✅ Respuestas uniformes.
✅ Mejor depuración.
Negativas:
⚠️ Si no se capturan excepciones específicas, se pierde detalle.
