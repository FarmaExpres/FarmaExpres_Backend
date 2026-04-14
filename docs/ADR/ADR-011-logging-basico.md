
# ADR-011: Configuración de Logging Básico

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
| **Logging por defecto** | `application.yaml` | Nivel INFO configurado |

### 1.2 Patrones Ausentes Críticos

| Patrón Faltante | Justificación |
|-----------------|---------------|
| **Logging personalizado** | No hay configuración por ambiente |

---

## 2. Análisis del Proyecto y ADRs Potenciales

El sistema usa logging por defecto, pero no hay control granular por ambiente. Los logs pueden ser excesivos en producción o insuficientes en desarrollo.

---

## 3. Identificación de Antipatrones

- Logging excesivo en producción.
- Falta de logs en desarrollo.
- No hay separación de logs por ambiente.

---

## 4. Segmentos de Código con Malas Prácticas

- No hay configuración explícita de logging en application.yaml.
- Logging hardcodeado en el código.

---

## 5. Diseño e Implementación del ADR

### 5.1 Contexto
FarmaExpres usa microservicios Spring Boot. El logging es fundamental para monitoreo y depuración, pero actualmente no está controlado por ambiente.

### 5.2 Decisión
Adoptar configuración explícita de logging en application.yaml, permitiendo control por ambiente y evitando logs innecesarios.

### 5.3 Diseño de la Solución
Variable | Uso
--- | ---
LOG_LEVEL | Nivel de logging por ambiente

### 5.4 Implementación Propuesta
Agregar en `application.yaml`:

```yaml
logging:
  level:
    root: ${LOG_LEVEL:INFO}
```

En docker-compose.yml:

```yaml
environment:
  LOG_LEVEL: INFO
```

### 5.5 Estructura de Archivos Esperada
FarmaExpres_Backend/
├── auth-service/
│   └── src/main/resources/application.yaml
├── inventory-service/
│   └── src/main/resources/application.yaml
├── docker-compose.yml
└── .env.example

### 5.6 Plan de Implementación
Fase 1:
├── Añadir variable LOG_LEVEL en application.yaml
├── Definir LOG_LEVEL en docker-compose
└── Crear .env.example

Fase 2:
├── Probar levantamiento local con diferentes niveles
└── Validar logs en cada microservicio

Fase 3:
└── Documentar variable LOG_LEVEL en README

---

## 6. Impacto sobre el Sistema

### 6.1 Beneficios
Beneficio | Descripción | Impacto
--- | --- | ---
Control | Permite ajustar logs por ambiente | 🟢 Alto
Mantenibilidad | Facilita depuración | 🟢 Alto
Seguridad | Evita exposición de datos sensibles en logs | 🟢 Medio

### 6.2 Riesgos y Mitigación
Riesgo | Probabilidad | Impacto | Mitigación
--- | --- | --- | ---
Logs insuficientes | Baja | Medio | Revisar configuración en desarrollo
Logs excesivos | Media | Bajo | Ajustar LOG_LEVEL en producción

### 6.3 Consecuencias
Positivas:
✅ Mejor monitoreo.
✅ Facilita troubleshooting.
Negativas:
⚠️ Configuración incorrecta puede dificultar depuración.
