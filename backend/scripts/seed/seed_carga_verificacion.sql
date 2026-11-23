-- Verificacion de integridad de la semilla de carga (seed_carga.sql). Cada fila es una comprobacion:
-- resultado = esperado => OK. Ejecutar en la misma base (tienda_db_carga).
SELECT comprobacion, resultado, esperado, CASE WHEN resultado = esperado THEN 'OK' ELSE 'FALLA' END AS estado
FROM (
    SELECT 1 AS orden, 'Ventas generadas' AS comprobacion, (SELECT count(*) FROM ventas)::text AS resultado, '10000' AS esperado
    UNION ALL SELECT 2, 'Lotes o inventarios con stock negativo',
        (SELECT count(*) FROM inventarios WHERE stock_actual < 0)::text, '0'
    UNION ALL SELECT 3, 'Ventas cuyos pagos no suman el total',
        (SELECT count(*) FROM ventas v WHERE v.total <> (SELECT COALESCE(sum(monto), 0) FROM venta_pagos WHERE venta_id = v.id))::text, '0'
    UNION ALL SELECT 4, 'Ventas cuyo total no es la suma de sus renglones',
        (SELECT count(*) FROM ventas v
         WHERE v.total <> (SELECT COALESCE(sum(subtotal), 0) FROM venta_detalle WHERE venta_id = v.id))::text, '0'
    UNION ALL SELECT 5, 'Renglones cuya cantidad por lote no coincide con cantidad x factor',
        (SELECT count(*) FROM venta_detalle d
         WHERE round(d.cantidad * d.factor_conversion, 4) <>
               (SELECT COALESCE(sum(cantidad), 0) FROM venta_detalle_lote WHERE venta_detalle_id = d.id))::text, '0'
    UNION ALL SELECT 6, 'Stock distinto de lo que dice el kardex (deposito x lote)',
        (SELECT count(*) FROM inventarios i
         LEFT JOIN (SELECT dep, lote, sum(delta) AS neto
                    FROM (SELECT m.deposito_destino_id AS dep, d.lote_id AS lote, d.cantidad AS delta
                          FROM movimientos_inventarios m JOIN movimiento_inventario_detalle d ON d.movimiento_id = m.id
                          WHERE m.deposito_destino_id IS NOT NULL
                          UNION ALL
                          SELECT m.deposito_origen_id, d.lote_id, -d.cantidad
                          FROM movimientos_inventarios m JOIN movimiento_inventario_detalle d ON d.movimiento_id = m.id
                          WHERE m.deposito_origen_id IS NOT NULL) x
                    GROUP BY dep, lote) k ON k.dep = i.deposito_id AND k.lote = i.lote_id
         WHERE i.stock_actual <> COALESCE(k.neto, 0))::text, '0'
    UNION ALL SELECT 7, 'Ventas COMPLETADAS que consumieron un lote vencido',
        (SELECT count(*) FROM venta_detalle_lote vdl
         JOIN venta_detalle d ON d.id = vdl.venta_detalle_id JOIN ventas v ON v.id = d.venta_id
         JOIN lotes l ON l.id = vdl.lote_id
         WHERE l.fecha_vencimiento < CAST(v.fecha_venta AS DATE))::text, '0'
    UNION ALL SELECT 8, 'Turnos cerrados sin esperado o sin declarado',
        (SELECT count(*) FROM turnos_caja WHERE estado = 'CERRADO'
           AND (monto_cierre_esperado IS NULL OR monto_cierre_declarado IS NULL))::text, '0'
    UNION ALL SELECT 9, 'Turnos abiertos (solo los de hoy)',
        (SELECT count(*) FROM turnos_caja WHERE estado = 'ABIERTO' AND CAST(fecha_apertura AS DATE) <> current_date)::text, '0'
    UNION ALL SELECT 10, 'Cuentas por cobrar con saldo distinto de original - abonos aplicados',
        (SELECT count(*) FROM cuentas_por_cobrar c
         WHERE c.saldo <> c.monto_original - COALESCE((SELECT sum(monto) FROM abonos_credito_aplicacion WHERE cuenta_id = c.id), 0))::text, '0'
    UNION ALL SELECT 11, 'Abonos cuyo monto no coincide con lo aplicado a cuentas',
        (SELECT count(*) FROM abonos_credito a
         WHERE a.monto <> COALESCE((SELECT sum(monto) FROM abonos_credito_aplicacion WHERE abono_id = a.id), 0))::text, '0'
    UNION ALL SELECT 12, 'Pedidos RECIBIDOS con lineas sin recibir completas',
        (SELECT count(*) FROM pedidos_proveedor p JOIN pedido_proveedor_detalle d ON d.pedido_id = p.id
         WHERE p.estado = 'RECIBIDO' AND d.cantidad_recibida <> d.cantidad_pedida)::text, '0'
    UNION ALL SELECT 13, 'Pedidos con total distinto de la suma de sus lineas',
        (SELECT count(*) FROM pedidos_proveedor p
         WHERE p.total <> COALESCE((SELECT sum(cantidad_pedida * costo_unitario) FROM pedido_proveedor_detalle WHERE pedido_id = p.id), 0))::text, '0'
    UNION ALL SELECT 14, 'Ventas fuera del horario del turno o en el futuro',
        (SELECT count(*) FROM ventas v JOIN turnos_caja t ON t.id = v.turno_id
         WHERE v.fecha_venta < t.fecha_apertura OR v.fecha_venta > localtimestamp)::text, '0'
) q
ORDER BY orden;

-- Resumen para mirar a ojo
SELECT 'ventas por mes' AS tema, to_char(fecha_venta, 'YYYY-MM') AS clave, count(*) AS cantidad, round(sum(total), 2) AS monto
FROM ventas WHERE estado = 'COMPLETADA' GROUP BY 2
UNION ALL
SELECT 'ventas por estado', estado, count(*), round(sum(total), 2) FROM ventas GROUP BY estado
UNION ALL
SELECT 'pagos por tipo', CASE WHEN m.nombre LIKE 'TARJETA%' THEN 'TARJETA' WHEN m.nombre LIKE 'TRANSFERENCIA%' THEN 'TRANSFERENCIA' ELSE m.nombre END,
       count(*), round(sum(p.monto), 2)
FROM venta_pagos p JOIN metodos_pago m ON m.id = p.metodo_pago_id GROUP BY 2
UNION ALL
SELECT 'pedidos por estado', estado, count(*), round(sum(total), 2) FROM pedidos_proveedor GROUP BY estado
UNION ALL
SELECT 'turnos', estado, count(*), round(sum(COALESCE(monto_cierre_declarado - monto_cierre_esperado, 0)), 2)
FROM turnos_caja GROUP BY estado
ORDER BY 1, 2;
