# HU-AC02 - Exponer los servicios backend mediante un API Gateway centralizado

## 1. Informacion general
- HU: `HU-AC02`
- Nombre: Exponer los servicios backend mediante un API Gateway centralizado
- Microservicio principal: `api-gateway`
- Servicios impactados: `auth-service`, `inventory-service`, `alert-service`
- Estado: Implementado
- Rama de trabajo: `HU-AC02-dev`

## 2. Objetivo de la HU
Habilitar el `api-gateway` como punto unico de acceso para el frontend, de forma que las solicitudes a los microservicios del backend se consuman a traves de una unica puerta de entrada.

## 3. Justificacion funcional
Actualmente los microservicios exponen endpoints por puertos independientes, lo que obliga al frontend a conocer multiples direcciones internas del backend.

Con esta HU se busca centralizar el acceso en el `api-gateway` para:
- simplificar la integracion del frontend
- desacoplar al frontend de los puertos internos de cada microservicio
- facilitar la evolucion futura de rutas, seguridad y politicas transversales
- dejar una arquitectura mas alineada con el enfoque de microservicios

## 4. Alcance funcional esperado
- Definir al `api-gateway` como canal oficial de salida del backend hacia el frontend.
- Exponer por gateway las rutas necesarias de autenticacion, usuarios, inventario, movimientos y alertas.
- Mantener rutas consistentes bajo un prefijo comun consumible desde frontend.
- Validar que las rutas del gateway redirijan correctamente a cada microservicio interno.
- Dejar documentado el consumo oficial de endpoints desde el puerto `8080`.

## 5. Alcance tecnico esperado
- Revisar y ajustar configuracion de rutas en `api-gateway`.
- Confirmar integracion de rutas hacia:
  - `auth-service`
  - `inventory-service`
  - `alert-service`
- Verificar compatibilidad entre rutas internas y rutas expuestas al frontend.
- Mantener `api-gateway` como unico punto publico de acceso HTTP para consumo del cliente.
- Validar disponibilidad del gateway y respuesta correcta hacia servicios dependientes.

## 6. Alcance no incluido en esta HU
- Reescritura completa de contratos funcionales de cada microservicio.
- Cambios profundos de logica de negocio en `auth-service`, `inventory-service` o `alert-service`.
- Implementacion de balanceo de carga o descubrimiento de servicios.
- Incorporacion de rate limiting, circuit breakers o cache distribuido.
- Refactor completo del frontend.

## 7. Endpoints de salida esperados por gateway
### Acceso oficial para frontend
- Base URL esperada: `http://localhost:8080`

### Rutas a exponer o consolidar
- `POST /api/auth/login`
- `GET /api/users`
- `POST /api/users`
- `PUT /api/users/{id}/update`
- `PUT /api/users/{id}/password`
- `PUT /api/users/{id}/block`
- `PUT /api/users/{id}/unlock`
- `DELETE /api/users/{id}`
- `GET /api/binnacle`
- `GET /api/products`
- `POST /api/products`
- `PUT /api/products/{id}`
- `DELETE /api/products/{id}`
- `GET /api/products/out-of-stock`
- `GET /api/movements`
- `GET /api/movements/entrance`
- `GET /api/movements/exit`
- `GET /api/movements/updated`
- `GET /api/alerts/low-stock`
- `GET /api/alerts/expired`
- `GET /api/alerts/out-of-stock`
- `GET /api/alerts/expiring-soon`
- `GET /api/alerts`

## 8. Resultado esperado para arquitectura
El frontend no debera consumir directamente:
- `http://localhost:8081`
- `http://localhost:8082`
- `http://localhost:8083`

En su lugar, debera consumir unicamente:
- `http://localhost:8080`

## 9. Archivos que probablemente seran impactados
- `api-gateway/src/main/java/co/edu/corhuila/api_gateway/Config/SecurityConfig.java`
- `api-gateway/src/main/resources/application.yaml`
- `api-gateway/src/main/java/co/edu/corhuila/api_gateway/StatusController.java`
- `docker-compose.yml`
- `docs/Guia_Inicializacion_Backend_Frontend.md`

