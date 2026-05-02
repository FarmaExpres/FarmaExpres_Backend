# HU-JVL-001 - Validacion centralizada de JWT en API Gateway y renovacion segura de sesion

## 1. Informacion general
- HU: `HU-JVL-001`
- Nombre: Centralizar la validacion de JWT en el API Gateway y definir refresh token
- Componente principal: `api-gateway`
- Componentes relacionados: `auth-service`, `inventory-service`, `alert-service`, `docker-compose`, archivos `.env`
- Estado: Propuesto
- Rama de trabajo sugerida: `HU-JVL-001-dev`

## 2. Objetivo de la HU
Definir la estrategia para que el `api-gateway` sea el punto central de validacion de tokens JWT antes de permitir el acceso a los microservicios internos de `FarmaExpres`.

La intencion es fortalecer la seguridad del ecosistema, evitar validaciones inconsistentes entre microservicios y hacer que el gateway funcione como frontera real de entrada al sistema.

Adicionalmente, esta HU define la incorporacion de `refresh tokens` para permitir que los usuarios mantengan una sesion activa de forma segura sin tener que volver a iniciar sesion cada vez que expire el token de acceso.

## 3. Justificacion funcional
El sistema actualmente cuenta con autenticacion basada en JWT y un `api-gateway` que enruta las solicitudes hacia los microservicios.

Sin embargo, la validacion del token no esta completamente centralizada. Esto genera varios riesgos:
- cada microservicio puede validar el JWT de forma distinta
- algunos servicios pueden aceptar tokens sin verificar correctamente la firma
- se duplica logica de seguridad
- se dificulta mantener una politica uniforme de acceso
- el gateway no actua todavia como frontera de seguridad completa
- cuando el token expira, el usuario debe volver a iniciar sesion para continuar trabajando

Esta HU propone evolucionar el gateway para que valide el token antes de enrutar solicitudes protegidas.

## 4. Problema que resuelve la HU
La arquitectura actual puede permitir inconsistencias de seguridad entre servicios.

Problemas identificados:
- `auth-service` genera tokens JWT
- `inventory-service` valida tokens con logica propia
- `alert-service` puede leer informacion del token sin validar criptograficamente su firma
- `api-gateway` enruta `/api/**` sin hacer validacion JWT centralizada
- el secreto JWT debe ser consistente en todos los componentes
- no existe un mecanismo de renovacion segura de sesion con `refresh token`

Esto hace que la seguridad dependa demasiado de que cada microservicio implemente correctamente la misma regla.

## 5. Historia de usuario
Como equipo de desarrollo de `FarmaExpres`,  
queremos centralizar la validacion de JWT en el `api-gateway`,  
para garantizar que toda peticion protegida sea autenticada antes de llegar a los microservicios internos, mantener una politica de seguridad consistente en todo el backend y permitir la renovacion segura de sesion sin obligar al usuario a volver a iniciar sesion durante su trabajo.

## 6. Alcance funcional propuesto
Esta HU cubre la definicion de:
- validacion de JWT en `api-gateway`
- rutas publicas y rutas protegidas
- estrategia de propagacion de identidad hacia microservicios internos
- uso consistente de `JWT_SECRET`
- definicion de `access token` y `refresh token`
- nueva entidad persistente para administrar `refresh tokens`
- endpoint de renovacion de sesion
- endurecimiento de acceso directo a microservicios
- criterios minimos de prueba para autenticacion y autorizacion

Esta HU no implementa los cambios tecnicos todavia. Solo documenta lo que se realizara.

## 7. Estado actual resumido
Actualmente el flujo conceptual es:

```text
Cliente
  |
  v
api-gateway
  |
  +--> auth-service
  +--> inventory-service
  +--> alert-service

Cada microservicio decide si valida o no el JWT.
```

Esto permite que el gateway funcione como enrutador, pero no como punto principal de seguridad.

## 8. Estado objetivo propuesto
El flujo objetivo es:

```text
Cliente
  |
  v
api-gateway
  |
  |-- valida JWT
  |-- rechaza tokens invalidos o expirados
  |-- permite rutas publicas definidas
  |
  +--> auth-service       interno
  +--> inventory-service  interno
  +--> alert-service      interno
```

El gateway debe convertirse en la primera barrera de autenticacion.

