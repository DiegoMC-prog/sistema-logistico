import { Estado } from '../../../core/api/estado';
import { PageParams } from '../../../core/api/page.model';

export interface Caja {
  id: number;
  nombre: string;
  depositoId: number;
  estado: Estado;
}

export interface CajaRequest {
  nombre: string;
  depositoId: number;
}

export interface CajaFilters extends PageParams {
  depositoId?: number;
  estado?: Estado;
}