## 10. Criterios de aceptacion propuestos
1. El frontend debe poder consumir los servicios backend usando unicamente el puerto `8080`.
2. El `api-gateway` debe enrutar correctamente solicitudes hacia `auth-service`, `inventory-service` y `alert-service`.
3. Las rutas publicadas por gateway deben responder con el mismo comportamiento funcional esperado de cada microservicio.
4. El acceso por gateway no debe requerir que el frontend conozca los puertos internos `8081`, `8082` o `8083`.
5. Debe existir documentacion clara del conjunto de rutas oficiales expuestas por `api-gateway`.
6. El gateway debe responder correctamente a su endpoint de salud.
7. Las rutas de alertas ya implementadas deben quedar disponibles tambien a traves del gateway.

## 11. Riesgos o puntos a validar
1. Confirmar si todas las rutas actuales del frontend ya coinciden con las expuestas en `api-gateway`.
2. Validar si existen endpoints adicionales en `auth-service` o `inventory-service` que aun no esten publicados en gateway.
3. Confirmar si la seguridad JWT se validara solo en microservicios o tambien en el gateway.
4. Verificar manejo de CORS, encabezados y errores propagados al frontend.
5. Validar si se requiere conservar compatibilidad temporal con accesos directos por puerto interno durante la transicion.

## 12. Definicion de terminado
La HU se considerara terminada cuando:
- el `api-gateway` sea el punto oficial de acceso del frontend
- las rutas requeridas esten publicadas y funcionales en `:8080`
- el frontend ya no necesite consumir directamente puertos internos de microservicios
- la documentacion tecnica refleje el nuevo flujo de comunicacion

## 13. Evidencia esperada
- Pruebas de consumo exitoso desde `http://localhost:8080`
- Validacion de rutas hacia autenticacion, inventario y alertas
- Actualizacion de documentacion de integracion backend-frontend

## 14. Cambios implementados
- Se configuro `api-gateway` para exponer como punto oficial de acceso las rutas `/api/auth/**`, `/api/users/**`, `/api/binnacle/**`, `/api/products/**`, `/api/movements/**` y `/api/alerts/**`.
- Se agrego configuracion de seguridad WebFlux para permitir el trafico del frontend hacia `/api/**`, `/status` y endpoints de salud.
- Se agrego configuracion global de CORS para consumo desde entornos locales de frontend.
- Se mejoro el endpoint `GET /status` del gateway para responder con `status`, `service` y `timestamp`.
- Se actualizo la guia de inicializacion para indicar que el frontend debe consumir unicamente `http://localhost:8080`.
- Se agrego prueba automatizada para validar el contrato del endpoint `/status`.
- Se confirmo que la ruta del gateway `/api/movements/**` cubre tambien los nuevos endpoints:
  - `/api/movements/entrance`
  - `/api/movements/exit`
  - `/api/movements/updated`

## 15. Archivos modificados
- `api-gateway/src/main/java/co/edu/corhuila/api_gateway/Config/SecurityConfig.java`
- `api-gateway/src/main/java/co/edu/corhuila/api_gateway/StatusController.java`
- `api-gateway/src/main/resources/application.yaml`
- `api-gateway/src/test/java/co/edu/corhuila/api_gateway/StatusControllerTest.java`
- `docs/Guia_Inicializacion_Backend_Frontend.md`

## 16. Actualizacion HU-ACFE-03 (2026-04-04)

Con la alineacion FE/BE por lotes, el gateway tambien debe considerar como rutas oficiales adicionales:

### Alertas y reportes batch-aware
- `GET /api/alerts/expired-batches`
- `GET /api/alerts/expiring-batches`
- `GET /api/alerts/expiring-batches/report`
- `GET /api/alerts/low-stock-batches`
- `GET /api/alerts/low-stock-batches/critical`
- `GET /api/alerts/low-stock-batches/alert`
- `GET /api/alerts/out-of-stock-batches`
- `GET /api/reports/alerts-batches`

### Inventario / reportes batch
- `GET /api/reports/inventory-batches`
- `GET /api/reports/movements-batches`
- `GET /api/products/fefo-snapshot`

Nota de compatibilidad:
- endpoints legacy (`/api/alerts/expired`, `/api/alerts/expiring-soon`, etc.) pueden seguir temporalmente,
  pero las nuevas integraciones deben priorizar contratos batch-aware.
