-- Mitigaciones de la matriz de riesgos: R06 (bloqueo de cuenta), R03 (idempotencia de abonos),
-- R08 (autorizacion del administrador sobre un monto umbral) y R10 (nota de ajuste auditada del costo de un lote).

-- ---------------------------------------------------------------- R06: bloqueo por intentos fallidos
ALTER TABLE usuarios
    ADD COLUMN intentos_fallidos INT NOT NULL DEFAULT 0,
    ADD COLUMN bloqueado_hasta TIMESTAMP;

-- ---------------------------------------------------------------- R03: abonos idempotentes
-- Un reintento con la misma clave del mismo usuario devuelve el abono ya registrado (como en ventas y devoluciones)
ALTER TABLE abonos_credito
    ADD COLUMN idempotency_key VARCHAR(64),
    ADD COLUMN idempotency_hash VARCHAR(64);

CREATE UNIQUE INDEX uk_abonos_credito_idempotencia ON abonos_credito(usuario_id, idempotency_key)
    WHERE idempotency_key IS NOT NULL;

-- ---------------------------------------------------------------- R08: autorizaciones sobre el monto umbral
-- Bitacora de solo insercion: que operacion, por cuanto, quien la pidio y que administrador la autorizo
CREATE TABLE autorizaciones_operacion (
    id BIGSERIAL PRIMARY KEY,
    operacion VARCHAR(30) NOT NULL,
    referencia_id BIGINT NOT NULL,
    monto DECIMAL(14, 2) NOT NULL,
    umbral DECIMAL(14, 2) NOT NULL,
    solicitado_por VARCHAR(150) NOT NULL,
    autorizado_por VARCHAR(150) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_autorizaciones_operacion CHECK (operacion IN ('ANULACION_VENTA', 'DEVOLUCION', 'CAMBIO'))
);

CREATE INDEX idx_autorizaciones_operacion_ref ON autorizaciones_operacion(operacion, referencia_id);
CREATE INDEX idx_autorizaciones_operacion_fecha ON autorizaciones_operacion(fecha DESC);

INSERT INTO permisos (nombre, modulo, accion, estandar, descripcion, created_by, updated_by) VALUES
    ('OPERACION_AUTORIZAR', 'AUTORIZACION', 'AUTORIZAR', FALSE,
     'Autorizar devoluciones, cambios y anulaciones que superan el monto umbral', 'SISTEMA', 'SISTEMA');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permisos p
WHERE r.nombre = 'ADMIN' AND p.nombre = 'OPERACION_AUTORIZAR';

-- ---------------------------------------------------------------- R10: ajustes de costo de lote
-- El costo de un lote solo se corrige con una nota: queda el valor anterior, el nuevo, el motivo y quien lo hizo
CREATE TABLE ajustes_costo_lote (
    id BIGSERIAL PRIMARY KEY,
    lote_id BIGINT NOT NULL REFERENCES lotes(id),
    costo_anterior DECIMAL(14, 4) NOT NULL,
    costo_nuevo DECIMAL(14, 4) NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    usuario VARCHAR(150) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_ajustes_costo_lote_distinto CHECK (costo_anterior <> costo_nuevo)
);

CREATE INDEX idx_ajustes_costo_lote_lote ON ajustes_costo_lote(lote_id, fecha DESC);
