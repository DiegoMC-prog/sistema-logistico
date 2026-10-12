# 📦 Sistema Logístico - Backend API

Backend del Sistema de Gestión Logística y Control de Inventarios, construido con **Java 21**, **Spring Boot**, **PostgreSQL** y **Arquitectura Hexagonal (Puertos y Adaptadores)**.

---

## 🛠️ Tecnologías y Requisitos

* **Java:** OpenJDK 21 (LTS) o superior
* **Framework:** Spring Boot 3.x / 4.x
* **Base de Datos:** PostgreSQL 14+
* **Gestor de Construcción:** Gradle (incluye wrapper `gradlew` / `gradlew.bat`)
* **Migraciones de BD:** Flyway
* **Seguridad:** Spring Security con JWT (JJWT 0.12.6)

---

## ⚙️ 1. Configuración de Variables de Entorno

El proyecto lee sus variables de configuración desde un archivo `.env` en la raíz de `backend/`.

1. Copia el archivo de ejemplo:
   ```bash
   cp .env.example .env
   ```
   *(En Windows PowerShell: `Copy-Item .env.example .env`)*

2. Configura los valores correspondientes en tu archivo `.env`:
   ```properties
   # Base de Datos PostgreSQL
   DB_HOST=localhost
   DB_PORT=5432
   DB_NAME=tienda_db
   DB_USER=postgres
   DB_PASSWORD=tu_password

   # Seguridad y JWT (clave en hexadecimal o texto largo mínimo de 256 bits)
   JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
   JWT_EXPIRATION_MS=86400000

   # Almacenamiento local de archivos / imágenes
   STORAGE_LOCATION=uploads
   MAX_FILE_SIZE=5MB
   MAX_REQUEST_SIZE=10MB
   ```

---

## 🐘 2. Base de Datos PostgreSQL

Asegúrate de tener un servidor PostgreSQL en ejecución y crea la base de datos configurada:

```sql
CREATE DATABASE tienda_db;
```

> **Nota sobre Flyway:** No es necesario ejecutar scripts SQL manualmente para crear tablas. Al iniciar la aplicación, Flyway aplicará automáticamente las migraciones versionadas (`V1` a `V5`) ubicadas en `src/main/resources/db/migration`.

---

## 🚀 3. Ejecución del Proyecto

### En Windows (PowerShell / CMD):
```powershell
.\gradlew.bat bootRun
```

### En Linux / macOS:
```bash
./gradlew bootRun
```

### O desde tu IDE (IntelliJ IDEA / VS Code / Eclipse):
* Ejecuta directamente la clase principal: `com.tienda.backend.BackendApplication`.

El servidor iniciará por defecto en el puerto: **`http://localhost:8080`**.

---

## 📂 Arquitectura del Proyecto

El código está estructurado bajo **Arquitectura Hexagonal**:

```text
src/main/java/com/tienda/backend/
├── domain/                      # 1. Capa de Dominio (Puro Java, sin frameworks)
│   ├── exception/               # Excepciones de negocio
│   ├── model/                   # Entidades y objetos de valor
│   └── repository/              # Puertos de salida (interfaces de repositorio)
│
├── application/                 # 2. Capa de Aplicación (Casos de uso)
│   ├── dto/                     # Request y Response DTOs
│   ├── mapper/                  # Mappers DTO <-> Dominio
│   └── service/                 # Interfaces e implementaciones de servicios
│
└── infrastructure/              # 3. Capa de Infraestructura (Adaptadores externos)
    ├── config/                  # Configuración Web y recursos estáticos
    ├── handler/                 # Manejador global de excepciones REST
    ├── persistence/             # Adaptadores JPA, Repositorios Spring Data, Entidades
    ├── security/                # Filtros JWT, UserDetailsService, SecurityConfig
    ├── storage/                 # Adaptador de almacenamiento de archivos en disco
    └── web/controller/          # Controladores REST (Adaptadores de entrada HTTP)
```

---

## 🛣️ Resumen de Endpoints Principales

