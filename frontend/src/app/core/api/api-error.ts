import { HttpErrorResponse } from '@angular/common/http';

/** Extrae un mensaje legible de un error HTTP (el formato del backend no está en el spec). */
export function errorMessage(err: unknown, fallback = 'Ocurrió un error inesperado'): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) return 'No se pudo conectar con el servidor';
    const body = err.error;
    if (typeof body === 'string' && body) return body;
    if (body && typeof body === 'object') {
      return body.message ?? body.mensaje ?? body.error ?? body.detail ?? fallback;
    }
    if (err.status === 403) return 'No tienes permiso para realizar esta acción';
  }
  return fallback;
}
