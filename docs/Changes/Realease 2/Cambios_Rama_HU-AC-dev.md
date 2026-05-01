# Cambios de la Rama `HU-AC-dev`

Fecha de generación: 2026-03-30  
Rama analizada: `HU-AC-dev`  
Base de comparación: `develop` (`431acf8`)

## 1. Resumen General

Esta rama contiene 4 commits sobre `develop` y concentra una actualización funcional y técnica importante del backend.

Métricas del diff (`develop..HU-AC-dev`):
- Archivos cambiados: 69
- Inserciones: 3096
- Eliminaciones: 1021

## 2. Commits Incluidos en la Rama

| Commit | Fecha | Autor | Mensaje |
|---|---|---|---|
| `dfed877` | 2026-03-30 | jose6668 | `fix(Error injecting data and creating database)` |
| `87280af` | 2026-03-30 | jose6668 | `fix(Errors in the messages were corrected)` |
| `c875b4a` | 2026-03-29 | Nicolas Tello Mendez | `feat(auth,inventory,docs): strengthen login/password rules, add seeders, persist db and update integration guide` |
| `0630d71` | 2026-03-29 | jose6668 | `Refactor(the microservices were updated to English and new methods were implemented in the Inventory microservice; in addition, both microservices were documented in md files)` |

## 3. Cambios Funcionales y Técnicos

### 3.1 `auth-service`
- Refactor de nombres en inglés en controladores, entidades, DTOs, repositorios y servicios.
- Sustitución de componentes en español por equivalentes en inglés (ej.: `Bitacora` -> `Binnacle`, `Rol` -> `Role`, `Usuario` -> `User`).
- Ajustes en seguridad (`SecurityConfig`) y filtro JWT (`JwtFilter`).
- Mejoras en login:
  - Respuesta de autenticación enriquecida con información adicional de usuario.
  - Manejo de errores de credenciales con respuestas más consistentes.
- Fortalecimiento de validaciones en cambio de contraseña.
- Estado final de seed/inicialización de datos en esta rama:
  - No existen archivos `DataInitializer.java` activos en `auth-service`.
  - La inicialización quedó centralizada en `database/init.sql`.
- Nuevas pruebas agregadas:
  - `UsurControllersTest`
  - `JwtFilterTest`
- Actualizaciones en pruebas existentes (`AuthServiceTest`).

### 3.2 `inventory-service`
- Refactor de nombres de dominio en inglés:
  - `Producto` -> `Product`
  - `Movimiento` -> `Motion`
  - `TipoMovimiento` -> `MovementType`
- Sustitución de controladores y servicios a versiones en inglés:
  - `ProductoController` -> `ProductController`
  - `MovimientoController` -> `MotionController`
  - `ProductoService` -> `ProductService`
  - `MovimientoService` -> `MotionService`
- Nuevas capacidades en endpoints de inventario:
  - Reporte de productos sin stock.
  - Endpoints y DTOs reestructurados.
- Ajustes en `SecurityConfig` y `JwtFilter`.
- Estado final de seed/inicialización de datos:
  - No existen archivos `DataInitializer.java` activos en `inventory-service`.
  - La inicialización quedó centralizada en `database/init.sql`.
- Pruebas actualizadas/renombradas para integración y JWT filter.

### 3.3 Base de Datos e Infraestructura
- `docker-compose.yml` actualizado para persistencia y soporte de arranque con datos.
- `database/init.sql` ampliado y corregido para:
  - Creación de bases `farmaexpres_users` y `farmaexpres_inventory`.
  - Creación de tablas compatibles con los modelos actuales.
  - Inserción de roles y usuarios base.
  - Inserción de catálogo nuevo de productos.
  - Inserción de movimientos iniciales.
- El commit `dfed877` corrige errores de inyección de datos y creación de BD moviendo la carga semilla al script SQL.

#### Catálogo de productos vigente en `init.sql`
- `ACM-001` Acetaminophen 500mg
- `IBU-001` Ibuprofen 400mg
- `LOT-001` Loratadina 10 mg
- `AMX-001` Amoxicillin 500mg
- `OMP-001` Omeprazol 20 mg
- `DCF-001` Diclofenaco 50 mg
- `VIC-001` Vitamina C 1 g
- `SAT-001` Salbutamol Inhalador
- `MET-001` Metformina 850 mg
- `SIM-001` Simvastatina 20 mg
- `LOS-001` Losartan 50 mg

### 3.4 Documentación
- Se agregaron documentos de microservicios:
  - `docs/Micro/Micro_Login.md`
  - `docs/Micro/Micro_Inventory.md`
- Se agregó guía de inicialización backend/frontend:
  - `docs/Guia_Inicializacion_Backend_Frontend.md`
- Se agregó bitácora de cambios previos:
  - `docs/Changes/Cambios_Backend_Hasta_Ahora_2026-03-29.md`

## 4. Correcciones Finales Aplicadas en la Rama

### Commit `87280af`
- Corrección de mensajes y ajustes de validación en:
  - `auth-service/.../SecurityConfig.java`
  - `auth-service/.../userService.java`

### Commit `dfed877`
- Eliminación de `DataInitializer` en `auth-service` e `inventory-service` (estado actual de la rama: no existen).
- Consolidación de seed y creación de BD en `database/init.sql`.

## 5. Listado Completo de Archivos Modificados (`develop..HU-AC-dev`)

