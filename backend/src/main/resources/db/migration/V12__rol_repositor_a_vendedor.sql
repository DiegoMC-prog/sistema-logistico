-- El personal de este rol son los vendedores de la tienda (atienden y reciben la mercaderia): REPOSITOR pasa a
-- llamarse VENDEDOR. Los permisos no cambian. Incluye las variantes por zona de la semilla de desarrollo.
UPDATE roles
SET nombre = 'VENDEDOR' || substr(nombre, length('REPOSITOR') + 1),
    updated_by = 'SISTEMA',
    updated_at = CURRENT_TIMESTAMP
WHERE nombre = 'REPOSITOR' OR nombre LIKE 'REPOSITOR %';
