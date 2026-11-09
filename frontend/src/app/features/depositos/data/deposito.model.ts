import { Estado } from '../../../core/api/estado';
import { PageParams } from '../../../core/api/page.model';

export interface Deposito {
  id: number;
  nombre: string;
  descripcion: string | null;
  estado: Estado;
}

export interface DepositoRequest {
  nombre: string;
  descripcion?: string;
}

export interface DepositoFilters extends PageParams {
  estado?: Estado;
}
