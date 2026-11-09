import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { errorMessage } from '../../../core/api/api-error';
import { Btn, IconBtn } from '../../../shared/ui/button';
import { MovimientoInv, Stock } from '../data/inventario.model';
import { InventarioService } from '../data/inventario.service';
import { DepositosRef } from './depositos-ref';
import { StockPicker } from './stock-picker';

interface FilaSalida {
  stock: Stock;
  cantidad: number;
}

/** Salida (con motivo) o traslado entre depósitos: ambos mueven lotes con stock del depósito origen. */
@Component({
  selector: 'app-salida-form',
  imports: [DecimalPipe, FormsModule, Btn, IconBtn, StockPicker],
  template: `
    <div class="space-y-5">
      @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }

      <div class="grid gap-4 sm:grid-cols-2">
        <label class="block text-sm font-medium text-ink">{{ traslado() ? 'Depósito de origen' : 'Depósito' }}
          <select class="field-input mt-1" [ngModel]="origenId()" (ngModelChange)="cambiarOrigen($event)">
            <option [ngValue]="null" disabled>Selecciona un depósito</option>
            @for (d of depositos(); track d.id) { <option [ngValue]="d.id">{{ d.nombre }}</option> }
          </select>
        </label>
        @if (traslado()) {
          <label class="block text-sm font-medium text-ink">Depósito de destino
            <select class="field-input mt-1" [ngModel]="destinoId()" (ngModelChange)="destinoId.set($event)">
              <option [ngValue]="null" disabled>Selecciona un depósito</option>
              @for (d of destinos(); track d.id) { <option [ngValue]="d.id">{{ d.nombre }}</option> }
            </select>
          </label>
        }
        <label class="block text-sm font-medium text-ink sm:col-span-2">
          {{ traslado() ? 'Observación' : 'Motivo' }}
          @if (traslado()) { <span class="font-normal text-muted">(opcional)</span> }
          <input class="field-input mt-1" maxlength="200" [ngModel]="texto()" (ngModelChange)="texto.set($event)"
            [placeholder]="traslado() ? '' : 'Ej. Merma, consumo interno, vencido…'" />
        </label>
      </div>

      <section class="space-y-3">
        <p class="text-xs font-semibold uppercase tracking-wide text-muted">Agregar lote</p>
        <app-stock-picker [depositoId]="origenId()" (picked)="agregar($event)" />
      </section>

      @for (f of filas(); track f.stock.id) {
        <div class="flex flex-wrap items-center gap-3 rounded-lg border border-border px-4 py-3 text-sm">
          <span class="min-w-0 flex-1">
            <span class="font-semibold text-ink">{{ f.stock.producto.nombre }}</span>
            <span class="font-mono text-xs text-muted"> · lote {{ f.stock.lote.numeroLote }}</span>
            <span class="block text-xs text-muted">Disponible: {{ f.stock.stockActual | number: '1.0-4' }} {{ f.stock.unidadBase }}</span>
          </span>
          <input type="number" min="0.0001" [max]="f.stock.stockActual" step="any" class="field-input !h-10 w-28"
            [ngModel]="f.cantidad" (ngModelChange)="patch(f.stock.id, +$event)" [attr.aria-label]="'Cantidad de ' + f.stock.producto.nombre" />
          <span class="text-xs text-muted">{{ f.stock.unidadBase }}</span>
          <button appIconBtn icon="trash" danger label="Quitar" (click)="quitar(f.stock.id)"></button>
        </div>
      } @empty {
        <p class="text-sm text-muted">Aún no agregaste lotes. Las cantidades van en unidad base.</p>
      }

      <div class="flex justify-end">
        <button appBtn [icon]="traslado() ? 'truck' : 'upload'" [variant]="traslado() ? 'primary' : 'danger'" [disabled]="enviando()" (click)="enviar()">
          {{ traslado() ? 'Registrar traslado' : 'Registrar salida' }}
        </button>
      </div>
    </div>
  `,
})
export class SalidaForm {
  private readonly service = inject(InventarioService);

  readonly modo = input.required<'SALIDA' | 'TRASLADO'>();
  readonly done = output<MovimientoInv>();

  protected readonly depositos = inject(DepositosRef).depositos;
  protected readonly traslado = computed(() => this.modo() === 'TRASLADO');
  protected readonly origenId = signal<number | null>(null);
  protected readonly destinoId = signal<number | null>(null);
  protected readonly destinos = computed(() => this.depositos().filter((d) => d.id !== this.origenId()));
  protected readonly texto = signal('');
  protected readonly filas = signal<FilaSalida[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly enviando = signal(false);

  protected cambiarOrigen(id: number): void {
    this.origenId.set(id);
    this.filas.set([]); // los lotes elegidos pertenecen al depósito anterior
    if (this.destinoId() === id) this.destinoId.set(null);
  }

  protected agregar(s: Stock): void {
    if (this.filas().some((f) => f.stock.id === s.id)) return;
    this.filas.update((fs) => [...fs, { stock: s, cantidad: Math.min(1, s.stockActual) }]);
  }

  protected patch(stockId: number, cantidad: number): void {
    this.filas.update((fs) => fs.map((f) => (f.stock.id === stockId ? { ...f, cantidad } : f)));
  }

  protected quitar(stockId: number): void {
    this.filas.update((fs) => fs.filter((f) => f.stock.id !== stockId));
  }

  protected enviar(): void {
    const origen = this.origenId();
    if (!origen) return void this.error.set('Selecciona el depósito');
    if (this.traslado() && !this.destinoId()) return void this.error.set('Selecciona el depósito de destino');
    if (!this.traslado() && !this.texto().trim()) return void this.error.set('Indica el motivo de la salida');
    const filas = this.filas();
    if (filas.length === 0) return void this.error.set('Agrega al menos un lote');
    for (const f of filas) {
      if (!(f.cantidad > 0)) return void this.error.set(`La cantidad de «${f.stock.producto.nombre}» debe ser mayor a cero`);
      if (f.cantidad > f.stock.stockActual) {
        return void this.error.set(`«${f.stock.producto.nombre}» solo tiene ${f.stock.stockActual} ${f.stock.unidadBase} disponibles`);
      }
    }

    const items = filas.map((f) => ({ loteId: f.stock.lote.id, cantidad: f.cantidad }));
    const texto = this.texto().trim();
    const req = this.traslado()
      ? this.service.traslado({ depositoOrigenId: origen, depositoDestinoId: this.destinoId()!, observacion: texto || undefined, items })
      : this.service.salida({ depositoId: origen, motivo: texto, items });

    this.enviando.set(true);
    this.error.set(null);
    req.subscribe({
      next: (m) => {
        this.enviando.set(false);
        this.filas.set([]);
        this.texto.set('');
        this.done.emit(m);
      },
      error: (e) => {
        this.enviando.set(false);
        this.error.set(errorMessage(e, 'No se pudo registrar el movimiento'));
      },
    });
  }
}
