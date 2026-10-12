export interface Deposito {
  id: number;
  nombre: string;
  descripcion?: string;
  estado: boolean;
}

export interface DepositoRequest {
  nombre: string;
  descripcion?: string;
  estado?: boolean;
}

export interface DepositoStats {
  totalDepositos: number;
  activos: number;
  inactivos: number;
}
