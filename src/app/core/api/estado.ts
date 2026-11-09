import { Observable, forkJoin, map } from 'rxjs';
import { Page } from './page.model';

export type Estado = 'ACTIVO' | 'INACTIVO';

export const isActivo = (estado: string | null | undefined): boolean => estado === 'ACTIVO';

export interface EstadoCounts {
  total: number;
  activos: number;
  inactivos: number;
}

/** Totales reales para las tarjetas de resumen: 3 consultas de tamaño 1 filtradas por estado. */
export function estadoCounts(
  list: (f: { size: number; estado?: Estado }) => Observable<Page<unknown>>,
): Observable<EstadoCounts> {
  return forkJoin({
    total: list({ size: 1 }),
    activos: list({ size: 1, estado: 'ACTIVO' }),
    inactivos: list({ size: 1, estado: 'INACTIVO' }),
  }).pipe(
    map((r) => ({
      total: r.total.totalElements,
      activos: r.activos.totalElements,
      inactivos: r.inactivos.totalElements,
    })),
  );
}
