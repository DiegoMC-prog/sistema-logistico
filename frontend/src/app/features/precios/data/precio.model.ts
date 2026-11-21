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
  /** Precio a cobrar: incluye el recargo por escasez, si hay. */
  precio: number;
  /** Precio de la jerarquía, sin recargo. */
  precioBase: number;
  /** Recargo por escasez aplicado (porcentaje en fracción: 0.15 = +15 %); null si no hay. */
  recargo: { id: number; alcance: 'PRODUCTO' | 'CATEGORIA'; porcentaje: number; motivo: string } | null;
  [k: string]: unknown;
}
