export type EstadoSugerencia = 'REORDENAR' | 'SIN_NECESIDAD' | 'DATOS_INSUFICIENTES' | 'SIN_DEMANDA' | 'SIN_PROVEEDOR';
export type AccionSugerencia = 'PENDIENTE' | 'PEDIDO_CREADO' | 'DESCARTADA';
export type Escenario = 'PESIMISTA' | 'ESTANDAR' | 'OPTIMISTA';

export interface ResultadoEscenario {
  eoq: number;
  stockSeguridad: number;
  puntoReorden: number;
  cantidad: number;
}

/** Cantidades en unidad base; tiempos en días. */
export interface Sugerencia {
  id: number;
  calculoId: number;
  productoId: number;
  producto: string;
  categoria: string;
  perecedero: boolean;
  proveedorId: number | null;
  proveedor: string | null;
  estado: EstadoSugerencia;
  accion: AccionSugerencia;
  pedidoId: number | null;
  numeroPedido: string | null;
  accionPor: string | null;
  accionAt: string | null;
  diasHistorial: number;
  demandaDiaria: number;
  desviacionDemanda: number;
  leadTimeDias: number;
  desviacionLeadTime: number;
  muestrasLeadTime: number;
  costoUnitario: number;
  costoPedido: number;
  costoAlmacenAnual: number;
  nivelServicio: number;
  factorZ: number;
  stockActual: number;
  enTransito: number;
  vidaUtilDias: number | null;
  pesimista: ResultadoEscenario;
  estandar: ResultadoEscenario;
  optimista: ResultadoEscenario;
  motivo: string;
  /** Variación reciente del costo del proveedor como fracción (0.08 = +8 %); null sin datos. */
  variacionCosto: number | null;
  /** Días que se adelantó la compra por alza de costo (0 = no se adelantó). */
  diasAnticipo: number;
}

export interface CalculoReorden {
  id: number;
  fecha: string;
  origen: 'PROGRAMADO' | 'MANUAL';
  ejecutadoPor: string;
  diasVentana: number;
  historialMinimoDias: number;
  productosEvaluados: number;
  productosAReordenar: number;
  porEstado: Record<EstadoSugerencia, number>;
}

export interface SugerenciaFiltro {
  estado?: EstadoSugerencia;
  accion?: AccionSugerencia;
  proveedorId?: number;
  search?: string;
  page?: number;
  size?: number;
}

export interface CrearPedidosRequest {
  depositoDestinoId: number;
  escenario: Escenario;
  observacion?: string;
  items: { sugerenciaId: number; cantidad?: number }[];
}

export interface PedidoGenerado {
  pedidoId: number;
  numeroPedido: string;
  proveedorId: number;
  proveedor: string;
  lineas: number;
  total: number;
}

export interface ParametrosReorden {
  productoId: number;
  producto: string;
  /** Propios del producto; null = usa el global. */
  costoPedido: number | null;
  costoAlmacenPct: number | null;
  nivelServicio: number | null;
  proveedorId: number | null;
  proveedor: string | null;
  activo: boolean;
  costoPedidoEfectivo: number;
  costoAlmacenPctEfectivo: number;
  nivelServicioEfectivo: number;
}

export interface ParametrosReordenRequest {
  costoPedido: number | null;
  costoAlmacenPct: number | null;
  nivelServicio: number | null;
  proveedorId: number | null;
  activo: boolean;
}

/** Evolución del costo de un producto con un proveedor (RF-35). Variaciones en fracción; null sin datos. */
export interface VariacionCosto {
  proveedorId: number;
  proveedor: string;
  productoId: number;
  producto: string;
  categoria: string;
  compras: number;
  primeraCompra: string;
  ultimaCompra: string;
  ultimoCosto: number;
  costoMinimo: number;
  costoMaximo: number;
  variacion30: number | null;
  variacion90: number | null;
  /** Supera el umbral de alza del motor de reorden. */
  alza: boolean;
}

export interface HistoricoCosto {
  detalleId: number;
  pedidoId: number;
  numeroPedido: string;
  estadoPedido: string;
  proveedorId: number;
  proveedor: string;
  productoId: number;
  producto: string;
  categoria: string;
  fecha: string;
  costoUnitario: number;
  cantidad: number;
  /** Contra la compra anterior del mismo producto al mismo proveedor. */
  variacionAnterior: number | null;
}

export interface VariacionProveedor {
  proveedorId: number;
  proveedor: string;
  productos: number;
  productosConAlza: number;
  variacionMedia30: number | null;
  variacionMedia90: number | null;
  mayorAlza90: number | null;
}

export interface CostoFiltro {
  proveedorId?: number;
  productoId?: number;
  search?: string;
  page?: number;
  size?: number;
}

export const ESTADO_LABEL: Record<EstadoSugerencia, string> = {
  REORDENAR: 'Reordenar',
  SIN_NECESIDAD: 'Stock suficiente',
  DATOS_INSUFICIENTES: 'Datos insuficientes',
  SIN_DEMANDA: 'Sin demanda',
  SIN_PROVEEDOR: 'Sin proveedor',
};

export const ACCION_LABEL: Record<AccionSugerencia, string> = {
  PENDIENTE: 'Pendiente',
  PEDIDO_CREADO: 'Pedido creado',
  DESCARTADA: 'Descartada',
};

export const ESCENARIO_LABEL: Record<Escenario, string> = {
  PESIMISTA: 'Pesimista',
  ESTANDAR: 'Estándar',
  OPTIMISTA: 'Optimista',
};

export const ESCENARIOS: readonly Escenario[] = ['PESIMISTA', 'ESTANDAR', 'OPTIMISTA'];

export function resultado(s: Sugerencia, e: Escenario): ResultadoEscenario {
  return e === 'PESIMISTA' ? s.pesimista : e === 'OPTIMISTA' ? s.optimista : s.estandar;
}
