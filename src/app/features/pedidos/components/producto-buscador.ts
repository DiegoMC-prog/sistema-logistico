import { Component, inject, output, signal } from '@angular/core';
import { errorMessage } from '../../../core/api/api-error';
import { Icon } from '../../../shared/ui/icon';
import { SearchInput } from '../../../shared/ui/search-input';
import { Producto } from '../../productos/data/producto.model';
import { ProductosService } from '../../productos/data/productos.service';

/** Busca productos activos por nombre y emite el elegido. */
@Component({
  selector: 'app-producto-buscador',
  imports: [Icon, SearchInput],
  template: `
    <div class="space-y-2">
      <app-search-input class="!max-w-none" placeholder="Buscar producto por nombre para agregarlo…" (searched)="buscar($event)" />
      @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }
      @if (items().length > 0) {
        <ul class="max-h-52 divide-y divide-border overflow-y-auto rounded-lg border border-border">
          @for (p of items(); track p.id) {
            <li>
              <button type="button" class="flex w-full items-center gap-2 px-3 py-2 text-left text-sm hover:bg-subtle" (click)="elegir(p)">
                <app-icon name="box" class="size-4 text-muted" />
                <span class="flex-1 text-ink">{{ p.nombre }}</span>
                <span class="text-xs text-muted">{{ p.categoria.nombre }}</span>
              </button>
            </li>
          }
        </ul>
      }
    </div>
  `,
})
export class ProductoBuscador {
  private readonly service = inject(ProductosService);
  readonly picked = output<Producto>();
  protected readonly items = signal<Producto[]>([]);
  protected readonly error = signal<string | null>(null);

  protected buscar(texto: string): void {
    if (!texto) return void this.items.set([]);
    this.service.list({ search: texto, size: 8, estado: 'ACTIVO' }).subscribe({
      next: (p) => {
        this.error.set(null);
        this.items.set(p.content);
      },
      error: (e) => this.error.set(errorMessage(e, 'No se pudo buscar')),
    });
  }

  protected elegir(p: Producto): void {
    this.picked.emit(p);
    this.items.set([]);
  }
}
