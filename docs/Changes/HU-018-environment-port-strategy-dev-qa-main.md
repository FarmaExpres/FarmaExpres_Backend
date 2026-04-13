# HU-018 - Estrategia de puertos por ambiente para dev, qa y main

## 1. Informacion general
- HU: `HU-018`
- Nombre: Definir estrategia de puertos por ambiente para backend, frontend y base de datos
- Componente principal: `infraestructura`
- Estado: Propuesta funcional y tecnica documentada
- Rama de trabajo sugerida: `HU-018-dev`

## 2. Objetivo de la HU
Documentar una estrategia clara para asignar puertos diferentes por ambiente `dev`, `qa` y `main` a los componentes del proyecto `FarmaExpres`.

La intencion es que cada ambiente tenga su propia configuracion de puertos sin necesidad de reescribir el codigo fuente de cada microservicio.

## 3. Justificacion funcional
El proyecto requiere diferenciar ambientes de desarrollo, pruebas y despliegue principal para evitar conflictos de puertos y permitir una ejecucion ordenada de los servicios.

Esto es importante porque:
- varios servicios conviven dentro del mismo ecosistema
- cada ambiente debe poder identificarse con claridad
- se evita que `dev`, `qa` y `main` compitan por el mismo puerto
- se facilita la validacion de despliegues por ambiente
- se mejora la trazabilidad operativa del proyecto

## 4. Problema que resuelve la HU
Si todos los ambientes usan los mismos puertos:
- no se pueden levantar facilmente en un mismo equipo
- se producen conflictos al ejecutar contenedores o aplicaciones locales
- se dificulta distinguir a que ambiente pertenece cada servicio
- se complica la validacion funcional y tecnica antes de pasar a produccion

Esta HU resuelve ese problema definiendo una matriz de puertos por ambiente y una forma estandar de parametrizarlos.

## 5. Historia de usuario
Como equipo de desarrollo de `FarmaExpres`,  
queremos definir puertos independientes para `dev`, `qa` y `main` en backend, frontend y base de datos,  
para poder ejecutar y validar cada ambiente de forma ordenada, sin conflictos y con una configuracion consistente.

## 6. Alcance funcional propuesto
Esta HU cubre la documentacion y definicion de:
- puertos por ambiente para cada microservicio backend
- puertos por ambiente para frontend
- puertos por ambiente para base de datos
- estrategia de uso con `docker`, variables de entorno y archivos `.env`

No implica en esta HU hacer todos los cambios tecnicos de implementacion.

## 7. Regla principal de implementacion
La estrategia recomendada no consiste en hacer que un mismo servicio escuche tres puertos al mismo tiempo.

La regla correcta es:
- `dev` usa un puerto asignado
- `qa` usa otro puerto asignado
- `main` usa otro puerto asignado

Cada ambiente expone su propio puerto, mientras que el puerto interno del servicio puede mantenerse estable.

## 8. Estrategia tecnica recomendada
La implementacion recomendada es parametrizar los puertos mediante variables de entorno.

Ejemplo conceptual:
- el contenedor mantiene su puerto interno
- `docker-compose` publica un puerto externo distinto segun el ambiente
- cada ambiente usa su propio archivo `.env`

Esto permite:
- reutilizar la misma configuracion base
- cambiar solo valores de entorno
- evitar duplicacion innecesaria de archivos

## 9. Matriz de puertos propuesta

### Backend
- `api-gateway`
  - `dev`: `8080`
  - `qa`: `9080`
  - `main`: `10080`

- `auth-service`
  - `dev`: `8081`
  - `qa`: `9081`
  - `main`: `10081`

- `inventory-service`
  - `dev`: `8082`
  - `qa`: `9082`
  - `main`: `10082`

- `alert-service`
  - `dev`: `8083`
  - `qa`: `9083`
  - `main`: `10083`

### Frontend
- `frontend`
  - `dev`: `3000`
  - `qa`: `4000`
  - `main`: `5000`

### Base de datos
- `postgres`
  - `dev`: `5433`
  - `qa`: `6433`
  - `main`: `7433`

## 10. Forma recomendada de configuracion
La estrategia recomendada es trabajar con archivos de variables por ambiente:
- `.env.dev`
- `.env.qa`
- `.env.main`

Cada archivo define los puertos publicados de cada servicio.

Ejemplo conceptual:

```env
GATEWAY_PORT=8080
AUTH_PORT=8081
INVENTORY_PORT=8082
ALERT_PORT=8083
FRONTEND_PORT=3000
POSTGRES_PORT=5433
```

Y para otro ambiente:

```env
GATEWAY_PORT=9080
AUTH_PORT=9081
INVENTORY_PORT=9082
ALERT_PORT=9083
FRONTEND_PORT=4000
POSTGRES_PORT=6433
```

## 11. Recomendacion de despliegue
El valor del puerto debe cambiar por ambiente en el punto de exposicion, especialmente en `docker-compose`.

Recomendacion:
- mantener el puerto interno del servicio
- variar solo el puerto externo publicado

Ejemplo conceptual:
- `8081:8081` para `dev`
- `9081:8081` para `qa`
- `10081:8081` para `main`

Esto simplifica la configuracion interna del microservicio y concentra el cambio en infraestructura.

## 12. Beneficios esperados
Implementar esta estrategia aporta:
- separacion clara entre ambientes
- menos conflictos de puertos
- mayor facilidad para pruebas
- mejor organizacion operativa
- despliegues mas predecibles
- menor probabilidad de errores manuales al cambiar de ambiente

## 13. Fuera de alcance de esta HU
No hace parte de esta HU:
- cambiar inmediatamente todos los archivos de configuracion
- modificar de forma completa el frontend
- desplegar los tres ambientes al mismo tiempo en infraestructura real

La HU documenta la estrategia y deja lista la base para implementarla.

## 14. Criterios de aceptacion propuestos
1. Debe existir una historia de usuario que documente la estrategia de puertos por ambiente.
2. La HU debe diferenciar claramente los ambientes `dev`, `qa` y `main`.
3. La HU debe incluir puertos propuestos para backend, frontend y base de datos.
4. La HU debe indicar que la parametrizacion debe hacerse por variables de entorno.
5. La HU debe dejar claro que no es necesario que un servicio escuche tres puertos al mismo tiempo.
6. La HU debe recomendar el uso de archivos `.env` por ambiente.

## 15. Resultado esperado
Al aprobar esta HU, el equipo contara con una guia clara para implementar una configuracion de puertos por ambiente en `FarmaExpres`, manteniendo orden, consistencia y facilidad de despliegue para backend, frontend y base de datos.
