import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../auth/services/auth.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Si existe un token activo y sesión válida, permite el acceso
  if (authService.isAuthenticated()) {
    return true;
  }

  // Si no está autenticado, redirige al login y bloquea la navegación
  return router.createUrlTree(['/login']);
};