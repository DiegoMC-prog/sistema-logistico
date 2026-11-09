import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from './auth.service';

const PUBLIC_PATHS = ['/auth/login', '/auth/refresh'];

const withToken = (req: HttpRequest<unknown>, token: string | null) =>
  token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (PUBLIC_PATHS.some((p) => req.url.includes(p))) return next(req);

  return next(withToken(req, auth.accessToken())).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status !== 401 || !auth.accessToken()) return throwError(() => err);

      // Un 401 con token: intentar renovar y reintentar una vez; si falla, cerrar sesión.
      return auth.refresh().pipe(
        switchMap((t) => next(withToken(req, t.accessToken))),
        catchError(() => {
          auth.clear();
          router.navigate(['/login']);
          return throwError(() => err);
        }),
      );
    }),
  );
};
