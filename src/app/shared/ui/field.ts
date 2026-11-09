import { Component, booleanAttribute, input } from '@angular/core';
import { AbstractControl } from '@angular/forms';

/**
 * Etiqueta + control + mensaje de error.
 * <app-field label="Nombre" [control]="form.controls.nombre" required><input class="field-input" formControlName="nombre" /></app-field>
 */
@Component({
  selector: 'app-field',
  host: { class: 'block' },
  template: `
    <label class="flex flex-col gap-2">
      <span class="field-label">
        {{ label() }}@if (required()) {<span class="text-danger"> *</span>}
        @if (optional()) {<span class="font-normal text-muted"> (opcional)</span>}
      </span>
      <ng-content />
      @if (message(); as m) {
        <span class="text-xs text-danger">{{ m }}</span>
      } @else if (helper()) {
        <span class="text-xs text-muted">{{ helper() }}</span>
      }
    </label>
  `,
})
export class Field {
  readonly label = input.required<string>();
  readonly control = input<AbstractControl | null>(null);
  readonly helper = input<string>();
  readonly required = input(false, { transform: booleanAttribute });
  readonly optional = input(false, { transform: booleanAttribute });

  protected message(): string | null {
    const c = this.control();
    if (!c || !c.touched || !c.errors) return null;
    if (c.errors['required']) return 'Este campo es obligatorio';
    if (c.errors['email']) return 'Ingresa un correo válido';
    if (c.errors['minlength']) return `Mínimo ${c.errors['minlength'].requiredLength} caracteres`;
    if (c.errors['maxlength']) return `Máximo ${c.errors['maxlength'].requiredLength} caracteres`;
    if (c.errors['min']) return `El valor mínimo es ${c.errors['min'].min}`;
    return 'Valor no válido';
  }
}
