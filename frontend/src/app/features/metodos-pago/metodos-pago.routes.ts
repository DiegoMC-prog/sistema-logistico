import { Routes } from '@angular/router';

export const METODOS_PAGO_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Métodos de pago', subtitle: 'Formas de cobro disponibles en el punto de venta' },
    loadComponent: () => import('./pages/metodos-pago-page').then((m) => m.MetodosPagoPage),
  },
];
