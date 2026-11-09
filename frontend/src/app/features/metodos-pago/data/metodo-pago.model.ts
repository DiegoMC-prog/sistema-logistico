import { Estado } from '../../../core/api/estado';
import { PageParams } from '../../../core/api/page.model';

export interface MetodoPago {
  id: number;
  nombre: string;
  estado: Estado;
  paraCobro: boolean;
}

export interface MetodoPagoRequest {
  nombre: string;
}

export interface MetodoPagoFilters extends PageParams {
  estado?: Estado;
}

/** `EFECTIVO`, `CREDITO` (fiado) y `CREDITO_DEVOLUCION` son de sistema. */
export const esMetodoSistema = (m: { nombre: string }): boolean =>
  m.nombre === 'EFECTIVO' || m.nombre === 'CREDITO' || m.nombre === 'CREDITO_DEVOLUCION';

/** Métodos que se pueden ofrecer al cobrar o reembolsar. */
export const esUtilizable = (m: MetodoPago): boolean => m.paraCobro && m.estado === 'ACTIVO';
