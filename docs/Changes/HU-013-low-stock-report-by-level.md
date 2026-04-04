# HU-013 - Reporte de productos con bajo stock por nivel (inventory-service)

## 1. Informacion general
- HU: `HU-013`
- Nombre: Reporte de productos con bajo stock por nivel
- Microservicio: `inventory-service`
- Estado: Propuesto
- Rama de trabajo sugerida: `HU-013-dev`

## 2. Objetivo de la HU
Exponer metodos en `inventory-service` para consultar el reporte de productos con bajo stock en una vista tipo tabla, permitiendo visualizar:
- todos los productos con bajo stock
- solo productos en estado `Critico`
- solo productos en estado `Alerta`

La respuesta debe soportar la estructura esperada por frontend para la vista mostrada en la imagen, incluyendo columnas de codigo, medicamento, stock, minimo, cobertura, estado y sugerencia.

## 3. Necesidad funcional
La interfaz requiere tres filtros visibles:
- `Todos`
- `Critico`
- `Alerta`

El backend debe permitir:
- listar todos los productos que esten por debajo o igual a su stock minimo
- separar los productos segun severidad del bajo stock
- entregar informacion calculada para representar el estado visual en frontend
- sugerir cuantas unidades deben reponerse

## 4. Endpoints funcionales propuestos
### Consumo oficial (gateway)
- Metodo: `GET`
- URL: `http://localhost:8080/api/products/low-stock-report`
- URL: `http://localhost:8080/api/products/low-stock-report/critical`
- URL: `http://localhost:8080/api/products/low-stock-report/alert`

### Endpoint interno del microservicio
- Metodo: `GET`
- URL: `http://localhost:8082/api/products/low-stock-report`
- URL: `http://localhost:8082/api/products/low-stock-report/critical`
- URL: `http://localhost:8082/api/products/low-stock-report/alert`

### Compatibilidad temporal implementada
Para no romper pruebas previas, el endpoint critico actual tambien acepta:
- `http://localhost:8082/api/products/low-stock/critical`

## 5. Parametros de consulta propuestos
Opcionalmente se puede unificar la consulta principal con un parametro:
- `level=all`
- `level=critical`
- `level=alert`

Sin embargo, para alinearse con la necesidad actual del frontend, esta HU propone tres metodos explicitos:
- un metodo para `Todos`
- un metodo para `Critico`
- un metodo para `Alerta`

## 6. Alcance funcional esperado
- Obtener productos activos del inventario con stock comprometido.
- Calcular el porcentaje de cobertura respecto al stock minimo.
- Clasificar cada producto como `Critico` o `Alerta`.
- Generar una sugerencia de reposicion por producto.
- Permitir que el frontend consuma:
  - el consolidado general
  - el subconjunto critico
  - el subconjunto en alerta

## 7. Reglas de negocio propuestas
Se consideran productos de bajo stock cuando:
- `stock <= minimumStock`
- `active = true`

Clasificacion sugerida:
- `Critico`: cuando `stock <= minimumStock * 0.50`
- `Alerta`: cuando `stock > minimumStock * 0.50` y `stock <= minimumStock`

Calculos esperados:
- `cobertura = (stock / minimumStock) * 100`
- `cobertura` debe redondearse a entero para la vista
- `faltanteMinimo = minimumStock - stock`
- `sugerenciaReposicion = minimumStock + faltanteMinimo`

Texto de sugerencia esperado:
- `Reponer {sugerenciaReposicion} unidades`

Ejemplos:
- si `stock = 5` y `minimumStock = 10`, entonces:
  - `cobertura = 50%`
  - `estado = Critico`
  - `sugerencia = Reponer 15 unidades`
- si `stock = 19` y `minimumStock = 20`, entonces:
  - `cobertura = 95%`
  - `estado = Alerta`
  - `sugerencia = Reponer 21 unidades`

## 8. Orden esperado del reporte
- `estado ASC` priorizando `Critico` antes de `Alerta` en el consolidado
- `cobertura ASC`
- `name ASC`

## 9. Contrato de respuesta esperado
```json
{
  "generatedAt": "2026-04-03T21:30:00.000Z",
  "total": 2,
  "filters": {
    "appliedLevel": "all",
    "availableLevels": ["all", "critical", "alert"]
  },
  "summary": {
    "critical": 1,
    "alert": 1
  },
  "reports": [
    {
      "id": "1",
      "code": "AMX-001",
      "name": "Amoxicillin 500mg",
      "stock": 5,
      "minimumStock": 10,
      "coverage": 50,
      "coverageLabel": "50%",
      "status": "Critico",
      "suggestion": "Reponer 15 unidades",
      "active": true
    },
    {
      "id": "2",
      "code": "ACM-001",
      "name": "Acetaminophen 500mg",
      "stock": 19,
      "minimumStock": 20,
      "coverage": 95,
      "coverageLabel": "95%",
      "status": "Alerta",
      "suggestion": "Reponer 21 unidades",
      "active": true
    }
  ]
}
```

## 10. Implementacion tecnica propuesta
Se recomienda mantener la arquitectura actual:
- `router -> controller -> service -> repository`

Componentes a crear o ajustar:
- endpoint nuevo para reporte consolidado de bajo stock
- endpoint nuevo para bajo stock critico
- endpoint nuevo para bajo stock en alerta
- service para armar el payload y los calculos de cobertura, estado y sugerencia
- repository con consultas filtradas por stock y severidad
- pruebas unitarias y de integracion para filtros y reglas de clasificacion

## 11. Archivos estimados a modificar
- `inventory-service/src/main/java/.../controller/...`
- `inventory-service/src/main/java/.../service/...`
- `inventory-service/src/main/java/.../repository/...`
- `inventory-service/src/main/java/.../dto/...`
- `inventory-service/src/test/java/...`
- `docs/Changes/HU-013-low-stock-report-by-level.md`

## 12. Criterios de aceptacion propuestos
1. Existe un metodo para consultar todos los productos con bajo stock.
2. Existe un metodo para consultar solo los productos en estado `Critico`.
3. Existe un metodo para consultar solo los productos en estado `Alerta`.
4. El reporte incluye `code`, `name`, `stock`, `minimumStock`, `coverage`, `status` y `suggestion`.
5. El sistema clasifica correctamente los productos entre `Critico` y `Alerta`.
6. La respuesta del consolidado incluye un resumen con totales por estado.
7. Solo se listan productos activos.
8. Los productos se ordenan priorizando los de mayor urgencia.
9. Los endpoints pueden consumirse desde `api-gateway`.
10. Se agregan pruebas automatizadas para validar filtros, calculos y clasificacion.

## 13. Notas de implementacion
- Esta HU esta enfocada en reportes de inventario y debe implementarse en `inventory-service`.
- El campo `coverageLabel` facilita el consumo directo en frontend.
- El campo `status` puede mapearse directamente a chips visuales como en la interfaz.
- La formula de sugerencia se basa en el comportamiento observado en la imagen compartida.
- Si mas adelante se desea evitar tres endpoints, puede mantenerse uno solo con parametro `level`, pero esta HU conserva ambos enfoques como opcion.

## 14. Resultado esperado en frontend
La vista debera poder renderizar tres pestanas:
- `Todos`: consulta el consolidado completo
- `Critico`: consulta solo productos criticos
- `Alerta`: consulta solo productos en alerta

Cada fila debera mostrar:
- codigo del producto
- nombre del medicamento
- stock actual
- stock minimo
- cobertura porcentual
- estado visual
- sugerencia de reposicion
