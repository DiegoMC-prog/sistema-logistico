import { PERMISOS } from '../../../core/auth/permisos';

/** Cómo se formatea y pinta un valor. */
export type TipoCol = 'texto' | 'dinero' | 'entero' | 'cantidad' | 'pct' | 'fecha' | 'fechahora' | 'estado' | 'si-no';

/**
 * Resalta el valor según su signo:
 * - `diferencia`: negativo = faltante (rojo), positivo = sobrante (ámbar).
 * - `costo`: positivo = subió (rojo), negativo = bajó (verde).
 * - `positivo-malo`: cualquier valor mayor a cero se marca en rojo (atrasos, pérdidas).
 */
export type Resaltado = 'diferencia' | 'costo' | 'positivo-malo';

export interface Col {
  key: string;
  label: string;
  tipo?: TipoCol;
  resaltar?: Resaltado;
}

export type TipoFiltro = 'select' | 'categoria' | 'caja' | 'deposito' | 'proveedor' | 'numero' | 'check' | 'producto';

export interface FiltroDef {
  key: string;
  label: string;
  tipo: TipoFiltro;
  opciones?: { value: string; label: string }[];
  defecto?: string | boolean;
  min?: number;
  max?: number;
  /** Sin este filtro no se puede consultar el reporte. */
  obligatorio?: boolean;
}

export type Valores = Record<string, string | boolean | undefined>;

export interface ReporteDef {
  id: string;
  titulo: string;
  descripcion: string;
  /** Ruta bajo `/api/v1/reportes/<familia>/`. */
  path: string;
  /** Acepta `desde` y `hasta`. Los reportes de estado actual no. */
  rango: boolean;
  filtros: FiltroDef[];
  /** Tarjetas de totales: solo se muestran las que el servidor devuelve. */
  totales: Col[] | ((v: Valores) => Col[]);
  columnas: Col[] | ((v: Valores) => Col[]);
}

export interface FamiliaDef {
  id: string;
  titulo: string;
  permiso: string;
  reportes: ReporteDef[];
}

const AGRUPACION: FiltroDef = {
  key: 'agrupacion',
  label: 'Agrupar por',
  tipo: 'select',
  defecto: 'DIA',
  opciones: [
    { value: 'DIA', label: 'Día' },
    { value: 'SEMANA', label: 'Semana' },
    { value: 'MES', label: 'Mes' },
  ],
};

const CATEGORIA: FiltroDef = { key: 'categoriaId', label: 'Categoría', tipo: 'categoria' };
const DEPOSITO: FiltroDef = { key: 'depositoId', label: 'Depósito', tipo: 'deposito' };
const CAJA: FiltroDef = { key: 'cajaId', label: 'Caja', tipo: 'caja' };
const PROVEEDOR: FiltroDef = { key: 'proveedorId', label: 'Proveedor', tipo: 'proveedor' };
const PRODUCTO: FiltroDef = { key: 'productoId', label: 'Producto', tipo: 'producto', obligatorio: true };
const limite = (defecto: number): FiltroDef => ({ key: 'limite', label: 'Límite de filas', tipo: 'numero', defecto: String(defecto), min: 1, max: 5000 });

const VENTAS_PERSONAL: Col[] = [
  { key: 'ventas', label: 'Ventas', tipo: 'entero' },
  { key: 'total', label: 'Total', tipo: 'dinero' },
  { key: 'ticketPromedio', label: 'Ticket promedio', tipo: 'dinero' },
  { key: 'anuladas', label: 'Anuladas', tipo: 'entero' },
  { key: 'totalAnuladas', label: 'Total anuladas', tipo: 'dinero' },
  { key: 'devoluciones', label: 'Devoluciones', tipo: 'entero' },
  { key: 'devuelto', label: 'Devuelto', tipo: 'dinero' },
];

