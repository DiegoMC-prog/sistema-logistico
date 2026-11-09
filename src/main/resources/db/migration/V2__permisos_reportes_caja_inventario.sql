-- Reportes de caja e inventario. Cada familia de reportes tiene su permiso (modulo REPORTE, accion especial).
INSERT INTO permisos (nombre, modulo, accion, estandar, descripcion, created_by, updated_by) VALUES
    ('REPORTE_INVENTARIO', 'REPORTE', 'INVENTARIO', FALSE, 'Ver los reportes de inventario (stock valorizado, stock bajo, lotes por vencer y kardex) y exportarlos', 'SISTEMA', 'SISTEMA'),
    ('REPORTE_CAJA', 'REPORTE', 'CAJA', FALSE, 'Ver los reportes de caja (cierres de turno y diferencias de caja) y exportarlos', 'SISTEMA', 'SISTEMA');

-- El rol de sistema ADMIN conserva todos los permisos
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permisos p
WHERE r.nombre = 'ADMIN' AND p.nombre IN ('REPORTE_INVENTARIO', 'REPORTE_CAJA');
