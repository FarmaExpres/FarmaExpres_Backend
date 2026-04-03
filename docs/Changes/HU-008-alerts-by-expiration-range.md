# HU-008 - Alertas de productos proximos a vencer por rango (alert-service)

## 1. Informacion general
- HU: `HU-008`
- Nombre: Consulta de alertas de productos proximos a vencer por rangos de dias
- Microservicio: `alert-service`
- Estado: En progreso
- Rama de trabajo: `HU-008-dev`

## 2. Objetivo de la HU
Agregar dos nuevos metodos en `alert-service` para generar alertas de productos activos que esten proximos a vencer en dos ventanas de tiempo independientes:
- productos que vencen entre `16 y 30 dias`
- productos que vencen entre `31 y 60 dias`

Esta HU busca complementar la HU-005, que actualmente cubre solo productos proximos a vencer en una ventana de hasta `15 dias`.

## 3. Alcance propuesto
Se documenta la implementacion de dos nuevas consultas de alertas:
- un metodo para alertas de vencimiento en rango `16-30 dias`  Implementado
- un metodo para alertas de vencimiento en rango `31-60 dias`

Cada metodo debera seguir la arquitectura actual del microservicio:
- `router -> controller -> service -> repository`

## 4. Endpoints propuestos
### Consumo oficial (gateway)
- Metodo: `GET`
- URL propuesta: `http://localhost:8080/api/alerts/expiring-half-month`

- Metodo: `GET`
- URL propuesta: `http://localhost:8080/api/alerts/expiring-month`

### Endpoint interno del microservicio
- Metodo: `GET`
- URL propuesta: `http://localhost:8083/api/alerts/expiring-half-month`

- Metodo: `GET`
- URL propuesta: `http://localhost:8083/api/alerts/expiring-month`

## 5. Logica de negocio esperada
Se consideraran solo productos que cumplan:
- `asset = TRUE`
- `expirationdate > CURRENT_DATE`

Para el primer metodo:
- `expirationdate >= CURRENT_DATE + 16 dias`
- `expirationdate <= CURRENT_DATE + 30 dias`

Para el segundo metodo:
- `expirationdate >= CURRENT_DATE + 31 dias`
- `expirationdate <= CURRENT_DATE + 60 dias`

Orden esperado de salida:
- `expirationdate ASC`
- `name ASC`

## 6. Resultado funcional esperado
Cada endpoint debera:
- responder con `200 OK` cuando el proceso sea correcto
- retornar `generatedAt`, `total` y `alerts`
- incluir una lista de alertas con informacion del producto
- mantener consistencia con el contrato ya usado en `alert-service`

## 7. Contrato de respuesta esperado
```json
{
  "generatedAt": "2026-04-02T21:00:00.000Z",
  "total": 1,
  "alerts": [
    {
      "type": "EXPIRING_RANGE",
      "severity": "LOW",
      "message": "Producto proximo a vencer entre 16 y 30 dias: Loratadina 10 mg",
      "product": {
        "id": "33",
        "code": "EXP-001",
        "name": "Loratadina 10 mg",
        "stock": 12,
        "minimumStock": 5,
        "expirationDate": "2026-04-20",
        "active": true
      }
    }
  ]
}
```

## 8. Archivos estimados a modificar
- `alert-service/src/repositories/productRepository.js`
- `alert-service/src/controllers/`
- `alert-service/src/services/`
- `alert-service/src/routers/alertRoutes.js`
- `alert-service/tests/`
- `alert-service/package.json`
- `docs/Changes/HU-008-alerts-by-expiration-range.md`

## 9. Criterios de aceptacion propuestos
1. Existen dos nuevos endpoints para consultar alertas por rangos de vencimiento.
2. Un endpoint retorna productos que vencen entre `16 y 30 dias`.
3. Un endpoint retorna productos que vencen entre `31 y 60 dias`.
4. Solo se consideran productos activos.
5. La respuesta mantiene el formato estandar del microservicio.
6. Ambos endpoints pueden consumirse por `api-gateway`.
7. Se agregan pruebas automatizadas para validar ambos rangos.

## 10. Notas de implementacion
- Esta HU no reemplaza `expiring-soon`; lo complementa.
- Se recomienda definir nombres de tipos de alerta y severidad consistentes con las HUs previas.
- Si posteriormente se requiere, estas dos categorias podran integrarse tambien en el endpoint consolidado `GET /api/alerts`.
