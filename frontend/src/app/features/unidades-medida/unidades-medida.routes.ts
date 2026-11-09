import { Routes } from '@angular/router';

export const UNIDADES_MEDIDA_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Unidades de medida', subtitle: 'Unidades en las que se presentan tus productos' },
    loadComponent: () => import('./pages/unidades-medida-page').then((m) => m.UnidadesMedidaPage),
  },
];
