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
}

export interface LoteFilters extends PageParams {
  productoId?: number;
  venceDesde?: string;
  venceHasta?: string;
}
