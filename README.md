# Sistema Logístico

Aplicación web para administrar una tienda: **punto de venta (POS)**, **inventario por lotes con salida FEFO**, **caja y turnos**, **clientes con crédito (fiado)**, **compras a proveedores**, **devoluciones y cambios**, **reportes** y **administración de usuarios con roles y permisos**.

Es un monorepo con dos aplicaciones independientes:

| Carpeta | Descripción | Tecnología |
|---|---|---|
| [`backend/`](backend) | API REST (`/api/v1`) | Java 21 · Spring Boot 4.1 · PostgreSQL · Flyway |
| [`frontend/`](frontend) | Aplicación de una sola página (SPA) | Angular 22 · TypeScript · Tailwind CSS 4 |

---

## Funcionalidades

**Ventas**
- POS con búsqueda y escaneo por código de barras, precio según cantidad y tipo de cliente, y pagos combinados.
- Registro idempotente (los reintentos no duplican la venta), anulación con motivo, devoluciones y cambios.
- **Crédito (fiado):** clientes con límite de crédito, cuentas por cobrar, abonos aplicados a la deuda más antigua y comprobante.

**Caja**
- Cajas y turnos: apertura, ingresos y egresos, efectivo esperado (incluye abonos en efectivo) y cierre con diferencia.

**Inventario y compras**
- Stock por depósito, producto, presentación y lote; entradas, salidas, traslados y ajustes.
- Salida **FEFO**: sale primero el lote que vence antes, con descuento automático por cercanía al vencimiento.
- Proveedores, pedidos (borrador, enviado, parcial, recibido, cancelado) y recepciones que generan lotes y stock.

**Catálogo y precios**
- Categorías, productos, unidades de medida, presentaciones y listas de precios por tipo de cliente.

**Reportes**
- Ventas, caja, inventario, utilidad y compras, con rango de fechas y exportación a CSV.

**Seguridad**
- Login con JWT (token de acceso corto y refresh token), recuperación de contraseña por correo y revocación de sesiones.
- Roles y permisos; el frontend oculta menús, rutas y botones según los permisos y el backend siempre los valida.
- Roles predefinidos: `ADMIN`, `CAJERO`, `REPOSITOR` y `ENCARGADO_COMPRAS`.

---

## Arquitectura

**Backend: arquitectura hexagonal** organizada por módulos de negocio. Cada módulo tiene tres capas:

```
backend/src/main/java/com/tienda/backend/<modulo>/
├── domain/           # modelos y excepciones de negocio, sin dependencias de framework
├── application/      # casos de uso (port/in), puertos de salida (port/out) y servicios
└── infrastructure/   # adaptadores: REST (in) y persistencia (out)
```

Módulos: `auth`, `usuario`, `rol`, `permiso`, `categoria`, `producto`, `unidadmedida`, `presentacion`, `lote`, `deposito`, `inventario`, `proveedor`, `pedido`, `tipocliente`, `cliente`, `precio`, `metodopago`, `caja`, `venta`, `devolucion`, `credito`, `reporte`, `shared`.

- Esquema y datos base con **Flyway** (`db/migration`); Hibernate solo valida.
- Errores en formato `ProblemDetail` (JSON) con mensajes en español.
- Documentación interactiva de la API: Swagger UI en `http://localhost:8080/swagger-ui.html`.

**Frontend: Angular standalone** con carga diferida por módulo (`core/`, `features/`, `shared/`), guards de autenticación y permisos, interceptor que renueva la sesión y tema claro/oscuro.

---

## Requisitos

- **JDK 21**
- **Node.js** compatible con Angular 22 y npm 11
- **PostgreSQL** con una base de datos vacía
- Opcional: servidor SMTP de pruebas (Mailtrap o Mailpit) para el correo de recuperación de contraseña. Redis solo si usa `APP_REVOCACION=redis`.

---

## Puesta en marcha

### 1. Backend

```bash
cd backend
cp .env.example .env          # en Windows: copy .env.example .env
```

Edite `.env`. Variables principales:

| Variable | Descripción |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Conexión a PostgreSQL |
| `JWT_SECRET` | **Obligatorio**, mínimo 32 caracteres (`python -c "import secrets; print(secrets.token_urlsafe(48))"`) |
| `SPRING_PROFILES_ACTIVE` | `seed` para cargar datos de ejemplo en una base vacía (ver más abajo) |
| `MAIL_*` | Servidor SMTP (solo para «olvidé mi contraseña») |
| `CORS_ORIGENES` | Orígenes del frontend permitidos |

Cree la base de datos y arranque; Flyway crea las tablas:

```bash
createdb tienda_db            # use el nombre que puso en DB_NAME
./gradlew bootRun             # Windows: gradlew.bat bootRun
```

`bootRun` no lee el `.env` por sí solo: exporte las variables en la terminal o ejecute desde IntelliJ con la configuración `BackendApp` (carga `backend/.env` mediante el plugin EnvFile). La API queda en `http://localhost:8080`.

### 2. Frontend

```bash
cd frontend
npm install
npm start                     # ng serve con proxy de /api hacia :8080
```

Abra `http://localhost:4200`.

### 3. Datos de ejemplo (opcional)

Con `SPRING_PROFILES_ACTIVE=seed` y una **base vacía**, Flyway ejecuta además las migraciones repetibles de `backend/src/main/resources/db/seed/`:

- 50 categorías, 50 unidades de medida, 90 productos con presentaciones y precios.
- 50 depósitos, 50 cajas, 52 roles, 60 usuarios, 90 clientes (18 con crédito), 60 proveedores y 50 métodos de pago.
- Pedidos en todos los estados, turnos de caja abiertos y cerrados, 36 ventas, cuentas por cobrar, abonos y devoluciones.

Si la base ya tiene productos, las semillas no hacen nada. Todo lo sembrado lleva `created_by = 'SEED'`.

### 4. Primer ingreso

| Usuario | Para qué |
|---|---|
| `supervisor01` | Supervisor de caja: ventas, anulaciones, caja, créditos y reportes |
| `cajero01` | Cajero: POS y caja |
| `repositor01` | Inventario y recepciones |
| `compras01` | Proveedores y pedidos |

Todos los usuarios sembrados usan la contraseña `Admin#2026`. También existe el usuario `admin` (creado por la migración inicial, con la misma contraseña temporal, que el sistema obliga a cambiar al ingresar).

> Son credenciales de **desarrollo**. Cámbielas o no cargue las semillas en un entorno real.

---

## Pruebas

```bash
cd backend  && ./gradlew test
cd frontend && npm test
```

---

## Estructura del repositorio

```
.
├── backend/
│   ├── build.gradle
│   ├── scripts/seed/                     # semilla alternativa por API (Python)
│   └── src/main/
│       ├── java/com/tienda/backend/      # módulos hexagonales
│       └── resources/
│           ├── application*.properties
│           ├── db/migration/             # V1..V6: esquema y datos base
│           ├── db/seed/                  # R__*.sql: datos de ejemplo (perfil seed)
│           └── templates/                # plantillas de correo
└── frontend/
    ├── angular.json · proxy.conf.json · package.json
    └── src/app/{core,features,shared}
```
