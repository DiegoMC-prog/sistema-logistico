-- RF-27/28/29: ventas a credito (fiados), cuentas por cobrar y abonos de deuda.

-- Metodo de pago CREDITO: la parte de la venta que el cliente deja a deber (distinto de CREDITO_DEVOLUCION).
INSERT INTO metodos_pago (nombre, created_by, updated_by) VALUES ('CREDITO', 'SISTEMA', 'SISTEMA');

-- Configuracion de credito por cliente
ALTER TABLE clientes
    ADD COLUMN credito_habilitado BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN limite_credito DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    ADD CONSTRAINT ck_clientes_limite_credito CHECK (limite_credito >= 0);

-- Una cuenta por cobrar por venta a credito
CREATE TABLE cuentas_por_cobrar(
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id),
    venta_id BIGINT NOT NULL REFERENCES ventas(id),
    monto_original DECIMAL(14, 2) NOT NULL,
    saldo DECIMAL(14, 2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(150) NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_cuentas_por_cobrar_venta UNIQUE (venta_id),
    CONSTRAINT ck_cuentas_por_cobrar_estado CHECK (estado IN ('PENDIENTE', 'PAGADA', 'ANULADA')),
    CONSTRAINT ck_cuentas_por_cobrar_montos CHECK (monto_original > 0 AND saldo >= 0 AND saldo <= monto_original)
);

CREATE INDEX idx_cuentas_por_cobrar_cliente ON cuentas_por_cobrar(cliente_id, estado, fecha);

-- Un abono entra a la caja del turno abierto y se reparte entre las deudas mas antiguas
CREATE TABLE abonos_credito(
    id BIGSERIAL PRIMARY KEY,
    numero_abono VARCHAR(30) NOT NULL,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id),
    turno_id BIGINT NOT NULL REFERENCES turnos_caja(id),
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    metodo_pago_id BIGINT NOT NULL REFERENCES metodos_pago(id),
    monto DECIMAL(14, 2) NOT NULL,
    referencia VARCHAR(100),
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_abonos_credito_numero UNIQUE (numero_abono),
    CONSTRAINT ck_abonos_credito_monto CHECK (monto > 0)
);

CREATE INDEX idx_abonos_credito_cliente ON abonos_credito(cliente_id);
CREATE INDEX idx_abonos_credito_turno ON abonos_credito(turno_id);

CREATE TABLE abonos_credito_aplicacion(
    id BIGSERIAL PRIMARY KEY,
    abono_id BIGINT NOT NULL REFERENCES abonos_credito(id),
    cuenta_id BIGINT NOT NULL REFERENCES cuentas_por_cobrar(id),
    monto DECIMAL(14, 2) NOT NULL,

    CONSTRAINT ck_abonos_credito_aplicacion_monto CHECK (monto > 0)
);

CREATE INDEX idx_abonos_aplicacion_abono ON abonos_credito_aplicacion(abono_id);
CREATE INDEX idx_abonos_aplicacion_cuenta ON abonos_credito_aplicacion(cuenta_id);

CREATE SEQUENCE seq_numero_abono START 1;

-- Permiso: consultar cuentas por cobrar y registrar abonos
INSERT INTO permisos (nombre, modulo, accion, estandar, descripcion, created_by, updated_by) VALUES
    ('CREDITO_GESTIONAR', 'CREDITO', 'GESTIONAR', FALSE, 'Consultar el estado de cuenta de clientes a credito y registrar abonos', 'SISTEMA', 'SISTEMA');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permisos p
WHERE r.nombre IN ('ADMIN', 'CAJERO') AND p.nombre = 'CREDITO_GESTIONAR';
