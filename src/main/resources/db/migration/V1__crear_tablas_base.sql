CREATE TABLE usuarios(
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(150) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    estado VARCHAR(50) NOT NULL,
    -- true mientras la contrasena sea la temporal generada por el sistema (debe cambiarse al ingresar)
    password_temporal BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(150) NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL
);

CREATE UNIQUE INDEX idx_usuarios_username_unique ON usuarios(username) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX idx_usuarios_email_unique ON usuarios(lower(email)) WHERE deleted_at IS NULL;

CREATE TABLE roles(
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL
);

CREATE UNIQUE INDEX idx_roles_nombre_unique ON roles(nombre) WHERE deleted_at IS NULL;

CREATE TABLE usuario_rol(
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    rol_id BIGINT NOT NULL REFERENCES roles(id),
    assigned_by VARCHAR(150) NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_usuario_rol UNIQUE (usuario_id, rol_id)
);

CREATE INDEX idx_usuario_rol_rol_id ON usuario_rol(rol_id);

CREATE TABLE permisos(
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    created_by VARCHAR(150) NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL
);

CREATE UNIQUE INDEX idx_permisos_nombre_unique ON permisos(nombre) WHERE deleted_at IS NULL;

CREATE TABLE rol_permiso(
    id BIGSERIAL PRIMARY KEY,
    rol_id BIGINT NOT NULL REFERENCES roles(id),
    permiso_id BIGINT NOT NULL REFERENCES permisos(id),

    CONSTRAINT uq_rol_permiso UNIQUE (rol_id, permiso_id)
);

CREATE INDEX idx_rol_permiso_permiso_id ON rol_permiso(permiso_id);

CREATE TABLE categorias (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL
);

CREATE UNIQUE INDEX uk_categorias_nombre_activo ON categorias (nombre) WHERE deleted_at IS NULL;

CREATE TABLE productos(
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    categoria_id BIGINT NOT NULL REFERENCES categorias(id),
    stock_minimo DECIMAL(14, 4) NOT NULL DEFAULT 0.0000,
    es_perecedero BOOLEAN NOT NULL DEFAULT TRUE,
    estado VARCHAR(50) NOT NULL DEFAULT 'ACTIVO',
    created_by VARCHAR(150) NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL
);

CREATE UNIQUE INDEX uk_productos_nombre_activo ON productos (nombre) WHERE deleted_at IS NULL;
CREATE INDEX idx_productos_categoria_id ON productos(categoria_id);

CREATE TABLE unidades_medida(
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    abreviatura VARCHAR(10) NOT NULL,
    created_by VARCHAR(150) NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL
);

CREATE UNIQUE INDEX uk_unidades_medida_nombre ON unidades_medida(nombre) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uk_unidades_medida_abreviatura ON unidades_medida(abreviatura) WHERE deleted_at IS NULL;

CREATE TABLE presentaciones_producto(
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL REFERENCES productos(id),
    unidad_medida_id BIGINT NOT NULL REFERENCES unidades_medida(id),
    codigo_barras VARCHAR(64) NOT NULL,
    factor_conversion DECIMAL(12, 4) NOT NULL,
    es_unidad_base BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(150) NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL,

    CONSTRAINT ck_presentaciones_factor CHECK (factor_conversion > 0),
    -- La unidad base siempre equivale a 1 unidad base de si misma
    CONSTRAINT ck_presentaciones_base_factor CHECK (NOT es_unidad_base OR factor_conversion = 1)
);

CREATE UNIQUE INDEX uk_presentaciones_codigo_barras ON presentaciones_producto(codigo_barras) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uk_presentaciones_unidad_base ON presentaciones_producto(producto_id) WHERE es_unidad_base AND deleted_at IS NULL;
CREATE INDEX idx_presentaciones_producto_id ON presentaciones_producto(producto_id);
CREATE INDEX idx_presentaciones_unidad_medida_id ON presentaciones_producto(unidad_medida_id);

