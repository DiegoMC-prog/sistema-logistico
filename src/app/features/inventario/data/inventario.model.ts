import { PageParams } from '../../../core/api/page.model';
export interface Resumen {
  id: number;
  nombre: string;
}

export interface Stock {
  id: number;
  deposito: Resumen;
  producto: Resumen;
  lote: { id: number; numeroLote: string };
  stockActual: number;
  unidadBase: string;
}

export interface StockFilters extends PageParams {
  depositoId?: number;
  productoId?: number;
  loteId?: number;
  conStock?: boolean;
  venceHasta?: string;
}

export type TipoMovimientoInv = 'ENTRADA' | 'SALIDA' | 'TRASLADO' | 'AJUSTE_POSITIVO' | 'AJUSTE_NEGATIVO';

export interface MovimientoInv {
  id: number;
  tipo: TipoMovimientoInv;
  fecha: string;
  depositoOrigen: Resumen | null;
  depositoDestino: Resumen | null;
  usuario: Resumen | null;
  observacion: string | null;
  referencia: { tipo: string; id: number } | null;
  items: { producto: Resumen; lote: { id: number; numeroLote: string }; cantidad: number }[];
}

export interface MovimientoFilters extends PageParams {
  tipo?: TipoMovimientoInv;
  depositoId?: number;
  productoId?: number;
  loteId?: number;
  desde?: string;
  hasta?: string;
}

export interface NuevoLoteRequest {
  numeroLote?: string;
  fechaVencimiento?: string;
  costoUnitarioCompra: number;
  porcentajeDescuentoVencimiento?: number;
}

export interface ItemEntradaRequest {
  productoId?: number;
  loteId?: number;
  lote?: NuevoLoteRequest;
  presentacionId?: number;
  cantidad: number;
}

export interface EntradaRequest {
  depositoId: number;
  observacion?: string;
  items: ItemEntradaRequest[];
}

export interface ItemMovimientoRequest {
  loteId: number;
  presentacionId?: number;
  cantidad: number;
}

export interface SalidaRequest {
  depositoId: number;
  motivo: string;
  items: ItemMovimientoRequest[];
}

export interface TrasladoRequest {
  depositoOrigenId: number;
  depositoDestinoId: number;
  observacion?: string;
  items: ItemMovimientoRequest[];
}

export interface AjusteRequest {
  depositoId: number;
  loteId: number;
  stockReal: number;
  motivo: string;
}

export const TIPOS_MOVIMIENTO: { value: TipoMovimientoInv; label: string }[] = [
  { value: 'ENTRADA', label: 'Entrada' },
  { value: 'SALIDA', label: 'Salida' },
  { value: 'TRASLADO', label: 'Traslado' },
  { value: 'AJUSTE_POSITIVO', label: 'Ajuste (+)' },
  { value: 'AJUSTE_NEGATIVO', label: 'Ajuste (−)' },
];

export const etiquetaMovimiento = (t: string): string => TIPOS_MOVIMIENTO.find((x) => x.value === t)?.label ?? t;

export const toneMovimiento = (t: string): 'success' | 'danger' | 'info' | 'warning' =>
  t === 'ENTRADA' || t === 'AJUSTE_POSITIVO' ? 'success' : t === 'TRASLADO' ? 'info' : t === 'SALIDA' ? 'danger' : 'warning';
