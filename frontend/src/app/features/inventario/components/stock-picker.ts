import { DecimalPipe } from '@angular/common';
import { Component, effect, inject, input, output, signal } from '@angular/core';
import { errorMessage } from '../../../core/api/api-error';
import { SearchInput } from '../../../shared/ui/search-input';
import { Stock } from '../data/inventario.model';
import { InventarioService } from '../data/inventario.service';

/** Busca lotes con existencias en un depósito y emite el elegido. */
@Component({
  selector: 'app-stock-picker',
  imports: [DecimalPipe, SearchInput],
  template: `
    <div class="space-y-2">
      <app-search-input class="!max-w-none" placeholder="Buscar producto con stock en este depósito…" (searched)="buscar($event)" />
      @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }
      @if (!depositoId()) {
        <p class="text-sm text-muted">Selecciona primero un depósito.</p>
      } @else if (items().length > 0) {
        <ul class="max-h-52 divide-y divide-border overflow-y-auto rounded-lg border border-border">
          @for (s of items(); track s.id) {
            <li>
              <button type="button" class="flex w-full items-center justify-between gap-3 px-3 py-2 text-left text-sm hover:bg-subtle" (click)="picked.emit(s)">
                <span><span class="font-semibold text-ink">{{ s.producto.nombre }}</span> <span class="font-mono text-xs text-muted">· lote {{ s.lote.numeroLote }}</span></span>
                <span class="text-muted">{{ s.stockActual | number: '1.0-4' }} {{ s.unidadBase }}</span>
              </button>
            </li>
          }
        </ul>
      } @else if (buscado()) {
        <p class="text-sm text-muted">Sin resultados con stock.</p>
      }
    </div>
  `,
})
export class StockPicker {
  private readonly service = inject(InventarioService);

  readonly depositoId = input<number | null>(null);
  readonly picked = output<Stock>();

  protected readonly items = signal<Stock[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly buscado = signal(false);
  private texto = '';

  constructor() {
    effect(() => {
      this.depositoId(); // al cambiar de depósito se recarga
      this.cargar();
    });
  }

  protected buscar(texto: string): void {
    this.texto = texto;
    this.cargar();
  }

  private cargar(): void {
    const dep = this.depositoId();
    if (!dep) return void this.items.set([]);
    this.service.stock({ depositoId: dep, conStock: true, search: this.texto || undefined, size: 15 }).subscribe({
      next: (p) => {
        this.error.set(null);
        this.items.set(p.content);
        this.buscado.set(true);
      },
      error: (e) => this.error.set(errorMessage(e, 'No se pudo consultar el stock')),
    });
  }
}
