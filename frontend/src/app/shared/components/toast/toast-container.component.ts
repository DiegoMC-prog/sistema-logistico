import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService, ToastTone } from '../../../core/services/toast.service';
import { IconComponent, IconName } from '../icon/icon.component';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule, IconComponent],
  template: `
    <div
      class="fixed top-5 right-5 z-[100] flex flex-col gap-2.5 max-w-sm w-full pointer-events-none"
      aria-live="polite"
    >
      @for (toast of toastService.toasts(); track toast.id) {
        <div
          class="pointer-events-auto flex items-start gap-3 p-4 rounded-xl border bg-surface shadow-2xl transition-all animate-row-in"
          [ngClass]="getCardClasses(toast.tone)"
        >
          <div
            class="grid size-8 shrink-0 place-items-center rounded-lg"
            [ngClass]="getIconBgClasses(toast.tone)"
          >
            <app-icon [name]="getIconName(toast.tone)" svgClass="size-4" />
          </div>

          <div class="flex-1 min-w-0 pt-0.5">
            <h4 class="text-sm font-semibold text-ink leading-tight">
              {{ toast.title }}
            </h4>
            @if (toast.message) {
              <p class="mt-1 text-xs text-muted leading-relaxed">
                {{ toast.message }}
              </p>
            }
          </div>

          <button
            type="button"
            (click)="toastService.dismiss(toast.id)"
            class="text-faint hover:text-ink transition-colors p-1 rounded-lg"
            aria-label="Cerrar notificación"
          >
            <app-icon name="plus" svgClass="size-3.5 rotate-45" />
          </button>
        </div>
      }
    </div>
  `,
})
export class ToastContainerComponent {
  readonly toastService = inject(ToastService);

  getIconName(tone: ToastTone): IconName {
    switch (tone) {
      case 'error':
        return 'alert';
      case 'warning':
        return 'alert';
      case 'info':
        return 'help';
      case 'success':
      default:
        return 'box';
    }
  }

  getCardClasses(tone: ToastTone): string {
    switch (tone) {
      case 'error':
        return 'border-danger/30';
      case 'warning':
        return 'border-warning/30';
      case 'info':
        return 'border-info/30';
      case 'success':
      default:
        return 'border-accent/30';
    }
  }

  getIconBgClasses(tone: ToastTone): string {
    switch (tone) {
      case 'error':
        return 'bg-danger-soft text-danger';
      case 'warning':
        return 'bg-warning-soft text-warning-dark';
      case 'info':
        return 'bg-info-soft text-info';
      case 'success':
      default:
        return 'bg-success-soft text-success';
    }
  }
}
