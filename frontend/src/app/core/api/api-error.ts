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

/** Código de negocio que el backend agrega a algunos errores (propiedad `code` del ProblemDetail). */
export function errorCode(err: unknown): string | undefined {
  if (err instanceof HttpErrorResponse && err.error && typeof err.error === 'object') {
    const code = (err.error as { code?: unknown }).code;
    return typeof code === 'string' ? code : undefined;
  }
  return undefined;
}

/** R08: la operación supera el monto umbral y hacen falta las credenciales de un administrador. */
export const AUTORIZACION_REQUERIDA = 'AUTORIZACION_REQUERIDA';
/** R08: las credenciales del administrador enviadas no sirven. */
export const AUTORIZACION_INVALIDA = 'AUTORIZACION_INVALIDA';

export function esErrorAutorizacion(err: unknown): boolean {
  const code = errorCode(err);
  return code === AUTORIZACION_REQUERIDA || code === AUTORIZACION_INVALIDA;
}
