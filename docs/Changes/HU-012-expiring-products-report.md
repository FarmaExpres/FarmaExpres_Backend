# HU-012 - Reporte consolidado de productos proximos a vencer (alert-service)

## 1. Informacion general
- HU: `HU-012`
- Nombre: Reporte de productos proximos a vencer
- Microservicio: `alert-service`
- Estado: Implementado
- Rama de trabajo sugerida: `HU-012-dev`

## 2. Objetivo de la HU
Exponer un metodo en `alert-service` para consultar en un solo reporte todos los productos vencidos o proximos a vencer, incluyendo los campos calculados `diasRestantes` y `estado`, para soportar la vista mostrada en frontend.

## 3. Necesidad funcional
La vista requiere un listado unico de productos con vencimiento cercano, permitiendo:
- visualizar todos los registros en una sola tabla
- identificar rapidamente si un producto esta `Vencido`, `Critico`, `Medio` o `Controlado`
- mostrar el valor de `diasRestantes`
- filtrar por grupos visibles en interfaz:
  - `Todos`
  - `Vencidos`
  - `0-15 dias`
  - `16-30 dias`
  - `31-60 dias`

## 4. Endpoint funcional
### Consumo oficial (gateway)
- Metodo: `GET`
- URL: `http://localhost:8080/api/alerts/expiring-report`

### Endpoint interno del microservicio
- Metodo: `GET`
- URL: `http://localhost:8083/api/alerts/expiring-report`

## 5. Parametros de consulta propuestos
- `range` opcional para filtrar desde backend.
- Valores esperados:
  - `all`
  - `expired`
  - `0-15`
  - `16-30`
  - `31-60`

Si no se envia `range`, el endpoint debera responder con `all`.

## 6. Alcance funcional esperado
- Consolidar en una sola respuesta los productos:
  - vencidos
  - proximos a vencer entre `0 y 15` dias
  - proximos a vencer entre `16 y 30` dias
  - proximos a vencer entre `31 y 60` dias
- Reutilizar la logica ya existente de consultas por vencimiento en `alert-service`.
- Agregar al resultado los campos:
  - `diasRestantes`
  - `estado`
- Mantener datos base del producto:
  - `id`
  - `code`
  - `name`
  - `stock`
  - `minimumStock`
  - `expirationDate`
  - `active`

## 7. Reglas de negocio propuestas
Se consideran solo productos con:
- `asset = TRUE`
- `expirationdate <= CURRENT_DATE + 60 dias`

Clasificacion esperada:
- `Vencido`: cuando `diasRestantes < 0`
- `Critico`: cuando `diasRestantes` este entre `0` y `15`
- `Medio`: cuando `diasRestantes` este entre `16` y `30`
- `Controlado`: cuando `diasRestantes` este entre `31` y `60`

Regla de calculo:
- `diasRestantes = expirationDate - CURRENT_DATE`
- si el valor es negativo, puede mostrarse en frontend como `N dia(s) vencido`

Orden esperado:
- `expirationDate ASC`
- `name ASC`

## 8. Contrato de respuesta esperado
```json
{
  "generatedAt": "2026-04-03T21:00:00.000Z",
  "total": 5,
  "filters": {
    "appliedRange": "all",
    "availableRanges": ["all", "expired", "0-15", "16-30", "31-60"]
  },
  "summary": {
    "expired": 1,
    "range0To15": 1,
    "range16To30": 2,
    "range31To60": 1
  },
  "reports": [
    {
      "id": "10",
      "code": "LOS-001",
      "name": "Losartan 50 mg",
      "stock": 75,
      "minimumStock": 10,
      "expirationDate": "2026-02-12",
      "active": true,
      "diasRestantes": -50,
      "estado": "Vencido"
    },
    {
      "id": "11",
      "code": "LOT-001",
      "name": "Loratadina 10 mg",
      "stock": 9,
      "minimumStock": 5,
      "expirationDate": "2026-04-10",
      "active": true,
      "diasRestantes": 7,
      "estado": "Critico"
    },
    {
      "id": "12",
      "code": "SIM-001",
      "name": "Simvastatina 20 mg",
      "stock": 75,
      "minimumStock": 15,
      "expirationDate": "2026-04-20",
      "active": true,
      "diasRestantes": 17,
      "estado": "Medio"
    },
    {
      "id": "13",
      "code": "VIC-001",
      "name": "Vitamina C 1 g",
      "stock": 110,
      "minimumStock": 20,
      "expirationDate": "2026-04-30",
      "active": true,
      "diasRestantes": 27,
      "estado": "Medio"
    },
    {
      "id": "14",
      "code": "SAT-001",
      "name": "Salbutamol Inhalador",
      "stock": 35,
      "minimumStock": 8,
      "expirationDate": "2026-05-07",
      "active": true,
      "diasRestantes": 34,
      "estado": "Controlado"
    }
  ]
}
```