Todos los endpoints (salvo `/api/v1/auth/**`, `/error` y `/uploads/**`) requieren la cabecera:
`Authorization: Bearer <token_jwt>`

### 🔐 Autenticación (`/api/v1/auth`)
* `POST /api/v1/auth/login`: Inicio de sesión (retorna token JWT y perfil).
* `POST /api/v1/auth/register`: Registro de nuevos usuarios.

### 🏷️ Categorías (`/api/v1/categorias`)
* `GET /api/v1/categorias/stats`: Estadísticas (`totalCategorias`, `categoriasEnUso`).
* `GET /api/v1/categorias`: Listado paginado con filtro `?nombre=&page=&size=`.
* `GET /api/v1/categorias/{id}`: Detalle por ID.
* `POST /api/v1/categorias`: Crear categoría.
* `PUT /api/v1/categorias/{id}`: Actualizar categoría.
* `DELETE /api/v1/categorias/{id}`: Eliminación lógica (soft-delete).

### 📦 Productos (`/api/v1/productos`)
* `GET /api/v1/productos/stats`: Estadísticas (`totalProductos`, `activos`, `inactivos`).
* `GET /api/v1/productos`: Listado paginado con filtros independientes `?busqueda=&categoriaId=&estado=&page=&size=`.
* `GET /api/v1/productos/{id}`: Detalle por ID.
* `POST /api/v1/productos`: Crear producto con imagen (`multipart/form-data`: part `producto` y part opcional `imagen`).
* `PUT /api/v1/productos/{id}`: Actualizar producto e imagen (`multipart/form-data`).
* `PATCH /api/v1/productos/{id}/estado?estado=true|false`: Cambiar estado activo/inactivo.
* `DELETE /api/v1/productos/{id}`: Eliminación lógica y limpieza de imagen física.

### 🏢 Depósitos (`/api/v1/depositos`)
* `GET /api/v1/depositos/stats`: Estadísticas (`totalDepositos`, `activos`, `inactivos`).
* `GET /api/v1/depositos`: Listado paginado con filtro `?nombre=&page=&size=`.
* `GET /api/v1/depositos/{id}`: Detalle por ID.
* `POST /api/v1/depositos`: Crear depósito.
* `PUT /api/v1/depositos/{id}`: Actualizar depósito.
* `PATCH /api/v1/depositos/{id}/estado?estado=true|false`: Cambiar estado.
* `DELETE /api/v1/depositos/{id}`: Eliminación lógica.

### 🚚 Proveedores (`/api/v1/proveedores`)
* `GET /api/v1/proveedores/stats`: Estadísticas (`totalProveedores`, `activos`, `inactivos`).
* `GET /api/v1/proveedores`: Listado paginado con filtro `?nombreEmpresa=&page=&size=`.
* `GET /api/v1/proveedores/{id}`: Detalle por ID.
* `POST /api/v1/proveedores`: Crear proveedor.
* `PUT /api/v1/proveedores/{id}`: Actualizar proveedor.
* `PATCH /api/v1/proveedores/{id}/estado?estado=true|false`: Cambiar estado.
* `DELETE /api/v1/proveedores/{id}`: Eliminación lógica.

### 👥 Usuarios (`/api/v1/usuarios`) *(Requiere rol `ROLE_ADMIN`)*
* `GET /api/v1/usuarios`: Listado paginado.
* `GET /api/v1/usuarios/{id}`: Detalle por ID.
* `PUT /api/v1/usuarios/{id}`: Actualizar perfil de usuario.
* `PATCH /api/v1/usuarios/{id}/password`: Cambiar contraseña.
* `PATCH /api/v1/usuarios/{id}/estado?estado=true|false`: Habilitar/deshabilitar cuenta.
* `DELETE /api/v1/usuarios/{id}`: Eliminación lógica.

### 🖼️ Archivos e Imágenes Estáticas (`/uploads/**`)
* `GET /uploads/productos/{nombre_archivo}`: Acceso directo público a las imágenes almacenadas.
