-- Reportes de utilidad y de compras. Utilidad va aparte de los de ventas porque revela costos y margenes.
INSERT INTO permisos (nombre, modulo, accion, estandar, descripcion, created_by, updated_by) VALUES
    ('REPORTE_UTILIDAD', 'REPORTE', 'UTILIDAD', FALSE, 'Ver los reportes de utilidad (costos y margenes por producto y por periodo, precios manuales) y exportarlos', 'SISTEMA', 'SISTEMA'),
    ('REPORTE_COMPRAS', 'REPORTE', 'COMPRAS', FALSE, 'Ver los reportes de compras (por proveedor, pedidos pendientes y evolucion del costo) y exportarlos', 'SISTEMA', 'SISTEMA');

-- El rol de sistema ADMIN conserva todos los permisos
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permisos p
WHERE r.nombre = 'ADMIN' AND p.nombre IN ('REPORTE_UTILIDAD', 'REPORTE_COMPRAS');
