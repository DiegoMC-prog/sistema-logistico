-- RF-35 (2/2): recargos por escasez activados manualmente por el administrador.

-- ---------------------------------------------------------------- recargos por escasez
-- Un recargo apunta a un producto o a una categoria (nunca a ambos). El del producto gana sobre el de su categoria.
-- Nunca se borran: al desactivarlos queda quien y cuando, asi el historial sirve de auditoria.
CREATE TABLE recargos_escasez (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT REFERENCES productos(id),
    categoria_id BIGINT REFERENCES categorias(id),
    -- Fraccion sobre el precio resuelto: 0.15 = +15 %
    porcentaje DECIMAL(6, 4) NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    activado_por VARCHAR(150) NOT NULL,
    activado_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    desactivado_por VARCHAR(150),
    desactivado_at TIMESTAMP,

    CONSTRAINT ck_recargos_escasez_destino CHECK ((producto_id IS NULL) <> (categoria_id IS NULL)),
    CONSTRAINT ck_recargos_escasez_porcentaje CHECK (porcentaje > 0 AND porcentaje <= 1),
    CONSTRAINT ck_recargos_escasez_desactivado CHECK (activo OR desactivado_at IS NOT NULL)
);

-- Como mucho un recargo activo por producto y uno por categoria
CREATE UNIQUE INDEX uk_recargos_escasez_producto_activo ON recargos_escasez(producto_id)
    WHERE activo AND producto_id IS NOT NULL;
CREATE UNIQUE INDEX uk_recargos_escasez_categoria_activo ON recargos_escasez(categoria_id)
    WHERE activo AND categoria_id IS NOT NULL;
CREATE INDEX idx_recargos_escasez_fecha ON recargos_escasez(activado_at DESC);

INSERT INTO permisos (nombre, modulo, accion, estandar, descripcion, created_by, updated_by) VALUES
    ('RECARGO_GESTIONAR', 'RECARGO', 'GESTIONAR', FALSE,
     'Activar y desactivar recargos por escasez sobre el precio de venta', 'SISTEMA', 'SISTEMA');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permisos p
WHERE r.nombre = 'ADMIN' AND p.nombre = 'RECARGO_GESTIONAR';
