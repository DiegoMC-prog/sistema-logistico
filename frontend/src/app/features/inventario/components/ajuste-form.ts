import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { errorMessage } from '../../../core/api/api-error';
import { Btn } from '../../../shared/ui/button';
import { MovimientoInv, Stock } from '../data/inventario.model';
import { InventarioService } from '../data/inventario.service';
import { DepositosRef } from './depositos-ref';
import { StockPicker } from './stock-picker';

/** Ajuste por conteo físico: se indica el stock real y el servidor calcula la diferencia. */
@Component({
  selector: 'app-ajuste-form',
  imports: [DecimalPipe, FormsModule, Btn, StockPicker],
  template: `
    <div class="space-y-5">
      @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }

      <label class="block text-sm font-medium text-ink">Depósito
        <select class="field-input mt-1" [ngModel]="depositoId()" (ngModelChange)="cambiarDeposito($event)">
          <option [ngValue]="null" disabled>Selecciona un depósito</option>
          @for (d of depositos(); track d.id) { <option [ngValue]="d.id">{{ d.nombre }}</option> }
        </select>
      </label>

      <section class="space-y-3">
        <p class="text-xs font-semibold uppercase tracking-wide text-muted">Lote a ajustar</p>
        <app-stock-picker [depositoId]="depositoId()" (picked)="elegir($event)" />
        <p class="text-xs text-muted">Solo aparecen lotes con stock mayor a cero.</p>
      </section>

      @if (stock(); as s) {
        <div class="space-y-3 rounded-lg border border-border p-4 text-sm">
          <p><span class="font-semibold text-ink">{{ s.producto.nombre }}</span>
            <span class="font-mono text-xs text-muted"> · lote {{ s.lote.numeroLote }}</span></p>
          <p class="text-muted">Stock en sistema: <strong class="text-ink">{{ s.stockActual | number: '1.0-4' }} {{ s.unidadBase }}</strong></p>
          <label class="block text-sm font-medium text-ink">Stock real contado ({{ s.unidadBase }})
            <input type="number" min="0" step="any" class="field-input mt-1 max-w-xs" [ngModel]="stockReal()" (ngModelChange)="stockReal.set(+$event)" />
          </label>
          <p class="font-semibold" [class]="diferencia() < 0 ? 'text-danger' : diferencia() > 0 ? 'text-success' : 'text-muted'">
            Diferencia: {{ diferencia() > 0 ? '+' : '' }}{{ diferencia() | number: '1.0-4' }} {{ s.unidadBase }}
            {{ diferencia() < 0 ? '(ajuste negativo)' : diferencia() > 0 ? '(ajuste positivo)' : '(sin cambios)' }}
          </p>
        </div>
      }

      <label class="block text-sm font-medium text-ink">Motivo
        <input class="field-input mt-1" maxlength="200" [ngModel]="motivo()" (ngModelChange)="motivo.set($event)" placeholder="Ej. Conteo físico, rotura…" />
      </label>

      <div class="flex justify-end">
        <button appBtn icon="check-circle" [disabled]="enviando()" (click)="enviar()">Registrar ajuste</button>
      </div>
    </div>
  `,
})
export class AjusteForm {
  private readonly service = inject(InventarioService);

  readonly done = output<MovimientoInv>();

  protected readonly depositos = inject(DepositosRef).depositos;
  protected readonly depositoId = signal<number | null>(null);
  protected readonly stock = signal<Stock | null>(null);
  protected readonly stockReal = signal(0);
  protected readonly motivo = signal('');
  protected readonly error = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly diferencia = computed(() => Math.round(((this.stockReal() - (this.stock()?.stockActual ?? 0)) + Number.EPSILON) * 10000) / 10000);

  protected cambiarDeposito(id: number): void {
    this.depositoId.set(id);
    this.stock.set(null);
  }

  protected elegir(s: Stock): void {
    this.stock.set(s);
    this.stockReal.set(s.stockActual);
  }

  protected enviar(): void {
    const dep = this.depositoId();
    const s = this.stock();
    if (!dep) return void this.error.set('Selecciona el depósito');
    if (!s) return void this.error.set('Elige el lote a ajustar');
    if (!(this.stockReal() >= 0)) return void this.error.set('El stock real no puede ser negativo');
    if (this.diferencia() === 0) return void this.error.set('El stock real es igual al del sistema: no hay nada que ajustar');
    if (!this.motivo().trim()) return void this.error.set('Indica el motivo del ajuste');

    this.enviando.set(true);
    this.error.set(null);
    this.service.ajuste({ depositoId: dep, loteId: s.lote.id, stockReal: this.stockReal(), motivo: this.motivo().trim() }).subscribe({
      next: (m) => {
        this.enviando.set(false);
        this.stock.set(null);
        this.motivo.set('');
        this.done.emit(m);
      },
      error: (e) => {
        this.enviando.set(false);
        this.error.set(errorMessage(e, 'No se pudo registrar el ajuste'));
      },
    });
  }
}
