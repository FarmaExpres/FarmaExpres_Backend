# Guia Rapida: Inicializar Backend y Conectar Frontend (FarmaExpress)

## 1) Prerrequisitos

- Docker Desktop instalado y en ejecucion.
- Git instalado.
- (Opcional para frontend) Node.js 18+.

---

## 2) Clonar y levantar backend

Desde la carpeta donde quieras clonar:

```bash
git clone <URL_DEL_REPO_BACKEND>
cd FarmaExpres_Backend
docker compose up -d --build
```

Servicios esperados:
- `postgres` -> `localhost:5433`
- `api-gateway` -> `localhost:8080`
- `auth-service` -> `localhost:8081`
- `inventory-service` -> `localhost:8082`
- `alert-service` -> `localhost:8083`

Verificar estado:

```bash
docker compose ps
```

---

## 3) Datos iniciales (seed)

Al arrancar por primera vez se crean automaticamente:

- Roles:
  - `ADMIN`
  - `AUDITOR`
  - `FARMACEUTICO`

- Usuario administrador inicial:
  - Email: `temenico5@gmail.com`
  - Password: `admin123`
  - Rol: `ADMIN`

- Usuarios adicionales:
  - Email: `leonardojv@gmail.com` | Password: `admin123` | Rol: `ADMIN`
  - Email: `jerssson@gmail.com` | Password: `admin123` | Rol: `AUDITOR`
  - Email: `marlon@gmail.com` | Password: `admin123` | Rol: `FARMACEUTICO`

- 10 medicamentos base:
  - `MED-001` a `MED-010`

---

## 4) Login rapido (Postman)

URL:

`POST http://localhost:8080/api/auth/login`

Body JSON:

```json
{
  "email": "temenico5@gmail.com",
  "password": "admin123"
}
```

Si es correcto, retorna token JWT. Usar en headers:

`Authorization: Bearer <token>`

---

## 5) Persistencia de datos

Este backend usa volumen Docker para PostgreSQL (`postgres_data`), por lo tanto:

- `docker compose down` -> **NO** borra datos.
- `docker compose up -d` -> recupera datos existentes.

Para reiniciar todo desde cero (elimina BD y vuelve a sembrar):

```bash
docker compose down -v
docker compose up -d --build
```

---

## 6) Conectar frontend

### Flujo oficial (sin token manual)

El frontend ya autentica por pantalla de login, por lo tanto:

1. Levantar backend (`docker compose up -d --build`).
2. Levantar frontend (`npm run dev`).
3. Iniciar sesion desde el formulario del frontend con un usuario valido (ADMIN, AUDITOR o FARMACEUTICO).
4. El frontend recibe y guarda el JWT automaticamente desde `POST /api/auth/login`.
5. Con esa sesion, el acceso a modulos y endpoints se habilita segun rol.

Consumo oficial desde frontend:
- Base URL backend: `http://localhost:8080`
- El frontend no debe consumir directamente `8081`, `8082` ni `8083`.

Si el frontend tiene proxy configurado, debe apuntar al gateway:
- `/api/auth`, `/api/users`, `/api/binnacle` -> `http://localhost:8080`
- `/api/products`, `/api/movements` -> `http://localhost:8080`
- `/api/movements/entrance`, `/api/movements/exit`, `/api/movements/updated` -> `http://localhost:8080`
- `/api/alerts` -> `http://localhost:8080`

Nota:
- Ya no se requiere configurar `VITE_DEV_TOKEN` manualmente para operar el flujo normal.

---

## 7) Endpoints principales

- Auth/Login:
  - `POST /api/auth/login`

- Usuarios:
  - `GET /api/users`
  - `POST /api/users`
  - `PUT /api/users/{id}/update`
  - `PUT /api/users/{id}/password`
  - `PUT /api/users/{id}/block`
  - `PUT /api/users/{id}/unlock`
  - `DELETE /api/users/{id}`

- Inventario:
  - `GET /api/products`
  - `POST /api/products`
  - `PUT /api/products/{id}`
  - `DELETE /api/products/{id}`
  - `GET /api/products/Assets`
  - `GET /api/products/out-of-stock`
  - `GET /api/movements`
  - `GET /api/movements/entrance`
  - `GET /api/movements/exit`
  - `GET /api/movements/updated`

- Alertas:
  - `GET /api/alerts`
  - `GET /api/alerts/low-stock`
  - `GET /api/alerts/expired`
  - `GET /api/alerts/out-of-stock`
  - `GET /api/alerts/expiring-soon`
  - `GET /api/alerts/expiring-half-month`
  - `GET /api/alerts/expiring-month`

---

## 8) Convencion oficial de nombres (Backend-Frontend)

Para mantener consistencia entre equipos, se define:

- Nombre tecnico canonico: `product`
- Endpoint canonico: `/api/products`
- Entidad y tabla en backend: `Product` / `product`

Uso permitido en frontend:
- "Medicamentos" o "Medicines" solo para textos visuales (labels, titulos, menu).
- En codigo de integracion (servicios, rutas API, tipados de contrato), usar siempre `product`/`products`.

---

## 9) Solucion de problemas rapida

- Si algun servicio no responde:
  - `docker compose logs -f api-gateway`
  - `docker compose logs -f auth-service`
  - `docker compose logs -f inventory-service`
  - `docker compose logs -f alert-service`
  - `docker compose logs -f postgres`

- Si hay conflicto por datos viejos y quieres iniciar limpio:
  - `docker compose down -v`
  - `docker compose up -d --build`

- Si el frontend muestra pantalla de login pero no entra:
  - Verificar credenciales de seed en la seccion 3.
  - Verificar que `POST /api/auth/login` por `http://localhost:8080` responda `200` con usuario valido.
  - Verificar proxy del frontend hacia `8080`.
