# HU-JVL-002 - Separacion logica de base de datos por microservicio

## 1. Informacion general
- HU: `HU-JVL-002`
- Nombre: Separar logicamente la base de datos por microservicio usando schemas y usuarios tecnicos
- Componente principal: `database`
- Componentes relacionados: `auth-service`, `inventory-service`, `docker-compose`, archivos `.env`
- Estado: implementado
- Rama de trabajo sugerida: `HU-JVL-002-dev`

## 2. Objetivo de la HU
Mejorar la arquitectura de persistencia de `FarmaExpres` manteniendo un unico motor PostgreSQL, pero separando logicamente los datos de cada microservicio mediante schemas, usuarios tecnicos y permisos independientes.

La intencion es evitar que los servicios compartan tablas sin control, reducir el uso del usuario administrador `postgres` en tiempo de ejecucion y dejar una base mas cercana a una arquitectura de microservicios defendible.

## 3. Justificacion funcional
Actualmente los microservicios dependen de una base PostgreSQL administrada desde el proyecto `database`. Aunque esto facilita la ejecucion local, existe riesgo de que los servicios terminen compartiendo tablas, credenciales o permisos de forma incorrecta.

Esta HU propone mantener una infraestructura sencilla para el proyecto academico, pero con una separacion clara de responsabilidades:
- `auth-service` administra usuarios, roles, bitacora y refresh tokens.
- `inventory-service` administra productos, lotes y movimientos.
- cada servicio accede solo a su propio schema.
- ningun servicio usa credenciales administrativas para operar.

## 4. Problema que resuelve la HU
La configuracion actual puede generar estos problemas:
- uso de `postgres` como usuario de aplicacion.
- passwords hardcodeadas en archivos de configuracion o migraciones.
- riesgo de que un microservicio lea o modifique tablas de otro dominio.
- dificultad para justificar independencia entre microservicios.
- mayor impacto ante errores de codigo o consultas incorrectas.
- crecimiento desordenado del modelo de datos.

## 5. Historia de usuario
Como equipo de desarrollo de `FarmaExpres`,  
queremos separar logicamente la base de datos de cada microservicio,  
para garantizar que `auth-service` e `inventory-service` administren unicamente sus propios datos, reducir el acoplamiento entre dominios y mejorar la seguridad de acceso a la informacion.

## 6. Alcance funcional propuesto
Esta HU cubre:
- definicion de schemas separados para autenticacion e inventario.
- creacion de usuarios tecnicos por servicio.
- asignacion de permisos minimos por schema.
- ajuste de migraciones Liquibase para trabajar en el schema correspondiente.
- ajuste de variables de entorno para evitar uso de `postgres` como usuario de aplicacion.
- validacion de que los servicios sigan iniciando correctamente.

Esta HU no busca crear multiples contenedores PostgreSQL. La separacion propuesta es logica, no fisica.

## 7. Estado actual resumido
El estado actual puede representarse asi:

```text
PostgreSQL
  |
  +-- tablas de auth-service
  +-- tablas de inventory-service

Servicios usando credenciales compartidas o administrativas.
```

Esto funciona para desarrollo, pero no representa una separacion fuerte de responsabilidades.

## 8. Estado objetivo propuesto
El estado objetivo es:

```text
PostgreSQL
  |
  +-- schema auth
  |     +-- role
  |     +-- users
  |     +-- binnacle
  |     +-- refresh_token
  |
  +-- schema inventory
        +-- product
        +-- batch
        +-- motion

auth-service      -> auth_app_user      -> solo schema auth
inventory-service -> inventory_app_user -> solo schema inventory
```

El motor puede seguir siendo unico, pero cada dominio debe tener un limite claro.

## 9. Reglas propuestas de separacion
1. `auth-service` no debe consultar tablas del schema `inventory`.
2. `inventory-service` no debe consultar tablas del schema `auth`.
3. Ningun microservicio debe conectarse en ejecucion con el usuario `postgres`.
4. Cada microservicio debe tener un usuario tecnico propio.
5. Cada usuario tecnico debe tener permisos minimos sobre su schema.
6. Las migraciones deben declarar explicitamente el schema objetivo.
7. Las credenciales reales no deben quedar hardcodeadas en el repositorio.

## 10. Estrategia tecnica recomendada

### 10.1 Schemas propuestos
Crear dos schemas principales:

```sql
auth
inventory
```

El schema `public` no debe usarse para nuevas tablas de negocio.

### 10.2 Usuarios tecnicos propuestos
Definir usuarios tecnicos por servicio:

```text
auth_app_user
inventory_app_user
auth_read_user
inventory_read_user
```

Los usuarios `*_app_user` se usan por los servicios en ejecucion.

Los usuarios `*_read_user` pueden usarse para reportes, diagnostico o consultas de solo lectura cuando sea necesario.

### 10.3 Permisos esperados
Permisos recomendados:

```text
auth_app_user:
  - USAGE sobre schema auth
  - SELECT, INSERT, UPDATE, DELETE sobre tablas de auth
  - USAGE sobre secuencias de auth

inventory_app_user:
  - USAGE sobre schema inventory
  - SELECT, INSERT, UPDATE, DELETE sobre tablas de inventory
  - USAGE sobre secuencias de inventory
```

