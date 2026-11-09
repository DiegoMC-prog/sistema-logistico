import { Routes } from '@angular/router';

export const TIPOS_CLIENTE_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Tipos de cliente', subtitle: 'Segmenta a tus clientes para aplicar precios distintos' },
    loadComponent: () => import('./pages/tipos-cliente-page').then((m) => m.TiposClientePage),
  },
];
