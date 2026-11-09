-- ============================================================================
-- SEMILLA DE DESARROLLO (1/3): catalogo = categorias, unidades de medida, productos, presentaciones,
-- precios y un lote inicial por producto.
--
-- Migracion repetible de Flyway: solo se carga con el perfil "seed" (ver application-seed.properties).
-- Es idempotente: si la base ya tiene productos, no hace nada. Todo lo sembrado lleva created_by = 'SEED'.
-- ============================================================================
DO $seed$
BEGIN
    IF EXISTS (SELECT 1 FROM productos) THEN
        RAISE NOTICE 'Semilla de catalogo omitida: ya hay productos.';
        RETURN;
    END IF;

    PERFORM setseed(0.37);

    -- ---------------------------------------------------------------- categorias (50)
    INSERT INTO categorias (nombre, created_by, updated_by)
    SELECT n, 'SEED', 'SEED'
    FROM unnest(ARRAY[
        'Abarrotes', 'Lácteos', 'Quesos y Embutidos', 'Carnes Rojas', 'Aves', 'Pescados y Mariscos',
        'Frutas', 'Verduras', 'Panadería', 'Pastelería', 'Bebidas Gaseosas', 'Aguas',
        'Jugos y Néctares', 'Cervezas', 'Vinos y Licores', 'Café e Infusiones', 'Snacks', 'Golosinas',
        'Galletas', 'Cereales', 'Pastas', 'Arroz y Granos', 'Legumbres', 'Aceites y Grasas',
        'Salsas y Condimentos', 'Enlatados', 'Conservas', 'Congelados', 'Helados', 'Huevos',
        'Productos de Limpieza', 'Detergentes', 'Papel y Desechables', 'Higiene Personal', 'Cuidado del Cabello',
        'Cuidado Dental', 'Bebés', 'Mascotas', 'Farmacia Básica', 'Cosméticos', 'Útiles Escolares',
        'Papelería', 'Ferretería Ligera', 'Hogar y Cocina', 'Bazar', 'Electro Menor', 'Jardinería',
        'Repostería', 'Comida Preparada', 'Productos Orgánicos']) AS n;

    -- ---------------------------------------------------------------- unidades de medida (50)
    INSERT INTO unidades_medida (nombre, abreviatura, created_by, updated_by)
    SELECT split_part(x, '|', 1), split_part(x, '|', 2), 'SEED', 'SEED'
    FROM unnest(ARRAY[
        'Unidad|und', 'Kilogramo|kg', 'Gramo|g', 'Libra|lb', 'Onza|oz', 'Litro|l', 'Mililitro|ml', 'Galón|gal',
        'Docena|doc', 'Media docena|mdoc', 'Caja x12|cj12', 'Caja x24|cj24', 'Caja x6|cj6', 'Caja x10|cj10',
        'Paquete x6|pq6', 'Paquete x10|pq10', 'Paquete x12|pq12', 'Paquete x20|pq20', 'Six pack|6pk',
        'Pack x12|12pk', 'Pack x24|24pk', 'Bandeja x6|bd6', 'Bandeja x12|bd12', 'Bandeja x30|bd30',
        'Bolsa 500g|bl500', 'Bolsa 1kg|bl1k', 'Bolsa 2kg|bl2k', 'Saco 25kg|sc25', 'Saco 50kg|sc50',
        'Fardo x12|fd12', 'Fardo x24|fd24', 'Bidón 5l|bd5l', 'Bidón 20l|bd20l', 'Botella 1l|bt1l',
        'Botella 2l|bt2l', 'Botella 500ml|bt500', 'Lata 330ml|lt330', 'Lata 410g|lt410', 'Frasco|fr', 'Tarro|tr',
        'Sachet|sch', 'Barra|br', 'Rollo|rl', 'Tira x10|tr10', 'Display x12|dsp12', 'Display x24|dsp24',
        'Cartón x30|ct30', 'Atado|at', 'Mazo|mz', 'Quintal|qq']) AS x;

    -- ---------------------------------------------------------------- productos de trabajo
    -- (nombre, categoria, unidad base, perecedero, costo, precio, stock minimo, presentacion extra, factor)
    CREATE TEMP TABLE seed_prod (
        nombre text, categoria text, unidad text, perecedero boolean, costo numeric, precio numeric,
        stock_min numeric, extra text, factor numeric) ON COMMIT DROP;

    INSERT INTO seed_prod VALUES
        ('Azúcar Blanca', 'Abarrotes', 'kg', false, 0.85, 1.20, 100, 'sc50', 50),
        ('Sal Yodada 1kg', 'Abarrotes', 'und', false, 0.35, 0.55, 40, 'cj12', 12),
        ('Harina de Trigo 1kg', 'Abarrotes', 'und', false, 0.80, 1.15, 40, 'cj12', 12),
        ('Avena en Hojuelas 500g', 'Cereales', 'und', false, 0.70, 1.05, 30, 'cj12', 12),
        ('Cereal Maíz Tostado 400g', 'Cereales', 'und', false, 1.90, 2.75, 20, 'cj12', 12),
        ('Arroz Superior 1kg', 'Arroz y Granos', 'und', false, 0.90, 1.25, 80, 'fd24', 24),
        ('Arroz Integral 1kg', 'Arroz y Granos', 'und', false, 1.30, 1.85, 25, NULL, NULL),
        ('Fideo Espagueti 400g', 'Pastas', 'und', false, 0.55, 0.85, 60, 'cj24', 24),
        ('Fideo Tornillo 400g', 'Pastas', 'und', false, 0.55, 0.85, 40, 'cj24', 24),
        ('Lenteja 500g', 'Legumbres', 'und', false, 0.95, 1.40, 30, 'cj12', 12),
        ('Frijol Negro 500g', 'Legumbres', 'und', false, 1.00, 1.45, 30, 'cj12', 12),
        ('Garbanzo 500g', 'Legumbres', 'und', false, 1.20, 1.70, 20, NULL, NULL),
        ('Aceite Vegetal 1l', 'Aceites y Grasas', 'und', false, 2.10, 2.95, 40, 'cj12', 12),
        ('Aceite de Oliva 500ml', 'Aceites y Grasas', 'und', false, 5.80, 7.90, 10, 'cj6', 6),
        ('Manteca Vegetal 500g', 'Aceites y Grasas', 'und', false, 1.40, 2.00, 15, NULL, NULL),
        ('Salsa de Tomate 400g', 'Salsas y Condimentos', 'und', false, 0.90, 1.35, 30, 'cj24', 24),
        ('Mayonesa 400g', 'Salsas y Condimentos', 'und', false, 1.50, 2.20, 25, 'cj12', 12),
        ('Mostaza 220g', 'Salsas y Condimentos', 'und', false, 0.80, 1.25, 15, 'cj12', 12),
        ('Salsa de Soya 150ml', 'Salsas y Condimentos', 'und', false, 0.95, 1.45, 10, NULL, NULL),
        ('Orégano Molido 20g', 'Salsas y Condimentos', 'und', false, 0.40, 0.70, 20, NULL, NULL),
        ('Atún en Aceite 170g', 'Enlatados', 'und', false, 1.20, 1.75, 60, 'cj24', 24),
        ('Sardina en Tomate 425g', 'Enlatados', 'und', false, 1.10, 1.60, 30, 'cj24', 24),
        ('Durazno en Almíbar 820g', 'Conservas', 'und', false, 2.30, 3.20, 12, 'cj12', 12),
        ('Choclo Desgranado 340g', 'Conservas', 'und', false, 1.10, 1.60, 12, 'cj24', 24),
        ('Café Molido 250g', 'Café e Infusiones', 'und', false, 2.80, 3.90, 20, 'cj12', 12),
        ('Café Instantáneo 170g', 'Café e Infusiones', 'und', false, 4.20, 5.80, 15, 'cj12', 12),
        ('Té Filtrante x25', 'Café e Infusiones', 'und', false, 0.95, 1.45, 20, 'cj12', 12),
        ('Yerba Mate 500g', 'Café e Infusiones', 'und', false, 2.20, 3.10, 10, NULL, NULL),
        ('Leche Entera 1l', 'Lácteos', 'und', true, 0.85, 1.20, 120, 'cj12', 12),
        ('Leche Descremada 1l', 'Lácteos', 'und', true, 0.90, 1.30, 40, 'cj12', 12),
        ('Yogurt Natural 1l', 'Lácteos', 'und', true, 1.40, 2.00, 30, 'cj12', 12),
        ('Yogurt de Fresa 200g', 'Lácteos', 'und', true, 0.45, 0.75, 60, 'bd12', 12),
        ('Mantequilla 200g', 'Lácteos', 'und', true, 1.60, 2.30, 20, NULL, NULL),
        ('Crema de Leche 200ml', 'Lácteos', 'und', true, 0.95, 1.40, 15, NULL, NULL),
        ('Queso Fresco 500g', 'Quesos y Embutidos', 'und', true, 2.50, 3.60, 20, NULL, NULL),
        ('Queso Mozzarella', 'Quesos y Embutidos', 'kg', true, 6.80, 9.20, 10, NULL, NULL),
        ('Jamón Cocido 200g', 'Quesos y Embutidos', 'und', true, 1.80, 2.60, 20, NULL, NULL),
        ('Salchicha Tipo Viena 500g', 'Quesos y Embutidos', 'und', true, 2.00, 2.90, 25, NULL, NULL),
        ('Huevos Bandeja x30', 'Huevos', 'und', true, 4.20, 5.50, 30, NULL, NULL),
        ('Pechuga de Pollo', 'Aves', 'kg', true, 3.20, 4.50, 40, NULL, NULL),
        ('Pollo Entero', 'Aves', 'kg', true, 2.20, 3.10, 50, NULL, NULL),
        ('Carne Molida de Res', 'Carnes Rojas', 'kg', true, 4.80, 6.50, 30, NULL, NULL),
        ('Lomo de Res', 'Carnes Rojas', 'kg', true, 7.50, 10.20, 15, NULL, NULL),
        ('Chuleta de Cerdo', 'Carnes Rojas', 'kg', true, 4.10, 5.70, 20, NULL, NULL),
        ('Filete de Tilapia', 'Pescados y Mariscos', 'kg', true, 5.20, 7.30, 10, NULL, NULL),
        ('Banano', 'Frutas', 'kg', true, 0.45, 0.80, 80, NULL, NULL),
        ('Manzana Roja', 'Frutas', 'kg', true, 1.20, 1.90, 50, NULL, NULL),
        ('Naranja', 'Frutas', 'kg', true, 0.60, 1.00, 60, NULL, NULL),
        ('Limón', 'Frutas', 'kg', true, 0.90, 1.50, 30, NULL, NULL),
        ('Tomate', 'Verduras', 'kg', true, 0.80, 1.30, 60, NULL, NULL),
        ('Cebolla Blanca', 'Verduras', 'kg', true, 0.55, 0.95, 60, NULL, NULL),
        ('Papa', 'Verduras', 'kg', true, 0.50, 0.85, 100, NULL, NULL),
        ('Zanahoria', 'Verduras', 'kg', true, 0.50, 0.90, 40, NULL, NULL),
        ('Lechuga Criolla', 'Verduras', 'und', true, 0.40, 0.75, 40, NULL, NULL),
        ('Pan Francés', 'Panadería', 'und', true, 0.08, 0.15, 300, NULL, NULL),
        ('Pan de Molde Blanco 600g', 'Panadería', 'und', true, 1.10, 1.65, 40, NULL, NULL),
        ('Torta de Chocolate', 'Pastelería', 'und', true, 5.50, 8.50, 5, NULL, NULL),
        ('Gaseosa Cola 2l', 'Bebidas Gaseosas', 'und', false, 1.20, 1.80, 80, '6pk', 6),
        ('Gaseosa Naranja 2l', 'Bebidas Gaseosas', 'und', false, 1.10, 1.70, 40, '6pk', 6),
        ('Gaseosa Cola 500ml', 'Bebidas Gaseosas', 'und', false, 0.50, 0.85, 100, 'cj24', 24),
        ('Agua Mineral 500ml', 'Aguas', 'und', false, 0.25, 0.50, 150, 'cj24', 24),
        ('Agua Purificada 5l', 'Aguas', 'und', false, 0.90, 1.50, 30, NULL, NULL),
        ('Jugo de Naranja 1l', 'Jugos y Néctares', 'und', true, 1.00, 1.55, 30, 'cj12', 12),
        ('Néctar de Durazno 1l', 'Jugos y Néctares', 'und', false, 0.95, 1.45, 25, 'cj12', 12),
        ('Cerveza Lager Lata 355ml', 'Cervezas', 'und', false, 0.70, 1.10, 120, '24pk', 24),
        ('Cerveza Lager Botella 620ml', 'Cervezas', 'und', false, 0.95, 1.50, 80, 'cj12', 12),
        ('Vino Tinto 750ml', 'Vinos y Licores', 'und', false, 4.50, 6.90, 12, 'cj6', 6),
        ('Ron Añejo 750ml', 'Vinos y Licores', 'und', false, 8.00, 11.50, 8, 'cj6', 6),
        ('Papas Fritas 120g', 'Snacks', 'und', false, 0.80, 1.25, 60, 'cj24', 24),
        ('Maní Salado 100g', 'Snacks', 'und', false, 0.50, 0.85, 40, 'cj24', 24),
        ('Chocolate en Barra 40g', 'Golosinas', 'und', false, 0.45, 0.80, 80, 'cj24', 24),
        ('Caramelos Surtidos 100g', 'Golosinas', 'und', false, 0.55, 0.90, 40, NULL, NULL),
        ('Galleta de Soda x6', 'Galletas', 'und', false, 0.40, 0.70, 60, 'cj24', 24),
        ('Galleta Rellena Vainilla', 'Galletas', 'und', false, 0.35, 0.60, 80, 'cj24', 24),
        ('Detergente en Polvo 1kg', 'Detergentes', 'und', false, 2.20, 3.20, 30, 'cj12', 12),
        ('Lavavajilla 750ml', 'Productos de Limpieza', 'und', false, 1.40, 2.10, 25, 'cj12', 12),
        ('Cloro 1l', 'Productos de Limpieza', 'und', false, 0.60, 1.00, 30, 'cj12', 12),
        ('Papel Higiénico x12', 'Papel y Desechables', 'und', false, 3.50, 5.00, 30, NULL, NULL),
        ('Servilletas x200', 'Papel y Desechables', 'und', false, 0.50, 0.85, 30, NULL, NULL),
        ('Jabón de Tocador 150g', 'Higiene Personal', 'und', false, 0.55, 0.95, 60, 'cj12', 12),
        ('Shampoo 400ml', 'Cuidado del Cabello', 'und', false, 2.40, 3.50, 20, NULL, NULL),
        ('Pasta Dental 90g', 'Cuidado Dental', 'und', false, 1.10, 1.65, 40, 'cj12', 12),
        ('Pañales Talla M x30', 'Bebés', 'und', false, 6.50, 9.20, 15, NULL, NULL),
        ('Alimento para Perro 1kg', 'Mascotas', 'und', false, 1.80, 2.60, 20, NULL, NULL),
        ('Paracetamol 500mg x10', 'Farmacia Básica', 'und', false, 0.60, 1.00, 30, NULL, NULL),
        ('Cuaderno 100 hojas', 'Útiles Escolares', 'und', false, 0.90, 1.50, 40, NULL, NULL),
        ('Bolígrafo Azul', 'Papelería', 'und', false, 0.15, 0.30, 100, 'cj24', 24),
        ('Foco LED 9W', 'Ferretería Ligera', 'und', false, 1.20, 1.90, 20, NULL, NULL),
        ('Escoba de Cerda', 'Hogar y Cocina', 'und', false, 1.80, 2.80, 10, NULL, NULL),
        ('Harina Preparada 500g', 'Repostería', 'und', false, 1.30, 1.95, 10, NULL, NULL);

    -- Las unidades ya sembradas deben existir (falla temprano si se cambia una abreviatura)
    IF EXISTS (SELECT 1 FROM seed_prod s WHERE NOT EXISTS (SELECT 1 FROM unidades_medida u WHERE u.abreviatura = s.unidad)
            OR NOT EXISTS (SELECT 1 FROM categorias c WHERE c.nombre = s.categoria)
            OR (s.extra IS NOT NULL AND NOT EXISTS (SELECT 1 FROM unidades_medida u WHERE u.abreviatura = s.extra))) THEN
        RAISE EXCEPTION 'La semilla de catalogo referencia una unidad o categoria inexistente';
    END IF;

    INSERT INTO productos (nombre, descripcion, categoria_id, stock_minimo, es_perecedero, estado, created_by, updated_by)
    SELECT s.nombre, 'Producto de ' || lower(s.categoria), c.id, s.stock_min, s.perecedero, 'ACTIVO', 'SEED', 'SEED'
    FROM seed_prod s JOIN categorias c ON c.nombre = s.categoria
    ORDER BY c.id, s.nombre;

    -- Dos productos descontinuados (inactivos) para probar filtros
    UPDATE productos SET estado = 'INACTIVO'
    WHERE nombre IN ('Torta de Chocolate', 'Yerba Mate 500g') AND created_by = 'SEED';

    -- ---------------------------------------------------------------- presentaciones
    -- Unidad base de cada producto (codigo de barras de 13 digitos)
    INSERT INTO presentaciones_producto (producto_id, unidad_medida_id, codigo_barras, factor_conversion, es_unidad_base,
                                         created_by, updated_by)
    SELECT p.id, u.id, '750' || lpad(p.id::text, 6, '0') || '0001', 1, true, 'SEED', 'SEED'
    FROM seed_prod s
    JOIN productos p ON p.nombre = s.nombre AND p.created_by = 'SEED'
    JOIN unidades_medida u ON u.abreviatura = s.unidad;

    -- Presentacion mayor (caja, saco, six pack...)
    INSERT INTO presentaciones_producto (producto_id, unidad_medida_id, codigo_barras, factor_conversion, es_unidad_base,
                                         created_by, updated_by)
    SELECT p.id, u.id, '750' || lpad(p.id::text, 6, '0') || '0002', s.factor, false, 'SEED', 'SEED'
    FROM seed_prod s
    JOIN productos p ON p.nombre = s.nombre AND p.created_by = 'SEED'
    JOIN unidades_medida u ON u.abreviatura = s.extra
    WHERE s.extra IS NOT NULL;

    -- ---------------------------------------------------------------- precios
    -- General por unidad base
    INSERT INTO precios_presentacion (presentacion_id, tipo_cliente_id, cantidad_minima, precio, created_by, updated_by)
    SELECT pr.id, NULL, 1, s.precio, 'SEED', 'SEED'
    FROM seed_prod s
    JOIN productos p ON p.nombre = s.nombre AND p.created_by = 'SEED'
    JOIN presentaciones_producto pr ON pr.producto_id = p.id AND pr.es_unidad_base;

    -- General por presentacion mayor (7% de descuento sobre comprar las unidades sueltas)
    INSERT INTO precios_presentacion (presentacion_id, tipo_cliente_id, cantidad_minima, precio, created_by, updated_by)
    SELECT pr.id, NULL, 1, round(s.precio * s.factor * 0.93, 2), 'SEED', 'SEED'
    FROM seed_prod s
    JOIN productos p ON p.nombre = s.nombre AND p.created_by = 'SEED'
    JOIN presentaciones_producto pr ON pr.producto_id = p.id AND NOT pr.es_unidad_base
    WHERE s.extra IS NOT NULL;

    -- Cliente FRECUENTE: 5% menos en la unidad base
    INSERT INTO precios_presentacion (presentacion_id, tipo_cliente_id, cantidad_minima, precio, created_by, updated_by)
    SELECT pr.id, t.id, 1, round(s.precio * 0.95, 2), 'SEED', 'SEED'
    FROM seed_prod s
    JOIN productos p ON p.nombre = s.nombre AND p.created_by = 'SEED'
    JOIN presentaciones_producto pr ON pr.producto_id = p.id AND pr.es_unidad_base
    JOIN tipos_cliente t ON t.nombre = 'FRECUENTE';

    -- Cliente MAYORISTA: 10% menos comprando 12 o mas unidades
    INSERT INTO precios_presentacion (presentacion_id, tipo_cliente_id, cantidad_minima, precio, created_by, updated_by)
    SELECT pr.id, t.id, 12, round(s.precio * 0.90, 2), 'SEED', 'SEED'
    FROM seed_prod s
    JOIN productos p ON p.nombre = s.nombre AND p.created_by = 'SEED'
    JOIN presentaciones_producto pr ON pr.producto_id = p.id AND pr.es_unidad_base
    JOIN tipos_cliente t ON t.nombre = 'MAYORISTA';

    -- ---------------------------------------------------------------- lote inicial por producto
    -- Perecederos: vencen entre 6 y 40 dias; cada 15.o producto vence en 4 dias con 20 % de descuento.
    INSERT INTO lotes (producto_id, numero_lote, fecha_vencimiento, costo_unitario_compra,
                       porcentaje_descuento_vencimiento, created_by, updated_by)
    SELECT p.id, 'INI-' || lpad(p.id::text, 5, '0'),
           CASE WHEN NOT p.es_perecedero THEN NULL
                WHEN p.id % 15 = 0 THEN CURRENT_DATE + 4
                ELSE CURRENT_DATE + 6 + ((p.id * 7) % 35)::int END,
           s.costo,
           CASE WHEN p.es_perecedero AND p.id % 15 = 0 THEN 20 ELSE 0 END,
           'SEED', 'SEED'
    FROM seed_prod s JOIN productos p ON p.nombre = s.nombre AND p.created_by = 'SEED';
END
$seed$;
