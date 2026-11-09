import { Routes } from '@angular/router';

export const USUARIOS_ROUTES: Routes = [
  {
    path: '',
    data: { title: 'Usuarios', subtitle: 'Cuentas de acceso, roles y estado' },
    loadComponent: () => import('./pages/usuarios-page').then((m) => m.UsuariosPage),
  },
];