CREATE TABLE lotes(
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL REFERENCES productos(id),
    numero_lote VARCHAR(80) NOT NULL,
    fecha_vencimiento DATE,
    costo_unitario_compra DECIMAL(14, 4) NOT NULL,
    porcentaje_descuento_vencimiento DECIMAL(14, 4) NOT NULL DEFAULT 0,
    created_by VARCHAR(150) NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL,

    CONSTRAINT ck_lotes_costo CHECK (costo_unitario_compra >= 0),
    CONSTRAINT ck_lotes_descuento CHECK (porcentaje_descuento_vencimiento BETWEEN 0 AND 100)
);

CREATE UNIQUE INDEX uk_lotes_producto_numero_lote ON lotes(producto_id, numero_lote) WHERE deleted_at IS NULL;
CREATE INDEX idx_lotes_fecha_vencimiento ON lotes(fecha_vencimiento);

-- Numeracion automatica de lotes (LOT-yyyyMMdd-NNNNNN) cuando no se informa el numero
CREATE SEQUENCE seq_numero_lote;

CREATE TABLE depositos(
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    estado VARCHAR(50) NOT NULL DEFAULT 'ACTIVO',
    created_by VARCHAR(150) NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL
);

CREATE UNIQUE INDEX uk_depositos_nombre_activo ON depositos (nombre) WHERE deleted_at IS NULL;

CREATE TABLE inventarios(
    id BIGSERIAL PRIMARY KEY,
    deposito_id BIGINT NOT NULL REFERENCES depositos(id),
    lote_id BIGINT NOT NULL REFERENCES lotes(id),
    stock_actual DECIMAL(14, 4) NOT NULL DEFAULT 0.0000,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(150) NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL,

    CONSTRAINT ck_inventarios_stock_actual CHECK (stock_actual >= 0)
);

