import { Component, input, output } from '@angular/core';
import { Btn } from './button';
import { Icon, IconName } from './icon';

@Component({
  selector: 'app-confirm-dialog',
  imports: [Btn, Icon],
  template: `
    <div
      class="fixed inset-0 z-50 grid animate-fade-in place-items-center bg-overlay p-4 backdrop-blur-[2px]"
      role="alertdialog"
      aria-modal="true"
      (click)="cancelled.emit()"
      (keydown.escape)="cancelled.emit()"
    >
      <div
        class="w-full max-w-md animate-pop-in rounded-xl border border-border bg-surface p-6 shadow-xl"
        (click)="$event.stopPropagation()"
      >
        <div class="grid size-12 place-items-center rounded-full bg-danger-soft text-danger">
          <app-icon [name]="icon()" />
        </div>
        <h2 class="mt-4 text-lg font-bold text-ink">{{ title() }}</h2>
        <p class="mt-2 text-sm leading-6 text-muted">{{ message() }}</p>
        @if (error()) {
          <p class="alert-error mt-4">{{ error() }}</p>
        }
        <div class="mt-6 flex justify-end gap-3">
          <button appBtn variant="secondary" (click)="cancelled.emit()" [disabled]="busy()">Cancelar</button>
          <button appBtn variant="danger" [icon]="icon()" (click)="confirmed.emit()" [disabled]="busy()">
            {{ confirmLabel() }}
          </button>
        </div>
      </div>
    </div>
  `,
})
export class ConfirmDialog {
  readonly title = input.required<string>();
  readonly message = input.required<string>();
  readonly confirmLabel = input('Eliminar');
  readonly icon = input<IconName>('trash');
  readonly busy = input(false);
  readonly error = input<string | null>(null);
  readonly confirmed = output<void>();
  readonly cancelled = output<void>();
}
