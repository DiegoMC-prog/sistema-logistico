import { Routes } from '@angular/router';

export const STOCK_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Stock', subtitle: 'Existencias por depósito, producto y lote' },
    loadComponent: () => import('./pages/stock-page').then((m) => m.StockPage),
  },
];

export const OPERACIONES_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Registrar movimiento', subtitle: 'Entradas, salidas, traslados y ajustes de inventario' },
    loadComponent: () => import('./pages/operaciones-page').then((m) => m.OperacionesPage),
  },
];

export const MOVIMIENTOS_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Movimientos de inventario', subtitle: 'Entradas, salidas, traslados y ajustes' },
    loadComponent: () => import('./pages/movimientos-page').then((m) => m.MovimientosPage),
  },
];
