export interface ErrorCampo {
  campo: string;
  mensaje: string;
}

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  mensaje: string;
  detalles?: ErrorCampo[];
}
