# HU-017 - Diseno y versionado de la base de datos de FarmaExpres

## 1. Informacion general
- HU: `HU-017`
- Nombre: Diseno y versionado de la base de datos de FarmaExpres
- Componente principal: `database`
- Estado: Propuesta funcional para implementacion de base de datos
- Rama de trabajo sugerida: `HU-017-dev`

## 2. Objetivo de la HU
Definir e implementar una base de datos versionada para `FarmaExpres`, tomando como referencia la arquitectura del repositorio `shopping-cart-db`, con el fin de contar con una estructura organizada, auditable y preparada para evolucionar junto con los microservicios del proyecto.

La propuesta busca que la base de datos deje de depender de un unico script inicial y pase a un modelo gobernado por cambios versionados, rollback controlado y separacion por responsabilidades SQL.

## 3. Justificacion funcional
Actualmente el proyecto necesita una base de datos que soporte de forma clara y mantenible los dominios funcionales ya presentes en el backend, especialmente:
- autenticacion y usuarios
- inventario de medicamentos
- lotes
- movimientos de inventario
- trazabilidad operativa
- futuras capacidades de ventas, pedidos, alertas y auditoria

Tomar como referencia `shopping-cart-db` si aporta valor porque muestra una forma ordenada de construir una base de datos profesional:
- separa cambios estructurales de cambios de datos
- organiza la seguridad y los permisos como una capa independiente
- deja preparados scripts de rollback
- permite crecer sin desordenar el repositorio
- facilita la auditoria del historial de cambios

Esto es importante para `FarmaExpres` porque el sistema ya no es un ejercicio pequeno con una sola tabla o un solo servicio.  
El proyecto necesita una base de datos que pueda evolucionar con varios modulos y con cambios sucesivos sin perder trazabilidad.

## 4. Problema que resuelve la HU
Si la base de datos se mantiene solo con scripts manuales o inicializaciones aisladas, aparecen varios riesgos:
- dificultad para reconstruir ambientes de desarrollo y pruebas
- poca trazabilidad sobre que cambio se aplico y cuando
- mayor probabilidad de inconsistencias entre integrantes del equipo
- dificultad para revertir cambios estructurales
- crecimiento desordenado de tablas, relaciones, constraints y datos iniciales

Esta HU resuelve ese problema proponiendo una base de datos gobernada como artefacto de software y no como un conjunto disperso de scripts.

## 5. Justificacion tecnica
La referencia `shopping-cart-db` propone una organizacion por capas SQL:
- `01_ddl` para estructura
- `02_dml` para datos
- `03_dcl` para permisos y seguridad
- `04_tcl` para operaciones transaccionales excepcionales
- `05_rollbacks` para reversas controladas

Este enfoque es adecuado para `FarmaExpres` porque:
- permite modularizar el crecimiento del esquema
- evita mezclar tablas, datos y permisos en un mismo archivo
- facilita el despliegue automatizado
- mejora la comprension del proyecto para nuevos integrantes
- deja la puerta abierta para ambientes locales, pruebas y produccion con el mismo contrato de despliegue

Ademas, herramientas como `Liquibase` y `PostgreSQL` encajan bien con la arquitectura actual del backend y con la necesidad de versionar los cambios de base de datos como parte del repositorio.

## 6. Necesidad del proyecto observada
En `FarmaExpres_Backend` ya existe evidencia de que el dominio requiere una base de datos mas robusta que el script actual:
- el `inventory-service` maneja productos, movimientos y lotes
- el modulo de autenticacion requiere usuarios, roles y trazabilidad
- el sistema maneja estados, validaciones y reglas de negocio que deben apoyarse en el modelo fisico
- el proyecto usa varios microservicios, por lo que conviene separar responsabilidades tambien a nivel de datos

Por eso no basta con mantener `init.sql` como unica fuente de verdad.  
Se necesita una estructura versionada que permita evolucionar el modelo sin perder control.

## 7. Historia de usuario
Como equipo de desarrollo de `FarmaExpres`,  
queremos definir y versionar la base de datos del proyecto con una arquitectura similar a `shopping-cart-db`,  
para garantizar orden, trazabilidad, mantenibilidad y despliegue consistente del modelo de datos.

## 8. Alcance funcional propuesto
Esta HU propone como alcance inicial:
- crear un repositorio o modulo de base de datos versionada para `FarmaExpres`
- definir un `changelog-master` como punto de entrada
- organizar la estructura por capas SQL
- modelar los esquemas y tablas base del proyecto
- registrar rollbacks de los cambios estructurales principales
- dejar preparado el uso con `Docker`, `PostgreSQL` y `Liquibase`

La HU se enfoca en justificar y gobernar la construccion de la base de datos del proyecto, no solo en crear tablas aisladas.

## 9. Modelo funcional esperado
La base de datos de `FarmaExpres` debe quedar preparada para representar al menos estos dominios:
- `security` o modulo equivalente para usuarios, roles y bitacora
- `inventory` para productos, lotes y movimientos
- estructuras futuras para ventas, pedidos, alertas o facturacion si el proyecto las incorpora

Cada cambio relevante del modelo debe quedar:
- identificado por `changeSet`
- documentado
- versionado
- desplegable de forma repetible
- reversible cuando aplique

## 10. Beneficios esperados
Implementar esta HU aporta beneficios concretos:
- reconstruccion confiable de ambientes
- trazabilidad completa del modelo de datos
- facilidad para revisar cambios entre entregas
- menor riesgo de errores manuales
- base mas preparada para crecer por iteraciones
- mejor soporte para trabajo colaborativo entre backend y base de datos

## 11. Alcance tecnico sugerido
La implementacion futura de esta HU deberia contemplar:
- estructura raiz de changelogs
- definicion de esquemas principales del proyecto
- migraciones para tablas base
- archivos de rollback por cada cambio relevante
- configuracion de `docker-compose` para PostgreSQL y tooling de Liquibase
- documentacion de arquitectura de capas

## 12. Fuera de alcance de esta HU
No hace parte de esta HU:
- implementar en este momento todos los endpoints del backend
- migrar inmediatamente toda la logica de los microservicios
- poblar datos finales de negocio completos
- redefinir todo el dominio funcional del proyecto

Esta HU primero justifica y formaliza la necesidad de construir la base de datos con una arquitectura versionada.

## 13. Criterios de aceptacion propuestos
1. Debe existir una historia de usuario que justifique formalmente la construccion de la base de datos de `FarmaExpres`.
2. La justificacion debe tomar como referencia la arquitectura observada en `shopping-cart-db`.
3. La HU debe explicar por que el proyecto necesita una base de datos versionada y no solo scripts sueltos.
4. La HU debe proponer una organizacion por capas para los cambios SQL.
5. La HU debe identificar los dominios principales que la base de datos debe soportar.
6. La HU debe dejar claro que el objetivo es construir una base de datos mantenible, auditable y preparada para crecer.

## 14. Resultado esperado de negocio
Al aprobar esta HU, el equipo contara con una justificacion clara para iniciar la construccion formal de la base de datos del proyecto bajo una estrategia de versionado, despliegue controlado y crecimiento ordenado.

Esto servira como base para las siguientes tareas tecnicas:
- definicion del repositorio o modulo de base de datos
- modelado inicial de esquemas y tablas
- implementacion de migraciones
- integracion con el backend existente
