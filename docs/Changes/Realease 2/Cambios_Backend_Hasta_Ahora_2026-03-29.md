# Cambios Backend Realizados Hasta Ahora

Fecha de corte: 2026-03-29
Rama de trabajo: `HU-AC-dev`

## 1. Cambios funcionales en auth-service

### 1.0 Ajuste de login para frontend (nombre de usuario)
Se actualizó `POST /api/auth/login` para incluir el nombre del usuario autenticado en la respuesta:

```json
{
  "token": "jwt",
  "type": "Bearer",
  "email": "user@mail.com",
  "role": "ADMIN",
  "name": "Nombre Usuario"
}
```

Adicionalmente, se incluyó `name` como claim en el JWT (manteniendo `sub` y `rol`).

Archivos modificados:
- `auth-service/src/main/java/co/edu/corhuila/auth_service/DTO/LoginResponseDto.java`
- `auth-service/src/main/java/co/edu/corhuila/auth_service/Service/JwtService.java`
- `auth-service/src/main/java/co/edu/corhuila/auth_service/Service/AuthService.java`

### 1.0.1 Corrección de errores de autenticación (correo no existe / contraseña incorrecta)
Se ajustó `POST /api/auth/login` para evitar `500` por credenciales inválidas:
- Correo no registrado -> `401 Unauthorized` con mensaje genérico `Credenciales incorrectas`.
- Contraseña incorrecta -> `401 Unauthorized` con el mismo mensaje genérico.
- Se evita revelar si falló correo o contraseña (mitiga user enumeration).
- Los errores `500` quedan reservados para fallas técnicas reales.

Adicionalmente, el registro de bitácora de login fallido se volvió tolerante a errores:
- Si falla el guardado en bitácora, no cambia el resultado funcional del login.

Archivo modificado:
- `auth-service/src/main/java/co/edu/corhuila/auth_service/Service/AuthService.java`

### 1.1 Validaciones de cambio de contraseña
Se actualizó la lógica de `PUT /api/users/{id}/password` para validar:
- `currentpassword` obligatoria.
- `newPassword` obligatoria.
- Longitud mínima de `newPassword` = 8.
- `newPassword` debe ser diferente de la contraseña actual.
- `404` cuando usuario no existe.
- `400` para validaciones de negocio (mensajes claros).

Archivo modificado:
- `auth-service/src/main/java/co/edu/corhuila/auth_service/Service/userService.java`

### 1.2 Datos semilla automáticos (roles + usuarios)
Se creó inicialización automática e idempotente al arrancar el servicio:
- Roles:
  - `ADMIN`
  - `AUDITOR`
  - `FARMACEUTICO`
- Usuarios base:
  - Nicolas Tello (`temenico5@gmail.com`) - `ADMIN`
  - Jose Leonardo Vargas (`leonardojv@gmail.com`) - `ADMIN`
  - Jersson Fabian Buitrago (`jerssson@gmail.com`) - `AUDITOR`
  - Marlon Romero (`marlon@gmail.com`) - `FARMACEUTICO`

Archivo agregado:
- `auth-service/src/main/java/co/edu/corhuila/auth_service/Config/DataInitializer.java`

---

## 2. Cambios funcionales en inventory-service

### 2.1 Datos semilla automáticos de medicamentos
Se creó inicialización automática e idempotente de 10 productos base:
- `MED-001` Acetaminofen 500 mg
- `MED-002` Ibuprofeno 400 mg
- `MED-003` Loratadina 10 mg
- `MED-004` Omeprazol 20 mg
- `MED-005` Amoxicilina 500 mg
- `MED-006` Diclofenaco 50 mg
- `MED-007` Vitamina C 1 g
- `MED-008` Salbutamol Inhalador
- `MED-009` Metformina 850 mg
- `MED-010` Losartan 50 mg

Nota:
- Solo inserta si el `code` no existe, para evitar duplicados.
- Usa `ProductService.createProduct(...)`, por lo que también se registran movimientos de entrada según lógica actual.

Archivo agregado:
- `inventory-service/src/main/java/co/edu/corhuila/inventory_service/Config/DataInitializer.java`

---

## 3. Cambios de infraestructura Docker

### 3.1 Persistencia de base de datos
Se agregó volumen persistente para PostgreSQL:
- `postgres_data:/var/lib/postgresql/data`

Impacto:
- `docker compose down` conserva datos.
- `docker compose down -v` elimina datos y permite reinicialización desde cero.

Archivo modificado:
- `docker-compose.yml`

---

## 4. Cambios de documentación

### 4.1 Guía de inicialización backend + conexión frontend
Se creó guía rápida en `docs` con:
- pasos de arranque por Docker,
- credenciales iniciales,
- login de prueba en Postman,
- persistencia de datos,
- conexión frontend por flujo de login real (sin token manual).
- convención oficial de nombres: `product` como término técnico canónico.

Archivo agregado/modificado:
- `docs/Guia_Inicializacion_Backend_Frontend.md`

---

## 5. Verificaciones realizadas en ejecución

- Se confirmó arranque saludable de contenedores (`postgres`, `auth-service`, `inventory-service`).
- Se confirmaron tablas creadas en ambas bases.
- Se validó login de usuario administrador.
- Se validaron nuevas reglas de contraseña en backend con casos:
  - contraseña actual vacía -> `400`
  - nueva contraseña menor a 8 -> `400`
  - nueva contraseña igual a actual -> `400`
  - cambio válido -> `200`

---

## 6. Notas de alcance

- Se hicieron pruebas de `api-gateway`, pero no se dejaron cambios activos allí; el repositorio quedó nuevamente sin modificaciones efectivas en ese módulo.
- El flujo operativo actual para integración es directo a microservicios (`8081` y `8082`) y en frontend por token (`VITE_DEV_TOKEN`) en desarrollo.
- Convención acordada de dominio: en backend/API/BD se usa `product` (`/api/products`), mientras que "medicamento/medicine" queda como etiqueta de interfaz.
