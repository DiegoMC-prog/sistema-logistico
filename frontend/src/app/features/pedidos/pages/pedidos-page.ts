import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PagedList } from '../../../core/api/paged-list';
import { AuthService } from '../../../core/auth/auth.service';
import { PERMISOS } from '../../../core/auth/permisos';
import { Badge } from '../../../shared/ui/badge';
import { Card } from '../../../shared/ui/card';
import { EmptyState } from '../../../shared/ui/empty-state';
import { FilterSelect } from '../../../shared/ui/filter-select';
import { Paginator } from '../../../shared/ui/paginator';
import { SearchInput } from '../../../shared/ui/search-input';
import { TableSkeleton } from '../../../shared/ui/table-skeleton';
import { Proveedor } from '../../proveedores/data/proveedor.model';
import { ProveedoresService } from '../../proveedores/data/proveedores.service';
import { ESTADOS_PEDIDO, Pedido, PedidoFilters, etiquetaEstadoPedido, toneEstadoPedido } from '../data/pedido.model';
import { PedidosService } from '../data/pedidos.service';

@Component({
  selector: 'app-pedidos-page',
  imports: [DatePipe, DecimalPipe, RouterLink, Badge, Card, EmptyState, FilterSelect, Paginator, SearchInput, TableSkeleton],
  template: `
    <div class="space-y-6">
      <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h2 class="text-lg font-semibold text-ink">Pedidos a proveedores</h2>
          <p class="mt-1 text-sm text-muted">Crea el pedido, envíalo y registra cada recepción: cada línea recibida crea un lote nuevo y suma stock.</p>
        </div>
        @if (puedeCrear) {
          <a routerLink="/pedidos/nuevo" class="inline-flex h-10 items-center justify-center rounded-lg bg-brand px-4 text-sm font-semibold text-white shadow-sm hover:bg-brand-hover">Nuevo pedido</a>
        }
      </div>

      <app-card class="overflow-hidden">
        <div class="flex flex-col gap-3 border-b border-border p-4 lg:flex-row lg:items-center">
          <app-search-input placeholder="Buscar por número (PP-00000007)…" (searched)="list.setFilters({ numero: $event || undefined })" />
          <app-filter-select class="lg:w-status" icon="check-circle" label="Filtrar por estado"
            (changed)="list.setFilters({ estado: $any($event) || undefined })">
            <option value="">Cualquier estado</option>
            @for (e of estados; track e.value) { <option [value]="e.value">{{ e.label }}</option> }
          </app-filter-select>
          @if (proveedores().length > 0) {
            <app-filter-select class="lg:w-status" icon="truck" label="Filtrar por proveedor"
              (changed)="list.setFilters({ proveedorId: $any($event) ? +$event : undefined })">
              <option value="">Todos los proveedores</option>
              @for (p of proveedores(); track p.id) { <option [value]="p.id">{{ p.nombreEmpresa }}</option> }
            </app-filter-select>
          }
          <label class="flex items-center gap-2 text-sm text-muted lg:ml-auto">Desde
            <input type="date" class="field-input !h-10" (change)="list.setFilters({ desde: $any($event.target).value || undefined })" />
          </label>
          <label class="flex items-center gap-2 text-sm text-muted">Hasta
            <input type="date" class="field-input !h-10" (change)="list.setFilters({ hasta: $any($event.target).value || undefined })" />
          </label>
        </div>

        @if (list.error()) {
          <p class="alert-error m-4" role="alert">{{ list.error() }}</p>
        }

        @if (list.data(); as d) {
          @if (d.content.length === 0) {
            <app-empty-state title="Sin pedidos" message="No hay pedidos con esos filtros." />
          } @else {
            <div class="overflow-x-auto">
              <table class="dt">
                <thead><tr><th>Número</th><th>Proveedor</th><th>Destino</th><th>Fecha</th><th>Entrega estimada</th><th>Estado</th><th class="text-right">Total</th></tr></thead>
                <tbody>
                  @for (p of d.content; track p.id) {
                    <tr>
                      <td><a [routerLink]="['/pedidos', p.id]" class="font-mono text-xs font-semibold text-accent hover:underline">{{ p.numeroPedido }}</a></td>
                      <td class="text-sm font-semibold text-ink">{{ p.proveedor.nombre }}</td>
                      <td class="text-sm text-muted">{{ p.depositoDestino.nombre }}</td>
                      <td class="text-sm text-muted">{{ p.fechaPedido | date: 'dd/MM/yyyy' }}</td>
                      <td class="text-sm text-muted">{{ p.fechaEntregaEstimada ? (p.fechaEntregaEstimada | date: 'dd/MM/yyyy') : '—' }}</td>
                      <td><app-badge [tone]="tone(p.estado)">{{ etiqueta(p.estado) }}</app-badge></td>
                      <td class="text-right text-sm font-semibold text-ink">{{ p.total | number: '1.2-2' }}</td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
            <app-paginator [page]="d.page" [size]="d.size" [totalPages]="d.totalPages" [totalElements]="d.totalElements" (pageChange)="list.load($event)" />
          }
        } @else if (list.loading()) {
          <app-table-skeleton />
        }
      </app-card>
    </div>
  `,
})
export class PedidosPage {
  private readonly service = inject(PedidosService);
  private readonly auth = inject(AuthService);
  protected readonly puedeCrear = this.auth.hasPermission(PERMISOS.pedidoCrear);
  protected readonly estados = ESTADOS_PEDIDO;
  protected readonly tone = toneEstadoPedido;
  protected readonly etiqueta = etiquetaEstadoPedido;

  protected readonly list = new PagedList<Pedido, PedidoFilters>((f) => this.service.list(f));
  protected readonly proveedores = signal<Proveedor[]>([]);

  constructor() {
    // Listar proveedores exige PROVEEDOR_VER: sin él, simplemente no se ofrece el filtro.
    if (this.auth.hasPermission(PERMISOS.proveedorVer)) {
      inject(ProveedoresService).list({ size: 100 }).subscribe({ next: (p) => this.proveedores.set(p.content) });
    }
  }
}
