import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./core/features/auth/pages/login.component').then((m) => m.LoginComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layout/dashboard-layout/dashboard-layout.component').then((m) => m.DashboardLayoutComponent),
    children: [
      {
        path: 'categorias',
        loadComponent: () =>
          import('./features/categoria/pages/categoria-list/categoria-list').then((m) => m.CategoriaList),
      },
      {
        path: 'depositos',
        loadComponent: () =>
          import('./features/depositos/pages/deposito-list/deposito-list').then((m) => m.DepositoList),
      },
      {
        path: 'productos',
        loadComponent: () =>
          import('./features/productos/pages/producto-list/producto-list').then((m) => m.ProductoList),
      },
      {
        path: '',
        redirectTo: 'categorias',
        pathMatch: 'full',
      },
    ],
  },
  {
    path: '**',
    redirectTo: '',
  },
];
