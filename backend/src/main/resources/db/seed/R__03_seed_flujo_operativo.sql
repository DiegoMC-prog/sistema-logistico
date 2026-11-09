-- ============================================================================
-- SEMILLA DE DESARROLLO (3/3): flujo operativo coherente sobre el catalogo y la organizacion sembrados.
--
--   * Inventario inicial en los 3 primeros pisos de venta + traslados, ajustes y salidas
--   * 15 pedidos a proveedor (borrador, enviado, parcial, recibido y cancelado) con sus recepciones y lotes
--     (los costos suben de un pedido a otro: sirven para ver la evolucion del costo)
--   * 12 turnos de caja (9 cerrados con diferencias de cuadre y 3 abiertos) y 12 movimientos de caja
--   * 36 ventas (2 anuladas, 8 a credito, 3 con precio manual) con consumo FEFO de lotes y movimientos de stock
--   * 10 abonos de deuda, 8 cuentas por cobrar y 10 devoluciones
--
-- Todo el stock y el efectivo esperado de los turnos cierran con las ventas, devoluciones y abonos.
-- Idempotente: se omite si ya hay pedidos sembrados o si falta la organizacion sembrada.
-- ============================================================================

CREATE OR REPLACE FUNCTION pg_temp.seed_stock(p_dep bigint, p_lote bigint, p_delta numeric)
RETURNS void LANGUAGE plpgsql AS $f$
BEGIN
    -- UPDATE primero: un INSERT con delta negativo violaria el CHECK antes de resolver el conflicto
    UPDATE inventarios SET stock_actual = stock_actual + p_delta, updated_at = now()
    WHERE deposito_id = p_dep AND lote_id = p_lote AND deleted_at IS NULL;
    IF NOT FOUND THEN
        INSERT INTO inventarios (deposito_id, lote_id, stock_actual, created_by, updated_by)
        VALUES (p_dep, p_lote, p_delta, 'SEED', 'SEED');
    END IF;
END
$f$;

