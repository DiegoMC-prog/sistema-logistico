import { Component, inject, output, signal } from '@angular/core';
import { forkJoin, of, switchMap } from 'rxjs';
import { errorMessage } from '../../../core/api/api-error';
import { Icon } from '../../../shared/ui/icon';
import { SearchInput } from '../../../shared/ui/search-input';
import { PresentacionesService } from '../../productos/data/presentaciones.service';
import { Presentacion, Producto } from '../../productos/data/producto.model';
import { ProductosService } from '../../productos/data/productos.service';

export interface ItemElegido {
  productoId: number;
  presentacionId: number;
  nombre: string;
  unidad: string;
}

/** Escáner de código de barras + buscador por nombre de producto. Emite la presentación elegida. */
@Component({
  selector: 'app-product-picker',
  imports: [Icon, SearchInput],
  template: `
    <div class="space-y-3">
      <label class="relative block">
        <app-icon name="barcode" class="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-faint" />
        <input class="field-input !h-11 pl-10" placeholder="Escanear o escribir código de barras y presionar Enter"
          autocomplete="off" (keydown.enter)="escanear($any($event.target))" />
      </label>
      <app-search-input class="!max-w-none" placeholder="…o buscar producto por nombre" (searched)="buscar($event)" />

      @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }

      @if (productos().length > 0 && !producto()) {
        <ul class="max-h-56 divide-y divide-border overflow-y-auto rounded-lg border border-border">
          @for (p of productos(); track p.id) {
            <li>
              <button type="button" class="flex w-full items-center gap-2 px-3 py-2 text-left text-sm hover:bg-subtle" (click)="elegirProducto(p)">
                <app-icon name="box" class="size-4 text-muted" />{{ p.nombre }}
              </button>
            </li>
          }
        </ul>
      }

      @if (producto(); as p) {
        <div class="rounded-lg border border-border p-3">
          <div class="mb-2 flex items-center justify-between text-sm">
            <span class="font-semibold text-ink">{{ p.nombre }}</span>
            <button type="button" class="text-xs text-accent hover:underline" (click)="producto.set(null)">Volver</button>
          </div>
          @if (presentaciones().length === 0) {
            <p class="text-sm text-muted">Sin presentaciones.</p>
          }
          <div class="flex flex-wrap gap-2">
            @for (pr of presentaciones(); track pr.id) {
              <button type="button" class="rounded-lg border border-border px-3 py-1.5 text-sm hover:bg-subtle"
                (click)="emitir(pr, p.nombre)">
                {{ pr.unidadMedida.abreviatura }} · ×{{ pr.factorConversion }}
              </button>
            }
          </div>
        </div>
      }
    </div>
  `,
})
export class ProductPicker {
  private readonly productosService = inject(ProductosService);
  private readonly presentacionesService = inject(PresentacionesService);

  readonly picked = output<ItemElegido>();

  protected readonly productos = signal<Producto[]>([]);
  protected readonly producto = signal<Producto | null>(null);
  protected readonly presentaciones = signal<Presentacion[]>([]);
  protected readonly error = signal<string | null>(null);

  protected escanear(input: HTMLInputElement): void {
    const codigo = input.value.trim();
    if (!codigo) return;
    this.error.set(null);
    this.presentacionesService
      .porCodigoBarras(codigo)
      .pipe(
        switchMap((pr) => forkJoin([of(pr), this.productosService.get(pr.productoId)])),
      )
      .subscribe({
        next: ([pr, prod]) => {
          input.value = '';
          this.emitir(pr, prod.nombre);
        },
        error: (e) => this.error.set(errorMessage(e, 'No se encontró el código de barras')),
      });
  }

  protected buscar(texto: string): void {
    this.producto.set(null);
    if (!texto) return void this.productos.set([]);
    this.productosService.list({ search: texto, size: 8, estado: 'ACTIVO' }).subscribe({
      next: (p) => {
        this.error.set(null);
        this.productos.set(p.content);
      },
      error: (e) => this.error.set(errorMessage(e, 'No se pudo buscar')),
    });
  }

  protected elegirProducto(p: Producto): void {
    this.producto.set(p);
    this.presentacionesService.list(p.id).subscribe({
      next: (r) => this.presentaciones.set(r),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  protected emitir(pr: Presentacion, nombre: string): void {
    this.picked.emit({ productoId: pr.productoId, presentacionId: pr.id, nombre, unidad: pr.unidadMedida.abreviatura });
  }
}
