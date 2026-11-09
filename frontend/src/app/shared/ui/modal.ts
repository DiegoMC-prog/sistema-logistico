import { Component, computed, input, output } from '@angular/core';
import { IconBtn } from './button';

/**
 * Diálogo modal. El contenido va en el cuerpo; el pie con [modal-footer].
 * Se muestra con @if desde el padre y emite (closed) al pulsar fuera, la X o Escape.
 */
@Component({
  selector: 'app-modal',
  imports: [IconBtn],
  template: `
    <div
      class="fixed inset-0 z-50 flex animate-fade-in items-start justify-center overflow-y-auto bg-overlay p-4 py-8 backdrop-blur-[2px]"
      role="dialog"
      aria-modal="true"
      [attr.aria-label]="title()"
      (click)="closed.emit()"
      (keydown.escape)="closed.emit()"
    >
      <div
        class="relative z-10 w-full animate-pop-in rounded-xl border border-border bg-surface shadow-xl"
        [class]="width()"
        (click)="$event.stopPropagation()"
      >
        <div class="flex items-start justify-between border-b border-border p-5">
          <div>
            <h2 class="text-lg font-bold text-ink">{{ title() }}</h2>
            @if (description()) {
              <p class="mt-1 text-sm text-muted">{{ description() }}</p>
            }
          </div>
          <button appIconBtn icon="close" label="Cerrar" (click)="closed.emit()"></button>
        </div>
        <ng-content />
        <div class="flex justify-end gap-3 border-t border-border bg-table-head p-4 empty:hidden">
          <ng-content select="[modal-footer]" />
        </div>
      </div>
    </div>
  `,
})
export class Modal {
  readonly title = input.required<string>();
  readonly description = input<string>();
  readonly size = input<'md' | 'lg'>('md');
  readonly closed = output<void>();
  protected readonly width = computed(() => (this.size() === 'lg' ? 'max-w-modal' : 'max-w-lg'));
}
