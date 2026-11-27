import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { PreloadAllModules, provideRouter, withPreloading } from '@angular/router';
import { routes } from './app.routes';
import { authInterceptor } from './core/auth/auth.interceptor';
import { toastInterceptor } from './core/ui/toast.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    // Las pantallas se descargan en segundo plano tras la carga inicial: al navegar ya estan listas
    provideRouter(routes, withPreloading(PreloadAllModules)),
    provideHttpClient(withInterceptors([toastInterceptor, authInterceptor])),
  ],
};
