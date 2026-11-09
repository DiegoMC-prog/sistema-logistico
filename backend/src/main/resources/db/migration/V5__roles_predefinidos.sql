-- RF-02: roles predefinidos. ADMIN ya existe (V1) con todos los permisos.
INSERT INTO roles (nombre, created_by, updated_by) VALUES
    ('CAJERO', 'SISTEMA', 'SISTEMA'),
    ('REPOSITOR', 'SISTEMA', 'SISTEMA'),
    ('ENCARGADO_COMPRAS', 'SISTEMA', 'SISTEMA');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p ON p.nombre IN
    ('CAJA_ABRIR', 'CAJA_CERRAR', 'CAJA_MOVIMIENTO', 'VENTA_VER', 'VENTA_CREAR', 'VENTA_DEVOLVER')
WHERE r.nombre = 'CAJERO';

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p ON p.nombre IN
    ('INVENTARIO_VER', 'INVENTARIO_MOVER', 'PEDIDO_VER', 'PEDIDO_RECIBIR')
WHERE r.nombre = 'REPOSITOR';

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r JOIN permisos p ON p.nombre IN
    ('PROVEEDOR_VER', 'PROVEEDOR_CREAR', 'PROVEEDOR_EDITAR', 'PROVEEDOR_ELIMINAR', 'PROVEEDOR_ACTIVAR',
     'PEDIDO_VER', 'PEDIDO_CREAR', 'PEDIDO_EDITAR', 'PEDIDO_ELIMINAR', 'PEDIDO_ENVIAR', 'PEDIDO_CANCELAR',
     'INVENTARIO_VER', 'REPORTE_COMPRAS')
WHERE r.nombre = 'ENCARGADO_COMPRAS';
