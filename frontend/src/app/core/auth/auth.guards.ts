import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { NAV_ITEMS } from '../layout/nav';
import { AuthService } from './auth.service';

/** Exige sesión; carga el perfil si falta y fuerza el cambio de contraseña temporal. */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) return router.createUrlTree(['/login']);

  const check = () =>
    auth.passwordTemporal() && !state.url.startsWith('/cambiar-password')
      ? router.createUrlTree(['/cambiar-password'])
      : true;

  if (auth.perfil()) return check();

  return auth.loadPerfil().pipe(
    map(() => check()),
    catchError(() => {
      auth.clear();
      return of(router.createUrlTree(['/login']));
    }),
  );
};

/** Evita mostrar el login a quien ya tiene sesión. */
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.isAuthenticated() ? inject(Router).createUrlTree(['/']) : true;
};

/**
 * Exige un permiso para entrar a la ruta. Si falta, manda al inicio, que a su vez
 * redirige a la primera sección permitida (o a /sin-acceso).
 */
export const permissionGuard =
  (permiso: string | readonly string[]): CanActivateFn =>
  () => {
    const auth = inject(AuthService);
    return auth.hasAnyPermission(permiso) ? true : inject(Router).createUrlTree(['/']);
  };

/** Ruta raíz: lleva a la primera sección del menú a la que el usuario tiene acceso. */
export const homeGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const first = NAV_ITEMS.find((i) => auth.hasAnyPermission(i.permiso));
  return inject(Router).createUrlTree([first ? first.path : '/sin-acceso']);
};
