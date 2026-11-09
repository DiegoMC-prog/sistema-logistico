import { Component, inject } from '@angular/core';
import { Toast, ToastService, ToastTone } from '../../core/ui/toast';
import { Icon, IconName } from './icon';

const ESTILOS: Record<ToastTone, { barra: string; icono: string; fondo: string; nombre: IconName }> = {
  success: { barra: 'bg-success', icono: 'text-success', fondo: 'bg-success-soft', nombre: 'check-circle' },
  error: { barra: 'bg-danger', icono: 'text-danger', fondo: 'bg-danger-soft', nombre: 'alert' },
  warning: { barra: 'bg-warning', icono: 'text-warning-dark', fondo: 'bg-warning-soft', nombre: 'alert' },
  info: { barra: 'bg-info', icono: 'text-info', fondo: 'bg-info-soft', nombre: 'help' },
};

/** Pila de notificaciones en la esquina superior derecha. Va una sola vez, en el componente raíz. */
@Component({
  selector: 'app-toast-container',
  imports: [Icon],
  template: `
    <div class="pointer-events-none fixed right-4 top-4 z-[100] flex w-[calc(100vw-2rem)] max-w-sm flex-col gap-3 sm:right-6 sm:top-6">
      @for (t of toasts(); track t.id) {
        <div
          class="animate-toast-in pointer-events-auto relative flex gap-3 overflow-hidden rounded-xl border border-border bg-surface p-4 pr-10 shadow-xl"
          [attr.role]="t.tone === 'error' ? 'alert' : 'status'"
          [attr.aria-live]="t.tone === 'error' ? 'assertive' : 'polite'"
          (mouseenter)="toasts_.pausar(t.id)"
          (mouseleave)="toasts_.reanudar(t.id)"
        >
          <span class="absolute inset-y-0 left-0 w-1" [class]="estilo(t).barra"></span>
          <span class="grid size-8 shrink-0 place-items-center rounded-full" [class]="estilo(t).fondo + ' ' + estilo(t).icono">
            <app-icon [name]="estilo(t).nombre" class="size-4" />
          </span>
          <div class="min-w-0">
            <p class="text-sm font-semibold text-ink">{{ t.title }}</p>
            @if (t.message) {
              <p class="mt-0.5 break-words text-sm text-muted">{{ t.message }}</p>
            }
          </div>
          <button
            type="button"
            class="absolute right-2 top-2 grid size-6 place-items-center rounded-md text-faint transition hover:bg-subtle hover:text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent"
            aria-label="Cerrar notificación"
            (click)="toasts_.dismiss(t.id)"
          >
            <app-icon name="close" class="size-3.5" />
          </button>
        </div>
      }
    </div>
  `,
})
export class ToastContainer {
  protected readonly toasts_ = inject(ToastService);
  protected readonly toasts = this.toasts_.toasts;
  protected estilo(t: Toast) {
    return ESTILOS[t.tone];
  }
}
