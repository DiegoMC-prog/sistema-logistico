import { Component, booleanAttribute, computed, input } from '@angular/core';
import { Icon, IconName } from './icon';

export type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger';

const VARIANTS: Record<ButtonVariant, string> = {
  primary: 'bg-brand text-white shadow-sm hover:bg-brand-hover',
  secondary: 'border border-border bg-surface text-ink hover:bg-subtle',
  ghost: 'text-muted hover:bg-subtle hover:text-ink',
  danger: 'bg-danger text-white shadow-sm hover:bg-danger-dark',
};

/** <button appBtn variant="secondary" icon="plus">Texto</button> */
@Component({
  selector: 'button[appBtn]',
  imports: [Icon],
  template: `@if (icon(); as i) {<app-icon [name]="i" class="size-4" />}<ng-content />`,
  host: { '[class]': 'classes()' },
})
export class Btn {
  readonly variant = input<ButtonVariant>('primary');
  readonly icon = input<IconName>();

  protected readonly classes = computed(
    () =>
      'inline-flex h-10 items-center justify-center gap-2 rounded-lg px-4 text-sm font-semibold transition duration-150 active:scale-[0.97] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 ' +
      VARIANTS[this.variant()],
  );
}

/** Botón cuadrado de solo icono. <button appIconBtn icon="edit" label="Editar" danger></button> */
@Component({
  selector: 'button[appIconBtn]',
  imports: [Icon],
  template: `<app-icon [name]="icon()" class="size-4" />`,
  host: { '[class]': 'classes()', '[attr.aria-label]': 'label()', '[attr.title]': 'label()', type: 'button' },
})
export class IconBtn {
  readonly icon = input.required<IconName>();
  readonly label = input.required<string>();
  readonly danger = input(false, { transform: booleanAttribute });

  protected readonly classes = computed(
    () =>
      'grid size-9 place-items-center rounded-lg text-muted transition duration-150 hover:scale-110 active:scale-95 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent disabled:opacity-40 ' +
      (this.danger() ? 'hover:bg-danger-soft hover:text-danger' : 'hover:bg-subtle hover:text-ink'),
  );
}
