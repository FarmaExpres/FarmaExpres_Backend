# HU-MDRT-001 - Control de concurrencia en movimientos de inventario

## 1. Informacion general

- HU: `HU-MDRT-001`
- Nombre: Control de concurrencia para salidas y ajustes de inventario
- Componente principal: `inventory-service`
- Componentes relacionados: `database`, `frontend` como consumidor de stock, documentacion ADR
- Rama de trabajo: `HU-MDRT-001`
- Estado: En desarrollo

## 2. Objetivo

Fortalecer la arquitectura de `inventory-service` para que las operaciones que modifican stock tengan
una regla clara de concurrencia. La mejora responde a la observacion de que el servicio maneja mucha
logica de dominio y necesitaba garantias explicitas para evitar inconsistencias cuando hay movimientos
simultaneos.

## 3. Historia de usuario

Como equipo de FarmaExpres,
queremos que las salidas y ajustes de inventario bloqueen el producto y los lotes involucrados,
para evitar doble consumo de stock cuando varios usuarios o replicas del servicio procesan movimientos
al mismo tiempo.

## 4. Justificacion arquitectonica

El inventario es una zona critica del sistema. Si dos solicitudes leen el mismo lote disponible antes
de que una de ellas confirme el descuento, el sistema puede registrar movimientos que no reflejan la
cantidad real. Esa situacion se vuelve mas probable al desplegar el backend con varias replicas en
Kubernetes.

La solucion no consiste en ocultar el problema con validaciones adicionales en memoria. La decision es
llevar la garantia al nivel transaccional usando bloqueo pesimista en la base de datos.

## 5. Alcance implementado

- Nuevo servicio `InventoryConcurrencyGuard`.
- Bloqueo de producto con `ProductRepository.findByIdForUpdate`.
- Bloqueo de lote especifico con `BatchRepository.findByIdAndProductIdForUpdate`.
- Bloqueo de lotes consumibles para salidas por orden de registro.
- Bloqueo de lotes consumibles para salidas FEFO.
- Ajuste de `MotionService` para usar el guard antes de mutar inventario.
- Pruebas unitarias para el guard y actualizacion de pruebas de movimientos.
- Configuracion de pruebas con H2 para que `inventory-service` no dependa de `DB_URL` durante `mvn test`.

## 6. Cambios de codigo

```text
inventory-service/src/main/java/.../Repository/ProductRepository.java
inventory-service/src/main/java/.../Repository/BatchRepository.java
inventory-service/src/main/java/.../Service/InventoryConcurrencyGuard.java
inventory-service/src/main/java/.../Service/MotionService.java
inventory-service/src/test/java/.../Service/InventoryConcurrencyGuardTest.java
inventory-service/src/test/java/.../Service/MotionServiceTest.java
inventory-service/src/test/resources/application.yaml
inventory-service/pom.xml
auth-service/src/main/java/.../exception/GlobalExceptionHandler.java
docs/ADR/ADR-021-control-concurrencia-inventario.md
```

## 7. Estado anterior

```text
MotionService
  |
  |-- consulta producto
  |-- consulta lotes disponibles
  |-- calcula descuento
  |-- guarda lote y movimiento
```

El flujo funcionaba para uso normal, pero no dejaba expresada una proteccion clara frente a solicitudes
simultaneas sobre el mismo producto.

## 8. Estado objetivo

```text
MotionService
  |
  |-- InventoryConcurrencyGuard bloquea producto/lotes
  |-- calcula descuento dentro de la misma transaccion
  |-- guarda lote y movimiento
  |-- sincroniza stock agregado
```

La regla de concurrencia queda nombrada, probada y reutilizable.

## 9. Relacion con frontend

Se reviso `FarmaExpres-Frontend` en `origin/Develop`, incluyendo la HU de NTM sobre control de stock.
La HU se dividio en dos partes:

- Backend: garantiza consistencia transaccional con bloqueos pesimistas en `inventory-service`.
- Frontend: reconoce conflictos `409` y `422` de inventario, muestra mensajes especificos y recarga
  entradas/salidas para evitar que el usuario continue con datos desactualizados.

La trazabilidad frontend quedo documentada en:
`doc/changes/MDRT/HU-MDRT-001-control-concurrencia-inventario-frontend.md`.

Adicionalmente, se ajusto el manejo de errores en `auth-service` para que solicitudes con JSON invalido
respondan `400 BAD_REQUEST` y los errores no controlados queden registrados en logs. Este ajuste ayudo a
validar la aplicacion completa con los usuarios semilla de la guia de inicializacion.

## 10. Criterios de aceptacion

1. Toda salida de inventario debe bloquear el producto antes de leer lotes consumibles.
2. Toda salida FEFO debe usar lotes bloqueados antes de descontar unidades.
3. Los movimientos con `batchId` deben bloquear ese lote antes de aplicar el cambio.
4. Si el producto esta inactivo, la operacion debe rechazarse con `409 CONFLICT`.
5. Las pruebas unitarias deben validar que el guard usa repositorios bloqueantes.
6. `mvn test` en `inventory-service` debe tener configuracion local de test.

## 11. Pruebas propuestas y ejecutadas

```bash
cd inventory-service
mvn test
```

Casos cubiertos por la HU:

- bloqueo de producto operable;
- rechazo de producto inactivo;
- bloqueo de lote para movimiento directo;
- bloqueo de lotes FEFO;
- salidas de inventario usando el guard;
- contexto Spring con base H2 de pruebas.

## 12. Riesgos

| Riesgo | Mitigacion |
|--------|------------|
| Esperas en productos muy consultados | Mantener transacciones cortas y bloquear por producto/lote, no tablas completas. |
| Nuevos flujos de stock sin guard | Documentar ADR-021 y reutilizar `InventoryConcurrencyGuard`. |
| Falta de prueba concurrente real con PostgreSQL | Dejar como mejora posterior con Testcontainers. |

## 13. Resultado esperado

La HU deja evidencia tecnica y documental de una mejora arquitectonica real sobre `inventory-service`.
El servicio sigue teniendo el mismo contrato HTTP, pero ahora las mutaciones de stock tienen una
frontera transaccional mas clara y preparada para correr con varias replicas.
