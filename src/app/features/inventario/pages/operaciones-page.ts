import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Card } from '../../../shared/ui/card';
import { AjusteForm } from '../components/ajuste-form';
import { EntradaForm } from '../components/entrada-form';
import { SalidaForm } from '../components/salida-form';
import { MovimientoInv, etiquetaMovimiento } from '../data/inventario.model';

type Operacion = 'ENTRADA' | 'SALIDA' | 'TRASLADO' | 'AJUSTE';

@Component({
  selector: 'app-operaciones-page',
  imports: [RouterLink, Card, EntradaForm, SalidaForm, AjusteForm],
  template: `
    <div class="space-y-6">
      <div role="tablist" class="flex flex-wrap gap-2">
        @for (t of tabs; track t.id) {
          <button type="button" role="tab" [attr.aria-selected]="tab() === t.id"
            class="h-10 rounded-lg border px-4 text-sm font-semibold transition"
            [class]="tab() === t.id ? 'border-brand bg-brand text-white' : 'border-border bg-surface text-ink hover:bg-subtle'"
            (click)="elegir(t.id)">{{ t.label }}</button>
        }
      </div>

      @if (ultimo(); as m) {
        <p class="alert-success" role="status">
          {{ etiqueta(m.tipo) }} registrada (movimiento #{{ m.id }}).
          <a routerLink="/movimientos" class="underline">Ver historial</a>
        </p>
      }

      <app-card class="block p-5">
        @switch (tab()) {
          @case ('ENTRADA') { <app-entrada-form (done)="ultimo.set($event)" /> }
          @case ('SALIDA') { <app-salida-form modo="SALIDA" (done)="ultimo.set($event)" /> }
          @case ('TRASLADO') { <app-salida-form modo="TRASLADO" (done)="ultimo.set($event)" /> }
          @case ('AJUSTE') { <app-ajuste-form (done)="ultimo.set($event)" /> }
        }
      </app-card>
    </div>
  `,
})
export class OperacionesPage {
  protected readonly tabs: { id: Operacion; label: string }[] = [
    { id: 'ENTRADA', label: 'Entrada' },
    { id: 'SALIDA', label: 'Salida' },
    { id: 'TRASLADO', label: 'Traslado' },
    { id: 'AJUSTE', label: 'Ajuste' },
  ];
  protected readonly tab = signal<Operacion>('ENTRADA');
  protected readonly ultimo = signal<MovimientoInv | null>(null);
  protected readonly etiqueta = etiquetaMovimiento;

  protected elegir(t: Operacion): void {
    this.tab.set(t);
    this.ultimo.set(null);
  }
}
