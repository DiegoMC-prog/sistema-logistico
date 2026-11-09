import { Routes } from '@angular/router';

export const CLIENTES_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Clientes', subtitle: 'Administra tu cartera de clientes' },
    loadComponent: () => import('./pages/clientes-page').then((m) => m.ClientesPage),
  },
];
