-- ============================================================================
-- SEMILLA DE DESARROLLO (2/3): depositos, cajas, roles, usuarios, tipos de cliente, clientes,
-- proveedores y metodos de pago. Minimo 50 registros por tabla padre.
--
-- Contrasena de todos los usuarios sembrados: Admin#2026 (la misma del usuario admin inicial).
-- Idempotente: se omite si ya hay datos sembrados o si falta el catalogo sembrado.
-- ============================================================================
DO $seed$
DECLARE
    v_hash CONSTANT text := '$2a$10$T8UudTC9A/EMPgdnXHRO9eJLeTFuC9hrdqaRGLymJGUCdb.1B8fJ6';
    v_zonas text[] := ARRAY['CENTRO', 'NORTE', 'SUR', 'ESTE', 'OESTE', 'MERCADO', 'TERMINAL', 'UNIVERSIDAD',
                            'AEROPUERTO', 'COSTANERA', 'INDUSTRIAL', 'VALLE'];
    v_nombres text[] := ARRAY['Carlos', 'María', 'José', 'Ana', 'Luis', 'Lucía', 'Jorge', 'Carmen', 'Pedro', 'Rosa',
                              'Miguel', 'Elena', 'Andrés', 'Patricia', 'Fernando', 'Sofía', 'Ricardo', 'Valeria',
                              'Diego', 'Gabriela', 'Javier', 'Daniela', 'Alberto', 'Paola', 'Héctor', 'Mónica',
                              'Raúl', 'Verónica', 'Esteban', 'Silvia'];
    v_apellidos text[] := ARRAY['Pérez', 'González', 'Rodríguez', 'Fernández', 'López', 'Martínez', 'Sánchez', 'Romero',
                                'Torres', 'Ramírez', 'Flores', 'Vargas', 'Castillo', 'Mendoza', 'Herrera', 'Medina',
                                'Aguilar', 'Rojas', 'Ortiz', 'Silva', 'Cruz', 'Morales', 'Núñez', 'Paredes', 'Salazar',
                                'Cabrera', 'Villavicencio', 'Espinoza', 'Zambrano', 'Guerrero'];
