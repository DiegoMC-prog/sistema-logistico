import { Routes } from '@angular/router';

export const VENTAS_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Ventas', subtitle: 'Historial de ventas y notas' },
    loadComponent: () => import('./pages/ventas-page').then((m) => m.VentasPage),
  },
  {
    path: ':id',
    data: { title: 'Detalle de venta', subtitle: 'Nota de venta, anulación, devoluciones y cambios' },
    loadComponent: () => import('./pages/venta-detalle-page').then((m) => m.VentaDetallePage),
  },
];

export const POS_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Punto de venta', subtitle: 'Escanea, cobra y entrega la nota' },
    loadComponent: () => import('./pages/pos-page').then((m) => m.PosPage),
  },
];
