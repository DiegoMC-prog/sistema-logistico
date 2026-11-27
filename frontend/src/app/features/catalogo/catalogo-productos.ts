import { DecimalPipe } from '@angular/common';
import { Component, effect, inject, input, output, signal, untracked } from '@angular/core';
import { errorMessage } from '../../core/api/api-error';
import { Page } from '../../core/api/page.model';
import { EmptyState } from '../../shared/ui/empty-state';
import { tamanoPagina } from '../../shared/ui/page-size';
import { Paginator } from '../../shared/ui/paginator';
import { SearchInput } from '../../shared/ui/search-input';
import { CategoriasService } from '../categorias/data/categorias.service';
import { CatalogoPresentacion, CatalogoProducto, CatalogoService } from './catalogo.service';

export interface EleccionCatalogo {
  producto: CatalogoProducto;
  /** null en modo «producto» (se elige el producto completo). */
  presentacion: CatalogoPresentacion | null;
}

/** Catálogo en tarjetas: categorías, buscador y paginación. Se usa en el punto de venta y en los movimientos. */
@Component({
  selector: 'app-catalogo-productos',
  imports: [DecimalPipe, EmptyState, Paginator, SearchInput],
  template: `
    <div class="space-y-3">
      <app-search-input class="!max-w-none" placeholder="Buscar producto por nombre…" (searched)="buscar($event)" />

      @if (categorias().length > 0) {
        <div class="chips flex gap-2 overflow-x-auto pb-2" role="group" aria-label="Categorías">
          <button type="button" class="chip" [class.chip-activa]="categoriaId() === null" [attr.aria-pressed]="categoriaId() === null"
            (click)="elegirCategoria(null)">Todos</button>
          @for (c of categorias(); track c.id) {
            <button type="button" class="chip" [class.chip-activa]="categoriaId() === c.id" [attr.aria-pressed]="categoriaId() === c.id"
              (click)="elegirCategoria(c.id)">{{ c.nombre }}</button>
          }
        </div>
      }

      @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }

      @if (pagina(); as p) {
        @if (p.content.length === 0) {
          <app-empty-state title="Sin productos" message="No hay productos para esta búsqueda o categoría." />
        } @else {
          <ul class="grid max-h-[26rem] grid-cols-[repeat(auto-fill,minmax(9.5rem,1fr))] gap-3 overflow-y-auto pr-1">
            @for (prod of p.content; track prod.productoId) {
              @if (modo() === 'producto') {
                <li>
                  <button type="button" class="tarjeta h-full w-full text-left hover:border-accent hover:bg-subtle" (click)="elegir(prod, null)">
                    <span class="text-sm font-semibold leading-snug text-ink">{{ prod.nombre }}</span>
                    <span class="text-xs text-muted">{{ prod.categoria }}</span>
                    @if (prod.stock !== null) {
                      <span class="mt-auto text-xs font-semibold text-ink">Stock: {{ prod.stock | number: '1.0-2' }} {{ prod.unidadBase }}</span>
                    }
                  </button>
                </li>
              } @else {
                <li class="tarjeta" [class.opacity-60]="agotado(prod)">
                  <span class="text-sm font-semibold leading-snug text-ink">{{ prod.nombre }}</span>
                  <span class="text-xs text-muted">{{ prod.categoria }}</span>
                  @if (prod.stock !== null) {
                    @if (agotado(prod)) {
                      <span class="text-xs font-semibold text-danger">{{ bloquearAgotados() ? 'Agotado' : 'Sin stock' }}</span>
                    } @else {
                      <span class="text-xs text-muted">Stock: {{ prod.stock | number: '1.0-2' }} {{ prod.unidadBase }}</span>
                    }
                  }
                  <div class="mt-auto flex flex-col gap-1.5 pt-1">
                    @for (pr of prod.presentaciones; track pr.id) {
                      <button type="button"
                        class="flex items-center justify-between gap-2 rounded-lg border border-border px-2.5 py-1.5 text-xs text-ink transition hover:border-accent hover:bg-subtle disabled:cursor-not-allowed disabled:opacity-50"
                        [disabled]="!disponible(prod, pr)" [attr.aria-label]="'Agregar ' + prod.nombre + ' ' + pr.abreviatura"
                        (click)="elegir(prod, pr)">
                        <span>{{ pr.abreviatura }}@if (pr.factor !== 1) { <span class="text-muted"> ×{{ pr.factor }}</span> }</span>
                        @if (conPrecios()) {
                          <span class="font-semibold">{{ pr.precio === null ? 'Sin precio' : (pr.precio | number: '1.2-2') }}</span>
                        }
                      </button>
                    } @empty {
                      <span class="text-xs text-muted">Sin presentaciones</span>
                    }
                  </div>
                </li>
              }
            }
          </ul>
          @if (p.totalElements > 10) {
            <app-paginator class="block" [page]="p.page" [size]="p.size" [totalPages]="p.totalPages" [totalElements]="p.totalElements"
              (pageChange)="irA($event)" (sizeChange)="cambiarTamano($event)" />
          }
        }
      } @else if (!error()) {
        <p class="text-sm text-muted">Cargando catálogo…</p>
      }
    </div>
  `,
  styles: `
    /* La fila de categorías se desplaza dentro del catálogo: su ancho no debe estirar la columna que lo contiene */
    :host { display: block; min-width: 0; contain: inline-size; }
    .chips { scrollbar-width: thin; scrollbar-color: var(--color-border) transparent; }
    .chip {
      flex-shrink: 0;
      white-space: nowrap;
      border: 1px solid var(--color-border);
      border-radius: 9999px;
      padding: 0.35rem 0.8rem;
      font-size: 0.75rem;
      font-weight: 600;
      color: var(--color-muted);
    }
    .chip:hover { background: var(--color-subtle); color: var(--color-ink); }
    .chip-activa,
    .chip-activa:hover { background: var(--color-accent); border-color: var(--color-accent); color: #fff; }
    .tarjeta {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
      border: 1px solid var(--color-border);
      border-radius: 0.75rem;
      padding: 0.75rem;
      transition: border-color 0.15s, background 0.15s;
    }
  `,
})
export class CatalogoProductos {
  private readonly service = inject(CatalogoService);

