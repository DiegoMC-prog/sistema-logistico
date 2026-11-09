import { Estado } from '../../../core/api/estado';
import { PageParams } from '../../../core/api/page.model';

export interface Cliente {
  id: number;
  tipoCliente: { id: number; nombre: string };
  documento: string | null;
  nombre: string;
  telefono: string | null;
  email: string | null;
  estado: Estado;
  creditoHabilitado: boolean;
  limiteCredito: number;
}

export interface ClienteRequest {
  tipoClienteId?: number;
  documento?: string;
  nombre: string;
  telefono?: string;
  email?: string;
  creditoHabilitado?: boolean;
  limiteCredito?: number;
}

export interface ClienteFilters extends PageParams {
  tipoClienteId?: number;
  estado?: Estado;
}
