import { Component, input, output, signal } from '@angular/core';
import { Icon, IconName } from './icon';

/**
 * Selector de filtro con icono. Las <option> van como contenido; la primera (value="") es "sin filtro".
 * Se resalta en verde cuando hay un valor elegido.
 *
 * <app-filter-select icon="tag" label="Categoría" (changed)="...">
 *   <option value="">Todas</option> ...
 * </app-filter-select>
 */
@Component({
  selector: 'app-filter-select',
  imports: [Icon],
  template: `
    <span class="relative block">
      <app-icon
        [name]="icon()"
        class="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 transition-colors"
        [class]="active() ? 'text-accent' : 'text-faint'"
      />
      <select
        class="field-input !h-10 pl-9"
        [class]="active() ? '!border-accent !bg-accent-soft/40' : ''"
        [attr.aria-label]="label()"
        (change)="onChange($any($event.target).value)"
      >
        <ng-content />
      </select>
    </span>
  `,
  host: { class: 'block' },
})
export class FilterSelect {
  readonly icon = input.required<IconName>();
  readonly label = input.required<string>();
  readonly changed = output<string>();
  protected readonly active = signal(false);

  protected onChange(value: string): void {
    this.active.set(value !== '');
    this.changed.emit(value);
  }
}
