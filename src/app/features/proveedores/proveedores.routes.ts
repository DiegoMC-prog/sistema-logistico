import { Routes } from '@angular/router';

export const PROVEEDORES_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Proveedores', subtitle: 'Empresas a las que les compras mercadería' },
    loadComponent: () => import('./pages/proveedores-page').then((m) => m.ProveedoresPage),
  },
];