## 9. Implementacion tecnica
Se mantuvo la arquitectura actual:
- `router -> controller -> service -> repository`

Componentes implementados:
- nuevo endpoint en `alertRoutes.js`
- nuevo controller para reporte consolidado
- nuevo service para armar el payload final
- nuevo metodo en repository para traer productos hasta 60 dias y vencidos
- apoyo de utilidades para calcular:
  - `diasRestantes`
  - `estado`

## 10. Archivos modificados
- `alert-service/src/routers/alertRoutes.js`
- `alert-service/src/controllers/expiringProductsReportController.js`
- `alert-service/src/services/expiringProductsReportService.js`
- `alert-service/src/repositories/productRepository.js`
- `alert-service/src/utils/alertUtils.js`
- `alert-service/tests/expiringProductsReport.test.js`
- `alert-service/package.json`
- `docs/Changes/HU-012-expiring-products-report.md`

## 11. Criterios de aceptacion cubiertos
1. Existe un endpoint para consultar el reporte consolidado de productos proximos a vencer.
2. El endpoint responde `200 OK` cuando el proceso es correcto.
3. La respuesta incluye `generatedAt`, `total`, `summary` y `reports`.
4. Cada registro incluye `diasRestantes` y `estado`.
5. El campo `estado` clasifica correctamente en `Vencido`, `Critico`, `Medio` y `Controlado`.
6. El endpoint permite filtrar por `expired`, `0-15`, `16-30`, `31-60` y `all`.
7. Solo se consideran productos activos.
8. Los resultados se entregan ordenados por fecha de vencimiento ascendente.
9. El endpoint puede consumirse desde `api-gateway`.
10. Se agregan pruebas automatizadas para validar calculo, clasificacion y filtros.

## 12. Evidencia de validacion
Se agrego prueba automatizada:
- `expiringProductsReport.test.js`

Adicionalmente, el script de test del microservicio fue actualizado para ejecutar tambien esta HU.

## 13. Alcance implementado en esta HU
Esta HU implementa un endpoint nuevo y consolidado:
- `GET /api/alerts/expiring-report`

En este endpoint si se encuentran implementados:
- `diasRestantes`
- `estado`
- filtros por rango:
  - `all`
  - `expired`
  - `0-15`
  - `16-30`
  - `31-60`

Adicionalmente, como ajuste complementario posterior dentro de la misma linea funcional, tambien se actualizaron los endpoints:
- `GET /api/alerts/expired`
- `GET /api/alerts/expiring-soon`
- `GET /api/alerts/expiring-half-month`
- `GET /api/alerts/expiring-month`

para incluir en `product`:
- `expirationDate` normalizado a formato `YYYY-MM-DD`
- `diasRestantes`
- `estado`

## 14. Aclaracion sobre endpoints existentes
Los endpoints individuales conservaron su estructura principal de alertas, pero ahora enriquecen el objeto `product` con campos adicionales para mantener consistencia con el reporte consolidado.

## 15. Ajuste complementario sugerido
Regla de conteo implementada para `diasRestantes`:
- no cuenta el dia actual
- si cuenta el dia de vencimiento

Ejemplo validado:
- si hoy es `2026-04-03` y el producto vence el `2026-04-10`, el resultado es `7`

## 16. Notas de implementacion
- Esta HU complementa las HUs `HU-005` y `HU-008`, reutilizando sus rangos de vencimiento.
- Se recomienda no devolver mensajes tipo alerta en este endpoint, sino una estructura orientada a tabla o reporte.
- Si frontend necesita etiquetas visuales, `estado` puede mapearse directamente a chips o badges.
- Si se desea exportar a Excel en una HU posterior, este mismo contrato puede reutilizarse como fuente de datos.
