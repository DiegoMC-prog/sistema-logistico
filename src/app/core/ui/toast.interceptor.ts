import { HttpErrorResponse, HttpEvent, HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, tap, throwError } from 'rxjs';
import { errorMessage } from '../api/api-error';
import { TOAST_MESSAGE, ToastService } from './toast';

const EXITO_POR_METODO: Record<string, string> = {
  POST: 'Operación realizada correctamente',
  PUT: 'Cambios guardados correctamente',
  PATCH: 'Estado actualizado correctamente',
  DELETE: 'Eliminado correctamente',
};

/**
 * Avisos globales: éxito en las operaciones que modifican datos y errores que la pantalla no suele pintar
 * (sin conexión, sin permiso, fallos del servidor). Los 400/404/409 los muestra cada formulario junto al campo.
 * Las rutas /auth/** tienen su propio manejo en cada pantalla.
 */
export const toastInterceptor: HttpInterceptorFn = (req, next) => {
  const toast = inject(ToastService);
  if (req.url.includes('/auth/')) return next(req);

  const exito = req.context.get(TOAST_MESSAGE) ?? EXITO_POR_METODO[req.method];

  return next(req).pipe(
    tap((event: HttpEvent<unknown>) => {
      if (event instanceof HttpResponse && exito) toast.success(exito);
    }),
    catchError((err: unknown) => {
      if (err instanceof HttpErrorResponse) {
        if (err.status === 0) toast.error('No se pudo conectar con el servidor', { title: 'Sin conexión' });
        else if (err.status === 403) toast.warning('No tienes permiso para realizar esta acción', { title: 'Sin permiso' });
        else if (err.status === 429) toast.warning(errorMessage(err, 'Demasiadas solicitudes: intenta más tarde'), { title: 'Demasiadas solicitudes' });
        else if (err.status >= 500) toast.error(errorMessage(err, 'Error del servidor'), { title: 'Error del servidor' });
      }
      return throwError(() => err);
    }),
  );
};
