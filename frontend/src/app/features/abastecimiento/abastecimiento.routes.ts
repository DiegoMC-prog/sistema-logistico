import { Routes } from '@angular/router';

export const ABASTECIMIENTO_ROUTES: Routes = [
  {
    path: '',
    data: {
      title: 'Sugerencias de reorden',
      subtitle: 'Lote económico (EOQ), stock de seguridad y punto de reorden por producto, en tres escenarios',
    },
    loadComponent: () => import('./pages/sugerencias-page').then((m) => m.SugerenciasPage),
  },
];
