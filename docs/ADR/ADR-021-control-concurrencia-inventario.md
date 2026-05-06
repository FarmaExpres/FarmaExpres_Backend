# ADR-021: Control de concurrencia para movimientos de inventario

**Fecha:** 2026-05-05  
**Estado:** Propuesto  
**Autor:** MDRT  
**Proyecto:** FarmaExpres  
**Componente principal:** `inventory-service`

---

## 1. Contexto

`inventory-service` concentra reglas importantes del dominio: medicamentos, lotes, movimientos,
FEFO, reportes y sincronizacion de stock. Esa concentracion permite avanzar rapido, pero tambien
incrementa el riesgo cuando dos usuarios registran salidas o ajustes sobre el mismo medicamento al
mismo tiempo.

La observacion arquitectonica recibida indica que el servicio tiene demasiada responsabilidad y que
faltaban garantias mas claras para la concurrencia de inventario. Por eso esta decision separa una
parte critica de la responsabilidad: antes de descontar o ajustar unidades, el sistema debe tomar un
bloqueo de escritura sobre el producto y los lotes que se van a consumir.

## 2. Problema

Sin una politica explicita de concurrencia, dos transacciones podrian leer el mismo stock disponible
y aplicar descuentos incompatibles. El resultado posible seria:

- stock disponible menor al esperado;
- doble consumo del mismo lote;
- reportes con inventario operativo inconsistente;
- fallos tardios por validaciones de base de datos;
- dificultad para probar el comportamiento de salidas concurrentes.

## 3. Decision

Se adopta bloqueo pesimista de escritura para operaciones que modifican inventario.

La regla queda encapsulada en `InventoryConcurrencyGuard`, un servicio de dominio pequeno que:

- bloquea el producto antes de modificar stock;
- rechaza productos inactivos despues de tomar la fila bloqueada;
- bloquea el lote especifico cuando el movimiento apunta a un `batchId`;
- bloquea los lotes consumibles antes de aplicar salidas FEFO o salidas por orden de registro.

Los repositorios exponen metodos `ForUpdate` con `@Lock(LockModeType.PESSIMISTIC_WRITE)`.
`MotionService` conserva la orquestacion del movimiento, pero ya no es el lugar donde vive la
politica de bloqueo.

## 4. Alternativas evaluadas

| Alternativa | Resultado | Motivo |
|-------------|-----------|--------|
| Mantener solo validaciones en memoria | Rechazada | No evita lecturas simultaneas del mismo stock. |
| Usar bloqueo optimista con `@Version` | Pospuesta | Requiere migracion de columnas de version y manejo de reintentos. |
| Bloquear filas con `PESSIMISTIC_WRITE` | Aceptada | Es directa, compatible con JPA y protege las salidas criticas. |
| Mover toda la logica a procedimientos SQL | Rechazada | Aumenta acoplamiento a la base y dificulta pruebas unitarias. |

## 5. Diseno aplicado

```text
Solicitud de salida
  |
  v
MotionService
  |
  v
InventoryConcurrencyGuard
  |-- bloquea Product por id
  |-- bloquea Batch o lista de Batch consumibles
  v
MotionService aplica descuento y registra Motion
  |
  v
BatchService sincroniza stock agregado del Product
```

## 6. Codigo modificado

| Archivo | Cambio |
|---------|--------|
| `ProductRepository.java` | Agrega `findByIdForUpdate` con bloqueo pesimista. |
| `BatchRepository.java` | Agrega consultas `ForUpdate` para lote directo y lotes consumibles. |
| `InventoryConcurrencyGuard.java` | Nuevo servicio que centraliza la politica de bloqueo. |
| `MotionService.java` | Usa el guard antes de entradas, salidas, FEFO y movimientos con lote. |
| `MotionServiceTest.java` | Ajusta pruebas para validar uso del guard. |
| `InventoryConcurrencyGuardTest.java` | Agrega pruebas unitarias de la politica de bloqueo. |
| `src/test/resources/application.yaml` | Configura H2 para que las pruebas de contexto no dependan de Docker. |
| `pom.xml` | Agrega H2 como dependencia de pruebas. |

## 7. Consecuencias

### Positivas

- Reduce el riesgo de doble consumo de lotes.
- Hace visible la decision arquitectonica en codigo.
- Saca una responsabilidad transversal de `MotionService`.
- Permite probar la regla sin levantar toda la aplicacion.
- Mejora la base para futuras pruebas de integracion con concurrencia real.

### Negativas

- Las salidas sobre el mismo producto se serializan.
- Una transaccion lenta puede hacer esperar a otra que use el mismo producto.
- Si en el futuro se necesita mayor rendimiento, podria requerirse bloqueo optimista con reintentos.

## 8. Riesgos y mitigacion

| Riesgo | Impacto | Mitigacion |
|--------|---------|------------|
| Mayor espera en productos con alta demanda | Medio | Mantener transacciones cortas y bloquear solo filas necesarias. |
| Consultas sin bloqueo nuevas en el futuro | Medio | Reusar `InventoryConcurrencyGuard` para toda mutacion de stock. |
| Falta de pruebas concurrentes reales | Medio | Agregar prueba de integracion con PostgreSQL/Testcontainers en una HU posterior. |

## 9. Criterios de aceptacion

1. Las salidas de inventario deben bloquear el producto antes de consultar lotes consumibles.
2. Las salidas FEFO deben usar una consulta bloqueante sobre lotes disponibles.
3. Los movimientos directos por `batchId` deben bloquear ese lote antes de descontar o sumar.
4. Las pruebas unitarias deben demostrar que la politica de bloqueo se invoca.
5. Las pruebas de contexto de `inventory-service` deben poder cargar con perfil de test local.

## 10. Relacion con Kubernetes

Esta decision ayuda al despliegue en Kubernetes porque varias replicas de `inventory-service` podrian
atender solicitudes al mismo tiempo. El bloqueo se apoya en la base de datos, por lo que la garantia
no depende de que exista una sola replica del microservicio.

---

*Documento preparado para la HU-MDRT-001 de FarmaExpres.*
