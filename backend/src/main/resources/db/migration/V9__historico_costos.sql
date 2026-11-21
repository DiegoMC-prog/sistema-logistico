-- RF-35 (1/2): historico de costos por proveedor y su variacion, que alimenta el motor de reorden.

-- ---------------------------------------------------------------- historico de costos
-- Un punto por linea de pedido confirmada (los borradores todavia no son un precio pactado y los cancelados no cuentan).
CREATE VIEW v_historico_costos_proveedor AS
SELECT d.id AS detalle_id,
       pp.id AS pedido_id,
       pp.numero_pedido,
       pp.proveedor_id,
       d.producto_id,
       pp.fecha_pedido AS fecha,
       d.costo_unitario,
       d.cantidad_pedida,
       pp.estado
FROM pedido_proveedor_detalle d
JOIN pedidos_proveedor pp ON pp.id = d.pedido_id
WHERE pp.deleted_at IS NULL AND pp.estado IN ('ENVIADO', 'RECIBIDO_PARCIAL', 'RECIBIDO');

CREATE INDEX IF NOT EXISTS idx_pedidos_proveedor_proveedor_fecha ON pedidos_proveedor(proveedor_id, fecha_pedido);

-- Cada sugerencia guarda la variacion de costo del proveedor usada en el calculo y cuantos dias se adelanto la compra
ALTER TABLE sugerencias_reorden
    ADD COLUMN variacion_costo DECIMAL(9, 4),
    ADD COLUMN dias_anticipo INT NOT NULL DEFAULT 0;
