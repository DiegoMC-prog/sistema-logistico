-- Reparte la actividad sembrada en los ultimos 21 dias para que los reportes por fecha tengan serie.
-- La API siempre registra "ahora"; aqui se mueven al pasado los turnos CERRADOS y todo lo que cuelga de ellos.
-- Los turnos abiertos conservan su fecha real. Ejecutar DESPUES de la fase 2.

-- 1. Turnos cerrados: de hace 21 dias a ayer, apertura 08:00 y cierre 18:00
WITH t AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn, COUNT(*) OVER () AS n
    FROM turnos_caja WHERE estado = 'CERRADO')
UPDATE turnos_caja c
SET fecha_apertura = (CURRENT_DATE - ((t.n - t.rn) * 21 / t.n + 1) * INTERVAL '1 day') + TIME '08:00',
    fecha_cierre   = (CURRENT_DATE - ((t.n - t.rn) * 21 / t.n + 1) * INTERVAL '1 day') + TIME '18:00'
FROM t WHERE c.id = t.id;

-- 2. Ventas de esos turnos: una cada 41 minutos desde la apertura
WITH r AS (
    SELECT id, turno_id, ROW_NUMBER() OVER (PARTITION BY turno_id ORDER BY id) AS rn FROM ventas)
UPDATE ventas v
SET fecha_venta = t.fecha_apertura + (r.rn * INTERVAL '41 minutes')
FROM r JOIN turnos_caja t ON t.id = r.turno_id
WHERE v.id = r.id AND t.estado = 'CERRADO';

UPDATE ventas v SET fecha_anulacion = v.fecha_venta + INTERVAL '12 minutes'
FROM turnos_caja t
WHERE t.id = v.turno_id AND t.estado = 'CERRADO' AND v.estado = 'ANULADA';

-- 3. Devoluciones y movimientos de caja de esos turnos
UPDATE devoluciones_venta d
SET fecha = t.fecha_apertura + INTERVAL '7 hours' + (d.id % 50) * INTERVAL '1 minute'
FROM turnos_caja t WHERE t.id = d.turno_id AND t.estado = 'CERRADO';

UPDATE movimientos_caja m
SET fecha = t.fecha_apertura + INTERVAL '2 hours' + (m.id % 10) * INTERVAL '1 minute'
FROM turnos_caja t WHERE t.id = m.turno_id AND t.estado = 'CERRADO';

-- 4. Compras e inventario inicial: ocurrieron antes de las ventas (hace unos 30 dias). Sin esto el kardex mostraria
--    ventas anteriores al ingreso de la mercaderia.
UPDATE pedidos_proveedor SET fecha_pedido = CURRENT_DATE - 35 + (id % 5)::int;

UPDATE recepciones_pedido SET fecha_recepcion = (CURRENT_DATE - 30) + id * INTERVAL '1 hour';

WITH iniciales AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS rn
    FROM movimientos_inventarios
    WHERE referencia_tipo IS NULL OR referencia_tipo = 'PEDIDO_PROVEEDOR')
UPDATE movimientos_inventarios m
SET fecha_movimiento = (CURRENT_DATE - 30) + (i.rn * INTERVAL '20 minutes')
FROM iniciales i WHERE m.id = i.id;

-- 5. Movimientos de inventario de las ventas, sus anulaciones y las devoluciones, con la fecha del hecho
UPDATE movimientos_inventarios m SET fecha_movimiento = v.fecha_venta
FROM ventas v
WHERE m.referencia_tipo = 'VENTA' AND m.referencia_id = v.id;

UPDATE movimientos_inventarios m SET fecha_movimiento = v.fecha_anulacion
FROM ventas v
WHERE m.referencia_tipo = 'ANULACION_VENTA' AND m.referencia_id = v.id AND v.fecha_anulacion IS NOT NULL;

UPDATE movimientos_inventarios m SET fecha_movimiento = d.fecha
FROM devoluciones_venta d
JOIN turnos_caja t ON t.id = d.turno_id AND t.estado = 'CERRADO'
WHERE m.referencia_tipo = 'DEVOLUCION_VENTA' AND m.referencia_id = d.id;

-- 6. admin con la contrasena conocida de la semilla y sin obligacion de cambiarla (solo desarrollo)
UPDATE usuarios SET password = '$2a$10$T8UudTC9A/EMPgdnXHRO9eJLeTFuC9hrdqaRGLymJGUCdb.1B8fJ6',
                    password_temporal = FALSE
WHERE username = 'admin';
