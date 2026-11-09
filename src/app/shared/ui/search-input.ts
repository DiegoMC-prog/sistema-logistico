import { Component, OnDestroy, input, output } from '@angular/core';
import { Icon } from './icon';

/** Campo de búsqueda con debounce de 300 ms. */
@Component({
  selector: 'app-search-input',
  imports: [Icon],
  template: `
    <label class="relative block w-full">
      <app-icon name="search" class="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-faint" />
      <input
        class="field-input !h-10 pl-10"
        [placeholder]="placeholder()"
        (input)="onInput($any($event.target).value)"
      />
    </label>
  `,
  host: { class: 'block w-full sm:max-w-search lg:flex-1' },
})
export class SearchInput implements OnDestroy {
  readonly placeholder = input('Buscar…');
  readonly searched = output<string>();
  private timer?: ReturnType<typeof setTimeout>;

  protected onInput(value: string): void {
    clearTimeout(this.timer);
    this.timer = setTimeout(() => this.searched.emit(value.trim()), 300);
  }

  ngOnDestroy(): void {
    clearTimeout(this.timer);
  }
}