  /** Depósito del que se muestra el stock (null: sin stock). */
  readonly depositoId = input<number | null>(null);
  readonly tipoClienteId = input<number | null>(null);
  /** «presentacion»: cada presentación es un botón. «producto»: se elige la tarjeta entera. */
  readonly modo = input<'presentacion' | 'producto'>('presentacion');
  readonly conPrecios = input(false);
  readonly soloConStock = input(false);
  readonly incluirVencidos = input(false);
  /** No permite elegir presentaciones sin stock suficiente (venta). */
  readonly bloquearAgotados = input(false);
  /** Al cambiar se vuelve a consultar (p. ej. después de una venta). */
  readonly version = input(0);
  readonly elegido = output<EleccionCatalogo>();

  protected readonly categorias = signal<{ id: number; nombre: string }[]>([]);
  protected readonly categoriaId = signal<number | null>(null);
  protected readonly pagina = signal<Page<CatalogoProducto> | null>(null);
  protected readonly error = signal<string | null>(null);

  private texto = '';
  private numero = 0;
  private tamano = tamanoPagina(10);
  private depositoAnterior: number | null = null;
  private pedido = 0;

  constructor() {
    inject(CategoriasService)
      .list({ size: 100 })
      .subscribe({ next: (p) => this.categorias.set(p.content), error: () => undefined });

    effect(() => {
      const deposito = this.depositoId();
      this.tipoClienteId();
      this.version();
      untracked(() => {
        // Otro depósito es otra lista: se vuelve a la primera página
        if (deposito !== this.depositoAnterior) this.numero = 0;
        this.depositoAnterior = deposito;
        this.cargar();
      });
    });
  }

  protected agotado(p: CatalogoProducto): boolean {
    return p.stock !== null && p.stock <= 0;
  }

  protected disponible(p: CatalogoProducto, pr: CatalogoPresentacion): boolean {
    if (this.conPrecios() && pr.precio === null) return false;
    return !this.bloquearAgotados() || p.stock === null || p.stock >= pr.factor;
  }

  protected elegir(producto: CatalogoProducto, presentacion: CatalogoPresentacion | null): void {
    this.elegido.emit({ producto, presentacion });
  }

  protected buscar(texto: string): void {
    this.texto = texto;
    this.irA(0);
  }

  protected elegirCategoria(id: number | null): void {
    this.categoriaId.set(id);
    this.irA(0);
  }

  protected cambiarTamano(tamano: number): void {
    this.tamano = tamano;
    this.irA(0);
  }

  protected irA(numero: number): void {
    this.numero = numero;
    this.cargar();
  }

  private cargar(): void {
    const pedido = ++this.pedido;
    this.service
      .listar({
        depositoId: this.depositoId() ?? undefined,
        tipoClienteId: this.tipoClienteId() ?? undefined,
        categoriaId: this.categoriaId() ?? undefined,
        search: this.texto || undefined,
        conPrecios: this.conPrecios(),
        soloConStock: this.soloConStock(),
        incluirVencidos: this.incluirVencidos(),
        page: this.numero,
        size: this.tamano,
      })
      .subscribe({
        next: (p) => {
          if (pedido !== this.pedido) return; // respuesta obsoleta
          this.pagina.set(p);
          this.error.set(null);
        },
        error: (e) => {
          if (pedido !== this.pedido) return;
          this.error.set(errorMessage(e, 'No se pudo cargar el catálogo'));
        },
      });
  }
}
