import { Component, input, output } from '@angular/core';

/** Interruptor controlado: el padre decide el valor y reacciona a (checkedChange). */
@Component({
  selector: 'app-toggle',
  template: `
    <button
      type="button"
      role="switch"
      class="relative h-6 w-11 shrink-0 rounded-full transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent focus-visible:ring-offset-2 disabled:opacity-50"
      [class]="checked() ? 'bg-success' : 'bg-faint'"
      [attr.aria-checked]="checked()"
      [disabled]="disabled()"
      (click)="checkedChange.emit(!checked())"
    >
      <span
        class="absolute top-1 size-4 rounded-full bg-white shadow-sm transition"
        [class]="checked() ? 'left-6' : 'left-1'"
      ></span>
      <span class="sr-only">{{ label() }}</span>
    </button>
  `,
  host: { class: 'inline-flex' },
})
export class Toggle {
  readonly checked = input(false);
  readonly label = input.required<string>();
  readonly disabled = input(false);
  readonly checkedChange = output<boolean>();
}