BEGIN
    IF NOT EXISTS (SELECT 1 FROM productos WHERE created_by = 'SEED') THEN
        RAISE NOTICE 'Semilla de organizacion omitida: falta el catalogo sembrado.';
        RETURN;
    END IF;
    IF EXISTS (SELECT 1 FROM usuarios WHERE created_by = 'SEED') THEN
        RAISE NOTICE 'Semilla de organizacion omitida: ya esta sembrada.';
        RETURN;
    END IF;

    -- ---------------------------------------------------------------- depositos (50)
    -- Los 12 primeros son pisos de venta (los ids 1 a 3 son los que venden en la semilla de flujo).
    INSERT INTO depositos (nombre, descripcion, estado, created_by, updated_by)
    SELECT 'Sucursal ' || initcap(z.zona) || ' - ' || k.tipo,
           k.tipo || ' de la sucursal ' || initcap(z.zona),
           CASE WHEN k.ord = 3 AND z.ord > 10 THEN 'INACTIVO' ELSE 'ACTIVO' END,
           'SEED', 'SEED'
    FROM unnest(v_zonas) WITH ORDINALITY AS z(zona, ord)
    CROSS JOIN unnest(ARRAY['Piso de Venta', 'Bodega', 'Cámara Fría', 'Almacén Seco']) WITH ORDINALITY AS k(tipo, ord)
    ORDER BY k.ord, z.ord;

    INSERT INTO depositos (nombre, descripcion, estado, created_by, updated_by) VALUES
        ('Bodega Central', 'Centro de acopio principal de la cadena', 'ACTIVO', 'SEED', 'SEED'),
        ('Depósito Mayorista', 'Ventas por volumen a restaurantes y tiendas', 'ACTIVO', 'SEED', 'SEED');

    -- ---------------------------------------------------------------- cajas (50)
    -- Las cajas se reparten entre los 12 pisos de venta.
    INSERT INTO cajas (nombre, deposito_id, estado, created_by, updated_by)
    SELECT 'Caja ' || lpad(n::text, 2, '0'),
           (SELECT id FROM depositos WHERE created_by = 'SEED' ORDER BY id OFFSET ((n - 1) % 12) LIMIT 1),
           CASE WHEN n % 17 = 0 THEN 'INACTIVO' ELSE 'ACTIVO' END,
           'SEED', 'SEED'
    FROM generate_series(1, 50) AS n;

    -- ---------------------------------------------------------------- roles (52 = 4 base + 48 por sucursal)
    INSERT INTO roles (nombre, created_by, updated_by)
    SELECT p.puesto || ' ' || z.zona, 'SEED', 'SEED'
    FROM unnest(ARRAY['CAJERO', 'REPOSITOR', 'SUPERVISOR DE CAJA', 'ENCARGADO DE SUCURSAL']) AS p(puesto)
    CROSS JOIN unnest(v_zonas) AS z(zona);

    -- Cajeros y repositores heredan los permisos de los roles base de V5
    INSERT INTO rol_permiso (rol_id, permiso_id)
    SELECT r.id, rp.permiso_id
    FROM roles r
    JOIN roles base ON base.nombre = split_part(r.nombre, ' ', 1) AND base.created_by <> 'SEED'
    JOIN rol_permiso rp ON rp.rol_id = base.id
    WHERE r.created_by = 'SEED' AND (r.nombre LIKE 'CAJERO %' OR r.nombre LIKE 'REPOSITOR %');

    -- Supervisor de caja: lo del cajero + supervision, anulaciones, precios y reportes de caja
    INSERT INTO rol_permiso (rol_id, permiso_id)
    SELECT r.id, p.id
    FROM roles r
    JOIN permisos p ON p.nombre IN ('CAJA_ABRIR', 'CAJA_CERRAR', 'CAJA_MOVIMIENTO', 'CAJA_SUPERVISAR',
                                    'VENTA_VER', 'VENTA_CREAR', 'VENTA_ANULAR', 'VENTA_DEVOLVER',
                                    'VENTA_PRECIO_MODIFICAR', 'REPORTE_VENTAS', 'REPORTE_CAJA',
                                    'CREDITO_GESTIONAR', 'INVENTARIO_VER', 'CLIENTE_CREAR', 'CLIENTE_EDITAR')
    WHERE r.created_by = 'SEED' AND r.nombre LIKE 'SUPERVISOR DE CAJA %';

    -- Encargado de sucursal: todo menos administrar usuarios y roles
    INSERT INTO rol_permiso (rol_id, permiso_id)
    SELECT r.id, p.id
    FROM roles r
    CROSS JOIN permisos p
    WHERE r.created_by = 'SEED' AND r.nombre LIKE 'ENCARGADO DE SUCURSAL %'
      AND p.modulo NOT IN ('USUARIO', 'ROL');

    -- ---------------------------------------------------------------- usuarios (60)
    -- 1-24 cajeros, 25-36 repositores, 37-42 supervisores, 43-48 compras, 49-60 encargados de sucursal
    INSERT INTO usuarios (username, nombre, email, password, password_temporal, estado, created_by, updated_by)
    SELECT u.username,
           v_nombres[1 + (n * 7) % 30] || ' ' || v_apellidos[1 + (n * 11) % 30] || ' ' || v_apellidos[1 + (n * 13 + 5) % 30],
           u.username || '@tienda.local', v_hash, false,
           CASE WHEN n IN (24, 36, 60) THEN 'INACTIVO' ELSE 'ACTIVO' END,
           'SEED', 'SEED'
    FROM (SELECT n,
                 CASE WHEN n <= 24 THEN 'cajero' || lpad(n::text, 2, '0')
                      WHEN n <= 36 THEN 'repositor' || lpad((n - 24)::text, 2, '0')
                      WHEN n <= 42 THEN 'supervisor' || lpad((n - 36)::text, 2, '0')
                      WHEN n <= 48 THEN 'compras' || lpad((n - 42)::text, 2, '0')
                      ELSE 'encargado' || lpad((n - 48)::text, 2, '0') END AS username
          FROM generate_series(1, 60) AS n) u
    ORDER BY n;

    INSERT INTO usuario_rol (usuario_id, rol_id, assigned_by)
    SELECT u.id, r.id, 'SEED'
    FROM usuarios u
    JOIN roles r ON r.created_by = 'SEED' AND r.nombre =
        CASE
            WHEN u.username LIKE 'cajero%' THEN 'CAJERO ' || v_zonas[1 + (substr(u.username, 7)::int - 1) % 12]
            WHEN u.username LIKE 'repositor%' THEN 'REPOSITOR ' || v_zonas[1 + (substr(u.username, 10)::int - 1) % 12]
            WHEN u.username LIKE 'supervisor%' THEN 'SUPERVISOR DE CAJA ' || v_zonas[1 + (substr(u.username, 11)::int - 1) % 12]
            WHEN u.username LIKE 'encargado%' THEN 'ENCARGADO DE SUCURSAL ' || v_zonas[1 + (substr(u.username, 10)::int - 1) % 12]
        END
    WHERE u.created_by = 'SEED' AND u.username NOT LIKE 'compras%';

    INSERT INTO usuario_rol (usuario_id, rol_id, assigned_by)
    SELECT u.id, r.id, 'SEED'
    FROM usuarios u JOIN roles r ON r.nombre = 'ENCARGADO_COMPRAS'
    WHERE u.created_by = 'SEED' AND u.username LIKE 'compras%';

    -- ---------------------------------------------------------------- tipos de cliente (3 base + 50)
    INSERT INTO tipos_cliente (nombre, estado, created_by, updated_by)
    SELECT upper(s.sector) || ' ' || upper(n.nivel),
           CASE WHEN n.nivel = 'Institucional' AND s.sector IN ('Iglesia', 'Cooperativa') THEN 'INACTIVO' ELSE 'ACTIVO' END,
           'SEED', 'SEED'
    FROM unnest(ARRAY['Restaurante', 'Hotel', 'Escuela', 'Hospital', 'Panadería', 'Cafetería', 'Tienda de Barrio',
                      'Catering', 'Iglesia', 'Cooperativa']) AS s(sector)
    CROSS JOIN unnest(ARRAY['Estándar', 'Preferencial', 'Premium', 'Corporativo', 'Institucional']) AS n(nivel);

    -- ---------------------------------------------------------------- clientes (90)
    -- 60 personas y 30 negocios. Un cliente de cada 5 tiene credito (fiado) con limite entre 300 y 2000.
    INSERT INTO clientes (tipo_cliente_id, documento, nombre, telefono, email, estado, credito_habilitado, limite_credito,
                          created_by, updated_by)
    SELECT
        CASE WHEN n <= 60 THEN
                 (SELECT id FROM tipos_cliente WHERE nombre = CASE WHEN n % 5 = 0 THEN 'FRECUENTE' ELSE 'GENERAL' END)
             WHEN n <= 70 THEN (SELECT id FROM tipos_cliente WHERE nombre = 'MAYORISTA')
             ELSE (SELECT id FROM tipos_cliente WHERE created_by = 'SEED' ORDER BY id OFFSET ((n - 71) * 2) % 50 LIMIT 1) END,
        CASE WHEN n <= 60 THEN '10' || lpad(((n * 104729) % 100000000)::text, 8, '0')
             ELSE '17' || lpad(((n * 15485863) % 100000000)::text, 8, '0') || '001' END,
        CASE WHEN n <= 60 THEN v_nombres[1 + (n * 3) % 30] || ' ' || v_apellidos[1 + (n * 5) % 30] || ' ' || v_apellidos[1 + (n * 17 + 3) % 30]
             ELSE (ARRAY['Restaurante El Sabor', 'Hotel Plaza Mayor', 'Panadería La Espiga', 'Cafetería Aroma',
                         'Tienda Doña Rosa', 'Catering Los Andes', 'Escuela Santa María', 'Clínica San Gabriel',
                         'Minimarket El Ahorro', 'Pizzería Don Luigi', 'Hostal Mirador', 'Asadero El Fogón',
                         'Cevichería Mar Azul', 'Pastelería Dulce Hogar', 'Comedor Universitario', 'Hotel Costa Brava',
                         'Tienda La Esquina', 'Frigorífico San Pedro', 'Cooperativa Agrícola Sur',
                         'Restaurante La Hacienda', 'Heladería Polar', 'Bar El Rincón', 'Colegio San José',
                         'Hospital del Norte', 'Distribuidora Los Pinos', 'Café Literario', 'Sushi Bar Kyoto',
                         'Fonda Doña Marta', 'Tienda Mi Barrio', 'Catering Eventos Premium'])[n - 60] END,
        '09' || lpad(((n * 7919) % 100000000)::text, 8, '0'),
        CASE WHEN n % 3 = 0 THEN NULL
             ELSE 'cliente' || lpad(n::text, 3, '0') || '@' || (ARRAY['gmail.com', 'hotmail.com', 'outlook.com', 'empresa.com'])[1 + n % 4] END,
        CASE WHEN n % 23 = 0 THEN 'INACTIVO' ELSE 'ACTIVO' END,
        n % 5 = 0,
        CASE WHEN n % 5 = 0 THEN (ARRAY[300, 500, 800, 1000, 1500, 2000])[1 + n % 6] ELSE 0 END,
        'SEED', 'SEED'
    FROM generate_series(1, 90) AS n;

    -- ---------------------------------------------------------------- proveedores (60)
    INSERT INTO proveedores (nombre_empresa, nombre_contacto, telefono, email, activo, created_by, updated_by)
    SELECT g.prefijo || ' ' || g.sufijo,
           v_nombres[1 + (g.n * 9) % 30] || ' ' || v_apellidos[1 + (g.n * 7) % 30],
           '02' || lpad(((g.n * 31337) % 10000000)::text, 7, '0'),
           'ventas@' || lower(replace(translate(g.prefijo || g.sufijo, 'áéíóúÁÉÍÓÚñ ', 'aeiouaeioun-'), '-', '')) || '.com',
           g.n % 20 <> 0,
           'SEED', 'SEED'
    FROM (SELECT row_number() OVER (ORDER BY pf.o, sf.o)::int AS n, pf.prefijo, sf.sufijo
          FROM unnest(ARRAY['Distribuidora', 'Importadora', 'Comercial', 'Industrias', 'Agroindustrial', 'Alimentos',
                            'Lácteos', 'Cárnicos', 'Bebidas', 'Abastecedora']) WITH ORDINALITY AS pf(prefijo, o)
          CROSS JOIN unnest(ARRAY['del Valle', 'Andina', 'San Miguel', 'La Esperanza', 'del Pacífico', 'El Progreso'])
                     WITH ORDINALITY AS sf(sufijo, o)) g
    ORDER BY g.n;

    -- ---------------------------------------------------------------- metodos de pago (5 base + 45)
    INSERT INTO metodos_pago (nombre, estado, created_by, updated_by)
    SELECT t.tipo || ' ' || b.banco, CASE WHEN b.ord = 15 THEN 'INACTIVO' ELSE 'ACTIVO' END, 'SEED', 'SEED'
    FROM unnest(ARRAY['TARJETA CRÉDITO', 'TARJETA DÉBITO', 'TRANSFERENCIA']) AS t(tipo)
    CROSS JOIN unnest(ARRAY['PICHINCHA', 'GUAYAQUIL', 'PACÍFICO', 'PRODUBANCO', 'BOLIVARIANO', 'INTERNACIONAL',
                            'AUSTRO', 'MACHALA', 'SOLIDARIO', 'COOPERATIVA JEP', 'COOPERATIVA 29 DE OCTUBRE',
                            'BANCO DEL BARRIO', 'LOJA', 'AMAZONAS', 'PROCREDIT']) WITH ORDINALITY AS b(banco, ord);
END
$seed$;
