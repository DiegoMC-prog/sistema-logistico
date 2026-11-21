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
  {
    path: 'costos',
    data: {
      title: 'Costos de proveedores',
      subtitle: 'Histórico de costos de compra y su variación por proveedor',
    },
    loadComponent: () => import('./pages/costos-page').then((m) => m.CostosPage),
  },
];