const CIERRE_COLS: Col[] = [
  { key: 'turnoId', label: 'Turno', tipo: 'entero' },
  { key: 'caja', label: 'Caja' },
  { key: 'cajero', label: 'Cajero' },
  { key: 'estado', label: 'Estado', tipo: 'estado' },
  { key: 'fechaApertura', label: 'Apertura', tipo: 'fechahora' },
  { key: 'fechaCierre', label: 'Cierre', tipo: 'fechahora' },
  { key: 'montoApertura', label: 'Monto apertura', tipo: 'dinero' },
  { key: 'ventas', label: 'Ventas', tipo: 'entero' },
  { key: 'totalVentas', label: 'Total ventas', tipo: 'dinero' },
  { key: 'ventasEfectivo', label: 'En efectivo', tipo: 'dinero' },
  { key: 'otrosMetodos', label: 'Otros métodos', tipo: 'dinero' },
  { key: 'ingresos', label: 'Ingresos', tipo: 'dinero' },
  { key: 'egresos', label: 'Egresos', tipo: 'dinero' },
  { key: 'reembolsosEfectivo', label: 'Reembolsos efectivo', tipo: 'dinero' },
  { key: 'efectivoEsperado', label: 'Esperado', tipo: 'dinero' },
  { key: 'efectivoDeclarado', label: 'Declarado', tipo: 'dinero' },
  { key: 'diferencia', label: 'Diferencia', tipo: 'dinero', resaltar: 'diferencia' },
];

