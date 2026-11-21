-- RF-13 (alertas tempranas de vencimiento) y RF-16 (aviso de stock bajo el minimo).
-- Una tarea programada revisa lotes y existencias y deja aqui las alertas; el frontend las muestra en la campana.

CREATE TABLE alertas(
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL,
    producto_id BIGINT NOT NULL REFERENCES productos(id),
    lote_id BIGINT REFERENCES lotes(id),
    -- Vencimiento del lote (VENCIMIENTO/VENCIDO); NULL en STOCK_BAJO
    fecha_referencia DATE,
    -- Existencias al momento de generar la alerta (unidades base)
    cantidad DECIMAL(14, 4) NOT NULL DEFAULT 0,
    mensaje VARCHAR(500) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    leida_por VARCHAR(150),
    leida_at TIMESTAMP,
    -- La condicion ya no se cumple (lote agotado, vencido o stock repuesto): deja de mostrarse como activa
    resuelta_at TIMESTAMP,

    CONSTRAINT ck_alertas_tipo CHECK (tipo IN ('VENCIMIENTO', 'VENCIDO', 'STOCK_BAJO')),
    CONSTRAINT ck_alertas_lote CHECK ((tipo = 'STOCK_BAJO') = (lote_id IS NULL))
);

-- Una sola alerta por lote y tipo: la tarea diaria no repite el aviso aunque se haya marcado como leida
CREATE UNIQUE INDEX uk_alertas_lote_tipo ON alertas(tipo, lote_id) WHERE lote_id IS NOT NULL;
-- Una sola alerta de stock bajo activa por producto; si se repone y vuelve a bajar se crea otra
CREATE UNIQUE INDEX uk_alertas_stock_activa ON alertas(producto_id) WHERE tipo = 'STOCK_BAJO' AND resuelta_at IS NULL;

CREATE INDEX idx_alertas_activas ON alertas(leida, fecha DESC) WHERE resuelta_at IS NULL;

INSERT INTO permisos (nombre, modulo, accion, estandar, descripcion, created_by, updated_by) VALUES
    ('ALERTA_VER', 'ALERTA', 'VER', FALSE, 'Ver las alertas de vencimiento y stock bajo y marcarlas como leidas', 'SISTEMA', 'SISTEMA');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permisos p
WHERE r.nombre IN ('ADMIN', 'REPOSITOR', 'ENCARGADO_COMPRAS') AND p.nombre = 'ALERTA_VER';
