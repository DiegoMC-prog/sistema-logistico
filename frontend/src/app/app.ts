import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeService } from './core/layout/theme.service';
import { ToastContainer } from './shared/ui/toast-container';

@Component({
  imports: [RouterOutlet, ToastContainer],
  selector: 'app-root',
  templateUrl: './app.html',
})
export class App {
  // Instanciarlo aquí aplica el tema guardado también en el login.
  protected readonly theme = inject(ThemeService);
}
