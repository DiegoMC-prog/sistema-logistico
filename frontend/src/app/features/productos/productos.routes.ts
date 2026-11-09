import { Routes } from '@angular/router';

export const PRODUCTOS_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Productos', subtitle: 'Identificación, clasificación y disponibilidad del catálogo' },
    loadComponent: () => import('./pages/productos-page').then((m) => m.ProductosPage),
  },
];
