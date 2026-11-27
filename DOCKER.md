# Ejecutar con Docker

Levanta la aplicación completa: PostgreSQL 17, backend (Spring Boot), frontend (Angular servido por Nginx) y un servidor de correo de pruebas (Mailpit). No hace falta instalar Java, Node ni PostgreSQL.

```
Navegador → web (Nginx + Angular) :80
                 └── /api → backend :8080 → db (PostgreSQL) y mail (Mailpit)
```

En Windows con Docker dentro de WSL, ejecute los comandos desde la terminal de WSL (la carpeta del proyecto está en `/mnt/<unidad>/...`).

## Primer arranque

```bash
cp .env.docker.example .env      # complete DB_PASSWORD y JWT_SECRET
docker compose up -d --build     # la primera vez tarda varios minutos
```

| Qué | Dónde |
|---|---|
| Aplicación | http://localhost |
| Correos enviados (recuperar contraseña, etc.) | http://localhost:8025 |

Usuario inicial: `admin` / `Admin#2026` (el sistema obliga a cambiarla). Cámbiela siempre antes de usar el sistema de verdad.

Toda la configuración está en `.env`. Si el puerto 80 está ocupado, ponga `WEB_PORT=8081` y ajuste `FRONTEND_URL_RESTABLECER` y `CORS_ORIGENES` a `http://localhost:8081`. Después de cambiar `.env`, ejecute `docker compose up -d`.

## Operación

```bash
docker compose ps                  # estado
docker compose logs -f backend     # registros
docker compose up -d --build       # aplicar cambios del código
docker compose down                # detener (los datos se conservan)
docker compose down -v             # detener y BORRAR la base de datos
```

Respaldo de la base:

```bash
docker compose exec -T db pg_dump -U postgres -Fc tienda_db > respaldo.dump
```

## Datos de ejemplo

En `.env` agregue `SPRING_PROFILES_ACTIVE=seed` **antes del primer arranque** (base vacía). Usuarios sembrados (contraseña `Admin#2026`): `supervisor01`, `cajero01`, `repositor01`, `compras01`.

Una base creada con el perfil `seed` necesita seguir usándolo; sin él, Flyway falla con `Detected applied migration not resolved locally`.

## Base de carga (10 000 ventas)

1. En `.env`: `DB_NAME=tienda_db_carga` y `SPRING_PROFILES_ACTIVE=seed`.
2. Empiece de cero (borra los datos actuales): `docker compose down -v && docker compose up -d --build`.
3. Cuando `docker compose ps` muestre el backend `healthy`:

```bash
docker compose cp backend/scripts/seed/. db:/tmp/seed
docker compose exec db psql -U postgres -d tienda_db_carga -v ON_ERROR_STOP=1 -f /tmp/seed/seed_carga.sql
docker compose exec db psql -U postgres -d tienda_db_carga -f /tmp/seed/seed_carga_verificacion.sql
```

Las 14 comprobaciones deben decir `OK`. Entre con `compras01` y pulse «Recalcular» en `/abastecimiento`.

## Demostración por Internet (Cloudflare Tunnel, sin dominio)

```bash
docker compose --profile demo up -d
docker compose logs tunel | grep -o 'https://[a-z0-9-]*\.trycloudflare\.com' | tail -1
```

La segunda orden muestra la dirección pública (tarda unos segundos en aparecer). **Cambia cada vez que el túnel se reinicia.**

Para cerrar el acceso desde Internet, dejando la aplicación local encendida:

```bash
docker compose --profile demo stop tunel
```

Antes de compartir la dirección:

- Los usuarios sembrados y `admin` usan la contraseña `Admin#2026`, publicada en el repositorio. Cambie al menos la de `admin` y apague el túnel al terminar.
- Los correos siguen llegando a Mailpit (solo visible en este equipo) y su enlace apunta a `localhost`: muestre la recuperación de contraseña desde el equipo.
- El equipo debe seguir encendido y con una terminal de WSL abierta.

## Alcance

Pensado para ejecutarse en un equipo local o en la red de la tienda. Para publicarlo en Internet faltan HTTPS, respaldos programados y credenciales propias.
