
# ADR-013: Uso de Variable de Entorno para Puerto

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
| **Puerto fijo** | `application.yaml` | Puerto definido estáticamente |

### 1.2 Patrones Ausentes Críticos

| Patrón Faltante | Justificación |
|-----------------|---------------|
| **Variable de entorno** | Permite flexibilidad en despliegue |

---

## 2. Análisis del Proyecto y ADRs Potenciales

El puerto está definido en el archivo de configuración, no es dinámico. Esto dificulta el despliegue en diferentes entornos y puede causar conflictos.

---

## 3. Identificación de Antipatrones

- Dificultad para cambiar puerto en diferentes entornos.
- Conflictos de puertos en despliegues simultáneos.

---

## 4. Segmentos de Código con Malas Prácticas

- Puerto hardcodeado en application.yaml.

---

## 5. Diseño e Implementación del ADR

### 5.1 Contexto
FarmaExpres usa microservicios Spring Boot. El puerto del servidor debe ser configurable para evitar conflictos y facilitar despliegues.

### 5.2 Decisión
Configurar el puerto del servidor usando una variable de entorno.

### 5.3 Diseño de la Solución
Variable | Uso
--- | ---
PORT | Puerto del servidor

### 5.4 Implementación Propuesta
Modificar `application.yaml`:

```yaml
server:
  port: ${PORT:8080}
```

En docker-compose.yml:

```yaml
environment:
  PORT: 8081
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
├── Reemplazar puerto fijo por variable en application.yaml
├── Definir PORT en docker-compose
└── Crear .env.example

Fase 2:
├── Probar levantamiento local con diferentes puertos

Fase 3:
└── Documentar variable PORT en README

---

## 6. Impacto sobre el Sistema

### 6.1 Beneficios
Beneficio | Descripción | Impacto
--- | --- | ---
Portabilidad | Facilita despliegue en diferentes entornos | 🟢 Alto
Evita conflictos | Permite múltiples instancias | 🟢 Medio

### 6.2 Riesgos y Mitigación
Riesgo | Probabilidad | Impacto | Mitigación
--- | --- | --- | ---
Variable faltante | Media | Bajo | .env.example y validación al arranque

### 6.3 Consecuencias
Positivas:
✅ Despliegue flexible.
✅ Menos conflictos.
Negativas:
⚠️ Si falta la variable, el servicio no arranca.
