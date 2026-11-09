import { Routes } from '@angular/router';

export const CATEGORIAS_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Categorías', subtitle: 'Organiza y mantén la clasificación de tus productos' },
    loadComponent: () => import('./pages/categorias-page').then((m) => m.CategoriasPage),
  },
];
