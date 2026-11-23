-- ============================================================================
-- SEMILLA DE CARGA: ~10 000 ventas con 120 dias de historia, para probar reportes, rendimiento y el motor de reorden.
--
-- Se ejecuta a mano (IntelliJ, DBeaver, psql) sobre una base DEDICADA cuyo nombre contenga "carga"
-- (p. ej. tienda_db_carga), creada con las migraciones y las semillas SQL (SPRING_PROFILES_ACTIVE=seed).
-- Reemplaza TODO el flujo operativo de esa base (ventas, caja, pedidos, lotes, inventario, creditos, reorden);
-- conserva el catalogo, la organizacion, los usuarios y los precios.
--
-- Simula dia por dia (hoy - 120 .. hoy): ventas con FEFO sobre lotes reales, reposicion con una politica empirica
-- (punto fijo + cantidad "a ojo", como el problema que describe el documento), pedidos con lead time por proveedor,
-- costos con tendencia y alza reciente, mermas por vencimiento, turnos de caja cuadrados, creditos con abonos,
-- anulaciones y devoluciones. Es determinista (misma semilla = mismos datos).
--
-- Perfiles de producto (por orden pseudoaleatorio estable), pensados para ejercitar cada rama del motor EOQ:
--   A alta rotacion | B media | C baja | D demanda volatil | E dejaron de venderse hace mas de 90 dias (SIN_DEMANDA)
--   F producto nuevo, menos de 90 dias de historia (DATOS_INSUFICIENTES) | G nunca pedido a un proveedor (SIN_PROVEEDOR)
--   H alta rotacion, vida util corta y proveedor con alza de costo (compra anticipada y tope por vencimiento)
-- Proveedores: unos puntuales y otros erraticos (lead time muy variable), unos estables y otros con alza reciente.
--
-- Todo lo creado lleva created_by = 'SEED_CARGA'. Al terminar, ejecute seed_carga_verificacion.sql.
-- ============================================================================

CREATE OR REPLACE FUNCTION pg_temp.randn() RETURNS double precision LANGUAGE sql AS
$$ SELECT sqrt(-2 * ln(1 - random())) * cos(2 * pi() * random()) $$;

-- UPDATE primero: un INSERT con delta negativo violaria el CHECK antes de resolver el conflicto
CREATE OR REPLACE FUNCTION pg_temp.stock(p_dep bigint, p_lote bigint, p_delta numeric, p_ts timestamp)
RETURNS void LANGUAGE plpgsql AS $f$
BEGIN
    UPDATE inventarios SET stock_actual = stock_actual + p_delta, updated_at = p_ts
    WHERE deposito_id = p_dep AND lote_id = p_lote AND deleted_at IS NULL;
    IF NOT FOUND THEN
        INSERT INTO inventarios (deposito_id, lote_id, stock_actual, created_by, updated_by, created_at, updated_at)
        VALUES (p_dep, p_lote, p_delta, 'SEED_CARGA', 'SEED_CARGA', p_ts, p_ts);
    END IF;
END
$f$;

DO $carga$
DECLARE
    -- ------------------------------------------------------------------ parametros
    p_ventas  int := 10000;            -- ventas a generar (hoy cuenta solo lo transcurrido del dia)
    p_dias    int := 120;              -- dias de historia
    p_semilla double precision := 0.2026;
    p_prob_credito numeric := 0.04;    -- ventas a credito
    p_prob_cliente numeric := 0.12;    -- ventas con cliente registrado (contado)
    p_prob_anulada numeric := 0.007;
    p_prob_devolucion numeric := 0.012;
    p_prob_caja numeric := 0.4;        -- turnos con un ingreso o egreso de caja

    -- ------------------------------------------------------------------ catalogos
    v_deps bigint[]; v_cajas bigint[];
    v_cajeros bigint[]; v_repos bigint[]; v_admin bigint;
    v_prov bigint[]; v_credit_clients bigint[]; v_reg_clients bigint[];
    m_efe bigint; m_tar bigint; m_cre bigint; m_trf bigint[]; m_tarj bigint[];

    -- ------------------------------------------------------------------ estado de la simulacion
    k int; i int; s int; l int; n int;
    d date; t timestamp; t_ini timestamp; t_fin timestamp; v_hoy timestamp := localtimestamp;
    v_total_w numeric; v_frac numeric; v_n_dia int;
    v_turnos bigint[]; v_turno bigint; v_caja_i int; v_dep bigint; v_dep_i int; v_user bigint;
    v_cliente bigint; v_tipo bigint; v_credito boolean; v_numero text; v_venta bigint;
    v_n_lineas int; v_pid bigint; v_factor numeric; v_pres bigint; v_qty numeric; v_need numeric; v_avail numeric;
    v_price numeric; v_take numeric; v_rem numeric; v_line_desc numeric; v_vdet bigint;
    v_sub numeric; v_desc numeric; v_total numeric; v_recibido numeric; v_vuelto numeric; v_cred numeric;
    v_usados bigint[]; v_intentos int; v_x numeric; v_mov bigint; v_metodo bigint; v_u numeric;
    v_first_det bigint; v_first_lote bigint; v_first_sub numeric; v_first_qty numeric; v_first_factor numeric;
    v_anulada boolean; v_dev bigint; v_ped bigint; v_det bigint; v_rec bigint; v_lote bigint;
    v_lt int; v_costo numeric; v_cant numeric; v_dias_idx int; v_saldo numeric; v_limite numeric;
    v_cli bigint; v_debt numeric; v_amt numeric; v_restante numeric; v_aplicado numeric; v_abono bigint;
    v_esperado numeric; v_att int;
    n_quiebres int := 0; n_ventas int := 0; n_anul int := 0; n_dev int := 0; n_cred int := 0; n_abonos int := 0;
    n_pedidos int := 0; n_mermas int := 0;
    r record; lt record; pr record; ln record;
