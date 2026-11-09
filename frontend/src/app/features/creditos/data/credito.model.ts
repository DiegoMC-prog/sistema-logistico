export type EstadoCuenta = 'PENDIENTE' | 'PAGADA' | 'ANULADA';

export interface Deudor {
  clienteId: number;
  clienteNombre: string;
  documento: string | null;
  limiteCredito: number;
  deudaTotal: number;
  cuentasPendientes: number;
}

export interface CuentaPorCobrar {
  id: number;
  ventaId: number;
  numeroVenta: string;
  montoOriginal: number;
  saldo: number;
  estado: EstadoCuenta;
  fecha: string;
}

export interface AplicacionAbono {
  cuentaId: number;
  numeroVenta: string;
  monto: number;
}

export interface Abono {
  id: number;
  numeroAbono: string;
  clienteId: number;
  clienteNombre: string;
  turnoId: number;
  usuario: string;
  metodoPago: string;
  monto: number;
  referencia: string | null;
  fecha: string;
  aplicaciones: AplicacionAbono[];
}

/** GET /clientes/{id}/estado-cuenta */
export interface EstadoCuentaCliente {
  clienteId: number;
  clienteNombre: string;
  creditoHabilitado: boolean;
  limiteCredito: number;
  deudaTotal: number;
  disponible: number;
  cuentas: CuentaPorCobrar[];
  abonos: Abono[];
}

export interface AbonoRequest {
  metodoPagoId: number;
  monto: number;
  referencia?: string;
}