export const FAMILIAS: FamiliaDef[] = [
  {
    id: 'ventas',
    titulo: 'Ventas',
    permiso: PERMISOS.reporteVentas,
    reportes: [
      {
        id: 'resumen', titulo: 'Resumen', path: 'resumen', rango: true, filtros: [AGRUPACION],
        descripcion: 'Una fila por período, incluidos los que no tuvieron ventas. Solo cuentan las ventas completadas.',
        totales: [
          { key: 'ventas', label: 'Ventas', tipo: 'entero' }, { key: 'total', label: 'Total', tipo: 'dinero' },
          { key: 'ticketPromedio', label: 'Ticket promedio', tipo: 'dinero' }, { key: 'descuentos', label: 'Descuentos', tipo: 'dinero' },
          { key: 'devuelto', label: 'Devuelto', tipo: 'dinero' }, { key: 'neto', label: 'Neto', tipo: 'dinero' },
          { key: 'anuladas', label: 'Anuladas', tipo: 'entero' }, { key: 'totalAnuladas', label: 'Total anuladas', tipo: 'dinero' },
        ],
        columnas: [
          { key: 'periodo', label: 'Período', tipo: 'fecha' }, { key: 'ventas', label: 'Ventas', tipo: 'entero' },
          { key: 'subtotal', label: 'Subtotal', tipo: 'dinero' }, { key: 'descuentos', label: 'Descuentos', tipo: 'dinero' },
          { key: 'total', label: 'Total', tipo: 'dinero' }, { key: 'ticketPromedio', label: 'Ticket prom.', tipo: 'dinero' },
          { key: 'anuladas', label: 'Anuladas', tipo: 'entero' }, { key: 'totalAnuladas', label: 'Total anuladas', tipo: 'dinero' },
          { key: 'devoluciones', label: 'Devoluciones', tipo: 'entero' }, { key: 'devuelto', label: 'Devuelto', tipo: 'dinero' },
          { key: 'neto', label: 'Neto', tipo: 'dinero' },
        ],
      },
      {
        id: 'por-producto', titulo: 'Por producto', path: 'por-producto', rango: true, filtros: [CATEGORIA, limite(100)],
        descripcion: 'Productos ordenados por importe vendido. Las unidades están en unidad base.',
        totales: [
          { key: 'productos', label: 'Productos', tipo: 'entero' }, { key: 'unidades', label: 'Unidades', tipo: 'cantidad' },
          { key: 'descuentos', label: 'Descuentos', tipo: 'dinero' }, { key: 'importe', label: 'Importe', tipo: 'dinero' },
        ],
        columnas: [
          { key: 'producto', label: 'Producto' }, { key: 'categoria', label: 'Categoría' },
          { key: 'unidades', label: 'Unidades', tipo: 'cantidad' }, { key: 'ventas', label: 'Ventas', tipo: 'entero' },
          { key: 'descuentos', label: 'Descuentos', tipo: 'dinero' }, { key: 'importe', label: 'Importe', tipo: 'dinero' },
        ],
      },
      {
        id: 'sin-ventas', titulo: 'Sin ventas', path: 'productos-sin-ventas', rango: true, filtros: [CATEGORIA, limite(100)],
        descripcion: 'Productos activos que no se vendieron en el rango.',
        totales: [{ key: 'productos', label: 'Productos sin ventas', tipo: 'entero' }],
        columnas: [{ key: 'producto', label: 'Producto' }, { key: 'categoria', label: 'Categoría' }],
      },
      {
        id: 'por-cajero', titulo: 'Por cajero', path: 'por-cajero', rango: true, filtros: [],
        descripcion: 'La venta y la devolución cuentan para quien las registró. Un cajero que solo tuvo devoluciones también aparece.',
        totales: VENTAS_PERSONAL,
        columnas: [{ key: 'nombre', label: 'Cajero' }, ...VENTAS_PERSONAL],
      },
      {
        id: 'por-caja', titulo: 'Por caja', path: 'por-caja', rango: true, filtros: [],
        descripcion: 'Según el turno de la venta o de la devolución.',
        totales: VENTAS_PERSONAL,
        columnas: [{ key: 'nombre', label: 'Caja' }, ...VENTAS_PERSONAL],
      },
      {
        id: 'por-metodo-pago', titulo: 'Por método de pago', path: 'por-metodo-pago', rango: true, filtros: [],
        descripcion: 'El cobrado no incluye el crédito de devolución y el efectivo va neto del vuelto.',
        totales: [
          { key: 'cobrado', label: 'Cobrado', tipo: 'dinero' }, { key: 'reembolsado', label: 'Reembolsado', tipo: 'dinero' },
          { key: 'neto', label: 'Neto', tipo: 'dinero' },
        ],
        columnas: [
          { key: 'metodoPago', label: 'Método' }, { key: 'ventas', label: 'Ventas', tipo: 'entero' },
          { key: 'cobrado', label: 'Cobrado', tipo: 'dinero' }, { key: 'reembolsado', label: 'Reembolsado', tipo: 'dinero' },
          { key: 'neto', label: 'Neto', tipo: 'dinero' },
        ],
      },
      {
        id: 'por-cliente', titulo: 'Por cliente', path: 'por-cliente', rango: true,
        filtros: [
          { key: 'agrupar', label: 'Agrupar por', tipo: 'select', defecto: 'CLIENTE', opciones: [{ value: 'CLIENTE', label: 'Cliente' }, { value: 'TIPO', label: 'Tipo de cliente' }] },
          limite(100),
        ],
        descripcion: 'Las ventas sin cliente aparecen como una fila aparte.',
        totales: [
          { key: 'ventas', label: 'Ventas', tipo: 'entero' }, { key: 'total', label: 'Total', tipo: 'dinero' },
          { key: 'ticketPromedio', label: 'Ticket promedio', tipo: 'dinero' },
        ],
        columnas: [
          { key: 'nombre', label: 'Nombre' }, { key: 'documento', label: 'Documento' }, { key: 'ventas', label: 'Ventas', tipo: 'entero' },
          { key: 'total', label: 'Total', tipo: 'dinero' }, { key: 'ticketPromedio', label: 'Ticket prom.', tipo: 'dinero' },
          { key: 'ultimaCompra', label: 'Última compra', tipo: 'fechahora' },
        ],
      },
      {
        id: 'anulaciones-devoluciones', titulo: 'Anulaciones y devoluciones', path: 'anulaciones-devoluciones', rango: true, filtros: [limite(100)],
        descripcion: 'Incidencias para auditoría, de la más reciente a la más antigua.',
        totales: [
          { key: 'anulaciones', label: 'Anulaciones', tipo: 'entero' }, { key: 'montoAnulaciones', label: 'Monto anulado', tipo: 'dinero' },
          { key: 'devoluciones', label: 'Devoluciones', tipo: 'entero' }, { key: 'montoDevoluciones', label: 'Monto devuelto', tipo: 'dinero' },
          { key: 'cambios', label: 'Cambios', tipo: 'entero' }, { key: 'montoCambios', label: 'Monto de cambios', tipo: 'dinero' },
        ],
        columnas: [
          { key: 'tipo', label: 'Tipo', tipo: 'estado' }, { key: 'numero', label: 'Número' }, { key: 'ventaNumero', label: 'Venta' },
          { key: 'fecha', label: 'Fecha', tipo: 'fechahora' }, { key: 'usuario', label: 'Usuario' }, { key: 'motivo', label: 'Motivo' },
          { key: 'monto', label: 'Monto', tipo: 'dinero' },
        ],
      },
    ],
  },
  {
    id: 'caja',
    titulo: 'Caja',
    permiso: PERMISOS.reporteCaja,
    reportes: [
      {
        id: 'cierres', titulo: 'Cierres de caja', path: 'cierres', rango: true,
        filtros: [CAJA, { key: 'estado', label: 'Estado', tipo: 'select', opciones: [{ value: '', label: 'Todos' }, { value: 'ABIERTO', label: 'Abiertos' }, { value: 'CERRADO', label: 'Cerrados' }] }],
        descripcion: 'El cuadre de cada turno. Esperado = apertura + ingresos − egresos + efectivo cobrado − reembolsos en efectivo. Diferencia = declarado − esperado.',
        totales: [
          { key: 'turnos', label: 'Turnos', tipo: 'entero' }, { key: 'turnosAbiertos', label: 'Abiertos', tipo: 'entero' },
          { key: 'totalVentas', label: 'Total ventas', tipo: 'dinero' }, { key: 'ventasEfectivo', label: 'En efectivo', tipo: 'dinero' },
          { key: 'otrosMetodos', label: 'Otros métodos', tipo: 'dinero' }, { key: 'ingresos', label: 'Ingresos', tipo: 'dinero' },
          { key: 'egresos', label: 'Egresos', tipo: 'dinero' }, { key: 'efectivoEsperadoCerrados', label: 'Esperado (cerrados)', tipo: 'dinero' },
          { key: 'efectivoDeclaradoCerrados', label: 'Declarado (cerrados)', tipo: 'dinero' },
          { key: 'diferenciaCerrados', label: 'Diferencia (cerrados)', tipo: 'dinero', resaltar: 'diferencia' },
          { key: 'turnosConDiferencia', label: 'Turnos con diferencia', tipo: 'entero' },
        ],
        columnas: CIERRE_COLS,
      },
      {
        id: 'diferencias', titulo: 'Diferencias', path: 'diferencias', rango: true,
        filtros: [
          { key: 'agrupar', label: 'Agrupar por', tipo: 'select', defecto: 'TURNO', opciones: [{ value: 'TURNO', label: 'Turno' }, { value: 'CAJERO', label: 'Cajero' }, { value: 'CAJA', label: 'Caja' }] },
          CAJA,
        ],
        descripcion: 'Solo turnos cerrados. Por turno: únicamente los que tienen sobrante o faltante. Por cajero o caja: resumen de todos sus turnos.',
        totales: [
          { key: 'turnos', label: 'Turnos', tipo: 'entero' }, { key: 'turnosConDiferencia', label: 'Con diferencia', tipo: 'entero' },
          { key: 'sobrante', label: 'Sobrante', tipo: 'dinero' }, { key: 'faltante', label: 'Faltante', tipo: 'dinero' },
          { key: 'neto', label: 'Neto', tipo: 'dinero', resaltar: 'diferencia' },
        ],
        columnas: (v) =>
          !v['agrupar'] || v['agrupar'] === 'TURNO'
            ? CIERRE_COLS
            : [
                { key: 'nombre', label: v['agrupar'] === 'CAJA' ? 'Caja' : 'Cajero' }, { key: 'turnos', label: 'Turnos', tipo: 'entero' },
                { key: 'turnosConDiferencia', label: 'Con diferencia', tipo: 'entero' }, { key: 'sobrante', label: 'Sobrante', tipo: 'dinero' },
                { key: 'faltante', label: 'Faltante', tipo: 'dinero', resaltar: 'diferencia' },
                { key: 'neto', label: 'Neto', tipo: 'dinero', resaltar: 'diferencia' },
              ],
      },
    ],
  },
  {
    id: 'inventario',
    titulo: 'Inventario',
    permiso: PERMISOS.reporteInventario,
    reportes: [
      {
        id: 'stock-valorizado', titulo: 'Stock valorizado', path: 'stock-valorizado', rango: false,
        filtros: [
          { key: 'agrupar', label: 'Agrupar por', tipo: 'select', defecto: 'PRODUCTO', opciones: [{ value: 'PRODUCTO', label: 'Producto' }, { value: 'DEPOSITO', label: 'Depósito' }, { value: 'CATEGORIA', label: 'Categoría' }] },
          DEPOSITO, CATEGORIA,
        ],
        descripcion: 'Estado actual. Valor = stock × costo de compra de cada lote. Solo cuenta lo que tiene stock.',
        totales: [{ key: 'filas', label: 'Filas', tipo: 'entero' }, { key: 'stock', label: 'Stock', tipo: 'cantidad' }, { key: 'valor', label: 'Valor', tipo: 'dinero' }],
        columnas: [
          { key: 'nombre', label: 'Nombre' }, { key: 'categoria', label: 'Categoría' }, { key: 'stock', label: 'Stock', tipo: 'cantidad' },
          { key: 'valor', label: 'Valor', tipo: 'dinero' }, { key: 'costoPromedio', label: 'Costo promedio', tipo: 'dinero' }, { key: 'lotes', label: 'Lotes', tipo: 'entero' },
        ],
      },
      {
        id: 'stock-bajo', titulo: 'Stock bajo el mínimo', path: 'stock-bajo', rango: false, filtros: [DEPOSITO, CATEGORIA],
        descripcion: 'Estado actual. Productos por debajo de su stock mínimo, los más críticos primero: la lista para pedir mercadería.',
        totales: [{ key: 'productos', label: 'Productos', tipo: 'entero' }, { key: 'sinStock', label: 'Sin stock', tipo: 'entero' }, { key: 'faltante', label: 'Faltante total', tipo: 'cantidad' }],
        columnas: [
          { key: 'producto', label: 'Producto' }, { key: 'categoria', label: 'Categoría' }, { key: 'stock', label: 'Stock', tipo: 'cantidad' },
          { key: 'stockMinimo', label: 'Mínimo', tipo: 'cantidad' }, { key: 'faltante', label: 'Faltante', tipo: 'cantidad', resaltar: 'positivo-malo' },
          { key: 'vendido30Dias', label: 'Vendido 30 días', tipo: 'cantidad' }, { key: 'diasDeStock', label: 'Días de stock', tipo: 'cantidad' },
        ],
      },
      {
        id: 'lotes-por-vencer', titulo: 'Lotes por vencer', path: 'lotes-por-vencer', rango: false,
        filtros: [
          { key: 'dias', label: 'Próximos (días)', tipo: 'numero', defecto: '30', min: 0, max: 365 },
          { key: 'incluirVencidos', label: 'Incluir vencidos', tipo: 'check', defecto: true },
          DEPOSITO,
        ],
        descripcion: 'Estado actual. Lotes con existencias que vencen pronto o ya vencieron.',
        totales: [
          { key: 'lotes', label: 'Lotes', tipo: 'entero' }, { key: 'vencidos', label: 'Vencidos', tipo: 'entero' }, { key: 'porVencer', label: 'Por vencer', tipo: 'entero' },
          { key: 'stock', label: 'Stock', tipo: 'cantidad' }, { key: 'valorEnRiesgo', label: 'Valor en riesgo', tipo: 'dinero' }, { key: 'valorVencido', label: 'Valor vencido', tipo: 'dinero' },
        ],
        columnas: [
          { key: 'producto', label: 'Producto' }, { key: 'numeroLote', label: 'Lote' }, { key: 'fechaVencimiento', label: 'Vence', tipo: 'fecha' },
          { key: 'diasRestantes', label: 'Días restantes', tipo: 'entero' }, { key: 'estado', label: 'Estado', tipo: 'estado' },
          { key: 'stock', label: 'Stock', tipo: 'cantidad' }, { key: 'costoUnitario', label: 'Costo unit.', tipo: 'dinero' },
          { key: 'valorEnRiesgo', label: 'Valor en riesgo', tipo: 'dinero' }, { key: 'descuentoVencimiento', label: 'Descuento', tipo: 'pct' },
        ],
      },
      {
        id: 'kardex', titulo: 'Kardex', path: 'kardex', rango: true, filtros: [PRODUCTO, DEPOSITO],
        descripcion: 'Movimientos de un producto con saldo acumulado. Sin depósito, el saldo es el total y un traslado entre depósitos no lo cambia.',
        totales: [
          { key: 'saldoInicial', label: 'Saldo inicial', tipo: 'cantidad' }, { key: 'entradas', label: 'Entradas', tipo: 'cantidad' },
          { key: 'salidas', label: 'Salidas', tipo: 'cantidad' }, { key: 'saldoFinal', label: 'Saldo final', tipo: 'cantidad' },
        ],
        columnas: [
          { key: 'fecha', label: 'Fecha', tipo: 'fechahora' }, { key: 'tipo', label: 'Tipo', tipo: 'estado' },
          { key: 'depositoOrigen', label: 'Origen' }, { key: 'depositoDestino', label: 'Destino' }, { key: 'usuario', label: 'Usuario' },
          { key: 'referencia', label: 'Referencia' }, { key: 'numeroLote', label: 'Lote' }, { key: 'entrada', label: 'Entrada', tipo: 'cantidad' },
          { key: 'salida', label: 'Salida', tipo: 'cantidad' }, { key: 'saldo', label: 'Saldo', tipo: 'cantidad' },
        ],
      },
    ],
  },
  {
    id: 'utilidad',
    titulo: 'Utilidad',
    permiso: PERMISOS.reporteUtilidad,
    reportes: [
      {
        id: 'por-periodo', titulo: 'Por período', path: 'por-periodo', rango: true, filtros: [AGRUPACION],
        descripcion: 'Utilidad neta = (ingreso − devuelto) − (costo − costo recuperado). Lo devuelto dañado, que no vuelve al stock, queda como pérdida.',
        totales: [
          { key: 'ventas', label: 'Ventas', tipo: 'entero' }, { key: 'ingreso', label: 'Ingreso', tipo: 'dinero' }, { key: 'costo', label: 'Costo', tipo: 'dinero' },
          { key: 'utilidad', label: 'Utilidad', tipo: 'dinero' }, { key: 'margen', label: 'Margen', tipo: 'pct' }, { key: 'devuelto', label: 'Devuelto', tipo: 'dinero' },
          { key: 'costoRecuperado', label: 'Costo recuperado', tipo: 'dinero' }, { key: 'utilidadNeta', label: 'Utilidad neta', tipo: 'dinero' },
        ],
        columnas: [
          { key: 'periodo', label: 'Período', tipo: 'fecha' }, { key: 'ventas', label: 'Ventas', tipo: 'entero' }, { key: 'ingreso', label: 'Ingreso', tipo: 'dinero' },
          { key: 'costo', label: 'Costo', tipo: 'dinero' }, { key: 'utilidad', label: 'Utilidad', tipo: 'dinero' }, { key: 'margen', label: 'Margen', tipo: 'pct' },
          { key: 'devuelto', label: 'Devuelto', tipo: 'dinero' }, { key: 'costoRecuperado', label: 'Costo recup.', tipo: 'dinero' }, { key: 'utilidadNeta', label: 'Utilidad neta', tipo: 'dinero' },
        ],
      },
      {
        id: 'por-producto', titulo: 'Por producto', path: 'por-producto', rango: true, filtros: [CATEGORIA, limite(100)],
        descripcion: 'De mayor a menor utilidad bruta (no descuenta devoluciones, que no se pueden atribuir a un producto con exactitud).',
        totales: [
          { key: 'productos', label: 'Productos', tipo: 'entero' }, { key: 'ingreso', label: 'Ingreso', tipo: 'dinero' }, { key: 'costo', label: 'Costo', tipo: 'dinero' },
          { key: 'utilidad', label: 'Utilidad', tipo: 'dinero' }, { key: 'margen', label: 'Margen', tipo: 'pct' },
        ],
        columnas: [
          { key: 'producto', label: 'Producto' }, { key: 'categoria', label: 'Categoría' }, { key: 'unidades', label: 'Unidades', tipo: 'cantidad' },
          { key: 'ingreso', label: 'Ingreso', tipo: 'dinero' }, { key: 'costo', label: 'Costo', tipo: 'dinero' }, { key: 'utilidad', label: 'Utilidad', tipo: 'dinero' },
          { key: 'margen', label: 'Margen', tipo: 'pct' },
        ],
      },
      {
        id: 'precios-manuales', titulo: 'Precios manuales', path: 'precios-manuales', rango: true, filtros: [limite(100)],
        descripcion: 'Ajustes de precio hechos por supervisores. Incluye ventas anuladas para poder auditarlas; los totales cuentan solo las vigentes.',
        totales: [
          { key: 'lineas', label: 'Líneas', tipo: 'entero' }, { key: 'bajoCosto', label: 'Bajo costo', tipo: 'entero' },
          { key: 'perdidaBajoCosto', label: 'Pérdida bajo costo', tipo: 'dinero' }, { key: 'importe', label: 'Importe', tipo: 'dinero' },
        ],
        columnas: [
          { key: 'ventaNumero', label: 'Venta' }, { key: 'estadoVenta', label: 'Estado', tipo: 'estado' }, { key: 'fecha', label: 'Fecha', tipo: 'fechahora' },
          { key: 'usuario', label: 'Usuario' }, { key: 'producto', label: 'Producto' }, { key: 'cantidad', label: 'Cant.', tipo: 'cantidad' },
          { key: 'precioUnitario', label: 'Precio', tipo: 'dinero' }, { key: 'costoUnitario', label: 'Costo', tipo: 'dinero' },
          { key: 'diferencia', label: 'Diferencia', tipo: 'dinero', resaltar: 'diferencia' }, { key: 'bajoCosto', label: 'Bajo costo', tipo: 'si-no' },
          { key: 'perdida', label: 'Pérdida', tipo: 'dinero', resaltar: 'positivo-malo' }, { key: 'subtotal', label: 'Subtotal', tipo: 'dinero' },
          { key: 'motivo', label: 'Motivo' },
        ],
      },
    ],
  },
  {
    id: 'compras',
    titulo: 'Compras',
    permiso: PERMISOS.reporteCompras,
    reportes: [
      {
        id: 'por-proveedor', titulo: 'Por proveedor', path: 'por-proveedor', rango: true, filtros: [],
        descripcion: 'Rango según la fecha del pedido. Cuentan los pedidos enviados o recibidos; los cancelados se informan aparte.',
        totales: [
          { key: 'proveedores', label: 'Proveedores', tipo: 'entero' }, { key: 'pedidos', label: 'Pedidos', tipo: 'entero' }, { key: 'cancelados', label: 'Cancelados', tipo: 'entero' },
          { key: 'montoPedido', label: 'Monto pedido', tipo: 'dinero' }, { key: 'montoRecibido', label: 'Monto recibido', tipo: 'dinero' },
          { key: 'montoPendiente', label: 'Monto pendiente', tipo: 'dinero' }, { key: 'cumplimiento', label: 'Cumplimiento', tipo: 'pct' },
        ],
        columnas: [
          { key: 'proveedor', label: 'Proveedor' }, { key: 'pedidos', label: 'Pedidos', tipo: 'entero' }, { key: 'cancelados', label: 'Cancelados', tipo: 'entero' },
          { key: 'montoPedido', label: 'Monto pedido', tipo: 'dinero' }, { key: 'montoRecibido', label: 'Monto recibido', tipo: 'dinero' },
          { key: 'montoPendiente', label: 'Monto pendiente', tipo: 'dinero' }, { key: 'cumplimiento', label: 'Cumplimiento', tipo: 'pct' },
        ],
      },
      {
        id: 'pendientes', titulo: 'Pendientes de recibir', path: 'pendientes', rango: false, filtros: [PROVEEDOR, DEPOSITO],
        descripcion: 'Estado actual. Pedidos enviados o parcialmente recibidos; los más atrasados primero.',
        totales: [
          { key: 'pedidos', label: 'Pedidos', tipo: 'entero' }, { key: 'atrasados', label: 'Atrasados', tipo: 'entero' }, { key: 'sinFechaEstimada', label: 'Sin fecha estimada', tipo: 'entero' },
          { key: 'unidadesPendientes', label: 'Unidades pendientes', tipo: 'cantidad' }, { key: 'montoPendiente', label: 'Monto pendiente', tipo: 'dinero' },
        ],
        columnas: [
          { key: 'numeroPedido', label: 'Pedido' }, { key: 'estado', label: 'Estado', tipo: 'estado' }, { key: 'proveedor', label: 'Proveedor' }, { key: 'deposito', label: 'Depósito' },
          { key: 'fechaPedido', label: 'Fecha', tipo: 'fecha' }, { key: 'fechaEntregaEstimada', label: 'Entrega estimada', tipo: 'fecha' },
          { key: 'diasAtraso', label: 'Días de atraso', tipo: 'entero', resaltar: 'positivo-malo' }, { key: 'total', label: 'Total', tipo: 'dinero' },
          { key: 'lineasPendientes', label: 'Líneas pend.', tipo: 'entero' }, { key: 'unidadesPendientes', label: 'Unid. pend.', tipo: 'cantidad' },
          { key: 'montoPendiente', label: 'Monto pend.', tipo: 'dinero' },
        ],
      },
      {
        id: 'evolucion-costo', titulo: 'Evolución del costo', path: 'evolucion-costo', rango: true, filtros: [PRODUCTO, PROVEEDOR],
        descripcion: 'Cada compra recibida de un producto, con el cambio de costo respecto de la anterior. Sirve para ver si un proveedor subió precios.',
        totales: [
          { key: 'compras', label: 'Compras', tipo: 'entero' }, { key: 'unidades', label: 'Unidades', tipo: 'cantidad' }, { key: 'gastado', label: 'Gastado', tipo: 'dinero' },
          { key: 'costoPromedio', label: 'Costo promedio', tipo: 'dinero' }, { key: 'costoMinimo', label: 'Costo mínimo', tipo: 'dinero' }, { key: 'costoMaximo', label: 'Costo máximo', tipo: 'dinero' },
          { key: 'costoUltimo', label: 'Último costo', tipo: 'dinero' }, { key: 'variacionTotal', label: 'Variación total', tipo: 'pct', resaltar: 'costo' },
        ],
        columnas: [
          { key: 'fecha', label: 'Fecha', tipo: 'fechahora' }, { key: 'numeroPedido', label: 'Pedido' }, { key: 'proveedor', label: 'Proveedor' }, { key: 'numeroLote', label: 'Lote' },
          { key: 'cantidad', label: 'Cantidad', tipo: 'cantidad' }, { key: 'costoUnitario', label: 'Costo unit.', tipo: 'dinero' },
          { key: 'variacion', label: 'Variación', tipo: 'pct', resaltar: 'costo' },
        ],
      },
    ],
  },
];

export const familiaPorId = (id: string | null | undefined): FamiliaDef | undefined => FAMILIAS.find((f) => f.id === id);

export const columnasDe = (r: ReporteDef, v: Valores): Col[] => (typeof r.columnas === 'function' ? r.columnas(v) : r.columnas);
export const totalesDe = (r: ReporteDef, v: Valores): Col[] => (typeof r.totales === 'function' ? r.totales(v) : r.totales);
