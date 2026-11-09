import { Component, computed, input, output } from '@angular/core';

@Component({
  selector: 'app-paginator',
  template: `
    <div
      class="flex flex-col items-center justify-between gap-3 border-t border-border px-5 py-4 text-sm text-muted sm:flex-row"
    >
      <p>
        Mostrando <span class="font-semibold text-ink">{{ from() }}–{{ to() }}</span> de
        <span class="font-semibold text-ink">{{ totalElements() }}</span> registros
      </p>
      <div class="flex items-center gap-1">
        <button type="button" class="btn-page px-3" [disabled]="page() <= 0" (click)="pageChange.emit(page() - 1)">
          Anterior
        </button>
        @for (p of pageNumbers(); track p) {
          <button
            type="button"
            class="btn-page size-10"
            [class]="p === page() ? '!bg-brand !text-white' : ''"
            [attr.aria-current]="p === page() ? 'page' : null"
            (click)="pageChange.emit(p)"
          >
            {{ p + 1 }}
          </button>
        }
        <button
          type="button"
          class="btn-page px-3"
          [disabled]="page() + 1 >= totalPages()"
          (click)="pageChange.emit(page() + 1)"
        >
          Siguiente
        </button>
      </div>
    </div>
  `,
  styles: `
    .btn-page {
      height: 2.5rem;
      border-radius: 0.5rem;
      font-weight: 600;
      color: var(--color-muted);
      background: transparent;
      transition: background 0.15s;
    }
    .btn-page:hover:not(:disabled) {
      background: var(--color-subtle);
      color: var(--color-ink);
    }
    .btn-page:disabled {
      opacity: 0.4;
      cursor: not-allowed;
    }
  `,
})
export class Paginator {
  /** Índice de página base 0, igual que el backend. */
  readonly page = input.required<number>();
  readonly size = input.required<number>();
  readonly totalPages = input.required<number>();
  readonly totalElements = input.required<number>();
  readonly pageChange = output<number>();

  protected readonly from = computed(() => (this.totalElements() === 0 ? 0 : this.page() * this.size() + 1));
  protected readonly to = computed(() => Math.min((this.page() + 1) * this.size(), this.totalElements()));
  protected readonly pageNumbers = computed(() => {
    const total = this.totalPages();
    const start = Math.max(0, Math.min(this.page() - 2, total - 5));
    return Array.from({ length: Math.min(5, total) }, (_, i) => start + i);
  });
}
