import { Component, computed, input } from '@angular/core';

export type BadgeTone = 'success' | 'warning' | 'danger' | 'neutral' | 'info';

const TONES: Record<BadgeTone, string> = {
  success: 'bg-success-soft text-success-dark',
  warning: 'bg-warning-soft text-warning-dark',
  danger: 'bg-danger-soft text-danger-dark',
  neutral: 'bg-subtle text-muted',
  info: 'bg-info-soft text-info',
};

@Component({
  selector: 'app-badge',
  template: `<ng-content />`,
  host: { '[class]': 'classes()' },
})
export class Badge {
  readonly tone = input<BadgeTone>('neutral');
  protected readonly classes = computed(
    () => 'inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold ' + TONES[this.tone()],
  );
}
