import { PageParams } from '../../../core/api/page.model';

export type EstadoTurno = 'ABIERTO' | 'CERRADO';

/** Fila de GET /turnos. */
export interface TurnoResumen {
  id: number;
  cajaId: number;
  cajaNombre: string;
  usuarioId: number;
  estado: EstadoTurno;
  fechaApertura: string;
  montoApertura: number;
  fechaCierre: string | null;
  montoCierreDeclarado: number | null;
  montoCierreEsperado: number | null;
}

export interface CobroMetodo {
  metodoPagoId: number;
  metodoPago: string;
  total: number;
}

/** GET /turnos/{id}, también al abrir y cerrar. */
export interface Turno {
  id: number;
  cajaId: number;
  cajaNombre: string;
  usuarioId: number;
  estado: EstadoTurno;
  fechaApertura: string;
  montoApertura: number;
  ingresos: number;
  egresos: number;
  cobrosPorMetodo: CobroMetodo[];
  efectivoCobrado: number;
  reembolsosEfectivo: number;
  /** Abonos de deudas de crédito cobrados en efectivo durante el turno. */
  abonosEfectivo: number;
  efectivoEsperado: number;
  fechaCierre: string | null;
  montoCierreDeclarado: number | null;
  diferencia: number | null;
}

export type TipoMovimiento = 'INGRESO' | 'EGRESO';

export interface MovimientoCaja {
  id: number;
  tipo: TipoMovimiento;
  monto: number;
  motivo: string;
  fecha?: string;
  usuarioId?: number;
}

export interface MovimientoRequest {
  tipo: TipoMovimiento;
  monto: number;
  motivo: string;
}

export interface TurnoFilters extends PageParams {
  cajaId?: number;
  usuarioId?: number;
  estado?: EstadoTurno;
  desde?: string;
  hasta?: string;
}
