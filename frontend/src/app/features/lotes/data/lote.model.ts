import { PageParams } from '../../../core/api/page.model';

export interface Lote {
  id: number;
  producto: { id: number; nombre: string };
  numeroLote: string;
  fechaVencimiento: string | null;
  costoUnitarioCompra: number;
  porcentajeDescuentoVencimiento: number | null;
}

export interface LoteRequest {
  numeroLote: string;
  fechaVencimiento?: string;
  costoUnitarioCompra: number;
  porcentajeDescuentoVencimiento?: number;
  /** R10: obligatorio si cambia el costo; queda en la nota de ajuste auditada. */
  motivoAjusteCosto?: string;
}

/** R10: corrección del costo de un lote. */
export interface AjusteCostoLote {
  id: number;
  loteId: number;
  costoAnterior: number;
  costoNuevo: number;
  motivo: string;
  usuario: string;
  fecha: string;
}

export interface LoteFilters extends PageParams {
  productoId?: number;
  venceDesde?: string;
  venceHasta?: string;
}