No deben existir permisos cruzados entre schemas.

### 10.4 Liquibase
Las migraciones deben ejecutarse de forma controlada:
- migraciones de `auth-service` crean o modifican objetos del schema `auth`.
- migraciones de `inventory-service` crean o modifican objetos del schema `inventory`.
- los cambios DCL deben crear usuarios y permisos sin exponer passwords reales.

### 10.5 Docker Compose y variables de entorno
Los servicios deben conectarse con usuarios tecnicos:

```env
AUTH_DB_USERNAME=auth_app_user
AUTH_DB_PASSWORD=valor_por_ambiente
INVENTORY_DB_USERNAME=inventory_app_user
INVENTORY_DB_PASSWORD=valor_por_ambiente
```

El usuario `postgres` debe quedar reservado para tareas administrativas, inicializacion o mantenimiento controlado.

## 11. Plan de implementacion propuesto

### Fase 1 - Preparacion
- Revisar tablas actuales de `auth-service` e `inventory-service`.
- Definir nombres definitivos de schemas.
- Revisar migraciones existentes para identificar dependencias con `public`.

### Fase 2 - Schemas y permisos
- Crear schema `auth`.
- Crear schema `inventory`.
- Crear usuarios tecnicos por servicio.
- Asignar permisos minimos por schema.

### Fase 3 - Ajuste de migraciones
- Ajustar migraciones de auth para crear objetos en `auth`.
- Ajustar migraciones de inventory para crear objetos en `inventory`.
- Validar rollbacks.
- Evitar passwords reales dentro de scripts versionados.

### Fase 4 - Ajuste de servicios
- Actualizar datasource de `auth-service`.
- Actualizar datasource de `inventory-service`.
- Actualizar `docker-compose.yml`.
- Actualizar `.env.dev`, `.env.qa` y `.env.main` con variables separadas.

### Fase 5 - Validacion
- Levantar base de datos desde cero.
- Ejecutar migraciones.
- Iniciar `auth-service`.
- Iniciar `inventory-service`.
- Ejecutar pruebas automatizadas.
- Verificar que un usuario tecnico no pueda acceder al schema de otro servicio.

## 12. Criterios de aceptacion propuestos
1. Debe existir un schema `auth` para tablas de autenticacion.
2. Debe existir un schema `inventory` para tablas de inventario.
3. `auth-service` debe conectarse usando un usuario tecnico propio.
4. `inventory-service` debe conectarse usando un usuario tecnico propio.
5. Ningun microservicio debe usar `postgres` como usuario de aplicacion.
6. `auth_app_user` no debe tener permisos sobre tablas de `inventory`.
7. `inventory_app_user` no debe tener permisos sobre tablas de `auth`.
8. Las migraciones deben poder ejecutarse desde una base limpia.
9. Los servicios deben iniciar correctamente despues del cambio.
10. Las pruebas de `auth-service` e `inventory-service` deben ejecutarse correctamente o quedar documentadas con su configuracion requerida.
11. La configuracion debe quedar documentada en README o ADR.
12. No deben agregarse secretos reales al repositorio.

## 13. Fuera de alcance de esta HU
No hace parte de esta HU:
- crear una base PostgreSQL fisica por microservicio.
- migrar `alert-service` para consumir APIs de inventario.
- cambiar el modelo de entidades de negocio.
- implementar cifrado de columnas.
- crear un sistema externo de gestion de secretos.
- modificar reglas funcionales de inventario o autenticacion.

## 14. Riesgos y mitigaciones

| Riesgo | Impacto | Mitigacion |
|--------|---------|------------|
| Las migraciones fallan por referencias al schema `public` | Alto | Revisar scripts DDL, DML y DCL antes de ejecutar |
| Los servicios no encuentran tablas por cambio de schema | Alto | Configurar schema por defecto en datasource o Liquibase |
| Permisos insuficientes para secuencias | Medio | Incluir `USAGE` sobre secuencias del schema |
| Passwords quedan versionadas | Alto | Usar variables de entorno y valores de ejemplo no reales |
| Cambio rompe ambiente local | Medio | Documentar comandos de reinicializacion y validacion |

## 15. Beneficios esperados
Implementar esta HU aportara:
- separacion mas clara entre dominios.
- menor riesgo de acceso indebido entre servicios.
- mejor defensa arquitectonica del proyecto.
- eliminacion del uso de `postgres` como usuario de aplicacion.
- base mas preparada para futuras mejoras de seguridad.
- camino mas limpio para desacoplar `alert-service`.

## 16. Resultado esperado
Al completar esta HU, `FarmaExpres` mantendra una infraestructura simple con un solo PostgreSQL, pero con separacion logica por schema y permisos.

El resultado esperado es:

```text
Mismo motor PostgreSQL
Distintos schemas
Distintos usuarios tecnicos
Permisos minimos por microservicio
```

Esto mejora la arquitectura sin aumentar innecesariamente la complejidad operativa del proyecto.
