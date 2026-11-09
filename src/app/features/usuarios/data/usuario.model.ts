import { Estado } from '../../../core/api/estado';
import { PageParams } from '../../../core/api/page.model';

export interface Usuario {
  id: number;
  username: string;
  nombre: string;
  email: string;
  estado: Estado;
  passwordTemporal: boolean;
  roles: { id: number; nombre: string }[];
}

export interface UsuarioRequest {
  username: string;
  nombre: string;
  email: string;
}

export interface EditarUsuarioRequest {
  nombre: string;
  email: string;
}

export interface UsuarioFilters extends PageParams {
  estado?: Estado;
  rolId?: number;
}
