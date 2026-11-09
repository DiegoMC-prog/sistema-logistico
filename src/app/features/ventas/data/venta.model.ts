import { PageParams } from '../../../core/api/page.model';

export type EstadoVenta = 'COMPLETADA' | 'ANULADA';

export interface VentaItemRequest {
  presentacionId: number;
  cantidad: number;
  /** Ajuste de precio en vivo: exige VENTA_PRECIO_MODIFICAR y motivo. */
  precioUnitario?: number;
  motivoPrecio?: string;
}

export interface PagoRequest {
  metodoPagoId: number;
  monto: number;
  referencia?: string;
}

export interface VentaRequest {
  clienteId?: number;
  items: VentaItemRequest[];
  pagos?: PagoRequest[];
}

export interface VentaLinea {
  id: number;
  productoId: number;
  producto: string;
  presentacionId: number;
  cantidad: number;
  factorConversion: number;
  precioUnitario: number;
  descuento: number;
  subtotal: number;
  precioModificado: boolean;
  motivoPrecio: string | null;
  lotes: { loteId: number; cantidadBase: number }[];
}

export interface VentaPago {
  metodoPagoId: number;
  metodoPago: string;
  monto: number;
  referencia: string | null;
}

/** Nota de venta (POST /ventas, GET /ventas/{id}). */
export interface Venta {
  id: number;
  numeroVenta: string;
  estado: EstadoVenta;
  fechaVenta: string;
  turnoId: number;
  usuarioId: number;
  depositoId: number;
  cliente: { id: number; nombre: string } | null;
  subtotal: number;
  descuentoTotal: number;
  total: number;
  efectivoRecibido: number | null;
  vuelto: number | null;
  lineas: VentaLinea[];
  pagos: VentaPago[];
  anulacion: { anuladaPor: string | number; fecha: string; motivo: string } | null;
}

/** Fila de GET /ventas. */
export interface VentaResumen {
  id: number;
  numeroVenta: string;
  estado: EstadoVenta;
  fechaVenta: string;
  turnoId: number;
  usuarioId: number;
  clienteId: number | null;
  total: number;
}

export interface VentaFilters extends PageParams {
  numero?: string;
  turnoId?: number;
  clienteId?: number;
  usuarioId?: number;
  estado?: EstadoVenta;
  desde?: string;
  hasta?: string;
}

/** Una venta pagada solo con crédito de devolución se muestra con este nombre. */
export const nombreMetodo = (n: string): string => (n === 'CREDITO_DEVOLUCION' ? 'Crédito por devolución' : n);

/** Redondeo a 2 decimales (solo vista previa: el servidor es la fuente). */
export const round2 = (n: number): number => Math.round((n + Number.EPSILON) * 100) / 100;