-- Una fila por (deposito, lote); el producto se obtiene a traves del lote
CREATE UNIQUE INDEX uk_inventarios_deposito_lote ON inventarios(deposito_id, lote_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_inventarios_lote_id ON inventarios(lote_id);

CREATE TABLE movimientos_inventarios(
    id BIGSERIAL PRIMARY KEY,
    tipo_movimiento VARCHAR(150) NOT NULL,
    deposito_origen_id BIGINT REFERENCES depositos(id),
    deposito_destino_id BIGINT REFERENCES depositos(id),
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    fecha_movimiento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacion TEXT,
    -- Origen del movimiento (VENTA, DEVOLUCION_VENTA, RECEPCION_PEDIDO, ...)
    referencia_tipo VARCHAR(50),
    referencia_id BIGINT
);

CREATE INDEX idx_movimientos_referencia ON movimientos_inventarios(referencia_tipo, referencia_id);
CREATE INDEX idx_movimientos_usuario_id ON movimientos_inventarios(usuario_id);
CREATE INDEX idx_movimientos_deposito_origen_id ON movimientos_inventarios(deposito_origen_id);
CREATE INDEX idx_movimientos_deposito_destino_id ON movimientos_inventarios(deposito_destino_id);

CREATE TABLE movimiento_inventario_detalle(
    id BIGSERIAL PRIMARY KEY,
    movimiento_id BIGINT NOT NULL REFERENCES movimientos_inventarios(id),
    producto_id BIGINT NOT NULL REFERENCES productos(id),
    lote_id BIGINT REFERENCES lotes(id),
    cantidad DECIMAL(14, 4) NOT NULL,

    CONSTRAINT ck_movimiento_detalle_cantidad CHECK (cantidad > 0)
);

CREATE INDEX idx_movimiento_detalle_movimiento_id ON movimiento_inventario_detalle(movimiento_id);
CREATE INDEX idx_movimiento_detalle_producto_id ON movimiento_inventario_detalle(producto_id);

CREATE TABLE proveedores(
    id BIGSERIAL PRIMARY KEY,
    nombre_empresa VARCHAR(100) NOT NULL,
    nombre_contacto VARCHAR(150) NOT NULL,
    telefono VARCHAR(30),
    email VARCHAR(100),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_by VARCHAR(150) NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL
);

CREATE UNIQUE INDEX uk_proveedores_nombre_empresa ON proveedores(nombre_empresa) WHERE deleted_at IS NULL;

-- ============================================================
-- Pedidos al proveedor
-- ============================================================

CREATE SEQUENCE seq_numero_pedido_proveedor START WITH 1 INCREMENT BY 1;

CREATE TABLE pedidos_proveedor(
    id BIGSERIAL PRIMARY KEY,
    numero_pedido VARCHAR(30) NOT NULL,
    proveedor_id BIGINT NOT NULL REFERENCES proveedores(id),
    deposito_destino_id BIGINT NOT NULL REFERENCES depositos(id),
    estado VARCHAR(50) NOT NULL,
    fecha_pedido DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_entrega_estimada DATE,
    observacion TEXT,
    total DECIMAL(14, 4) NOT NULL DEFAULT 0.0000,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(150) NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL,

    CONSTRAINT ck_pedidos_proveedor_estado CHECK (estado IN ('BORRADOR', 'ENVIADO', 'RECIBIDO_PARCIAL', 'RECIBIDO', 'CANCELADO')),
    CONSTRAINT ck_pedidos_proveedor_total CHECK (total >= 0)
);

CREATE UNIQUE INDEX uk_pedidos_proveedor_numero ON pedidos_proveedor(numero_pedido);
CREATE INDEX idx_pedidos_proveedor_proveedor_estado ON pedidos_proveedor(proveedor_id, estado);
CREATE INDEX idx_pedidos_proveedor_deposito_id ON pedidos_proveedor(deposito_destino_id);
CREATE INDEX idx_pedidos_proveedor_fecha_pedido ON pedidos_proveedor(fecha_pedido);

CREATE TABLE pedido_proveedor_detalle(
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL REFERENCES pedidos_proveedor(id),
    producto_id BIGINT NOT NULL REFERENCES productos(id),
    cantidad_pedida DECIMAL(14, 4) NOT NULL,
    cantidad_recibida DECIMAL(14, 4) NOT NULL DEFAULT 0.0000,
    costo_unitario DECIMAL(14, 4) NOT NULL,

    CONSTRAINT uq_pedido_detalle_producto UNIQUE (pedido_id, producto_id),
    CONSTRAINT ck_pedido_detalle_cantidad_pedida CHECK (cantidad_pedida > 0),
    CONSTRAINT ck_pedido_detalle_cantidad_recibida CHECK (cantidad_recibida >= 0 AND cantidad_recibida <= cantidad_pedida),
    CONSTRAINT ck_pedido_detalle_costo CHECK (costo_unitario >= 0)
);

CREATE INDEX idx_pedido_detalle_producto_id ON pedido_proveedor_detalle(producto_id);

CREATE TABLE recepciones_pedido(
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL REFERENCES pedidos_proveedor(id),
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    fecha_recepcion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    numero_guia VARCHAR(80),
    observacion TEXT
);

CREATE INDEX idx_recepciones_pedido_pedido_id ON recepciones_pedido(pedido_id);
CREATE INDEX idx_recepciones_pedido_usuario_id ON recepciones_pedido(usuario_id);

CREATE TABLE recepcion_pedido_detalle(
    id BIGSERIAL PRIMARY KEY,
    recepcion_id BIGINT NOT NULL REFERENCES recepciones_pedido(id),
    pedido_detalle_id BIGINT NOT NULL REFERENCES pedido_proveedor_detalle(id),
    lote_id BIGINT NOT NULL REFERENCES lotes(id),
    cantidad DECIMAL(14, 4) NOT NULL,

    CONSTRAINT ck_recepcion_detalle_cantidad CHECK (cantidad > 0)
);

CREATE INDEX idx_recepcion_detalle_recepcion_id ON recepcion_pedido_detalle(recepcion_id);
CREATE INDEX idx_recepcion_detalle_pedido_detalle_id ON recepcion_pedido_detalle(pedido_detalle_id);
CREATE INDEX idx_recepcion_detalle_lote_id ON recepcion_pedido_detalle(lote_id);

-- ============================================================
-- POS y ventas (nota de venta)
-- ============================================================

-- Clientes y precios ----------------------------------------

CREATE TABLE tipos_cliente(
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_by VARCHAR(150) NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL
);

CREATE UNIQUE INDEX uk_tipos_cliente_nombre ON tipos_cliente(nombre) WHERE deleted_at IS NULL;

CREATE TABLE clientes(
    id BIGSERIAL PRIMARY KEY,
    tipo_cliente_id BIGINT NOT NULL REFERENCES tipos_cliente(id),
    documento VARCHAR(30),
    nombre VARCHAR(150) NOT NULL,
    telefono VARCHAR(30),
    email VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_by VARCHAR(150) NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL
);

CREATE UNIQUE INDEX uk_clientes_documento ON clientes(documento) WHERE deleted_at IS NULL AND documento IS NOT NULL;
CREATE INDEX idx_clientes_tipo_cliente_id ON clientes(tipo_cliente_id);

-- Precio por presentacion, por tipo de cliente (NULL = precio general) y por cantidad minima
CREATE TABLE precios_presentacion(
    id BIGSERIAL PRIMARY KEY,
    presentacion_id BIGINT NOT NULL REFERENCES presentaciones_producto(id),
    tipo_cliente_id BIGINT REFERENCES tipos_cliente(id),
    cantidad_minima DECIMAL(14, 4) NOT NULL DEFAULT 1.0000,
    precio DECIMAL(14, 4) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_by VARCHAR(150) NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL,

    CONSTRAINT ck_precios_cantidad_minima CHECK (cantidad_minima > 0),
    CONSTRAINT ck_precios_precio CHECK (precio >= 0)
);

CREATE UNIQUE INDEX uk_precios_presentacion_tipo_cantidad
    ON precios_presentacion(presentacion_id, COALESCE(tipo_cliente_id, 0), cantidad_minima)
    WHERE deleted_at IS NULL;
CREATE INDEX idx_precios_tipo_cliente_id ON precios_presentacion(tipo_cliente_id);

-- Caja ------------------------------------------------------

CREATE TABLE cajas(
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    deposito_id BIGINT NOT NULL REFERENCES depositos(id),
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    created_by VARCHAR(150) NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL
);

CREATE UNIQUE INDEX uk_cajas_nombre ON cajas(nombre) WHERE deleted_at IS NULL;
CREATE INDEX idx_cajas_deposito_id ON cajas(deposito_id);

CREATE TABLE turnos_caja(
    id BIGSERIAL PRIMARY KEY,
    caja_id BIGINT NOT NULL REFERENCES cajas(id),
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    fecha_apertura TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    monto_apertura DECIMAL(14, 4) NOT NULL DEFAULT 0.0000,
    fecha_cierre TIMESTAMP,
    monto_cierre_declarado DECIMAL(14, 4),
    monto_cierre_esperado DECIMAL(14, 4),
    estado VARCHAR(50) NOT NULL,

    CONSTRAINT ck_turnos_caja_estado CHECK (estado IN ('ABIERTO', 'CERRADO')),
    CONSTRAINT ck_turnos_caja_monto_apertura CHECK (monto_apertura >= 0)
);

-- Un solo turno abierto por caja
CREATE UNIQUE INDEX uk_turnos_caja_abierto ON turnos_caja(caja_id) WHERE estado = 'ABIERTO';
CREATE INDEX idx_turnos_caja_usuario_id ON turnos_caja(usuario_id);

CREATE TABLE movimientos_caja(
    id BIGSERIAL PRIMARY KEY,
    turno_id BIGINT NOT NULL REFERENCES turnos_caja(id),
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    tipo VARCHAR(20) NOT NULL,
    monto DECIMAL(14, 4) NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_movimientos_caja_tipo CHECK (tipo IN ('INGRESO', 'EGRESO')),
    CONSTRAINT ck_movimientos_caja_monto CHECK (monto > 0)
);

CREATE INDEX idx_movimientos_caja_turno_id ON movimientos_caja(turno_id);
CREATE INDEX idx_movimientos_caja_usuario_id ON movimientos_caja(usuario_id);

-- Pagos -----------------------------------------------------

CREATE TABLE metodos_pago(
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_by VARCHAR(150) NOT NULL,
    updated_by VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL
);

CREATE UNIQUE INDEX uk_metodos_pago_nombre ON metodos_pago(nombre) WHERE deleted_at IS NULL;

-- Ventas (nota de venta; no se borran, se anulan) ------------

CREATE SEQUENCE seq_numero_venta START WITH 1 INCREMENT BY 1;

CREATE TABLE ventas(
    id BIGSERIAL PRIMARY KEY,
    numero_venta VARCHAR(30) NOT NULL,
    turno_id BIGINT NOT NULL REFERENCES turnos_caja(id),
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    cliente_id BIGINT REFERENCES clientes(id),
    tipo_cliente_id BIGINT REFERENCES tipos_cliente(id),
    deposito_id BIGINT NOT NULL REFERENCES depositos(id),
    estado VARCHAR(50) NOT NULL,
    subtotal DECIMAL(14, 4) NOT NULL DEFAULT 0.0000,
    descuento_total DECIMAL(14, 4) NOT NULL DEFAULT 0.0000,
    total DECIMAL(14, 4) NOT NULL DEFAULT 0.0000,
    fecha_venta TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    anulada_por VARCHAR(150),
    fecha_anulacion TIMESTAMP,
    motivo_anulacion VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT ck_ventas_estado CHECK (estado IN ('COMPLETADA', 'ANULADA')),
    CONSTRAINT ck_ventas_montos CHECK (subtotal >= 0 AND descuento_total >= 0 AND total >= 0)
);

CREATE UNIQUE INDEX uk_ventas_numero ON ventas(numero_venta);
CREATE INDEX idx_ventas_fecha_venta ON ventas(fecha_venta);
CREATE INDEX idx_ventas_turno_id ON ventas(turno_id);
CREATE INDEX idx_ventas_cliente_id ON ventas(cliente_id);
CREATE INDEX idx_ventas_usuario_id ON ventas(usuario_id);
CREATE INDEX idx_ventas_deposito_id ON ventas(deposito_id);

CREATE TABLE venta_detalle(
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT NOT NULL REFERENCES ventas(id),
    producto_id BIGINT NOT NULL REFERENCES productos(id),
    presentacion_id BIGINT NOT NULL REFERENCES presentaciones_producto(id),
    cantidad DECIMAL(14, 4) NOT NULL,
    factor_conversion DECIMAL(12, 4) NOT NULL,
    precio_unitario DECIMAL(14, 4) NOT NULL,
    descuento DECIMAL(14, 4) NOT NULL DEFAULT 0.0000,
    subtotal DECIMAL(14, 4) NOT NULL,
    es_precio_modifcado BOOLEAN NOT NULL DEFAULT FALSE,
    motivo_precio VARCHAR(150)

    CONSTRAINT ck_venta_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_venta_detalle_montos CHECK (precio_unitario >= 0 AND descuento >= 0 AND subtotal >= 0)
);

CREATE INDEX idx_venta_detalle_venta_id ON venta_detalle(venta_id);
CREATE INDEX idx_venta_detalle_producto_id ON venta_detalle(producto_id);
CREATE INDEX idx_venta_detalle_presentacion_id ON venta_detalle(presentacion_id);

-- Lotes de los que salio cada linea (cantidad en unidad base).
-- El lote lo asigna el sistema por FEFO (vencimiento mas proximo primero, NULLS LAST, excluyendo vencidos);
-- el codigo de barras solo identifica la presentacion. Si FEFO cruza lotes con distinto
-- porcentaje_descuento_vencimiento, el servicio divide la venta en lineas de venta_detalle separadas (una por lote),
-- de modo que cada linea mantiene un unico precio_unitario.
CREATE TABLE venta_detalle_lote(
    id BIGSERIAL PRIMARY KEY,
    venta_detalle_id BIGINT NOT NULL REFERENCES venta_detalle(id),
    lote_id BIGINT NOT NULL REFERENCES lotes(id),
    cantidad DECIMAL(14, 4) NOT NULL,
    -- Costo del lote al momento de la venta (snapshot) para calcular el margen real
    costo_unitario DECIMAL(14, 4) NOT NULL DEFAULT 0.0000,
    seleccion_manual BOOLEAN NOT NULL DEFAULT FALSE,
    usuario_autorizado_id BIGINT REFERENCES usuarios(id),

    CONSTRAINT ck_venta_detalle_lote_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_venta_detalle_lote_costo CHECK (costo_unitario >= 0)
);

CREATE INDEX idx_venta_detalle_lote_detalle_id ON venta_detalle_lote(venta_detalle_id);
CREATE INDEX idx_venta_detalle_lote_lote_id ON venta_detalle_lote(lote_id);

CREATE TABLE venta_pagos(
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT NOT NULL REFERENCES ventas(id),
    metodo_pago_id BIGINT NOT NULL REFERENCES metodos_pago(id),
    monto DECIMAL(14, 4) NOT NULL,
    referencia VARCHAR(100),

    CONSTRAINT ck_venta_pagos_monto CHECK (monto > 0)
);

CREATE INDEX idx_venta_pagos_venta_id ON venta_pagos(venta_id);
CREATE INDEX idx_venta_pagos_metodo_pago_id ON venta_pagos(metodo_pago_id);

-- Devoluciones ----------------------------------------------

CREATE TABLE devoluciones_venta(
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT NOT NULL REFERENCES ventas(id),
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo VARCHAR(255) NOT NULL,
    monto_reembolsado DECIMAL(14, 4) NOT NULL DEFAULT 0.0000,
    metodo_pago_id BIGINT REFERENCES metodos_pago(id),

    CONSTRAINT ck_devoluciones_monto CHECK (monto_reembolsado >= 0),
    -- Si hay reembolso, debe indicarse el metodo
    CONSTRAINT ck_devoluciones_metodo CHECK (monto_reembolsado = 0 OR metodo_pago_id IS NOT NULL)
);

CREATE INDEX idx_devoluciones_venta_metodo_pago_id ON devoluciones_venta(metodo_pago_id);
CREATE INDEX idx_devoluciones_venta_venta_id ON devoluciones_venta(venta_id);
CREATE INDEX idx_devoluciones_venta_usuario_id ON devoluciones_venta(usuario_id);

CREATE TABLE devolucion_venta_detalle(
    id BIGSERIAL PRIMARY KEY,
    devolucion_id BIGINT NOT NULL REFERENCES devoluciones_venta(id),
    venta_detalle_id BIGINT NOT NULL REFERENCES venta_detalle(id),
    lote_id BIGINT NOT NULL REFERENCES lotes(id),
    cantidad DECIMAL(14, 4) NOT NULL,
    -- FALSE si el producto devuelto esta danado/vencido y no debe volver al stock
    reingresa_stock BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT ck_devolucion_detalle_cantidad CHECK (cantidad > 0)
);

CREATE INDEX idx_devolucion_detalle_devolucion_id ON devolucion_venta_detalle(devolucion_id);
CREATE INDEX idx_devolucion_detalle_venta_detalle_id ON devolucion_venta_detalle(venta_detalle_id);
CREATE INDEX idx_devolucion_detalle_lote_id ON devolucion_venta_detalle(lote_id);

-- Datos semilla ---------------------------------------------

INSERT INTO tipos_cliente (nombre, created_by, updated_by) VALUES
    ('GENERAL', 'SISTEMA', 'SISTEMA'),
    ('FRECUENTE', 'SISTEMA', 'SISTEMA'),
    ('MAYORISTA', 'SISTEMA', 'SISTEMA');

INSERT INTO metodos_pago (nombre, created_by, updated_by) VALUES
    ('EFECTIVO', 'SISTEMA', 'SISTEMA'),
    ('TARJETA', 'SISTEMA', 'SISTEMA'),
    ('TRANSFERENCIA', 'SISTEMA', 'SISTEMA');

INSERT INTO permisos (nombre, descripcion, created_by, updated_by) VALUES
    ('CAJA_ABRIR', 'Abrir turno de caja', 'SISTEMA', 'SISTEMA'),
    ('CAJA_CERRAR', 'Cerrar turno de caja', 'SISTEMA', 'SISTEMA'),
    ('CAJA_MOVIMIENTO', 'Registrar ingresos y egresos de caja', 'SISTEMA', 'SISTEMA'),
    ('VENTA_CREAR', 'Registrar ventas', 'SISTEMA', 'SISTEMA'),
    ('VENTA_ANULAR', 'Anular ventas', 'SISTEMA', 'SISTEMA'),
    ('VENTA_DEVOLVER', 'Registrar devoluciones de venta', 'SISTEMA', 'SISTEMA'),
    ('VENTA_VER', 'Consultar ventas y reportes', 'SISTEMA', 'SISTEMA'),
    ('CLIENTE_GESTIONAR', 'Gestionar clientes y tipos de cliente', 'SISTEMA', 'SISTEMA'),
    ('PRECIO_GESTIONAR', 'Gestionar precios de venta', 'SISTEMA', 'SISTEMA'),
    ('USUARIO_GESTIONAR', 'Gestionar usuarios y sus roles', 'SISTEMA', 'SISTEMA'),
    ('ROL_GESTIONAR', 'Gestionar roles y sus permisos', 'SISTEMA', 'SISTEMA'),
    ('CATALOGO_GESTIONAR', 'Gestionar categorias, productos, unidades de medida, presentaciones y lotes', 'SISTEMA', 'SISTEMA'),
    ('DEPOSITO_GESTIONAR', 'Gestionar depositos', 'SISTEMA', 'SISTEMA'),
    ('INVENTARIO_VER', 'Consultar inventario y movimientos', 'SISTEMA', 'SISTEMA'),
    ('INVENTARIO_MOVER', 'Registrar entradas, salidas, traslados y ajustes de inventario', 'SISTEMA', 'SISTEMA'),
    ('PROVEEDOR_GESTIONAR', 'Gestionar proveedores', 'SISTEMA', 'SISTEMA'),
    ('PEDIDO_GESTIONAR', 'Gestionar pedidos a proveedores y sus recepciones', 'SISTEMA', 'SISTEMA');

-- Rol de sistema ADMIN con todos los permisos
INSERT INTO roles (nombre, created_by, updated_by) VALUES ('ADMIN', 'SISTEMA', 'SISTEMA');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permisos p WHERE r.nombre = 'ADMIN';

-- Usuario administrador inicial (contrasena inicial: Admin#2026, marcada como temporal: debe cambiarse al ingresar;
-- mientras tanto se puede generar una nueva por correo con POST /api/v1/usuarios/{id}/restablecer-password)
INSERT INTO usuarios (username, nombre, email, password, password_temporal, estado, created_by, updated_by) VALUES
    ('admin', 'Administrador', 'admin@tienda.local', '$2a$10$T8UudTC9A/EMPgdnXHRO9eJLeTFuC9hrdqaRGLymJGUCdb.1B8fJ6', TRUE, 'ACTIVO', 'SISTEMA', 'SISTEMA');

INSERT INTO usuario_rol (usuario_id, rol_id, assigned_by)
SELECT u.id, r.id, 'SISTEMA' FROM usuarios u CROSS JOIN roles r WHERE u.username = 'admin' AND r.nombre = 'ADMIN';
