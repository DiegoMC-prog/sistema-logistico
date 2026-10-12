import { Injectable, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiErrorResponse, ErrorCampo } from '../models/api-error.model';
import { ToastService } from './toast.service';

@Injectable({
  providedIn: 'root',
})
export class ErrorHandlerService {
  private readonly toastService = inject(ToastService);

  /**
   * Obtiene la respuesta estructurada de la API de Spring Boot si existe.
   */
  obtenerApiError(error: unknown): ApiErrorResponse | null {
    if (error instanceof HttpErrorResponse && error.error && typeof error.error === 'object') {
      const err = error.error as Partial<ApiErrorResponse>;
      if ('mensaje' in err || 'error' in err) {
        return err as ApiErrorResponse;
      }
    }
    return null;
  }

  /**
   * Extrae el mensaje principal del error (ej. "Recurso no encontrado", o el mensaje de negocio).
   */
  extraerMensaje(error: unknown, fallback = 'Ha ocurrido un error inesperado'): string {
    const apiError = this.obtenerApiError(error);
    if (apiError) {
      if (apiError.mensaje) return apiError.mensaje;
      if (apiError.error) return apiError.error;
    }

    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return 'No se pudo conectar con el servidor. Verifica tu conexión o el backend.';
      }
      if (error.status === 404) return 'El recurso solicitado no fue encontrado.';
      if (error.status === 403) return 'No tienes permisos para realizar esta acción.';
      if (error.status >= 500) return 'Ocurrió un error interno en el servidor.';
      if (typeof error.error === 'string' && error.error.trim()) return error.error;
    }

    if (error instanceof Error) {
      return error.message;
    }

    return fallback;
  }

  /**
   * Extrae el mapa de errores por campo para formularios { [campo]: mensaje }.
   * Útil para MethodArgumentNotValidException con "detalles": [{ campo, mensaje }].
   */
  extraerErroresCampos(error: unknown): Record<string, string> {
    const apiError = this.obtenerApiError(error);
    const resultado: Record<string, string> = {};

    if (apiError?.detalles && Array.isArray(apiError.detalles)) {
      for (const item of apiError.detalles) {
        if (item.campo && item.mensaje) {
          resultado[item.campo] = item.mensaje;
        }
      }
    }

    return resultado;
  }

  /**
   * Muestra automáticamente una notificación Toast tipo error con el mensaje procesado.
   */
  notificarError(error: unknown, titulo = 'Error en la solicitud'): void {
    const mensaje = this.extraerMensaje(error);
    this.toastService.error(titulo, mensaje);
  }
}
