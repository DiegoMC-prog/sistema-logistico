import { Routes } from '@angular/router';

export const TURNOS_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Turnos de caja', subtitle: 'Apertura, movimientos y cierre de caja' },
    loadComponent: () => import('./pages/turnos-page').then((m) => m.TurnosPage),
  },
  {
    path: ':id',
    data: { title: 'Detalle del turno', subtitle: 'Resumen de caja, movimientos y cuadre' },
    loadComponent: () => import('./pages/turno-detalle-page').then((m) => m.TurnoDetallePage),
  },
];
