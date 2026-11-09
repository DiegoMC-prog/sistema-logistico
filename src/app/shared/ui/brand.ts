import { Component, input } from '@angular/core';
import { Icon } from './icon';

@Component({
  selector: 'app-brand',
  imports: [Icon],
  template: `
    <div class="grid size-9 place-items-center rounded-lg bg-accent text-white"><app-icon name="box" /></div>
    <div>
      <p class="text-base font-bold tracking-tight" [class]="onDark() ? 'text-white' : 'text-ink'">Stockline</p>
      <p class="text-xs text-sidebar-muted">Warehouse OS</p>
    </div>
  `,
  host: { class: 'flex items-center gap-3' },
})
export class Brand {
  readonly onDark = input(false);
}