BEGIN
    IF current_database() NOT LIKE '%carga%' THEN
        RAISE EXCEPTION 'Esta semilla borra el flujo operativo: ejecutela solo en una base cuyo nombre contenga "carga" (actual: %)',
            current_database();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM usuarios WHERE created_by = 'SEED') THEN
        RAISE EXCEPTION 'Falta la semilla base: arranque la aplicacion con SPRING_PROFILES_ACTIVE=seed sobre esta base antes de cargar.';
    END IF;

    PERFORM setseed(p_semilla);

    -- ================================================================== A. catalogos de apoyo
    SELECT array_agg(c.id ORDER BY c.id), array_agg(c.deposito_id ORDER BY c.id) INTO v_cajas, v_deps
    FROM (SELECT id, deposito_id FROM cajas WHERE created_by = 'SEED' AND estado = 'ACTIVO' ORDER BY id LIMIT 3) c;
    SELECT id INTO v_admin FROM usuarios WHERE username = 'admin';
    SELECT array_agg(id ORDER BY username) INTO v_cajeros FROM usuarios WHERE username LIKE 'cajero%' AND estado = 'ACTIVO';
    SELECT array_agg(id ORDER BY username) INTO v_repos FROM usuarios WHERE username LIKE 'repositor%' AND estado = 'ACTIVO';
    SELECT array_agg(id ORDER BY id) INTO v_prov FROM proveedores WHERE created_by = 'SEED' AND activo AND deleted_at IS NULL;
    SELECT array_agg(id ORDER BY id) INTO v_credit_clients FROM clientes
    WHERE created_by = 'SEED' AND credito_habilitado AND estado = 'ACTIVO';
    SELECT array_agg(id ORDER BY id) INTO v_reg_clients FROM clientes
    WHERE created_by = 'SEED' AND NOT credito_habilitado AND estado = 'ACTIVO';
    SELECT id INTO m_efe FROM metodos_pago WHERE nombre = 'EFECTIVO';
    SELECT id INTO m_tar FROM metodos_pago WHERE nombre = 'TARJETA';
    SELECT id INTO m_cre FROM metodos_pago WHERE nombre = 'CREDITO';
    SELECT array_agg(id ORDER BY id) INTO m_trf FROM metodos_pago WHERE nombre LIKE 'TRANSFERENCIA %' AND estado = 'ACTIVO';
    SELECT array_agg(id ORDER BY id) INTO m_tarj FROM metodos_pago WHERE nombre LIKE 'TARJETA %' AND estado = 'ACTIVO';
    IF (SELECT count(DISTINCT x) FROM unnest(v_deps) x) < 3 THEN
        RAISE EXCEPTION 'Las 3 primeras cajas deben pertenecer a 3 depositos distintos.';
    END IF;
    IF array_length(v_cajas, 1) < 3 OR array_length(v_cajeros, 1) < 3 OR array_length(v_repos, 1) < 1
       OR array_length(v_prov, 1) < 10 OR m_efe IS NULL OR m_cre IS NULL THEN
        RAISE EXCEPTION 'La semilla base esta incompleta (cajas, cajeros, repositores, proveedores o metodos de pago).';
    END IF;

    -- ================================================================== B. perfiles de producto (antes de borrar los lotes: se usa su costo)
    CREATE TEMP TABLE ps (
        pid bigint PRIMARY KEY, rango int, perfil char(1), peso numeric, dep_i int, dep bigint,
        prov bigint, prov_i int, lt_media numeric, lt_desv numeric, costo0 numeric, tendencia numeric, alza numeric,
        vida int, perecedero boolean, pres_base bigint, pres_alt bigint, factor_alt numeric,
        dem_esp numeric, lanzamiento int, ordenable boolean, min_dias numeric, ped_dias numeric
    ) ON COMMIT DROP;

    INSERT INTO ps (pid, rango, perfil, perecedero, pres_base, costo0)
    SELECT p.id, row_number() OVER (ORDER BY md5(p.id::text)), 'C', p.es_perecedero,
           (SELECT pp.id FROM presentaciones_producto pp WHERE pp.producto_id = p.id AND pp.es_unidad_base AND pp.deleted_at IS NULL LIMIT 1),
           COALESCE((SELECT l.costo_unitario_compra FROM lotes l WHERE l.producto_id = p.id AND l.costo_unitario_compra > 0
                     ORDER BY l.id LIMIT 1),
                    (SELECT x.precio * 0.7 FROM precios_presentacion x JOIN presentaciones_producto pb ON pb.id = x.presentacion_id
                     WHERE pb.producto_id = p.id AND pb.es_unidad_base ORDER BY x.id LIMIT 1))
    FROM productos p
    WHERE p.created_by = 'SEED' AND p.estado = 'ACTIVO' AND p.deleted_at IS NULL;

    DELETE FROM ps WHERE pres_base IS NULL OR costo0 IS NULL OR costo0 <= 0;
    SELECT count(*) INTO n FROM ps;
    IF n < 30 THEN RAISE EXCEPTION 'Hay solo % productos utilizables (con presentacion base y costo).', n; END IF;

    UPDATE ps SET perfil = CASE
        WHEN rango <= 0.11 * n THEN 'A' WHEN rango <= 0.39 * n THEN 'B' WHEN rango <= 0.67 * n THEN 'C'
        WHEN rango <= 0.78 * n THEN 'D' WHEN rango <= 0.84 * n THEN 'E' WHEN rango <= 0.90 * n THEN 'F'
        WHEN rango <= 0.95 * n THEN 'G' ELSE 'H' END;
    UPDATE ps SET
        peso = CASE perfil WHEN 'A' THEN 10 WHEN 'B' THEN 4 WHEN 'C' THEN 1.2 WHEN 'D' THEN 3 WHEN 'E' THEN 2
                           WHEN 'F' THEN 3 WHEN 'G' THEN 1.5 ELSE 6 END,
        dep_i = 1 + (rango % 3);
    UPDATE ps SET dep = v_deps[dep_i];
    -- proveedor: reparto estable; los H van al proveedor numero 5 (el de alza de costo)
    UPDATE ps SET prov_i = CASE WHEN perfil = 'H' THEN 5 ELSE 1 + (rango * 7) % array_length(v_prov, 1) END;
    UPDATE ps SET prov = v_prov[prov_i],
        lt_media = 3 + (prov_i % 5),
        lt_desv = CASE WHEN prov_i % 4 = 0 THEN 3.0 ELSE 0.7 END,           -- erraticos vs puntuales
        tendencia = CASE WHEN prov_i % 3 = 0 THEN 0.015 ELSE 0.002 END,      -- inflacion mensual
        alza = CASE WHEN prov_i % 5 = 0 THEN 0.10 ELSE 0 END;                -- alza reciente (ultimos 30 dias)
    UPDATE ps SET
        vida = CASE WHEN NOT perecedero THEN NULL WHEN perfil = 'H' THEN 12 ELSE 20 + (rango * 11) % 100 END,
        lanzamiento = CASE WHEN perfil = 'F' THEN p_dias - 40 ELSE 0 END,
        ordenable = perfil NOT IN ('E', 'G'),
        min_dias = 6 + (rango % 4), ped_dias = 18 + (rango % 9);
    -- Los H deben ser perecederos para ejercitar el tope por vencimiento
    UPDATE ps SET perecedero = TRUE, vida = 12 WHERE perfil = 'H';
    UPDATE productos SET es_perecedero = TRUE, updated_at = now() WHERE id IN (SELECT pid FROM ps WHERE perfil = 'H');
    -- presentacion alternativa (caja/paquete) con precio, para vender por formato
    UPDATE ps SET pres_alt = x.id, factor_alt = x.factor_conversion
    FROM (SELECT DISTINCT ON (pp.producto_id) pp.producto_id, pp.id, pp.factor_conversion
          FROM presentaciones_producto pp
          WHERE NOT pp.es_unidad_base AND pp.deleted_at IS NULL AND pp.factor_conversion > 1
            AND EXISTS (SELECT 1 FROM precios_presentacion x WHERE x.presentacion_id = pp.id AND x.estado = 'ACTIVO' AND x.deleted_at IS NULL)
          ORDER BY pp.producto_id, pp.factor_conversion) x
    WHERE x.producto_id = ps.pid;
    -- demanda esperada por dia ("lo que el encargado cree"): reparte las unidades totales segun el peso
    UPDATE ps SET dem_esp = round(peso / NULLIF((SELECT sum(peso) FROM ps), 0)
                                  * (p_ventas * 2.0 * 2.8 / (p_dias + 1)), 4);

    -- ================================================================== C. limpieza del flujo operativo
    TRUNCATE sugerencias_reorden, calculos_reorden, alertas,
             abonos_credito_aplicacion, abonos_credito, cuentas_por_cobrar,
             devolucion_venta_detalle, devoluciones_venta, venta_pagos, venta_detalle_lote, venta_detalle, ventas,
             movimientos_caja, turnos_caja,
             recepcion_pedido_detalle, recepciones_pedido, pedido_proveedor_detalle, pedidos_proveedor,
             movimiento_inventario_detalle, movimientos_inventarios, ajustes_costo_lote, autorizaciones_operacion,
             inventarios, lotes RESTART IDENTITY;
    ALTER SEQUENCE seq_numero_venta RESTART;
    ALTER SEQUENCE seq_numero_devolucion RESTART;
    ALTER SEQUENCE seq_numero_abono RESTART;
    ALTER SEQUENCE seq_numero_pedido_proveedor RESTART;
    ALTER SEQUENCE seq_numero_lote RESTART;

    -- tablas de trabajo
    CREATE TEMP TABLE pend (pedido bigint, det bigint, pid bigint, qty numeric, costo numeric, llegada date,
                            dep bigint, vida int, perecedero boolean) ON COMMIT DROP;
    CREATE TEMP TABLE wd (dep_i int, pid bigint, cum numeric) ON COMMIT DROP;
    CREATE TEMP TABLE pos (pid bigint PRIMARY KEY, stock numeric, transito numeric) ON COMMIT DROP;
    CREATE TEMP TABLE pedir (pid bigint, prov bigint, dep bigint, qty numeric, costo numeric, lt int) ON COMMIT DROP;
    CREATE TEMP TABLE dias (dk int PRIMARY KEY, w numeric, nv int) ON COMMIT DROP;

    -- ================================================================== D. ventas por dia (reparto del total)
    v_frac := GREATEST(0, LEAST(1, (extract(epoch FROM v_hoy - date_trunc('day', v_hoy)) / 3600.0 - 8.5) / 11.5));
    INSERT INTO dias (dk, w)
    SELECT g, (CASE extract(dow FROM current_date - g) WHEN 0 THEN 0.5 WHEN 6 THEN 1.4 ELSE 1.0 END)
              * (0.85 + 0.3 * random()) * CASE WHEN g = 0 THEN v_frac ELSE 1 END
    FROM generate_series(0, p_dias) g;
    SELECT sum(w) INTO v_total_w FROM dias;
    UPDATE dias SET nv = round(p_ventas * w / v_total_w);
    UPDATE dias SET nv = nv + (p_ventas - (SELECT sum(nv) FROM dias)) WHERE dk = 3;   -- ajuste de redondeo

    -- ================================================================== E. inventario inicial (hace p_dias + 1 dias)
    d := current_date - (p_dias + 1);
    FOR r IN SELECT * FROM ps WHERE perfil <> 'F' ORDER BY pid LOOP
        INSERT INTO lotes (producto_id, numero_lote, fecha_vencimiento, costo_unitario_compra, porcentaje_descuento_vencimiento,
                           created_by, updated_by, created_at, updated_at)
        VALUES (r.pid, 'INI-' || lpad(r.pid::text, 5, '0'),
                CASE WHEN r.perecedero THEN d + r.vida + ((r.rango * 5) % 15) END,
                round(r.costo0, 4), 0, 'SEED_CARGA', 'SEED_CARGA', d + time '07:00', d + time '07:00')
        RETURNING id INTO v_lote;
        v_cant := CASE r.perfil WHEN 'E' THEN 40
                                WHEN 'G' THEN GREATEST(ceil(r.dem_esp * 140), 30)
                                ELSE GREATEST(ceil(r.dem_esp * 22), 12) END;
        PERFORM pg_temp.stock(r.dep, v_lote, v_cant, d + time '07:00');
    END LOOP;
    FOR i IN 1..3 LOOP
        INSERT INTO movimientos_inventarios (tipo_movimiento, deposito_destino_id, usuario_id, fecha_movimiento, observacion, referencia_tipo)
        VALUES ('ENTRADA', v_deps[i], v_admin, d + time '07:00', 'Inventario inicial', 'INVENTARIO_INICIAL')
        RETURNING id INTO v_mov;
        INSERT INTO movimiento_inventario_detalle (movimiento_id, producto_id, lote_id, cantidad)
        SELECT v_mov, l.producto_id, l.id, i2.stock_actual
        FROM inventarios i2 JOIN lotes l ON l.id = i2.lote_id WHERE i2.deposito_id = v_deps[i];
    END LOOP;

    -- ================================================================== F. simulacion dia por dia
    FOR k IN REVERSE p_dias..0 LOOP
        d := current_date - k;
        v_dias_idx := p_dias - k;                       -- 0 = primer dia

        -- F1. descuento por vencimiento proximo (<= 6 dias) y baja semanal de lotes vencidos
        UPDATE lotes SET porcentaje_descuento_vencimiento = 20, updated_at = d + time '06:00'
        WHERE fecha_vencimiento IS NOT NULL AND fecha_vencimiento - d BETWEEN 0 AND 6
          AND porcentaje_descuento_vencimiento = 0 AND deleted_at IS NULL;

        IF extract(dow FROM d) = 1 THEN
            FOR r IN SELECT i.deposito_id, i.lote_id, i.stock_actual, l.producto_id
                     FROM inventarios i JOIN lotes l ON l.id = i.lote_id
                     WHERE l.fecha_vencimiento < d AND i.stock_actual > 0 ORDER BY i.deposito_id LOOP
                INSERT INTO movimientos_inventarios (tipo_movimiento, deposito_origen_id, usuario_id, fecha_movimiento, observacion)
                VALUES ('AJUSTE_NEGATIVO', r.deposito_id, v_repos[1 + (k % array_length(v_repos, 1))], d + time '07:30',
                        'Baja por vencimiento')
                RETURNING id INTO v_mov;
                INSERT INTO movimiento_inventario_detalle (movimiento_id, producto_id, lote_id, cantidad)
                VALUES (v_mov, r.producto_id, r.lote_id, r.stock_actual);
                PERFORM pg_temp.stock(r.deposito_id, r.lote_id, -r.stock_actual, d + time '07:30');
                n_mermas := n_mermas + 1;
            END LOOP;
        END IF;

        -- F2. llegadas de pedidos
        FOR r IN SELECT DISTINCT pedido, dep FROM pend WHERE llegada = d ORDER BY pedido LOOP
            t := LEAST(d + time '09:30' + random() * interval '90 minutes', v_hoy);
            INSERT INTO recepciones_pedido (pedido_id, usuario_id, fecha_recepcion, numero_guia, observacion)
            VALUES (r.pedido, v_repos[1 + (r.pedido % array_length(v_repos, 1))], t,
                    'GR-' || lpad(r.pedido::text, 6, '0'), 'Entrega completa')
            RETURNING id INTO v_rec;
            INSERT INTO movimientos_inventarios (tipo_movimiento, deposito_destino_id, usuario_id, fecha_movimiento, observacion,
                                                 referencia_tipo, referencia_id)
            VALUES ('ENTRADA', r.dep, v_repos[1 + (r.pedido % array_length(v_repos, 1))], t, 'Recepción del pedido',
                    'PEDIDO_PROVEEDOR', r.pedido)
            RETURNING id INTO v_mov;
            FOR lt IN SELECT * FROM pend WHERE pedido = r.pedido ORDER BY det LOOP
                INSERT INTO lotes (producto_id, numero_lote, fecha_vencimiento, costo_unitario_compra,
                                   porcentaje_descuento_vencimiento, created_by, updated_by, created_at, updated_at)
                VALUES (lt.pid, 'REC-' || lpad(r.pedido::text, 6, '0') || '-' || lpad(lt.pid::text, 5, '0'),
                        CASE WHEN lt.perecedero THEN d + lt.vida END, lt.costo, 0, 'SEED_CARGA', 'SEED_CARGA', t, t)
                RETURNING id INTO v_lote;
                INSERT INTO recepcion_pedido_detalle (recepcion_id, pedido_detalle_id, lote_id, cantidad)
                VALUES (v_rec, lt.det, v_lote, lt.qty);
                UPDATE pedido_proveedor_detalle SET cantidad_recibida = lt.qty WHERE id = lt.det;
                PERFORM pg_temp.stock(r.dep, v_lote, lt.qty, t);
                INSERT INTO movimiento_inventario_detalle (movimiento_id, producto_id, lote_id, cantidad)
                VALUES (v_mov, lt.pid, v_lote, lt.qty);
            END LOOP;
            UPDATE pedidos_proveedor SET estado = 'RECIBIDO', updated_at = t WHERE id = r.pedido;
            DELETE FROM pend WHERE pedido = r.pedido;
        END LOOP;

        -- F3. turnos del dia (uno por caja)
        v_turnos := ARRAY[]::bigint[];
        FOR i IN 1..3 LOOP
            INSERT INTO turnos_caja (caja_id, usuario_id, fecha_apertura, monto_apertura, estado)
            VALUES (v_cajas[i], v_cajeros[1 + (k + i) % array_length(v_cajeros, 1)],
                    LEAST(d + time '08:00' + (i * 4) * interval '1 minute', v_hoy - interval '1 minute'),
                    100 + 50 * (i % 3), 'ABIERTO')
            RETURNING id INTO v_turno;
            v_turnos := v_turnos || v_turno;
        END LOOP;

        -- F4. pesos de venta del dia (volatiles y productos nuevos incluidos)
        DELETE FROM wd;
        INSERT INTO wd (dep_i, pid, cum)
        SELECT dep_i, pid, sum(w) OVER (PARTITION BY dep_i ORDER BY pid)
        FROM (SELECT dep_i, pid,
                     peso * CASE WHEN perfil = 'D' THEN exp(1.0 * pg_temp.randn()) ELSE 1 END
                          * CASE WHEN perfil = 'F' AND v_dias_idx < lanzamiento THEN 0 ELSE 1 END
                          * CASE WHEN perfil = 'E' AND v_dias_idx >= 25 THEN 0 ELSE 1 END AS w   -- E: dejaron de venderse
              FROM ps) q
        WHERE w > 0;

        -- F5. ventas
        v_n_dia := (SELECT nv FROM dias WHERE dias.dk = k);
        t_ini := d + time '08:30';
        t_fin := CASE WHEN k = 0 THEN LEAST(d + time '20:00', v_hoy - interval '1 minute') ELSE d + time '20:00' END;
        IF t_fin <= t_ini THEN v_n_dia := 0; END IF;

        -- si una venta se queda sin lineas (todo sin stock) se descarta y se intenta otra, para llegar a la cantidad del dia
        s := 0; v_att := 0;
        WHILE s < COALESCE(v_n_dia, 0) AND v_att < COALESCE(v_n_dia, 0) * 2 LOOP
            s := s + 1; v_att := v_att + 1;
            t := t_ini + (t_fin - t_ini) * ((s - random()) / v_n_dia);
            v_caja_i := 1 + floor(random() * 3)::int;
            v_turno := v_turnos[v_caja_i];
            v_dep := v_deps[v_caja_i];
            SELECT usuario_id INTO v_user FROM turnos_caja WHERE id = v_turno;

            -- cliente y forma de pago
            v_u := random();
            v_credito := FALSE; v_cliente := NULL; v_tipo := NULL;
            IF v_u < p_prob_credito AND v_credit_clients IS NOT NULL THEN
                v_cliente := v_credit_clients[1 + floor(random() * array_length(v_credit_clients, 1))::int];
                v_credito := TRUE;
            ELSIF v_u < p_prob_credito + p_prob_cliente AND v_reg_clients IS NOT NULL THEN
                v_cliente := v_reg_clients[1 + floor(random() * array_length(v_reg_clients, 1))::int];
            END IF;
            IF v_cliente IS NOT NULL THEN
                SELECT tipo_cliente_id INTO v_tipo FROM clientes WHERE id = v_cliente;
            END IF;

            INSERT INTO ventas (numero_venta, turno_id, usuario_id, cliente_id, tipo_cliente_id, deposito_id, estado, fecha_venta)
            VALUES ('V-' || lpad(nextval('seq_numero_venta')::text, 8, '0'), v_turno, v_user, v_cliente, v_tipo, v_dep,
                    'COMPLETADA', t)
            RETURNING id, numero_venta INTO v_venta, v_numero;

            v_x := random();
            v_n_lineas := CASE WHEN v_x < 0.40 THEN 1 WHEN v_x < 0.70 THEN 2 WHEN v_x < 0.90 THEN 3 ELSE 4 END;
            v_sub := 0; v_desc := 0; v_usados := ARRAY[]::bigint[];
            v_first_det := NULL;

            FOR l IN 1..v_n_lineas LOOP
                -- producto por peso, dentro del piso de venta de la caja; hasta 3 intentos para no repetir
                v_pid := NULL; v_intentos := 0;
                WHILE v_pid IS NULL AND v_intentos < 3 LOOP
                    v_intentos := v_intentos + 1;
                    v_x := random() * (SELECT max(cum) FROM wd WHERE dep_i = v_caja_i);   -- un solo sorteo por intento
                    SELECT pid INTO v_pid FROM wd WHERE dep_i = v_caja_i AND cum >= v_x ORDER BY cum LIMIT 1;
                    IF v_pid = ANY (v_usados) THEN v_pid := NULL; END IF;
                END LOOP;
                CONTINUE WHEN v_pid IS NULL;
                v_usados := v_usados || v_pid;
                SELECT pres_base, pres_alt, factor_alt INTO pr FROM ps WHERE ps.pid = v_pid;

                -- cantidad: casi siempre unidades sueltas; a veces la presentacion mayor (caja/paquete)
                v_qty := LEAST(1 + floor(-ln(1 - random()) * 1.2), 8);
                v_pres := pr.pres_base; v_factor := 1;
                IF pr.pres_alt IS NOT NULL AND random() < 0.08 THEN
                    v_pres := pr.pres_alt; v_factor := pr.factor_alt; v_qty := 1;
                END IF;
                v_need := v_qty * v_factor;

                SELECT COALESCE(sum(i.stock_actual), 0) INTO v_avail
                FROM inventarios i JOIN lotes l2 ON l2.id = i.lote_id
                WHERE i.deposito_id = v_dep AND l2.producto_id = v_pid AND i.stock_actual > 0
                  AND (l2.fecha_vencimiento IS NULL OR l2.fecha_vencimiento >= d);
                IF v_avail < 1 THEN
                    n_quiebres := n_quiebres + 1;      -- venta perdida por falta de stock
                    CONTINUE;
                END IF;
                IF v_factor > 1 THEN
                    -- el formato mayor solo se vende si el primer lote FEFO lo cubre entero; si no, se vende suelto
                    IF COALESCE((SELECT i.stock_actual FROM inventarios i JOIN lotes l2 ON l2.id = i.lote_id
                                 WHERE i.deposito_id = v_dep AND l2.producto_id = v_pid AND i.stock_actual > 0
                                   AND (l2.fecha_vencimiento IS NULL OR l2.fecha_vencimiento >= d)
                                 ORDER BY l2.fecha_vencimiento NULLS LAST, l2.id LIMIT 1), 0) < v_need THEN
                        v_pres := pr.pres_base; v_factor := 1; v_qty := LEAST(v_need, 8); v_need := v_qty;
                    END IF;
                END IF;
                IF v_avail < v_need THEN
                    v_qty := floor(v_avail / v_factor); v_need := v_qty * v_factor;
                    IF v_qty < 1 THEN n_quiebres := n_quiebres + 1; CONTINUE; END IF;
                END IF;

                SELECT precio INTO v_price FROM precios_presentacion
                WHERE presentacion_id = v_pres AND deleted_at IS NULL AND estado = 'ACTIVO'
                  AND (tipo_cliente_id = v_tipo OR tipo_cliente_id IS NULL) AND cantidad_minima <= v_qty
                ORDER BY (tipo_cliente_id IS NOT NULL) DESC, cantidad_minima DESC LIMIT 1;
                IF v_price IS NULL THEN CONTINUE; END IF;

                -- FEFO en cascada: un renglon por lote
                v_rem := v_need;
                FOR lt IN SELECT i.lote_id, i.stock_actual, l2.porcentaje_descuento_vencimiento AS pct,
                                 l2.costo_unitario_compra AS costo
                          FROM inventarios i JOIN lotes l2 ON l2.id = i.lote_id
                          WHERE i.deposito_id = v_dep AND l2.producto_id = v_pid AND i.stock_actual > 0
                            AND (l2.fecha_vencimiento IS NULL OR l2.fecha_vencimiento >= d)
                          ORDER BY l2.fecha_vencimiento NULLS LAST, l2.id LOOP
                    EXIT WHEN v_rem <= 0;
                    v_take := LEAST(v_rem, lt.stock_actual);
                    v_line_desc := round(v_price * (v_take / v_factor) * lt.pct / 100, 2);
                    INSERT INTO venta_detalle (venta_id, producto_id, presentacion_id, cantidad, factor_conversion,
                                               precio_unitario, descuento, subtotal)
                    VALUES (v_venta, v_pid, v_pres, v_take / v_factor, v_factor, v_price, v_line_desc,
                            round(v_price * (v_take / v_factor), 2) - v_line_desc)
                    RETURNING id INTO v_vdet;
                    INSERT INTO venta_detalle_lote (venta_detalle_id, lote_id, cantidad, costo_unitario)
                    VALUES (v_vdet, lt.lote_id, v_take, lt.costo);
                    PERFORM pg_temp.stock(v_dep, lt.lote_id, -v_take, t);
                    v_sub := v_sub + round(v_price * (v_take / v_factor), 2);
                    v_desc := v_desc + v_line_desc;
                    v_rem := v_rem - v_take;
                    IF v_first_det IS NULL THEN
                        v_first_det := v_vdet; v_first_lote := lt.lote_id; v_first_factor := v_factor;
                        v_first_qty := v_take / v_factor; v_first_sub := round(v_price * (v_take / v_factor), 2) - v_line_desc;
                    END IF;
                END LOOP;
            END LOOP;

            -- una venta sin lineas (todo sin stock) no existe
            IF v_first_det IS NULL THEN
                DELETE FROM ventas WHERE id = v_venta;
                s := s - 1;
                CONTINUE;
            END IF;

            v_total := v_sub - v_desc; v_vuelto := 0;

            -- credito: respeta el limite del cliente; si no alcanza, se cobra al contado
            IF v_credito THEN
                SELECT limite_credito INTO v_limite FROM clientes WHERE id = v_cliente;
                SELECT COALESCE(sum(saldo), 0) INTO v_saldo FROM cuentas_por_cobrar WHERE cliente_id = v_cliente AND estado = 'PENDIENTE';
                v_cred := CASE WHEN random() < 0.5 THEN v_total ELSE round(v_total / 2, 2) END;
                IF v_saldo + v_cred > COALESCE(v_limite, 0) OR v_cred <= 0 THEN v_credito := FALSE; END IF;
            END IF;

            -- pagos
            IF v_credito THEN
                INSERT INTO venta_pagos (venta_id, metodo_pago_id, monto) VALUES (v_venta, m_cre, v_cred);
                IF v_cred < v_total THEN
                    INSERT INTO venta_pagos (venta_id, metodo_pago_id, monto) VALUES (v_venta, m_efe, v_total - v_cred);
                END IF;
                INSERT INTO cuentas_por_cobrar (cliente_id, venta_id, monto_original, saldo, estado, fecha, created_by, updated_by,
                                                created_at, updated_at)
                VALUES (v_cliente, v_venta, v_cred, v_cred, 'PENDIENTE', t, 'SEED_CARGA', 'SEED_CARGA', t, t);
                n_cred := n_cred + 1;
            ELSE
                v_x := random();
                IF v_x < 0.50 THEN
                    v_recibido := ceil(v_total / CASE WHEN v_total > 50 THEN 10 ELSE 5 END) * CASE WHEN v_total > 50 THEN 10 ELSE 5 END;
                    v_vuelto := v_recibido - v_total;
                    INSERT INTO venta_pagos (venta_id, metodo_pago_id, monto) VALUES (v_venta, m_efe, v_total);
                ELSIF v_x < 0.72 THEN
                    v_metodo := CASE WHEN m_tarj IS NOT NULL AND random() < 0.5 THEN m_tarj[1 + floor(random() * array_length(m_tarj, 1))::int]
                                     ELSE COALESCE(m_tar, m_efe) END;
                    INSERT INTO venta_pagos (venta_id, metodo_pago_id, monto, referencia)
                    VALUES (v_venta, v_metodo, v_total, 'AUT-' || lpad((floor(random() * 1000000))::text, 6, '0'));
                ELSIF v_x < 0.90 AND m_trf IS NOT NULL THEN
                    INSERT INTO venta_pagos (venta_id, metodo_pago_id, monto, referencia)
                    VALUES (v_venta, m_trf[1 + floor(random() * array_length(m_trf, 1))::int], v_total,
                            'TRF-' || lpad((floor(random() * 10000000))::text, 7, '0'));
                ELSE
                    INSERT INTO venta_pagos (venta_id, metodo_pago_id, monto) VALUES (v_venta, m_efe, round(v_total / 2, 2));
                    INSERT INTO venta_pagos (venta_id, metodo_pago_id, monto, referencia)
                    VALUES (v_venta, COALESCE(m_tar, m_efe), v_total - round(v_total / 2, 2),
                            'AUT-' || lpad((floor(random() * 1000000))::text, 6, '0'));
                END IF;
            END IF;

            -- kardex de la venta
            INSERT INTO movimientos_inventarios (tipo_movimiento, deposito_origen_id, usuario_id, fecha_movimiento, observacion,
                                                 referencia_tipo, referencia_id)
            VALUES ('SALIDA', v_dep, v_user, t, 'Venta ' || v_numero, 'VENTA', v_venta)
            RETURNING id INTO v_mov;
            INSERT INTO movimiento_inventario_detalle (movimiento_id, producto_id, lote_id, cantidad)
            SELECT v_mov, vd.producto_id, vdl.lote_id, vdl.cantidad
            FROM venta_detalle vd JOIN venta_detalle_lote vdl ON vdl.venta_detalle_id = vd.id WHERE vd.venta_id = v_venta;

            v_anulada := NOT v_credito AND random() < p_prob_anulada;
            UPDATE ventas SET subtotal = v_sub, descuento_total = v_desc, total = v_total, vuelto = v_vuelto,
                   estado = CASE WHEN v_anulada THEN 'ANULADA' ELSE 'COMPLETADA' END,
                   anulada_por = CASE WHEN v_anulada THEN (SELECT username FROM usuarios WHERE id = v_user) END,
                   fecha_anulacion = CASE WHEN v_anulada THEN LEAST(t + interval '8 minutes', v_hoy) END,
                   motivo_anulacion = CASE WHEN v_anulada THEN 'El cliente cambió de opinión antes de retirarse' END
            WHERE id = v_venta;
            n_ventas := n_ventas + 1;

            IF v_anulada THEN
                -- la anulacion devuelve el stock a sus lotes
                n_anul := n_anul + 1;
                INSERT INTO movimientos_inventarios (tipo_movimiento, deposito_destino_id, usuario_id, fecha_movimiento, observacion,
                                                     referencia_tipo, referencia_id)
                VALUES ('ENTRADA', v_dep, v_user, LEAST(t + interval '8 minutes', v_hoy), 'Anulación de la venta ' || v_numero,
                        'ANULACION_VENTA', v_venta)
                RETURNING id INTO v_mov;
                FOR ln IN SELECT vd.producto_id, vdl.lote_id, vdl.cantidad
                          FROM venta_detalle vd JOIN venta_detalle_lote vdl ON vdl.venta_detalle_id = vd.id
                          WHERE vd.venta_id = v_venta LOOP
                    PERFORM pg_temp.stock(v_dep, ln.lote_id, ln.cantidad, t);
                    INSERT INTO movimiento_inventario_detalle (movimiento_id, producto_id, lote_id, cantidad)
                    VALUES (v_mov, ln.producto_id, ln.lote_id, ln.cantidad);
                END LOOP;
            ELSIF NOT v_credito AND v_first_factor = 1 AND random() < p_prob_devolucion
                  AND LEAST(t + interval '30 minutes', v_hoy) > t THEN
                -- devolucion de una unidad de la primera linea, reembolsada en el mismo turno
                n_dev := n_dev + 1;
                SELECT metodo_pago_id INTO v_metodo FROM venta_pagos WHERE venta_id = v_venta ORDER BY id LIMIT 1;
                v_amt := round(v_first_sub / v_first_qty, 2);
                INSERT INTO devoluciones_venta (venta_id, usuario_id, fecha, motivo, monto_reembolsado, metodo_pago_id, turno_id,
                                                numero_devolucion)
                VALUES (v_venta, v_user, LEAST(t + interval '30 minutes', v_hoy),
                        (ARRAY['Producto en mal estado', 'Cliente se equivocó de producto', 'Empaque dañado',
                               'Producto próximo a vencer', 'Cliente no quedó conforme'])[1 + floor(random() * 5)::int],
                        v_amt, v_metodo, v_turno, 'D-' || lpad(nextval('seq_numero_devolucion')::text, 8, '0'))
                RETURNING id INTO v_dev;
                INSERT INTO devolucion_venta_detalle (devolucion_id, venta_detalle_id, lote_id, cantidad, reingresa_stock)
                VALUES (v_dev, v_first_det, v_first_lote, 1, random() < 0.8);
                IF (SELECT reingresa_stock FROM devolucion_venta_detalle WHERE devolucion_id = v_dev) THEN
                    PERFORM pg_temp.stock(v_dep, v_first_lote, 1, t);
                    INSERT INTO movimientos_inventarios (tipo_movimiento, deposito_destino_id, usuario_id, fecha_movimiento,
                                                         observacion, referencia_tipo, referencia_id)
                    VALUES ('ENTRADA', v_dep, v_user, LEAST(t + interval '30 minutes', v_hoy), 'Devolución de la venta ' || v_numero,
                            'DEVOLUCION_VENTA', v_dev)
                    RETURNING id INTO v_mov;
                    INSERT INTO movimiento_inventario_detalle (movimiento_id, producto_id, lote_id, cantidad)
                    SELECT v_mov, producto_id, v_first_lote, 1 FROM venta_detalle WHERE id = v_first_det;
                END IF;
            END IF;
        END LOOP;

        -- F6. movimientos de caja y abonos de deuda
        FOR i IN 1..3 LOOP
            IF random() < p_prob_caja AND LEAST(d + time '12:00', v_hoy) > d + time '08:30' THEN
                SELECT usuario_id INTO v_user FROM turnos_caja WHERE id = v_turnos[i];
                INSERT INTO movimientos_caja (turno_id, usuario_id, tipo, monto, motivo, fecha)
                VALUES (v_turnos[i], v_user, CASE WHEN random() < 0.65 THEN 'EGRESO' ELSE 'INGRESO' END,
                        round((10 + random() * 90)::numeric, 2),
                        (ARRAY['Pago al proveedor de pan', 'Compra de bolsas y cinta de embalaje', 'Reposición de monedas',
                               'Pago de flete', 'Gasto de limpieza', 'Fondo adicional de cambio', 'Retiro de efectivo a bóveda'])
                            [1 + floor(random() * 7)::int],
                        LEAST(d + time '12:00' + i * interval '7 minutes', v_hoy));
            END IF;
        END LOOP;

        FOR s IN 1..2 LOOP
            CONTINUE WHEN random() > 0.55 OR LEAST(d + time '16:00', v_hoy) <= d + time '08:30';
            SELECT c.cliente_id, sum(c.saldo) INTO v_cli, v_debt
            FROM cuentas_por_cobrar c WHERE c.estado = 'PENDIENTE' AND c.fecha < d + time '08:00'
            GROUP BY c.cliente_id HAVING sum(c.saldo) > 1
            ORDER BY random() LIMIT 1;
            CONTINUE WHEN v_cli IS NULL;
            v_amt := CASE WHEN random() < 0.5 THEN greatest(round(v_debt * 0.4, 2), 0.01) ELSE v_debt END;
            v_turno := v_turnos[1 + floor(random() * 3)::int];
            SELECT usuario_id INTO v_user FROM turnos_caja WHERE id = v_turno;
            v_metodo := CASE WHEN random() < 0.75 THEN m_efe ELSE COALESCE(m_trf[1], m_efe) END;
            INSERT INTO abonos_credito (numero_abono, cliente_id, turno_id, usuario_id, metodo_pago_id, monto, referencia, fecha)
            VALUES ('AB-' || lpad(nextval('seq_numero_abono')::text, 8, '0'), v_cli, v_turno, v_user, v_metodo, v_amt,
                    CASE WHEN v_metodo <> m_efe THEN 'TRF-' || lpad((floor(random() * 10000000))::text, 7, '0') END,
                    LEAST(d + time '15:00' + s * interval '11 minutes', v_hoy))
            RETURNING id INTO v_abono;
            v_restante := v_amt;
            FOR ln IN SELECT id, saldo FROM cuentas_por_cobrar WHERE cliente_id = v_cli AND estado = 'PENDIENTE'
                      ORDER BY fecha, id LOOP
                EXIT WHEN v_restante <= 0;
                v_aplicado := LEAST(ln.saldo, v_restante);
                INSERT INTO abonos_credito_aplicacion (abono_id, cuenta_id, monto) VALUES (v_abono, ln.id, v_aplicado);
                UPDATE cuentas_por_cobrar
                SET saldo = saldo - v_aplicado, estado = CASE WHEN saldo - v_aplicado = 0 THEN 'PAGADA' ELSE 'PENDIENTE' END,
                    updated_at = d + time '15:00'
                WHERE id = ln.id;
                v_restante := v_restante - v_aplicado;
            END LOOP;
            n_abonos := n_abonos + 1;
        END LOOP;

        -- F7. cierre de los turnos de dias pasados (esperado = apertura + ingresos - egresos + efectivo + abonos - reembolsos)
        IF k > 0 THEN
            UPDATE turnos_caja tc
            SET fecha_cierre = d + time '20:10',
                monto_cierre_esperado = x.esperado,
                monto_cierre_declarado = x.esperado + CASE WHEN random() < 0.70 THEN 0
                                                           WHEN random() < 0.5 THEN -round((1 + random() * 7)::numeric, 2)
                                                           ELSE round((1 + random() * 4)::numeric, 2) END,
                estado = 'CERRADO'
            FROM (SELECT t2.id,
                         t2.monto_apertura
                         + COALESCE((SELECT sum(monto) FROM movimientos_caja WHERE turno_id = t2.id AND tipo = 'INGRESO'), 0)
                         - COALESCE((SELECT sum(monto) FROM movimientos_caja WHERE turno_id = t2.id AND tipo = 'EGRESO'), 0)
                         + COALESCE((SELECT sum(vp.monto) FROM venta_pagos vp JOIN ventas v3 ON v3.id = vp.venta_id
                                     WHERE v3.turno_id = t2.id AND v3.estado = 'COMPLETADA' AND vp.metodo_pago_id = m_efe), 0)
                         + COALESCE((SELECT sum(ab.monto) FROM abonos_credito ab WHERE ab.turno_id = t2.id AND ab.metodo_pago_id = m_efe), 0)
                         - COALESCE((SELECT sum(dv.monto_reembolsado) FROM devoluciones_venta dv
                                     WHERE dv.turno_id = t2.id AND dv.metodo_pago_id = m_efe), 0) AS esperado
                  FROM turnos_caja t2 WHERE t2.id = ANY (v_turnos)) x
            WHERE tc.id = x.id;
        END IF;

        -- F8. reposicion con la politica empirica del encargado: punto fijo y cantidad "a ojo"
        DELETE FROM pos;
        INSERT INTO pos (pid, stock, transito)
        SELECT ps.pid,
               COALESCE((SELECT sum(i.stock_actual) FROM inventarios i JOIN lotes l2 ON l2.id = i.lote_id
                         WHERE l2.producto_id = ps.pid AND (l2.fecha_vencimiento IS NULL OR l2.fecha_vencimiento >= d)), 0),
               COALESCE((SELECT sum(p2.qty) FROM pend p2 WHERE p2.pid = ps.pid), 0)
        FROM ps;

        DELETE FROM pedir;
        INSERT INTO pedir (pid, prov, dep, qty, costo, lt)
        SELECT ps.pid, ps.prov, ps.dep,
               GREATEST(ceil(ps.dem_esp * ps.ped_dias * (0.7 + 0.7 * random())), 6),
               round(ps.costo0 * (1 + ps.tendencia * v_dias_idx / 30.0)
                     * (1 + CASE WHEN v_dias_idx >= p_dias - 30 THEN ps.alza ELSE 0 END)
                     * (1 + (random() - 0.5) * 0.02)::numeric, 4),
               0
        FROM ps JOIN pos USING (pid)
        WHERE ps.ordenable AND ps.dem_esp > 0
          AND (ps.perfil <> 'F' OR v_dias_idx >= ps.lanzamiento - 12)
          AND pos.stock + pos.transito <= ps.dem_esp * ps.min_dias
          AND NOT (ps.perfil = 'F' AND pos.transito > 0);

        FOR r IN SELECT DISTINCT prov, dep FROM pedir ORDER BY prov, dep LOOP
            SELECT lt_media, lt_desv INTO pr FROM ps WHERE ps.prov = r.prov LIMIT 1;
            v_lt := GREATEST(1, round(pr.lt_media + pr.lt_desv * pg_temp.randn())::int);
            t := LEAST(d + time '09:00', v_hoy);
            INSERT INTO pedidos_proveedor (numero_pedido, proveedor_id, deposito_destino_id, estado, fecha_pedido,
                                           fecha_entrega_estimada, observacion, total, created_by, updated_by, created_at, updated_at)
            VALUES ('PP-' || lpad(nextval('seq_numero_pedido_proveedor')::text, 8, '0'), r.prov, r.dep, 'ENVIADO', d,
                    d + round(pr.lt_media)::int, 'Reposición habitual', 0, 'SEED_CARGA', 'SEED_CARGA', t, t)
            RETURNING id INTO v_ped;
            FOR lt IN SELECT * FROM pedir WHERE prov = r.prov AND dep = r.dep ORDER BY pid LOOP
                INSERT INTO pedido_proveedor_detalle (pedido_id, producto_id, cantidad_pedida, cantidad_recibida, costo_unitario)
                VALUES (v_ped, lt.pid, lt.qty, 0, lt.costo)
                RETURNING id INTO v_det;
                INSERT INTO pend (pedido, det, pid, qty, costo, llegada, dep, vida, perecedero)
                SELECT v_ped, v_det, lt.pid, lt.qty, lt.costo, d + v_lt, r.dep, ps.vida, ps.perecedero FROM ps WHERE ps.pid = lt.pid;
            END LOOP;
            UPDATE pedidos_proveedor
            SET total = (SELECT sum(cantidad_pedida * costo_unitario) FROM pedido_proveedor_detalle WHERE pedido_id = v_ped)
            WHERE id = v_ped;
            n_pedidos := n_pedidos + 1;
        END LOOP;
    END LOOP;

    RAISE NOTICE 'Carga lista: % ventas (% anuladas, % a credito, % devoluciones), % abonos, % pedidos, % bajas por vencimiento, % ventas de linea perdidas por falta de stock.',
        n_ventas, n_anul, n_cred, n_dev, n_abonos, n_pedidos, n_mermas, n_quiebres;
END
$carga$;

ANALYZE;
