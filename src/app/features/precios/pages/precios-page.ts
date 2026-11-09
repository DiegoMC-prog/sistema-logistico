import { Component, inject, signal } from '@angular/core';
import { errorMessage } from '../../../core/api/api-error';
import { Card } from '../../../shared/ui/card';
import { EmptyState } from '../../../shared/ui/empty-state';
import { Icon } from '../../../shared/ui/icon';
import { SearchInput } from '../../../shared/ui/search-input';
import { PresentacionesService } from '../../productos/data/presentaciones.service';
import { Presentacion, Producto } from '../../productos/data/producto.model';
import { ProductosService } from '../../productos/data/productos.service';
import { TipoCliente } from '../../tipos-cliente/data/tipo-cliente.model';
import { TiposClienteService } from '../../tipos-cliente/data/tipos-cliente.service';
import { PreciosPanel } from '../components/precios-panel';

@Component({
  selector: 'app-precios-page',
  imports: [Card, EmptyState, SearchInput, Icon, PreciosPanel],
  template: `
    <div class="grid gap-6 lg:grid-cols-[22rem_1fr]">
      <app-card class="h-fit overflow-hidden">
        <div class="border-b border-border p-4">
          <app-search-input placeholder="Buscar producto…" (searched)="buscar($event)" />
        </div>
        @if (error()) { <p class="alert-error m-4" role="alert">{{ error() }}</p> }
        <ul class="max-h-[32rem] divide-y divide-border overflow-y-auto">
          @for (p of productos(); track p.id) {
            <li>
              <button type="button" class="flex w-full items-center gap-3 px-4 py-3 text-left text-sm transition hover:bg-subtle"
                [class]="seleccionado()?.id === p.id ? 'bg-subtle font-semibold' : ''" (click)="seleccionar(p)">
                <app-icon name="box" class="size-4 text-muted" />
                <span class="min-w-0 flex-1 truncate text-ink">{{ p.nombre }}</span>
                <span class="text-xs text-muted">{{ p.categoria.nombre }}</span>
              </button>
            </li>
          } @empty {
            <li class="p-4 text-sm text-muted">Sin productos.</li>
          }
        </ul>
      </app-card>

      <div class="space-y-6">
        @if (seleccionado(); as prod) {
          <h2 class="text-lg font-semibold text-ink">{{ prod.nombre }}</h2>
          @if (presentaciones() === null) {
            <p class="text-sm text-muted">Cargando presentaciones…</p>
          } @else if (presentaciones()!.length === 0) {
            <app-card class="block p-5 text-sm text-muted">Este producto no tiene presentaciones. Créalas desde Productos.</app-card>
          }
          @for (pr of presentaciones(); track pr.id) {
            <app-card class="block p-5">
              <p class="mb-4 text-sm font-semibold text-ink">
                {{ pr.unidadMedida.nombre }} ({{ pr.unidadMedida.abreviatura }}) · ×{{ pr.factorConversion }}
                <span class="ml-2 font-mono text-xs font-normal text-muted">{{ pr.codigoBarras }}</span>
              </p>
              <app-precios-panel [presentacionId]="pr.id" [tipos]="tipos()" />
            </app-card>
          }
        } @else {
          <app-card><app-empty-state title="Elige un producto" message="Selecciona un producto de la lista para administrar los precios de sus presentaciones." /></app-card>
        }
      </div>
    </div>
  `,
})
export class PreciosPage {
  private readonly productosService = inject(ProductosService);
  private readonly presentacionesService = inject(PresentacionesService);
  private readonly tiposService = inject(TiposClienteService);

  protected readonly productos = signal<Producto[]>([]);
  protected readonly seleccionado = signal<Producto | null>(null);
  protected readonly presentaciones = signal<Presentacion[] | null>(null);
  protected readonly tipos = signal<TipoCliente[]>([]);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.buscar('');
    this.tiposService.list({ size: 100, estado: 'ACTIVO' }).subscribe({ next: (p) => this.tipos.set(p.content) });
  }

  protected buscar(search: string): void {
    this.productosService.list({ size: 50, search: search || undefined }).subscribe({
      next: (p) => {
        this.error.set(null);
        this.productos.set(p.content);
      },
      error: (e) => this.error.set(errorMessage(e, 'No se pudieron cargar los productos')),
    });
  }

  protected seleccionar(p: Producto): void {
    this.seleccionado.set(p);
    this.presentaciones.set(null);
    this.presentacionesService.list(p.id).subscribe({
      next: (r) => this.presentaciones.set(r),
      error: (e) => {
        this.presentaciones.set([]);
        this.error.set(errorMessage(e, 'No se pudieron cargar las presentaciones'));
      },
    });
  }
}
