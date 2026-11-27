import { DecimalPipe } from '@angular/common';
import { Component, effect, inject, input, output, signal, untracked } from '@angular/core';
import { errorMessage } from '../../../core/api/api-error';
import { CatalogoProductos, EleccionCatalogo } from '../../catalogo/catalogo-productos';
import { CatalogoProducto } from '../../catalogo/catalogo.service';
import { Stock } from '../data/inventario.model';
import { InventarioService } from '../data/inventario.service';

/** Catálogo de productos con existencias en un depósito; al elegir uno se escoge el lote, que es lo que se emite. */
@Component({
  selector: 'app-stock-picker',
  imports: [CatalogoProductos, DecimalPipe],
  template: `
    <div class="space-y-2">
      @if (!depositoId()) {
        <p class="text-sm text-muted">Selecciona primero un depósito.</p>
      } @else {
        @if (producto(); as p) {
          <div class="rounded-lg border border-accent p-3">
            <div class="mb-2 flex items-center justify-between gap-3 text-sm">
              <span class="font-semibold text-ink">{{ p.nombre }} · elige el lote</span>
              <button type="button" class="text-xs font-semibold text-accent hover:underline" (click)="producto.set(null)">Cerrar</button>
            </div>
            @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }
            @if (lotes(); as lista) {
              <ul class="max-h-52 divide-y divide-border overflow-y-auto rounded-lg border border-border">
                @for (s of lista; track s.id) {
                  <li>
                    <button type="button" class="flex w-full items-center justify-between gap-3 px-3 py-2 text-left text-sm hover:bg-subtle" (click)="elegirLote(s)">
                      <span class="font-mono text-xs text-ink">Lote {{ s.lote.numeroLote }}</span>
                      <span class="text-muted">{{ s.stockActual | number: '1.0-4' }} {{ s.unidadBase }}</span>
                    </button>
                  </li>
                } @empty {
                  <li class="px-3 py-2 text-sm text-muted">Sin lotes con stock.</li>
                }
              </ul>
            } @else if (!error()) {
              <p class="text-sm text-muted">Cargando lotes…</p>
            }
          </div>
        }
        <app-catalogo-productos modo="producto" [depositoId]="depositoId()" [soloConStock]="true" [incluirVencidos]="true"
          (elegido)="elegirProducto($event)" />
      }
    </div>
  `,
})
export class StockPicker {
  private readonly service = inject(InventarioService);

  readonly depositoId = input<number | null>(null);
  readonly picked = output<Stock>();

  protected readonly producto = signal<CatalogoProducto | null>(null);
  protected readonly lotes = signal<Stock[] | null>(null);
  protected readonly error = signal<string | null>(null);

  constructor() {
    effect(() => {
      this.depositoId(); // los lotes elegibles son los del depósito: al cambiarlo se cierra la selección
      untracked(() => this.producto.set(null));
    });
  }

  protected elegirProducto({ producto }: EleccionCatalogo): void {
    const depositoId = this.depositoId();
    if (!depositoId) return;
    this.producto.set(producto);
    this.lotes.set(null);
    this.error.set(null);
    this.service.stock({ depositoId, productoId: producto.productoId, conStock: true, size: 100 }).subscribe({
      next: (p) => {
        if (this.producto()?.productoId === producto.productoId) this.lotes.set(p.content);
      },
      error: (e) => this.error.set(errorMessage(e, 'No se pudieron consultar los lotes')),
    });
  }

  protected elegirLote(s: Stock): void {
    this.producto.set(null);
    this.picked.emit(s);
  }
}
