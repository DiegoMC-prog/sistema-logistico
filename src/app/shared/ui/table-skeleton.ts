import { Component, input } from '@angular/core';

/** Esqueleto de carga para tablas: filas con barras pulsantes. */
@Component({
  selector: 'app-table-skeleton',
  template: `
    <div class="divide-y divide-border" role="status" aria-label="Cargando">
      @for (row of rows(); track row) {
        <div class="flex animate-pulse items-center gap-4 px-5 py-4">
          <div class="size-10 shrink-0 rounded-lg bg-subtle"></div>
          <div class="flex-1 space-y-2">
            <div class="h-3 w-1/3 rounded bg-subtle"></div>
            <div class="h-2.5 w-1/2 rounded bg-subtle"></div>
          </div>
          <div class="hidden h-6 w-20 rounded-full bg-subtle sm:block"></div>
          <div class="h-6 w-24 rounded-full bg-subtle"></div>
        </div>
      }
    </div>
  `,
})
export class TableSkeleton {
  readonly count = input(5);
  protected rows = () => Array.from({ length: this.count() }, (_, i) => i);
}
