# HU-006 - Centro de alertas consolidado (alert-service)

## 1. Informacion general
- HU: `HU-006`
- Nombre: Consulta consolidada de todas las alertas
- Microservicio: `alert-service`
- Estado: Implementado
- Rama de trabajo: `HU-AC-dev`

## 2. Objetivo de la HU
Exponer un endpoint unico para consultar todas las alertas en un solo payload, incluyendo categorias nuevas agregadas en HUs posteriores.

## 3. Endpoint funcional
### Consumo oficial (gateway)
- Metodo: `GET`
- URL: `http://localhost:8080/api/alerts`

### Endpoint interno del microservicio
- Metodo: `GET`
- URL: `http://localhost:8083/api/alerts`

## 4. Alcance implementado
- Endpoint agregado `GET /api/alerts`.
- Integracion en una sola respuesta de categorias:
  - `outOfStock`
  - `expired`
  - `lowStock`
  - `expiringSoon`
- Resumen (`summary`) con conteo por categoria y total general.

## 5. Logica de agregacion
El endpoint consolida el resultado en:
- `generatedAt`
- `summary`
  - `totalAlerts`
  - `outOfStock`
  - `expired`
  - `lowStock`
  - `expiringSoon`
- `alerts`

## 6. Archivos modificados
- `alert-service/src/controllers/allAlertsController.js`
- `alert-service/src/services/allAlertsService.js`
- `alert-service/src/routers/alertRoutes.js`
- `alert-service/tests/allAlerts.test.js`
- `alert-service/package.json`

## 7. Contrato de respuesta esperado
```json
{
  "generatedAt": "2026-03-31T15:13:52.511Z",
  "summary": {
    "totalAlerts": 8,
    "outOfStock": 1,
    "expired": 2,
    "lowStock": 4,
    "expiringSoon": 1
  },
  "alerts": [
    {
      "type": "OUT_OF_STOCK",
      "severity": "HIGH",
      "message": "Producto sin stock: Acetaminophen 500mg",
      "product": {
        "id": "16",
        "code": "MASD-001",
        "name": "Acetaminophen 500mg",
        "stock": 0,
        "minimumStock": 8,
        "expirationDate": "2026-03-31",
        "active": true
      }
    }
  ]
}
```

## 8. Criterios de aceptacion cubiertos
1. Existe `GET /api/alerts`.
2. El endpoint responde `200 OK` en ejecucion correcta.
3. Incluye `generatedAt`, `summary` y `alerts`.
4. `summary` incluye `outOfStock`, `expired`, `lowStock`, `expiringSoon` y `totalAlerts`.
5. `alerts` contiene la union de las categorias agregadas.
6. El endpoint es consumible por gateway en `:8080`.

## 9. Evidencia de validacion
Prueba automatizada:
- `allAlerts.test.js`

Script de tests del microservicio actualizado para incluir nuevas HUs.
