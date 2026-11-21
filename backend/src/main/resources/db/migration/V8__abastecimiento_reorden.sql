-- RF-34: sugerencias prescriptivas de reorden con EOQ adaptativo, stock de seguridad, punto de reorden y escenarios.

-- Parametros de costo y servicio por producto. Una columna NULL usa el valor global (app.reorden.* en properties).
CREATE TABLE parametros_reorden(
    producto_id BIGINT PRIMARY KEY REFERENCES productos(id),
    -- S: costo de emitir un pedido (moneda por pedido)
    costo_pedido DECIMAL(14, 4),
    -- Costo anual de mantener una unidad, como fraccion del costo de compra (0.25 = 25 % anual)
    costo_almacen_pct DECIMAL(7, 4),
    -- Probabilidad de no quedarse sin stock durante el lead time (0.50 a 0.999)
    nivel_servicio DECIMAL(5, 4),
    -- Proveedor a usar; NULL = el del ultimo pedido del producto
    proveedor_id BIGINT REFERENCES proveedores(id),
    -- FALSE = el producto no se evalua (p. ej. se compra por encargo)
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    updated_by VARCHAR(150) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_parametros_reorden_costo CHECK (costo_pedido IS NULL OR costo_pedido >= 0),
    CONSTRAINT ck_parametros_reorden_almacen CHECK (costo_almacen_pct IS NULL OR costo_almacen_pct > 0),
    CONSTRAINT ck_parametros_reorden_servicio CHECK (nivel_servicio IS NULL OR nivel_servicio BETWEEN 0.5 AND 0.999)
);

-- Cada recalculo (nocturno o manual) queda registrado; las sugerencias vigentes son las del ultimo
CREATE TABLE calculos_reorden(
    id BIGSERIAL PRIMARY KEY,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    origen VARCHAR(20) NOT NULL,
    ejecutado_por VARCHAR(150) NOT NULL,
    dias_ventana INT NOT NULL,
    historial_minimo_dias INT NOT NULL,
    productos_evaluados INT NOT NULL DEFAULT 0,
    productos_a_reordenar INT NOT NULL DEFAULT 0,

    CONSTRAINT ck_calculos_reorden_origen CHECK (origen IN ('PROGRAMADO', 'MANUAL'))
);

CREATE TABLE sugerencias_reorden(
    id BIGSERIAL PRIMARY KEY,
    calculo_id BIGINT NOT NULL REFERENCES calculos_reorden(id),
    producto_id BIGINT NOT NULL REFERENCES productos(id),
    proveedor_id BIGINT REFERENCES proveedores(id),
    -- REORDENAR, SIN_NECESIDAD, DATOS_INSUFICIENTES, SIN_DEMANDA o SIN_PROVEEDOR
    estado VARCHAR(30) NOT NULL,
    -- Que hizo el encargado de compras: PENDIENTE, PEDIDO_CREADO o DESCARTADA
    accion VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    pedido_id BIGINT REFERENCES pedidos_proveedor(id),
    accion_por VARCHAR(150),
    accion_at TIMESTAMP,

    -- Datos de entrada del calculo (unidades base y dias)
    dias_historial INT NOT NULL DEFAULT 0,
    demanda_diaria DECIMAL(14, 4) NOT NULL DEFAULT 0,
    desviacion_demanda DECIMAL(14, 4) NOT NULL DEFAULT 0,
    lead_time_dias DECIMAL(10, 2) NOT NULL DEFAULT 0,
    desviacion_lead_time DECIMAL(10, 2) NOT NULL DEFAULT 0,
    muestras_lead_time INT NOT NULL DEFAULT 0,
    costo_unitario DECIMAL(14, 4) NOT NULL DEFAULT 0,
    costo_pedido DECIMAL(14, 4) NOT NULL DEFAULT 0,
    costo_almacen_anual DECIMAL(14, 4) NOT NULL DEFAULT 0,
    nivel_servicio DECIMAL(5, 4) NOT NULL DEFAULT 0,
    factor_z DECIMAL(6, 3) NOT NULL DEFAULT 0,
    stock_actual DECIMAL(14, 4) NOT NULL DEFAULT 0,
    en_transito DECIMAL(14, 4) NOT NULL DEFAULT 0,
    vida_util_dias DECIMAL(10, 2),

    -- Resultados por escenario: pesimista (mas demanda y mas demora), estandar y optimista
    eoq_pesimista DECIMAL(14, 4) NOT NULL DEFAULT 0,
    stock_seguridad_pesimista DECIMAL(14, 4) NOT NULL DEFAULT 0,
    punto_reorden_pesimista DECIMAL(14, 4) NOT NULL DEFAULT 0,
    cantidad_pesimista DECIMAL(14, 4) NOT NULL DEFAULT 0,
    eoq_estandar DECIMAL(14, 4) NOT NULL DEFAULT 0,
    stock_seguridad_estandar DECIMAL(14, 4) NOT NULL DEFAULT 0,
    punto_reorden_estandar DECIMAL(14, 4) NOT NULL DEFAULT 0,
    cantidad_estandar DECIMAL(14, 4) NOT NULL DEFAULT 0,
    eoq_optimista DECIMAL(14, 4) NOT NULL DEFAULT 0,
    stock_seguridad_optimista DECIMAL(14, 4) NOT NULL DEFAULT 0,
    punto_reorden_optimista DECIMAL(14, 4) NOT NULL DEFAULT 0,
    cantidad_optimista DECIMAL(14, 4) NOT NULL DEFAULT 0,

    motivo VARCHAR(1000) NOT NULL,

    CONSTRAINT uk_sugerencias_reorden_calculo_producto UNIQUE (calculo_id, producto_id),
    CONSTRAINT ck_sugerencias_reorden_estado CHECK (estado IN
        ('REORDENAR', 'SIN_NECESIDAD', 'DATOS_INSUFICIENTES', 'SIN_DEMANDA', 'SIN_PROVEEDOR')),
    CONSTRAINT ck_sugerencias_reorden_accion CHECK (accion IN ('PENDIENTE', 'PEDIDO_CREADO', 'DESCARTADA'))
);

CREATE INDEX idx_sugerencias_reorden_calculo ON sugerencias_reorden(calculo_id, estado);
CREATE INDEX idx_sugerencias_reorden_producto ON sugerencias_reorden(producto_id);

INSERT INTO permisos (nombre, modulo, accion, estandar, descripcion, created_by, updated_by) VALUES
    ('REORDEN_VER', 'REORDEN', 'VER', FALSE, 'Ver las sugerencias de reorden (EOQ) y sus parametros', 'SISTEMA', 'SISTEMA'),
    ('REORDEN_GESTIONAR', 'REORDEN', 'GESTIONAR', FALSE, 'Recalcular sugerencias de reorden, editar sus parametros y convertirlas en pedidos', 'SISTEMA', 'SISTEMA');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permisos p
WHERE r.nombre IN ('ADMIN', 'ENCARGADO_COMPRAS') AND p.nombre IN ('REORDEN_VER', 'REORDEN_GESTIONAR');
