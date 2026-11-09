import { Component, computed, input } from '@angular/core';
import { Card } from './card';

@Component({
  selector: 'app-stat-card',
  imports: [Card],
  template: `
    <app-card class="p-5">
      <p class="text-sm font-medium text-muted">{{ label() }}</p>
      <p class="mt-2 text-2xl font-bold" [class]="valueClass()">{{ value() ?? '—' }}</p>
      @if (hint()) {
        <p class="mt-2 text-xs text-muted">{{ hint() }}</p>
      }
    </app-card>
  `,
  host: { class: 'block transition duration-200 hover:-translate-y-0.5 hover:drop-shadow-md' },
})
export class StatCard {
  readonly label = input.required<string>();
  readonly value = input<number | string | null | undefined>(null);
  readonly hint = input<string>();
  readonly tone = input<'ink' | 'success' | 'muted'>('ink');
  protected readonly valueClass = computed(
    () => ({ ink: 'text-ink', success: 'text-success', muted: 'text-muted' })[this.tone()],
  );
}
