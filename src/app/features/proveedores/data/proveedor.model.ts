import { Estado } from '../../../core/api/estado';
import { PageParams } from '../../../core/api/page.model';

export interface Proveedor {
  id: number;
  nombreEmpresa: string;
  nombreContacto: string;
  telefono: string | null;
  email: string | null;
  estado: Estado;
}

export interface ProveedorRequest {
  nombreEmpresa: string;
  nombreContacto: string;
  telefono?: string;
  email?: string;
}

export interface ProveedorFilters extends PageParams {
  estado?: Estado;
}