Formato: `Estado<TAB>Ruta` (`A`=Added, `M`=Modified, `D`=Deleted, `R`=Renamed)
```text
M	auth-service/pom.xml
M	auth-service/src/main/java/co/edu/corhuila/auth_service/Config/SecurityConfig.java
A	auth-service/src/main/java/co/edu/corhuila/auth_service/Controllers/BinnacleController.java
D	auth-service/src/main/java/co/edu/corhuila/auth_service/Controllers/BitacoraController.java
A	auth-service/src/main/java/co/edu/corhuila/auth_service/Controllers/StatusController.java
D	auth-service/src/main/java/co/edu/corhuila/auth_service/Controllers/UsuarioControllers.java
A	auth-service/src/main/java/co/edu/corhuila/auth_service/Controllers/UsurControllers.java
D	auth-service/src/main/java/co/edu/corhuila/auth_service/DTO/CambiarPasswordRequest.java
M	auth-service/src/main/java/co/edu/corhuila/auth_service/DTO/LoginResponseDto.java
A	auth-service/src/main/java/co/edu/corhuila/auth_service/DTO/UpdateUserRequest.java
D	auth-service/src/main/java/co/edu/corhuila/auth_service/DTO/UsuarioRequest.java
D	auth-service/src/main/java/co/edu/corhuila/auth_service/DTO/UsuarioResponse.java
A	auth-service/src/main/java/co/edu/corhuila/auth_service/DTO/UsurRequest.java
A	auth-service/src/main/java/co/edu/corhuila/auth_service/DTO/UsurResponse.java
A	auth-service/src/main/java/co/edu/corhuila/auth_service/DTO/changePasswordRequest.java
A	auth-service/src/main/java/co/edu/corhuila/auth_service/Entity/Binnacle.java
D	auth-service/src/main/java/co/edu/corhuila/auth_service/Entity/Bitacora.java
D	auth-service/src/main/java/co/edu/corhuila/auth_service/Entity/EstadoUsuario.java
D	auth-service/src/main/java/co/edu/corhuila/auth_service/Entity/Rol.java
A	auth-service/src/main/java/co/edu/corhuila/auth_service/Entity/Role.java
R051	auth-service/src/main/java/co/edu/corhuila/auth_service/Entity/Usuario.java	auth-service/src/main/java/co/edu/corhuila/auth_service/Entity/User.java
A	auth-service/src/main/java/co/edu/corhuila/auth_service/Entity/UserStatus.java
A	auth-service/src/main/java/co/edu/corhuila/auth_service/Repository/BinnacleRepository.java
D	auth-service/src/main/java/co/edu/corhuila/auth_service/Repository/BitacoraRepository.java
D	auth-service/src/main/java/co/edu/corhuila/auth_service/Repository/RolRepository.java
A	auth-service/src/main/java/co/edu/corhuila/auth_service/Repository/RoleRepository.java
A	auth-service/src/main/java/co/edu/corhuila/auth_service/Repository/UserRepository.java
D	auth-service/src/main/java/co/edu/corhuila/auth_service/Repository/UsuarioRepository.java
M	auth-service/src/main/java/co/edu/corhuila/auth_service/Service/AuthService.java
M	auth-service/src/main/java/co/edu/corhuila/auth_service/Service/JwtFilter.java
M	auth-service/src/main/java/co/edu/corhuila/auth_service/Service/JwtService.java
D	auth-service/src/main/java/co/edu/corhuila/auth_service/Service/UsuarioService.java
A	auth-service/src/main/java/co/edu/corhuila/auth_service/Service/userService.java
M	auth-service/src/test/java/co/edu/corhuila/auth_service/AuthServiceTest.java
A	auth-service/src/test/java/co/edu/corhuila/auth_service/Controllers/UsurControllersTest.java
A	auth-service/src/test/java/co/edu/corhuila/auth_service/Service/JwtFilterTest.java
M	database/init.sql
M	docker-compose.yml
A	docs/Changes/Cambios_Backend_Hasta_Ahora_2026-03-29.md
A	docs/Guia_Inicializacion_Backend_Frontend.md
A	docs/Micro/Micro_Inventory.md
A	docs/Micro/Micro_Login.md
M	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Config/SecurityConfig.java
A	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Controllers/MotionController.java
D	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Controllers/MovimientoController.java
A	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Controllers/ProductController.java
D	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Controllers/ProductoController.java
A	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Dto/MotionResponse.java
D	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Dto/MovimientoResponse.java
A	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Dto/ProductOutOfStockResponse.java
D	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Dto/ProductoSinStockResponse.java
A	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Entity/Motion.java
A	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Entity/MovementType.java
D	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Entity/Movimiento.java
A	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Entity/Product.java
D	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Entity/Producto.java
D	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Entity/TipoMovimiento.java
A	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Repository/MotionRepository.java
D	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Repository/MovimientoRepository.java
A	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Repository/ProductRepository.java
D	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Repository/ProductoRepository.java
M	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/JwtFilter.java
A	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/MotionService.java
D	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/MovimientoService.java
A	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/ProductService.java
D	inventory-service/src/main/java/co/edu/corhuila/inventory_service/Service/ProductoService.java
A	inventory-service/src/main/java/co/edu/corhuila/inventory_service/StatusController.java
A	inventory-service/src/test/java/co/edu/corhuila/inventory_service/Service/JwtFilterTest.java
R082	inventory-service/src/test/java/co/edu/corhuila/inventory_service/ProductoControllerIntegrationTest.java	inventory-service/src/test/java/co/edu/corhuila/inventory_service/productControllerIntegrationTest.java
```
