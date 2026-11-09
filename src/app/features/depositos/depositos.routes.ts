import { Routes } from '@angular/router';

export const DEPOSITOS_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Depósitos', subtitle: 'Administra las instalaciones y su estado operativo' },
    loadComponent: () => import('./pages/depositos-page').then((m) => m.DepositosPage),
  },
];
