import { inject } from '@angular/core';
import { CanActivateFn, Router, Routes } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { familiaPorId } from './data/reportes.config';

/** Cada familia de reportes (ventas, caja, inventario, utilidad, compras) exige su propio permiso. */
const familiaGuard: CanActivateFn = (route) => {
  const familia = familiaPorId(route.paramMap.get('familia'));
  return familia && inject(AuthService).hasPermission(familia.permiso) ? true : inject(Router).createUrlTree(['/']);
};

const data = { title: 'Reportes', subtitle: 'Informes con rango de fechas y exportación a CSV' };

export const REPORTES_ROUTES: Routes = [
  { path: ':familia', canActivate: [familiaGuard], data, loadComponent: () => import('./pages/reportes-page').then((m) => m.ReportesPage) },
  { path: ':familia/:reporte', canActivate: [familiaGuard], data, loadComponent: () => import('./pages/reportes-page').then((m) => m.ReportesPage) },
];
