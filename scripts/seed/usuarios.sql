-- Usuarios de desarrollo. Contrasena de todos: Admin#2026 (el mismo hash que el admin de la semilla V1).
-- Se insertan por SQL porque la API envia un correo con una contrasena temporal por cada usuario creado.
-- Ejecutar DESPUES de la fase 1 (los roles ya existen) y ANTES de la fase 2.

INSERT INTO usuarios (username, nombre, email, password, estado, password_temporal, created_by, updated_by)
SELECT x.username, x.nombre, x.username || '@tienda.local',
       '$2a$10$T8UudTC9A/EMPgdnXHRO9eJLeTFuC9hrdqaRGLymJGUCdb.1B8fJ6', 'ACTIVO', FALSE, 'SEMILLA', 'SEMILLA'
FROM (VALUES
    ('cajero1', 'Carlos Mamani'), ('cajero2', 'Ana Quispe'), ('cajero3', 'Luis Flores'), ('cajero4', 'Rosa Choque'),
    ('supervisor1', 'Marcos Vargas'), ('supervisor2', 'Lidia Rojas'), ('encargado1', 'Pedro Salinas'),
    ('almacen1', 'Jorge Torrez'), ('comprador1', 'Silvia Mendez'), ('gerente1', 'Patricia Aguilar'),
    ('auditor1', 'Hugo Castro'), ('contador1', 'Elena Rios'), ('vendedor1', 'Daniel Paz')
) AS x(username, nombre);

INSERT INTO usuario_rol (usuario_id, rol_id, assigned_by)
SELECT u.id, r.id, 'SEMILLA'
FROM (VALUES
    ('cajero1', 'CAJERO'), ('cajero2', 'CAJERO'), ('cajero3', 'CAJERO'), ('cajero4', 'CAJERO'),
    ('supervisor1', 'SUPERVISOR'), ('supervisor2', 'SUPERVISOR'), ('encargado1', 'ENCARGADO_TURNO'),
    ('almacen1', 'ALMACENERO'), ('comprador1', 'COMPRADOR'), ('gerente1', 'GERENTE'),
    ('auditor1', 'AUDITOR'), ('contador1', 'CONTADOR'), ('vendedor1', 'VENDEDOR')
) AS m(username, rol)
JOIN usuarios u ON u.username = m.username
JOIN roles r ON r.nombre = m.rol;
