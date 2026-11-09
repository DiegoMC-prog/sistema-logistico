import { Routes } from '@angular/router';

export const ROLES_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Roles y permisos', subtitle: 'Define qué puede hacer cada rol en el sistema' },
    loadComponent: () => import('./pages/roles-page').then((m) => m.RolesPage),
  },
];