## 9. Reglas de seguridad propuestas

### 9.1 Rutas publicas
Las siguientes rutas deben poder accederse sin token:
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /status`
- `GET /actuator/health`
- `GET /actuator/info`
- solicitudes `OPTIONS` necesarias para CORS

### 9.2 Rutas protegidas
Todas las demas rutas bajo `/api/**` deben requerir un token JWT valido.

Ejemplos:
- `/api/users/**`
- `/api/binnacle/**`
- `/api/products/**`
- `/api/movements/**`
- `/api/reports/**`
- `/api/alerts/**`

### 9.3 Validaciones minimas del token
El gateway debe validar:
- existencia del header `Authorization`
- formato `Bearer <token>`
- firma del token
- expiracion del token
- estructura minima de claims requeridos

Claims esperados:
- `sub` o subject: email del usuario
- `rol`: rol del usuario
- `name`: nombre del usuario
- `userId`: identificador del usuario
- `exp`: expiracion del token

## 10. Estrategia tecnica recomendada

### 10.1 Auth-service
`auth-service` mantiene la responsabilidad de:
- autenticar credenciales
- generar JWT
- firmar JWT usando `JWT_SECRET`
- generar y administrar `refresh tokens`
- renovar `access tokens` cuando el `refresh token` sea valido
- revocar `refresh tokens` cuando aplique

### 10.2 Api-gateway
`api-gateway` asume la responsabilidad de:
- validar JWT antes de enrutar
- rechazar peticiones sin token valido
- permitir solamente rutas publicas definidas
- propagar la identidad del usuario hacia servicios internos

### 10.3 Microservicios internos
Los microservicios internos deben recibir solicitudes ya autenticadas por el gateway.

Se recomienda que, al menos durante una fase de transicion, los microservicios mantengan validaciones propias en endpoints criticos para defensa en profundidad.

## 10.4 Estrategia de access token y refresh token
Para mejorar la experiencia de usuario sin sacrificar seguridad, se propone separar la sesion en dos tipos de token:

- `accessToken`: JWT de corta duracion usado para acceder a rutas protegidas.
- `refreshToken`: token de mayor duracion usado solo para pedir un nuevo `accessToken`.

Duraciones definidas para esta HU:

```text
accessToken: 15 minutos
refreshToken: 1 dia
```

El `accessToken` sera validado por el `api-gateway` en cada solicitud protegida.

El `refreshToken` sera validado exclusivamente por `auth-service` mediante un endpoint de renovacion.

## 10.5 Flujo de renovacion propuesto
El flujo esperado es:

```text
1. Usuario inicia sesion en POST /api/auth/login
2. auth-service entrega accessToken y refreshToken
3. frontend usa accessToken para consumir rutas protegidas
4. accessToken vence despues de 15 minutos
5. frontend llama POST /api/auth/refresh con refreshToken
6. auth-service valida refreshToken
7. auth-service genera nuevo accessToken
8. usuario continua trabajando sin volver a escribir credenciales
```

El `refreshToken` no debe permitir acceso directo a recursos de negocio. Solo debe servir para renovar la sesion.

## 10.6 Nueva entidad propuesta: RefreshToken
Se propone implementar una nueva entidad persistente en `auth-service` para controlar la vida util, revocacion y trazabilidad de los `refresh tokens`.

Nombre sugerido de entidad:

```text
RefreshToken
```

Tabla sugerida:

```text
refresh_token
```

Campos propuestos:

```text
id
user_id
token_hash
expires_at
revoked
created_at
last_used_at
revoked_at
```

Descripcion de campos:
- `id`: identificador unico del registro.
- `user_id`: usuario propietario del refresh token.
- `token_hash`: hash del refresh token, no el token plano.
- `expires_at`: fecha y hora de expiracion.
- `revoked`: indica si el token fue revocado.
- `created_at`: fecha y hora de creacion.
- `last_used_at`: ultima fecha de uso para renovar sesion.
- `revoked_at`: fecha de revocacion, si aplica.

Reglas para esta entidad:
- no guardar el refresh token en texto plano
- guardar solo un hash del refresh token
- validar expiracion antes de emitir un nuevo access token
- validar que el token no este revocado
- asociar cada refresh token a un usuario existente
- permitir revocacion para logout o eventos de seguridad

## 10.7 Endpoint de renovacion propuesto
Se propone agregar el siguiente endpoint en `auth-service`:

```http
POST /api/auth/refresh
```

Request conceptual:

```json
{
  "refreshToken": "token-largo"
}
```

Response conceptual:

```json
{
  "accessToken": "nuevo-jwt-de-15-minutos",
  "refreshToken": "refresh-token-vigente-o-rotado",
  "type": "Bearer"
}
```

La respuesta puede mantener el mismo refresh token hasta su expiracion o aplicar rotacion. Como mejora recomendada, se propone rotar el refresh token en cada uso.

## 10.8 Logout y revocacion
Como complemento del refresh token, se recomienda definir un endpoint de cierre de sesion:

```http
POST /api/auth/logout
```

Este endpoint debe revocar el refresh token activo para impedir que se puedan generar nuevos access tokens despues del cierre de sesion.

El access token ya emitido seguira siendo valido hasta su expiracion natural de 15 minutos, salvo que en una fase futura se implemente lista de revocacion de access tokens.

## 11. Propagacion de identidad
Una vez validado el JWT, el gateway puede propagar informacion del usuario a los servicios internos mediante headers controlados.

Headers sugeridos:
- `X-User-Id`
- `X-User-Email`
- `X-User-Name`
- `X-User-Role`

Los microservicios no deben confiar en estos headers si la solicitud puede llegar directamente desde fuera del gateway.

Por eso esta HU debe complementarse con la restriccion de acceso directo a microservicios internos.

## 12. Relacion con acceso obligatorio por gateway
Esta HU se relaciona con la decision de hacer obligatorio el uso del gateway.

Para que la validacion centralizada sea segura:
- el cliente externo debe consumir solo el `api-gateway`
- `auth-service`, `inventory-service` y `alert-service` no deben exponerse publicamente en ambientes `qa` y `main`
- los microservicios deben comunicarse por red interna

En desarrollo local puede mantenerse exposicion temporal para depuracion, siempre que quede documentada como excepcion.

## 13. Configuracion de JWT_SECRET
Todos los componentes que firmen o validen JWT deben usar una unica fuente de configuracion.

Variable recomendada:

```env
JWT_SECRET=valor_seguro_por_ambiente
```

Reglas:
- no hardcodear secretos en codigo fuente
- definir secretos por ambiente
- no reutilizar secretos reales en documentacion
- mantener un secreto suficientemente largo para HS256
- coordinar rotacion de secreto porque invalida tokens activos

## 14. Autorizacion por roles
Esta HU se centra en autenticacion centralizada, no necesariamente en mover toda la autorizacion al gateway.

Estrategia recomendada por fases:

### Fase 1
El gateway valida autenticacion JWT.

Los microservicios conservan la autorizacion por rol en endpoints especificos.

### Fase 2
Evaluar si algunas reglas de autorizacion se pueden mover al gateway, especialmente reglas simples por ruta.

### Fase 3
Mantener reglas de negocio sensibles dentro del microservicio dueno del dominio.

## 15. Plan de implementacion propuesto

### Fase 1 - Configuracion base
- Definir `JWT_SECRET` por ambiente.
- Asegurar que `auth-service` firme tokens usando `JWT_SECRET`.
- Preparar `api-gateway` para leer el mismo `JWT_SECRET`.
- Definir duracion de `accessToken` en 15 minutos.
- Definir duracion de `refreshToken` en 1 dia.

### Fase 2 - Filtro JWT en gateway
- Implementar filtro de seguridad JWT en `api-gateway`.
- Excluir rutas publicas.
- Rechazar tokens invalidos, expirados o mal formados.
- Mantener CORS funcionando.

### Fase 3 - Propagacion de identidad
- Extraer claims del token validado.
- Propagar identidad mediante headers internos.
- Ajustar servicios internos si necesitan leer esos headers.

### Fase 4 - Refresh token en auth-service
- Crear entidad `RefreshToken`.
- Crear migracion de base de datos para tabla `refresh_token`.
- Ajustar respuesta de login para entregar `accessToken` y `refreshToken`.
- Implementar `POST /api/auth/refresh`.
- Implementar revocacion de refresh token para logout o eventos de seguridad.

### Fase 5 - Endurecimiento de microservicios
- Evitar acceso externo directo a microservicios internos en `qa` y `main`.
- Revisar `alert-service` para no depender de decodificacion manual sin firma.
- Mantener validaciones internas criticas donde aplique.

### Fase 6 - Pruebas y documentacion
- Probar rutas publicas sin token.
- Probar rutas protegidas sin token.
- Probar rutas protegidas con token valido.
- Probar token expirado.
- Probar token con firma invalida.
- Probar renovacion con refresh token valido.
- Probar rechazo de refresh token expirado.
- Probar rechazo de refresh token revocado.
- Documentar el flujo final en README o ADR.

## 16. Criterios de aceptacion propuestos
1. El gateway debe permitir `POST /api/auth/login` sin JWT.
2. El gateway debe permitir `/status` y `/actuator/health` sin JWT.
3. El gateway debe rechazar rutas protegidas sin header `Authorization`.
4. El gateway debe rechazar tokens con firma invalida.
5. El gateway debe rechazar tokens expirados.
6. El gateway debe permitir el acceso a rutas protegidas con JWT valido.
7. El gateway debe propagar informacion basica del usuario validado hacia los servicios internos.
8. `JWT_SECRET` debe obtenerse desde configuracion y no desde constantes hardcodeadas.
9. El `accessToken` debe tener una duracion de 15 minutos.
10. El `refreshToken` debe tener una duracion de 1 dia.
11. Debe existir una entidad persistente `RefreshToken` o equivalente en `auth-service`.
12. El sistema debe permitir renovar el `accessToken` mediante `POST /api/auth/refresh`.
13. El refresh token debe poder revocarse.
14. Debe existir documentacion del flujo de autenticacion centralizada y renovacion de sesion.
15. Las pruebas deben cubrir al menos login, acceso protegido exitoso, rechazo de token invalido y renovacion con refresh token valido.

## 17. Fuera de alcance de esta HU
No hace parte de esta HU:
- cambiar el modelo de entidades de `alert-service`
- implementar autenticacion OAuth2 externa
- crear un servidor de identidad dedicado
- reemplazar JWT por sesiones
- mover todas las reglas de autorizacion de dominio al gateway
- implementar lista de revocacion para access tokens ya emitidos

## 18. Riesgos y mitigaciones

| Riesgo | Impacto | Mitigacion |
|--------|---------|------------|
| El gateway se convierte en punto unico de fallo | Alto | Health checks, reinicio automatico y monitoreo |
| Headers internos pueden ser falsificados si hay acceso directo a microservicios | Alto | Restringir exposicion externa de servicios internos |
| Rotar `JWT_SECRET` invalida tokens activos | Medio | Coordinar ventanas de despliegue |
| Duplicidad temporal de validaciones | Bajo | Manejarlo como fase de transicion |
| Configuracion inconsistente por ambiente | Medio | Usar `.env.dev`, `.env.qa`, `.env.main` |
| Robo de refresh token | Alto | Guardar hash, usar expiracion de 1 dia, permitir revocacion y considerar rotacion |
| Renovaciones indefinidas | Medio | Limitar refresh token a 1 dia y validar `expires_at` |

## 19. Beneficios esperados
Implementar esta HU aportara:
- seguridad mas consistente
- gateway como frontera real del backend
- menor duplicacion de logica JWT
- mejor control de rutas publicas y protegidas
- mejor defensa frente a tokens manipulados
- arquitectura mas clara para frontend y backend
- base mas solida para escalar autorizacion y observabilidad
- mejor experiencia de usuario al evitar relogueos frecuentes
- control de sesiones mediante refresh tokens revocables

## 20. Resultado esperado
Al completar esta HU, `FarmaExpres` tendra una estrategia documentada para centralizar la validacion JWT en el `api-gateway`, manteniendo a los microservicios internos protegidos y reduciendo inconsistencias en la seguridad del sistema.

Tambien quedara definida la renovacion segura de sesion mediante `refresh tokens`, con `accessToken` de 15 minutos, `refreshToken` de 1 dia y una nueva entidad persistente en `auth-service` para administrarlos.

El resultado esperado no es solo que el token funcione, sino que la arquitectura tenga una frontera clara:

```text
Cliente externo -> api-gateway -> microservicios internos
```

El gateway validara la identidad antes de permitir el paso hacia los servicios de negocio.
