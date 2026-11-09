import { Estado } from '../../../core/api/estado';
import { PageParams } from '../../../core/api/page.model';

export interface TipoCliente {
  id: number;
  nombre: string;
  estado: Estado;
}

export interface TipoClienteRequest {
  nombre: string;
}

export interface TipoClienteFilters extends PageParams {
  estado?: Estado;
}

/** `GENERAL` es un registro de sistema: no se renombra, desactiva ni elimina. */
export const esTipoSistema = (t: { nombre: string }): boolean => t.nombre === 'GENERAL';
