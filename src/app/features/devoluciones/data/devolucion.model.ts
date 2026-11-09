import { PageParams } from '../../../core/api/page.model';
import { PagoRequest, VentaItemRequest } from '../../ventas/data/venta.model';

export interface DevolucionItemRequest {
  ventaDetalleId: number;
  cantidad: number;
  reingresaStock?: boolean;
}

export interface DevolucionRequest {
  motivo: string;
  reembolsar: boolean;
  metodoPagoId?: number;
  items: DevolucionItemRequest[];
}

export interface CambioRequest {
  motivo: string;
  devueltos: { ventaDetalleId: number; cantidad: number; reingresaStock?: boolean }[];
  nuevos: Pick<VentaItemRequest, 'presentacionId' | 'cantidad'>[];
  pagos?: PagoRequest[];
}

export interface DevolucionLinea {
  ventaDetalleId: number;
  loteId: number;
  cantidadBase: number;
  reingresaStock: boolean;
}

export interface Devolucion {
  id: number;
  numeroDevolucion: string;
  ventaId: number;
  usuarioId: number;
  turnoId: number;
  fecha: string;
  motivo: string;
  montoReembolsado: number;
  /** En los listados viene sin nombre de método ni venta nueva. */
  metodoPago?: { id: number; nombre: string } | null;
  ventaCambio?: { id: number; numeroVenta: string; total: number } | null;
  lineas?: DevolucionLinea[];
}

export interface CambioResponse {
  devolucion: Devolucion;
  creditoAplicado: number;
  diferenciaCobrada: number;
  saldoEntregadoEnEfectivo: number;
}

export interface DevolucionFilters extends PageParams {
  ventaId?: number;
  usuarioId?: number;
  turnoId?: number;
  desde?: string;
  hasta?: string;
}
