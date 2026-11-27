import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { errorMessage } from '../../../core/api/api-error';
import { Page } from '../../../core/api/page.model';
import { Badge } from '../../../shared/ui/badge';
import { IconBtn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { EmptyState } from '../../../shared/ui/empty-state';
import { tamanoPagina } from '../../../shared/ui/page-size';
import { Paginator } from '../../../shared/ui/paginator';
import { SearchInput } from '../../../shared/ui/search-input';
import { TableSkeleton } from '../../../shared/ui/table-skeleton';
import { CostoFiltro, HistoricoCosto, VariacionCosto, VariacionProveedor } from '../data/abastecimiento.model';
import { AbastecimientoService } from '../data/abastecimiento.service';

type Vista = 'proveedor' | 'producto';

/**
 * Histórico de costos por proveedor (RF-35): indicador de variación de precios por proveedor y, por producto, la
 * evolución del costo con el detalle de cada compra. Las alzas marcadas son las que el motor de reorden usa para
 * adelantar la compra.
 */
@Component({
  selector: 'app-costos-page',
  imports: [DatePipe, DecimalPipe, RouterLink, Badge, IconBtn, Card, EmptyState, Paginator, SearchInput, TableSkeleton],
  template: `
    <div class="space-y-6">
      <div>
        <h2 class="text-lg font-semibold text-ink">Costos de proveedores</h2>
        <p class="mt-1 text-sm text-muted">
          Variación del costo de compra en los últimos 30 y 90 días, calculada con los pedidos enviados o recibidos.
          Las alzas marcadas hacen que las sugerencias de reorden adelanten la compra.
        </p>
      </div>

      @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }

      <div class="flex flex-wrap gap-2" role="tablist" aria-label="Vista de costos">
        @for (v of vistas; track v.id) {
          <button type="button" role="tab" [attr.aria-selected]="vista() === v.id"
            class="h-10 rounded-lg border px-4 text-sm font-semibold transition"
            [class]="vista() === v.id ? 'border-brand bg-brand text-white' : 'border-border bg-surface text-ink hover:bg-subtle'"
            (click)="vista.set(v.id)">{{ v.label }}</button>
        }
      </div>

      <!-- Las dos vistas se ocultan sin destruirse: conservan la búsqueda y la página al cambiar de pestaña -->
      <app-card role="tabpanel" class="block overflow-hidden" [class.!hidden]="vista() !== 'proveedor'">
        <div class="border-b border-border px-5 py-3">
          <h3 class="text-sm font-semibold text-ink">Indicador por proveedor</h3>
          <p class="text-xs text-muted">Promedio de la variación de los productos que se le compraron. Elija uno para ver sus productos.</p>
        </div>
        @if (proveedores(); as lista) {
          @if (lista.length === 0) {
            <app-empty-state title="Sin compras confirmadas" message="Todavía no hay pedidos enviados o recibidos para calcular variaciones." />
          } @else {
            <div class="overflow-x-auto">
              <table class="dt">
                <thead>
                  <tr>
                    <th>Proveedor</th>
                    <th class="text-right">Productos</th>
                    <th class="text-right">Con alza</th>
                    <th class="text-right">Variación media 30 días</th>
                    <th class="text-right">Variación media 90 días</th>
                    <th class="text-right">Mayor alza 90 días</th>
                  </tr>
                </thead>
                <tbody>
                  @for (p of proveedoresVisibles(); track p.proveedorId) {
                    <tr class="cursor-pointer" [class.bg-accent-soft]="filtro.proveedorId === p.proveedorId" (click)="elegirProveedor(p)">
                      <td class="text-sm font-semibold text-ink">
                        <button type="button" class="text-left hover:underline" [attr.aria-pressed]="filtro.proveedorId === p.proveedorId">{{ p.proveedor }}</button>
                      </td>
                      <td class="text-right text-sm">{{ p.productos }}</td>
                      <td class="text-right text-sm">
                        @if (p.productosConAlza > 0) { <app-badge tone="danger">{{ p.productosConAlza }}</app-badge> } @else { 0 }
                      </td>
                      <td class="text-right text-sm" [class]="tono(p.variacionMedia30)">{{ pct(p.variacionMedia30) }}</td>
                      <td class="text-right text-sm font-semibold" [class]="tono(p.variacionMedia90)">{{ pct(p.variacionMedia90) }}</td>
                      <td class="text-right text-sm" [class]="tono(p.mayorAlza90)">{{ pct(p.mayorAlza90) }}</td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
            @if (lista.length > 10) {
              <app-paginator [page]="paginaProveedores()" [size]="tamanoProveedores()" [totalPages]="totalPaginasProveedores()"
                [totalElements]="lista.length" (pageChange)="paginaProveedores.set($event)" (sizeChange)="cambiarTamanoProveedores($event)" />
            }
          }
        } @else if (!error()) {
          <app-table-skeleton />
        }
      </app-card>

      <div role="tabpanel" class="space-y-4" [class.hidden]="vista() !== 'producto'">
      <div class="flex flex-col gap-3 sm:flex-row sm:items-center">
        <app-search-input class="sm:w-80" placeholder="Buscar producto o proveedor" (searched)="buscar($event)" />
        @if (proveedorElegido(); as p) {
          <div class="flex items-center gap-2 text-sm text-ink">
            Proveedor: <span class="font-semibold">{{ p }}</span>
            <button type="button" class="text-xs font-semibold text-accent hover:underline" (click)="quitarProveedor()">Ver todos</button>
          </div>
        }
      </div>

      <app-card class="overflow-hidden">
        @if (pagina(); as p) {
          @if (p.content.length === 0) {
            <app-empty-state title="Sin datos" message="No hay compras confirmadas para los filtros elegidos." />
          } @else {
            <div class="overflow-x-auto">
              <table class="dt">
                <thead>
                  <tr>
                    <th>Producto</th>
                    <th>Proveedor</th>
                    <th class="text-right">Compras</th>
                    <th class="text-right">Último costo</th>
                    <th class="text-right">Mínimo – máximo</th>
                    <th class="text-right">30 días</th>
                    <th class="text-right">90 días</th>
                    <th class="text-right"><span class="sr-only">Detalle</span></th>
                  </tr>
                </thead>
                <tbody>
                  @for (v of p.content; track v.proveedorId + '-' + v.productoId) {
                    <tr>
                      <td>
                        <p class="text-sm font-semibold text-ink">{{ v.producto }}</p>
                        <p class="mt-0.5 text-xs text-muted">{{ v.categoria }}</p>
                      </td>
                      <td class="text-sm">{{ v.proveedor }}</td>
                      <td class="text-right text-sm">{{ v.compras }}</td>
                      <td class="text-right text-sm">
                        <p class="font-semibold text-ink">{{ v.ultimoCosto | number: '1.2-4' }}</p>
                        <p class="text-xs text-muted">{{ v.ultimaCompra | date: 'dd/MM/yyyy' }}</p>
                      </td>
                      <td class="text-right text-sm text-muted">{{ v.costoMinimo | number: '1.2-4' }} – {{ v.costoMaximo | number: '1.2-4' }}</td>
                      <td class="text-right text-sm" [class]="tono(v.variacion30)">{{ pct(v.variacion30) }}</td>
                      <td class="text-right text-sm">
                        <span class="font-semibold" [class]="tono(v.variacion90)">{{ pct(v.variacion90) }}</span>
                        @if (v.alza) { <p class="mt-0.5"><app-badge tone="danger">Alza</app-badge></p> }
                      </td>
                      <td class="text-right">
                        <button
                          appIconBtn
                          [icon]="esAbierta(v) ? 'close' : 'eye'"
                          [label]="esAbierta(v) ? 'Ocultar compras' : 'Ver compras'"
                          (click)="alternar(v)"
                        ></button>
                      </td>
                    </tr>
                    @if (esAbierta(v)) {
                      <tr class="bg-table-head">
                        <td colspan="8" class="!whitespace-normal">
                          @if (historial(); as h) {
                            <div class="overflow-x-auto rounded-lg border border-border bg-surface">
                              <table class="dt">
                                <thead>
                                  <tr>
                                    <th>Fecha</th>
                                    <th>Pedido</th>
                                    <th class="text-right">Cantidad</th>
                                    <th class="text-right">Costo unitario</th>
                                    <th class="text-right">Contra la compra anterior</th>
                                  </tr>
                                </thead>
                                <tbody>
                                  @for (c of h; track c.detalleId) {
                                    <tr>
                                      <td class="text-sm">{{ c.fecha | date: 'dd/MM/yyyy' }}</td>
                                      <td class="text-sm">
                                        <a class="font-semibold text-accent hover:underline" [routerLink]="['/pedidos', c.pedidoId]">{{ c.numeroPedido }}</a>
                                      </td>
                                      <td class="text-right text-sm">{{ c.cantidad | number: '1.0-2' }}</td>
                                      <td class="text-right text-sm font-semibold text-ink">{{ c.costoUnitario | number: '1.2-4' }}</td>
                                      <td class="text-right text-sm" [class]="tono(c.variacionAnterior)">{{ c.variacionAnterior == null ? 'Primera compra' : pct(c.variacionAnterior) }}</td>
                                    </tr>
                                  }
                                </tbody>
                              </table>
                            </div>
                          } @else {
                            <p class="py-2 text-sm text-muted">Cargando compras…</p>
                          }
                        </td>
                      </tr>
                    }
                  }
                </tbody>
              </table>
            </div>
            @if (p.totalElements > 10) {
              <app-paginator [page]="p.page" [size]="p.size" [totalPages]="p.totalPages" [totalElements]="p.totalElements" (pageChange)="irA($event)" (sizeChange)="cambiarTamano($event)" />
            }
          }
        } @else if (!error()) {
          <app-table-skeleton />
        }
      </app-card>
      </div>
    </div>
  `,
})
export class CostosPage {
  private readonly service = inject(AbastecimientoService);

  protected readonly proveedores = signal<VariacionProveedor[] | null>(null);
  protected readonly pagina = signal<Page<VariacionCosto> | null>(null);

  protected readonly vistas: { id: Vista; label: string }[] = [
    { id: 'proveedor', label: 'Por proveedor' },
    { id: 'producto', label: 'Por producto' },
  ];
  protected readonly vista = signal<Vista>('proveedor');

  protected readonly paginaProveedores = signal(0);
  protected readonly tamanoProveedores = signal(tamanoPagina(10));
  protected readonly totalPaginasProveedores = computed(() => Math.ceil((this.proveedores()?.length ?? 0) / this.tamanoProveedores()));
  protected readonly proveedoresVisibles = computed(() => {
    const desde = this.paginaProveedores() * this.tamanoProveedores();
    return (this.proveedores() ?? []).slice(desde, desde + this.tamanoProveedores());
  });
  protected readonly historial = signal<HistoricoCosto[] | null>(null);
  protected readonly abierta = signal<string | null>(null);
  protected readonly proveedorElegido = signal<string | null>(null);
  protected readonly error = signal<string | null>(null);

  protected filtro: CostoFiltro = { page: 0, size: tamanoPagina(10) };

  constructor() {
    this.service.variacionProveedores().subscribe({
      next: (l) => this.proveedores.set(l),
      error: (e) => this.error.set(errorMessage(e, 'No se pudo cargar el indicador por proveedor')),
    });
    this.cargar();
  }

  /** Fracción a texto con signo: 0.083 -> "+8,3 %". */
  protected pct(v: number | null): string {
    if (v == null) return '—';
    const texto = (v * 100).toLocaleString('es', { minimumFractionDigits: 1, maximumFractionDigits: 1 });
    return `${v > 0 ? '+' : ''}${texto} %`;
  }

  /** Alza en rojo, baja en verde. */
  protected tono(v: number | null): string {
    if (v == null || Math.abs(v) < 0.0005) return 'text-muted';
    return v > 0 ? 'text-danger' : 'text-success';
  }

  protected esAbierta(v: VariacionCosto): boolean {
    return this.abierta() === this.clave(v);
  }

  protected alternar(v: VariacionCosto): void {
    if (this.esAbierta(v)) {
      this.abierta.set(null);
      return;
    }
    this.abierta.set(this.clave(v));
    this.historial.set(null);
    this.service.historialCostos(v.productoId, v.proveedorId).subscribe({
      next: (h) => this.historial.set(h),
      error: (e) => {
        this.abierta.set(null);
        this.error.set(errorMessage(e, 'No se pudieron cargar las compras'));
      },
    });
  }

  /** Lleva a la vista de productos, filtrada por ese proveedor. */
  protected elegirProveedor(p: VariacionProveedor): void {
    this.vista.set('producto');
    if (this.filtro.proveedorId === p.proveedorId) return;
    this.proveedorElegido.set(p.proveedor);
    this.filtro = { ...this.filtro, proveedorId: p.proveedorId, page: 0 };
    this.cargar();
  }

  protected cambiarTamanoProveedores(size: number): void {
    this.tamanoProveedores.set(size);
    this.paginaProveedores.set(0);
  }

  protected quitarProveedor(): void {
    this.proveedorElegido.set(null);
    this.filtro = { ...this.filtro, proveedorId: undefined, page: 0 };
    this.cargar();
  }

  protected buscar(texto: string): void {
    this.filtro = { ...this.filtro, search: texto || undefined, page: 0 };
    this.cargar();
  }

  protected cambiarTamano(size: number): void {
    this.filtro = { ...this.filtro, size, page: 0 };
    this.cargar();
  }

  protected irA(page: number): void {
    this.filtro = { ...this.filtro, page };
    this.cargar();
  }

  private clave(v: VariacionCosto): string {
    return `${v.proveedorId}-${v.productoId}`;
  }

  private cargar(): void {
    this.abierta.set(null);
    this.service.variacionesCosto(this.filtro).subscribe({
      next: (p) => this.pagina.set(p),
      error: (e) => this.error.set(errorMessage(e, 'No se pudieron cargar los costos')),
    });
  }
}
