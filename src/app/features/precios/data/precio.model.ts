import { Estado } from '../../../core/api/estado';
import { PageParams } from '../../../core/api/page.model';

export interface Precio {
  id: number;
  presentacionId: number;
  tipoCliente: { id: number; nombre: string } | null;
  cantidadMinima: number;
  precio: number;
  estado: Estado;
}

export interface PrecioRequest {
  tipoClienteId?: number;
  cantidadMinima?: number;
  precio: number;
}

export interface PrecioFilters extends PageParams {
  tipoClienteId?: number;
  soloGenerales?: boolean;
  estado?: Estado;
}

export interface PrecioAplicable {
  precio: number;
  [k: string]: unknown;
}
