import { Routes } from '@angular/router';

export const RECARGOS_ROUTES: Routes = [
  {
    path: '',
    data: {
      title: 'Recargos por escasez',
      subtitle: 'Porcentaje extra sobre el precio de venta de un producto o de una categoría, con registro de quién lo activó',
    },
    loadComponent: () => import('./pages/recargos-page').then((m) => m.RecargosPage),
  },
];
