import { Component, input } from '@angular/core';
import { Icon } from './icon';

@Component({
  selector: 'app-empty-state',
  imports: [Icon],
  template: `
    <div class="grid size-12 place-items-center rounded-full bg-subtle text-muted"><app-icon name="search" /></div>
    <p class="mt-3 font-semibold text-ink">{{ title() }}</p>
    <p class="mt-1 text-sm text-muted">{{ message() }}</p>
  `,
  host: { class: 'flex flex-col items-center justify-center px-5 py-16 text-center' },
})
export class EmptyState {
  readonly title = input('Sin resultados');
  readonly message = input('Prueba con otra búsqueda o crea un registro nuevo.');
}