DO $seed$
DECLARE
    v_deps bigint[];
    v_admin bigint;
    v_compras bigint[];
    v_repos bigint[];
    v_cajeros bigint[];
    v_prods bigint[];
    v_turnos bigint[] := ARRAY[]::bigint[];
    v_credit_clients bigint[];
    v_credit_ventas int[] := ARRAY[4, 9, 13, 17, 22, 26, 30, 35];
    v_motivos_devolucion text[] := ARRAY['Producto en mal estado', 'Cliente se equivocó de producto', 'Empaque dañado',
        'Producto próximo a vencer', 'Cobro duplicado de una unidad', 'Cliente no quedó conforme', 'Producto dañado',
        'Error de pesaje', 'Cambio de opinión del cliente', 'Producto con sabor alterado'];
    m_efe bigint;
    m_tar bigint;
    m_cre bigint;

    v_ped bigint; v_det bigint; v_rec bigint; v_mov bigint; v_lote bigint; v_venta bigint; v_vdet bigint; v_dev bigint;
    v_abono bigint; v_tid bigint;
    v_estado text; v_fecha date; v_fecha_rec timestamp; v_ts timestamp;
    v_n_rec int; v_q numeric; v_cost numeric; v_stock_min numeric;
    v_dep bigint; v_caja bigint; v_user bigint; v_apertura timestamp; v_cierre timestamp;
    v_cliente bigint; v_tipo_cliente bigint; v_pres bigint; v_price numeric;
    v_qty numeric; v_remaining numeric; v_take numeric; v_line_desc numeric;
    v_sub numeric; v_desc numeric; v_total numeric; v_recibido numeric; v_vuelto numeric; v_cred numeric;
    v_idx int; v_n_items int; v_item int; v_numero text; v_metodo bigint; v_nombre text;
    v_ndeb int; v_debt numeric; v_amt numeric; v_restante numeric; v_aplicado numeric; v_cli bigint;
    v_dev_n int := 0; v_manual boolean;
    k int; j int; e int; t int; v int; a int;
    r record; lt record; pr record; ln record;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM usuarios WHERE created_by = 'SEED') THEN
        RAISE NOTICE 'Semilla de flujo omitida: falta la organizacion sembrada.';
        RETURN;
    END IF;
    IF EXISTS (SELECT 1 FROM pedidos_proveedor WHERE created_by = 'SEED') THEN
        RAISE NOTICE 'Semilla de flujo omitida: ya esta sembrada.';
        RETURN;
    END IF;

    PERFORM setseed(0.42);

    SELECT array_agg(id ORDER BY id) INTO v_deps
    FROM (SELECT id FROM depositos WHERE created_by = 'SEED' ORDER BY id LIMIT 3) d;
    SELECT COALESCE((SELECT id FROM usuarios WHERE username = 'admin'), (SELECT min(id) FROM usuarios)) INTO v_admin;
    SELECT array_agg(id ORDER BY username) INTO v_compras FROM usuarios WHERE username LIKE 'compras%' AND estado = 'ACTIVO';
    SELECT array_agg(id ORDER BY username) INTO v_repos FROM usuarios WHERE username LIKE 'repositor%' AND estado = 'ACTIVO';
    SELECT array_agg(id ORDER BY username) INTO v_cajeros FROM usuarios WHERE username LIKE 'cajero%' AND estado = 'ACTIVO';
    SELECT array_agg(id ORDER BY id) INTO v_prods FROM productos WHERE created_by = 'SEED';
    SELECT id INTO m_efe FROM metodos_pago WHERE nombre = 'EFECTIVO';
    SELECT id INTO m_tar FROM metodos_pago WHERE nombre = 'TARJETA';
    SELECT id INTO m_cre FROM metodos_pago WHERE nombre = 'CREDITO';
    SELECT array_agg(id ORDER BY id) INTO v_credit_clients
    FROM (SELECT id FROM clientes WHERE created_by = 'SEED' AND credito_habilitado AND estado = 'ACTIVO'
          ORDER BY id LIMIT 6) c;

    -- ======================================================= A. inventario inicial
    CREATE TEMP TABLE seed_ini (dep bigint, lote bigint, prod bigint, qty numeric) ON COMMIT DROP;
    INSERT INTO seed_ini
    SELECT d.dep, l.id, l.producto_id,
           GREATEST(CEIL(p.stock_minimo * (3.0 - 0.6 * (d.ord - 1)) * (0.8 + random() * 0.6)), 10)
    FROM unnest(v_deps) WITH ORDINALITY AS d(dep, ord)
    CROSS JOIN lotes l
    JOIN productos p ON p.id = l.producto_id
    WHERE l.numero_lote LIKE 'INI-%' AND l.created_by = 'SEED';

    INSERT INTO inventarios (deposito_id, lote_id, stock_actual, created_by, updated_by)
    SELECT dep, lote, qty, 'SEED', 'SEED' FROM seed_ini;

    FOR k IN 1..3 LOOP
        INSERT INTO movimientos_inventarios (tipo_movimiento, deposito_destino_id, usuario_id, fecha_movimiento,
                                             observacion, referencia_tipo)
        VALUES ('ENTRADA', v_deps[k], v_admin, now() - interval '45 days', 'Inventario inicial de la sucursal',
                'INVENTARIO_INICIAL')
        RETURNING id INTO v_mov;
        INSERT INTO movimiento_inventario_detalle (movimiento_id, producto_id, lote_id, cantidad)
        SELECT v_mov, prod, lote, qty FROM seed_ini WHERE dep = v_deps[k];
    END LOOP;

    -- ======================================================= B. pedidos a proveedor y recepciones
    FOR k IN 1..15 LOOP
        v_estado := CASE WHEN k <= 6 THEN 'RECIBIDO' WHEN k <= 9 THEN 'RECIBIDO_PARCIAL' WHEN k <= 12 THEN 'ENVIADO'
                         WHEN k <= 14 THEN 'BORRADOR' ELSE 'CANCELADO' END;
        v_fecha := CURRENT_DATE - (50 - 3 * k);
        INSERT INTO pedidos_proveedor (numero_pedido, proveedor_id, deposito_destino_id, estado, fecha_pedido,
                                       fecha_entrega_estimada, observacion, total, created_by, updated_by,
                                       created_at, updated_at)
        VALUES ('PP-' || lpad(nextval('seq_numero_pedido_proveedor')::text, 8, '0'),
                (SELECT id FROM proveedores WHERE created_by = 'SEED' ORDER BY id OFFSET k - 1 LIMIT 1),
                v_deps[1 + (k - 1) % 3], v_estado, v_fecha,
                CASE WHEN v_estado = 'BORRADOR' THEN NULL ELSE v_fecha + 4 END,
                CASE v_estado WHEN 'CANCELADO' THEN 'Cancelado: el proveedor no tenía stock disponible'
                              WHEN 'BORRADOR' THEN 'Pendiente de confirmar cantidades con el proveedor'
                              WHEN 'ENVIADO' THEN 'Entrega coordinada para esta semana'
                              ELSE 'Reposición programada' END,
                0, 'SEED', 'SEED', v_fecha + time '09:00', v_fecha + time '09:00')
        RETURNING id INTO v_ped;

        -- 6 productos por pedido: el pedido k cubre los productos 6k-5 .. 6k. Cada pedido sube el costo un poco mas.
        FOR j IN 1..6 LOOP
            SELECT p.stock_minimo, l.costo_unitario_compra * (1.04 + 0.03 * (k % 5))
              INTO v_stock_min, v_cost
            FROM productos p JOIN lotes l ON l.producto_id = p.id AND l.numero_lote LIKE 'INI-%'
            WHERE p.id = v_prods[(k - 1) * 6 + j];
            INSERT INTO pedido_proveedor_detalle (pedido_id, producto_id, cantidad_pedida, cantidad_recibida, costo_unitario)
            VALUES (v_ped, v_prods[(k - 1) * 6 + j], GREATEST(CEIL(v_stock_min * 2), 12), 0, round(v_cost, 4));
        END LOOP;

        v_n_rec := CASE WHEN k <= 4 THEN 1 WHEN k <= 6 THEN 2 WHEN k <= 9 THEN 1 ELSE 0 END;
        FOR e IN 1..v_n_rec LOOP
            v_fecha_rec := (v_fecha + (2 + (k + e) % 4)) + time '10:00' + (e - 1) * interval '3 days';
            INSERT INTO recepciones_pedido (pedido_id, usuario_id, fecha_recepcion, numero_guia, observacion)
            VALUES (v_ped, v_repos[1 + (k + e) % array_length(v_repos, 1)], v_fecha_rec,
                    'GR-' || lpad((k * 10 + e)::text, 6, '0'),
                    CASE WHEN v_estado = 'RECIBIDO_PARCIAL' THEN 'Entrega parcial, el resto llega la próxima semana'
                         WHEN v_n_rec = 2 THEN 'Entrega ' || e || ' de 2' ELSE 'Entrega completa' END)
            RETURNING id INTO v_rec;

            INSERT INTO movimientos_inventarios (tipo_movimiento, deposito_destino_id, usuario_id, fecha_movimiento,
                                                 observacion, referencia_tipo, referencia_id)
            VALUES ('ENTRADA', v_deps[1 + (k - 1) % 3], v_repos[1 + (k + e) % array_length(v_repos, 1)], v_fecha_rec,
                    'Recepción del pedido', 'PEDIDO_PROVEEDOR', v_ped)
            RETURNING id INTO v_mov;

            FOR r IN SELECT d.id, d.producto_id, d.cantidad_pedida, d.costo_unitario, p.es_perecedero
                     FROM pedido_proveedor_detalle d JOIN productos p ON p.id = d.producto_id
                     WHERE d.pedido_id = v_ped ORDER BY d.id LOOP
                v_q := CASE WHEN v_estado = 'RECIBIDO_PARCIAL' THEN floor(r.cantidad_pedida * 0.6)
                            WHEN v_n_rec = 1 THEN r.cantidad_pedida
                            WHEN e = 1 THEN floor(r.cantidad_pedida * 0.6)
                            ELSE r.cantidad_pedida - floor(r.cantidad_pedida * 0.6) END;
                INSERT INTO lotes (producto_id, numero_lote, fecha_vencimiento, costo_unitario_compra,
                                   porcentaje_descuento_vencimiento, created_by, updated_by)
                VALUES (r.producto_id,
                        'REC-' || lpad(k::text, 2, '0') || '-' || lpad(r.producto_id::text, 5, '0') || '-' || e,
                        CASE WHEN r.es_perecedero THEN CURRENT_DATE + 25 + ((r.producto_id * 3 + k) % 45)::int END,
                        r.costo_unitario, 0, 'SEED', 'SEED')
                RETURNING id INTO v_lote;
                INSERT INTO recepcion_pedido_detalle (recepcion_id, pedido_detalle_id, lote_id, cantidad)
                VALUES (v_rec, r.id, v_lote, v_q);
                UPDATE pedido_proveedor_detalle SET cantidad_recibida = cantidad_recibida + v_q WHERE id = r.id;
                PERFORM pg_temp.seed_stock(v_deps[1 + (k - 1) % 3], v_lote, v_q);
                INSERT INTO movimiento_inventario_detalle (movimiento_id, producto_id, lote_id, cantidad)
                VALUES (v_mov, r.producto_id, v_lote, v_q);
            END LOOP;
        END LOOP;

        UPDATE pedidos_proveedor
        SET total = (SELECT sum(cantidad_pedida * costo_unitario) FROM pedido_proveedor_detalle WHERE pedido_id = v_ped)
        WHERE id = v_ped;
    END LOOP;

    -- ======================================================= C. otros movimientos de inventario
    -- 4 traslados entre pisos de venta (los lotes con vencimiento lejano)
    v_idx := 0;
    FOR r IN SELECT i.lote_id, l.producto_id FROM inventarios i JOIN lotes l ON l.id = i.lote_id
             WHERE i.deposito_id = v_deps[1] AND i.stock_actual >= 20
               AND (l.fecha_vencimiento IS NULL OR l.fecha_vencimiento > CURRENT_DATE + 10)
             ORDER BY l.producto_id LIMIT 4 LOOP
        v_idx := v_idx + 1;
        INSERT INTO movimientos_inventarios (tipo_movimiento, deposito_origen_id, deposito_destino_id, usuario_id,
                                             fecha_movimiento, observacion)
        VALUES ('TRASLADO', v_deps[1], v_deps[2 + v_idx % 2], v_repos[1 + v_idx % array_length(v_repos, 1)],
                now() - (v_idx * 3 || ' days')::interval, 'Reabastecimiento de sucursal')
        RETURNING id INTO v_mov;
        INSERT INTO movimiento_inventario_detalle (movimiento_id, producto_id, lote_id, cantidad)
        VALUES (v_mov, r.producto_id, r.lote_id, 5);
        PERFORM pg_temp.seed_stock(v_deps[1], r.lote_id, -5);
        PERFORM pg_temp.seed_stock(v_deps[2 + v_idx % 2], r.lote_id, 5);
    END LOOP;

    -- 3 ajustes negativos (merma)
    v_idx := 0;
    FOR r IN SELECT i.lote_id, l.producto_id FROM inventarios i JOIN lotes l ON l.id = i.lote_id
             WHERE i.deposito_id = v_deps[2] AND i.stock_actual >= 15 ORDER BY l.producto_id DESC LIMIT 3 LOOP
        v_idx := v_idx + 1;
        INSERT INTO movimientos_inventarios (tipo_movimiento, deposito_origen_id, usuario_id, fecha_movimiento, observacion)
        VALUES ('AJUSTE_NEGATIVO', v_deps[2], v_repos[1], now() - (v_idx * 2 || ' days')::interval,
                'Merma por producto dañado o golpeado')
        RETURNING id INTO v_mov;
        INSERT INTO movimiento_inventario_detalle (movimiento_id, producto_id, lote_id, cantidad)
        VALUES (v_mov, r.producto_id, r.lote_id, 2);
        PERFORM pg_temp.seed_stock(v_deps[2], r.lote_id, -2);
    END LOOP;

    -- 2 ajustes positivos (sobrante en conteo fisico)
    v_idx := 0;
    FOR r IN SELECT i.lote_id, l.producto_id FROM inventarios i JOIN lotes l ON l.id = i.lote_id
             WHERE i.deposito_id = v_deps[3] ORDER BY l.producto_id LIMIT 2 LOOP
        v_idx := v_idx + 1;
        INSERT INTO movimientos_inventarios (tipo_movimiento, deposito_destino_id, usuario_id, fecha_movimiento, observacion)
        VALUES ('AJUSTE_POSITIVO', v_deps[3], v_repos[2], now() - (v_idx || ' days')::interval,
                'Sobrante detectado en el conteo físico')
        RETURNING id INTO v_mov;
        INSERT INTO movimiento_inventario_detalle (movimiento_id, producto_id, lote_id, cantidad)
        VALUES (v_mov, r.producto_id, r.lote_id, 3);
        PERFORM pg_temp.seed_stock(v_deps[3], r.lote_id, 3);
    END LOOP;

    -- 2 salidas por consumo interno
    v_idx := 0;
    FOR r IN SELECT i.lote_id, l.producto_id FROM inventarios i JOIN lotes l ON l.id = i.lote_id
             WHERE i.deposito_id = v_deps[1] AND i.stock_actual >= 20 ORDER BY l.producto_id DESC LIMIT 2 LOOP
        v_idx := v_idx + 1;
        INSERT INTO movimientos_inventarios (tipo_movimiento, deposito_origen_id, usuario_id, fecha_movimiento, observacion)
        VALUES ('SALIDA', v_deps[1], v_repos[3], now() - (v_idx * 4 || ' days')::interval,
                'Consumo interno y degustación en el local')
        RETURNING id INTO v_mov;
        INSERT INTO movimiento_inventario_detalle (movimiento_id, producto_id, lote_id, cantidad)
        VALUES (v_mov, r.producto_id, r.lote_id, 3);
        PERFORM pg_temp.seed_stock(v_deps[1], r.lote_id, -3);
    END LOOP;

    -- ======================================================= D. turnos de caja
    -- Turnos 1-9 cerrados (de hace 13 a 5 dias) y 10-12 abiertos hoy, en las cajas 1 a 3 (pisos de venta 1 a 3).
    FOR t IN 1..12 LOOP
        SELECT id INTO v_caja FROM cajas WHERE created_by = 'SEED' ORDER BY id OFFSET (t - 1) % 3 LIMIT 1;
        IF t <= 9 THEN
            v_apertura := (CURRENT_DATE - (14 - t)) + time '08:00' + ((t % 3) * 30) * interval '1 minute';
            v_cierre := v_apertura + interval '9 hours';
        ELSE
            v_apertura := now() - interval '6 hours';
            v_cierre := NULL;
        END IF;
        INSERT INTO turnos_caja (caja_id, usuario_id, fecha_apertura, monto_apertura, fecha_cierre, estado)
        VALUES (v_caja, v_cajeros[t], v_apertura, 100 + (t % 3) * 50, v_cierre,
                CASE WHEN t <= 9 THEN 'CERRADO' ELSE 'ABIERTO' END)
        RETURNING id INTO v_tid;
        v_turnos := v_turnos || v_tid;
    END LOOP;

    -- ======================================================= E. movimientos de caja (12)
    FOR t IN 1..12 LOOP
        SELECT fecha_apertura, usuario_id INTO v_apertura, v_user FROM turnos_caja WHERE id = v_turnos[t];
        INSERT INTO movimientos_caja (turno_id, usuario_id, tipo, monto, motivo, fecha)
        VALUES (v_turnos[t], v_user, CASE WHEN t % 2 = 0 THEN 'INGRESO' ELSE 'EGRESO' END,
                round((10 + t * 2.5)::numeric, 2),
                (ARRAY['Fondo adicional de cambio', 'Pago al proveedor de pan', 'Retiro de efectivo a bóveda',
                       'Compra de bolsas y cinta de embalaje', 'Reposición de monedas', 'Pago de flete',
                       'Ingreso por sobrante de cambio', 'Gasto de limpieza', 'Fondo para billetes pequeños',
                       'Pago de gas del local', 'Ingreso por venta de reciclaje', 'Propina entregada al repartidor'])[t],
                v_apertura + interval '1 hour');
    END LOOP;

    -- ======================================================= F. ventas (36)
    FOR v IN 1..36 LOOP
        t := (v - 1) / 3 + 1;
        v_idx := COALESCE(array_position(v_credit_ventas, v), 0) - 1;   -- -1 si no es a credito
        SELECT tc.fecha_apertura, tc.usuario_id, c.deposito_id
          INTO v_apertura, v_user, v_dep
        FROM turnos_caja tc JOIN cajas c ON c.id = tc.caja_id WHERE tc.id = v_turnos[t];
        v_ts := v_apertura + (((v - 1) % 3 + 1) * 50 + t * 3) * interval '1 minute';

        IF v_idx >= 0 THEN
            v_cliente := v_credit_clients[1 + v_idx % array_length(v_credit_clients, 1)];
        ELSIF v % 2 = 0 THEN
            SELECT id INTO v_cliente FROM clientes
            WHERE created_by = 'SEED' AND estado = 'ACTIVO' AND NOT credito_habilitado
            ORDER BY id OFFSET (v * 3) % 50 LIMIT 1;
        ELSE
            v_cliente := NULL;
        END IF;
        v_tipo_cliente := (SELECT tipo_cliente_id FROM clientes WHERE id = v_cliente);

        INSERT INTO ventas (numero_venta, turno_id, usuario_id, cliente_id, tipo_cliente_id, deposito_id, estado,
                            fecha_venta)
        VALUES ('V-' || lpad(nextval('seq_numero_venta')::text, 8, '0'), v_turnos[t], v_user, v_cliente,
                v_tipo_cliente, v_dep, 'COMPLETADA', v_ts)
        RETURNING id, numero_venta INTO v_venta, v_numero;

        v_sub := 0; v_desc := 0; v_item := 0;
        v_n_items := 1 + v % 4;
        FOR pr IN SELECT l.producto_id AS pid, p.nombre
                  FROM inventarios i JOIN lotes l ON l.id = i.lote_id JOIN productos p ON p.id = l.producto_id
                  WHERE i.deposito_id = v_dep AND p.estado = 'ACTIVO'
                  GROUP BY l.producto_id, p.nombre HAVING sum(i.stock_actual) >= 8
                  ORDER BY random() LIMIT v_n_items LOOP
            v_item := v_item + 1;
            v_qty := CASE WHEN pr.nombre = 'Pan Francés' THEN 6 ELSE 1 + floor(random() * 3) END;
            SELECT id INTO v_pres FROM presentaciones_producto WHERE producto_id = pr.pid AND es_unidad_base;
            SELECT precio INTO v_price FROM precios_presentacion
            WHERE presentacion_id = v_pres AND deleted_at IS NULL AND estado = 'ACTIVO'
              AND (tipo_cliente_id = v_tipo_cliente OR tipo_cliente_id IS NULL) AND cantidad_minima <= v_qty
            ORDER BY (tipo_cliente_id IS NOT NULL) DESC, cantidad_minima DESC LIMIT 1;
            v_manual := v IN (5, 15, 25) AND v_item = 1;
            IF v_manual THEN
                v_price := round(v_price * 0.92, 2);
            END IF;

            v_remaining := v_qty;
            FOR lt IN SELECT i.lote_id, i.stock_actual, l.porcentaje_descuento_vencimiento AS pct,
                             l.costo_unitario_compra AS costo
                      FROM inventarios i JOIN lotes l ON l.id = i.lote_id
                      WHERE i.deposito_id = v_dep AND l.producto_id = pr.pid AND i.stock_actual > 0
                        AND (l.fecha_vencimiento IS NULL OR l.fecha_vencimiento >= CURRENT_DATE)
                      ORDER BY l.fecha_vencimiento NULLS LAST, l.id LOOP
                EXIT WHEN v_remaining <= 0;
                v_take := least(v_remaining, lt.stock_actual);
                v_line_desc := round(v_price * v_take * lt.pct / 100, 2);
                INSERT INTO venta_detalle (venta_id, producto_id, presentacion_id, cantidad, factor_conversion,
                                           precio_unitario, descuento, subtotal, es_precio_modificado, motivo_precio)
                VALUES (v_venta, pr.pid, v_pres, v_take, 1, v_price, v_line_desc,
                        round(v_price * v_take, 2) - v_line_desc, v_manual,
                        CASE WHEN v_manual THEN 'Cliente reclama el precio de la promoción' END)
                RETURNING id INTO v_vdet;
                INSERT INTO venta_detalle_lote (venta_detalle_id, lote_id, cantidad, costo_unitario)
                VALUES (v_vdet, lt.lote_id, v_take, lt.costo);
                IF v NOT IN (7, 28) THEN
                    PERFORM pg_temp.seed_stock(v_dep, lt.lote_id, -v_take);
                END IF;
                v_sub := v_sub + round(v_price * v_take, 2);
                v_desc := v_desc + v_line_desc;
                v_remaining := v_remaining - v_take;
            END LOOP;
        END LOOP;
        v_total := v_sub - v_desc;
        v_vuelto := 0;

        -- Pagos
        IF v_idx >= 0 THEN
            v_cred := CASE WHEN v_idx % 2 = 0 THEN v_total ELSE round(v_total / 2, 2) END;
            INSERT INTO venta_pagos (venta_id, metodo_pago_id, monto) VALUES (v_venta, m_cre, v_cred);
            IF v_cred < v_total THEN
                INSERT INTO venta_pagos (venta_id, metodo_pago_id, monto) VALUES (v_venta, m_efe, v_total - v_cred);
            END IF;
            INSERT INTO cuentas_por_cobrar (cliente_id, venta_id, monto_original, saldo, estado, fecha,
                                            created_by, updated_by)
            VALUES (v_cliente, v_venta, v_cred, v_cred, 'PENDIENTE', v_ts, 'SEED', 'SEED');
        ELSIF v % 4 = 0 THEN
            v_recibido := ceil(v_total / 5) * 5;
            v_vuelto := v_recibido - v_total;
            INSERT INTO venta_pagos (venta_id, metodo_pago_id, monto) VALUES (v_venta, m_efe, v_total);
        ELSIF v % 4 = 1 THEN
            SELECT CASE WHEN v % 8 < 4 THEN m_tar ELSE
                       (SELECT id FROM metodos_pago WHERE nombre LIKE 'TARJETA %' AND estado = 'ACTIVO'
                        ORDER BY id OFFSET (v * 5) % 20 LIMIT 1) END INTO v_metodo;
            INSERT INTO venta_pagos (venta_id, metodo_pago_id, monto, referencia)
            VALUES (v_venta, v_metodo, v_total, 'AUT-' || lpad(((v * 48271) % 1000000)::text, 6, '0'));
        ELSIF v % 4 = 2 THEN
            SELECT id INTO v_metodo FROM metodos_pago WHERE nombre LIKE 'TRANSFERENCIA %' AND estado = 'ACTIVO'
            ORDER BY id OFFSET (v * 3) % 14 LIMIT 1;
            INSERT INTO venta_pagos (venta_id, metodo_pago_id, monto, referencia)
            VALUES (v_venta, v_metodo, v_total, 'TRF-' || lpad(((v * 69621) % 10000000)::text, 7, '0'));
        ELSE
            INSERT INTO venta_pagos (venta_id, metodo_pago_id, monto) VALUES (v_venta, m_efe, round(v_total / 2, 2));
            INSERT INTO venta_pagos (venta_id, metodo_pago_id, monto, referencia)
            VALUES (v_venta, m_tar, v_total - round(v_total / 2, 2), 'AUT-' || lpad(((v * 48271) % 1000000)::text, 6, '0'));
        END IF;

        UPDATE ventas
        SET subtotal = v_sub, descuento_total = v_desc, total = v_total, vuelto = v_vuelto,
            estado = CASE WHEN v IN (7, 28) THEN 'ANULADA' ELSE 'COMPLETADA' END,
            anulada_por = CASE WHEN v IN (7, 28) THEN (SELECT username FROM usuarios WHERE id = v_user) END,
            fecha_anulacion = CASE WHEN v IN (7, 28) THEN v_ts + interval '10 minutes' END,
            motivo_anulacion = CASE WHEN v IN (7, 28) THEN 'El cliente cambió de opinión antes de retirarse' END
        WHERE id = v_venta;

        IF v NOT IN (7, 28) THEN
            INSERT INTO movimientos_inventarios (tipo_movimiento, deposito_origen_id, usuario_id, fecha_movimiento,
                                                 observacion, referencia_tipo, referencia_id)
            VALUES ('SALIDA', v_dep, v_user, v_ts, 'Venta ' || v_numero, 'VENTA', v_venta)
            RETURNING id INTO v_mov;
            INSERT INTO movimiento_inventario_detalle (movimiento_id, producto_id, lote_id, cantidad)
            SELECT v_mov, vd.producto_id, vdl.lote_id, vdl.cantidad
            FROM venta_detalle vd JOIN venta_detalle_lote vdl ON vdl.venta_detalle_id = vd.id
            WHERE vd.venta_id = v_venta;
        END IF;
    END LOOP;

    -- ======================================================= G. abonos de deuda (10)
    -- Los 6 primeros pagan el 30 % de lo que debe un cliente; los siguientes liquidan. Entran a los turnos abiertos.
    FOR a IN 1..12 LOOP
        SELECT count(DISTINCT cliente_id) INTO v_ndeb FROM cuentas_por_cobrar WHERE estado = 'PENDIENTE';
        EXIT WHEN v_ndeb = 0;
        SELECT c.cliente_id, sum(c.saldo) INTO v_cli, v_debt
        FROM cuentas_por_cobrar c WHERE c.estado = 'PENDIENTE'
        GROUP BY c.cliente_id HAVING sum(c.saldo) > 1
        ORDER BY c.cliente_id OFFSET (a - 1) % v_ndeb LIMIT 1;
        CONTINUE WHEN v_cli IS NULL;
        v_amt := CASE WHEN a <= 6 THEN greatest(round(v_debt * 0.3, 2), 0.01) ELSE v_debt END;

        v_tid := v_turnos[10 + a % 3];
        SELECT fecha_apertura, usuario_id INTO v_apertura, v_user FROM turnos_caja WHERE id = v_tid;
        IF a % 3 = 0 THEN
            SELECT id INTO v_metodo FROM metodos_pago WHERE nombre LIKE 'TRANSFERENCIA %' AND estado = 'ACTIVO'
            ORDER BY id OFFSET a LIMIT 1;
        ELSE
            v_metodo := m_efe;
        END IF;

        INSERT INTO abonos_credito (numero_abono, cliente_id, turno_id, usuario_id, metodo_pago_id, monto, referencia, fecha)
        VALUES ('AB-' || lpad(nextval('seq_numero_abono')::text, 8, '0'), v_cli, v_tid, v_user, v_metodo, v_amt,
                CASE WHEN a % 3 = 0 THEN 'TRF-' || lpad((a * 7351)::text, 7, '0') END,
                v_apertura + (110 + a * 15) * interval '1 minute')
        RETURNING id INTO v_abono;

        -- Se aplica a las deudas mas antiguas primero
        v_restante := v_amt;
        FOR r IN SELECT id, saldo FROM cuentas_por_cobrar
                 WHERE cliente_id = v_cli AND estado = 'PENDIENTE' ORDER BY fecha, id LOOP
            EXIT WHEN v_restante <= 0;
            v_aplicado := least(r.saldo, v_restante);
            INSERT INTO abonos_credito_aplicacion (abono_id, cuenta_id, monto) VALUES (v_abono, r.id, v_aplicado);
            UPDATE cuentas_por_cobrar
            SET saldo = saldo - v_aplicado, estado = CASE WHEN saldo - v_aplicado = 0 THEN 'PAGADA' ELSE 'PENDIENTE' END,
                updated_at = now()
            WHERE id = r.id;
            v_restante := v_restante - v_aplicado;
        END LOOP;
    END LOOP;

    -- ======================================================= H. devoluciones (10)
    -- Una unidad de la primera linea de ventas completadas y pagadas al contado (una de cada dos).
    FOR r IN SELECT v2.id, v2.turno_id, v2.usuario_id, v2.fecha_venta, v2.deposito_id, v2.numero_venta,
                    row_number() OVER (ORDER BY v2.id) AS rn
             FROM ventas v2
             WHERE v2.estado = 'COMPLETADA'
               AND NOT EXISTS (SELECT 1 FROM cuentas_por_cobrar c WHERE c.venta_id = v2.id)
             ORDER BY v2.id LOOP
        CONTINUE WHEN r.rn % 2 = 0;
        EXIT WHEN v_dev_n >= 10;
        v_dev_n := v_dev_n + 1;

        SELECT vd.id AS det_id, vd.producto_id, vd.cantidad, vd.subtotal, vdl.lote_id
          INTO ln
        FROM venta_detalle vd JOIN venta_detalle_lote vdl ON vdl.venta_detalle_id = vd.id
        WHERE vd.venta_id = r.id ORDER BY vd.id LIMIT 1;

        v_amt := round(ln.subtotal / ln.cantidad, 2);
        SELECT metodo_pago_id INTO v_metodo FROM venta_pagos WHERE venta_id = r.id ORDER BY id LIMIT 1;
        v_ts := least(now(), r.fecha_venta + interval '90 minutes');

        INSERT INTO devoluciones_venta (venta_id, usuario_id, fecha, motivo, monto_reembolsado, metodo_pago_id,
                                        turno_id, numero_devolucion)
        VALUES (r.id, r.usuario_id, v_ts, v_motivos_devolucion[v_dev_n], v_amt, v_metodo, r.turno_id,
                'D-' || lpad(nextval('seq_numero_devolucion')::text, 8, '0'))
        RETURNING id INTO v_dev;
        INSERT INTO devolucion_venta_detalle (devolucion_id, venta_detalle_id, lote_id, cantidad, reingresa_stock)
        VALUES (v_dev, ln.det_id, ln.lote_id, 1, v_dev_n NOT IN (3, 8));

        IF v_dev_n NOT IN (3, 8) THEN
            PERFORM pg_temp.seed_stock(r.deposito_id, ln.lote_id, 1);
            INSERT INTO movimientos_inventarios (tipo_movimiento, deposito_destino_id, usuario_id, fecha_movimiento,
                                                 observacion, referencia_tipo, referencia_id)
            VALUES ('ENTRADA', r.deposito_id, r.usuario_id, v_ts, 'Devolución de la venta ' || r.numero_venta,
                    'DEVOLUCION_VENTA', v_dev)
            RETURNING id INTO v_mov;
            INSERT INTO movimiento_inventario_detalle (movimiento_id, producto_id, lote_id, cantidad)
            VALUES (v_mov, ln.producto_id, ln.lote_id, 1);
        END IF;
    END LOOP;

    -- ======================================================= I. cierre de los turnos cerrados
    -- esperado = apertura + ingresos - egresos + ventas en efectivo + abonos en efectivo - reembolsos en efectivo
    -- declarado = esperado con un faltante, un sobrante o exacto, segun el turno
    UPDATE turnos_caja tc
    SET monto_cierre_esperado = x.esperado,
        monto_cierre_declarado = x.esperado + CASE tc.id % 4 WHEN 1 THEN -2.50 WHEN 2 THEN 1.00 ELSE 0 END
    FROM (SELECT t2.id,
                 t2.monto_apertura
                 + COALESCE((SELECT sum(monto) FROM movimientos_caja WHERE turno_id = t2.id AND tipo = 'INGRESO'), 0)
                 - COALESCE((SELECT sum(monto) FROM movimientos_caja WHERE turno_id = t2.id AND tipo = 'EGRESO'), 0)
                 + COALESCE((SELECT sum(vp.monto) FROM venta_pagos vp JOIN ventas v3 ON v3.id = vp.venta_id
                             WHERE v3.turno_id = t2.id AND v3.estado = 'COMPLETADA' AND vp.metodo_pago_id = m_efe), 0)
                 + COALESCE((SELECT sum(ab.monto) FROM abonos_credito ab
                             WHERE ab.turno_id = t2.id AND ab.metodo_pago_id = m_efe), 0)
                 - COALESCE((SELECT sum(d.monto_reembolsado) FROM devoluciones_venta d
                             WHERE d.turno_id = t2.id AND d.metodo_pago_id = m_efe), 0) AS esperado
          FROM turnos_caja t2 WHERE t2.estado = 'CERRADO' AND t2.id = ANY (v_turnos)) x
    WHERE tc.id = x.id;

    RAISE NOTICE 'Semilla de flujo cargada: % ventas, % cuentas por cobrar, % abonos, % devoluciones.',
        (SELECT count(*) FROM ventas), (SELECT count(*) FROM cuentas_por_cobrar),
        (SELECT count(*) FROM abonos_credito), (SELECT count(*) FROM devoluciones_venta);
END
$seed$;
