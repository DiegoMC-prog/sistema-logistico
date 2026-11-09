import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PagedList } from '../../../core/api/paged-list';
import { Badge } from '../../../shared/ui/badge';
import { Card } from '../../../shared/ui/card';
import { EmptyState } from '../../../shared/ui/empty-state';
import { FilterSelect } from '../../../shared/ui/filter-select';
import { Paginator } from '../../../shared/ui/paginator';
import { SearchInput } from '../../../shared/ui/search-input';
import { TableSkeleton } from '../../../shared/ui/table-skeleton';
import { VentaFilters, VentaResumen } from '../data/venta.model';
import { VentasService } from '../data/ventas.service';

@Component({
  selector: 'app-ventas-page',
  imports: [DatePipe, DecimalPipe, RouterLink, Badge, Card, EmptyState, FilterSelect, Paginator, SearchInput, TableSkeleton],
  template: `
    <div class="space-y-6">
      <div>
        <h2 class="text-lg font-semibold text-ink">Historial de ventas</h2>
        <p class="mt-1 text-sm text-muted">Con permiso de ver ventas aparecen todas; si solo puedes vender, únicamente las tuyas.</p>
      </div>

      <app-card class="overflow-hidden">
        <div class="flex flex-col gap-3 border-b border-border p-4 lg:flex-row lg:items-center">
          <app-search-input placeholder="Buscar por número (V-00000041)…" (searched)="list.setFilters({ numero: $event || undefined })" />
          <app-filter-select class="lg:w-status" icon="check-circle" label="Filtrar por estado"
            (changed)="list.setFilters({ estado: $any($event) || undefined })">
            <option value="">Cualquier estado</option>
            <option value="COMPLETADA">Completadas</option>
            <option value="ANULADA">Anuladas</option>
          </app-filter-select>
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
            <app-empty-state title="Sin ventas" message="No hay ventas con esos filtros." />
          } @else {
            <div class="overflow-x-auto">
              <table class="dt">
                <thead><tr><th>Número</th><th>Fecha</th><th>Turno</th><th>Estado</th><th class="text-right">Total</th></tr></thead>
                <tbody>
                  @for (v of d.content; track v.id) {
                    <tr>
                      <td><a [routerLink]="['/ventas', v.id]" class="font-mono text-xs font-semibold text-accent hover:underline">{{ v.numeroVenta }}</a></td>
                      <td class="text-sm text-muted">{{ v.fechaVenta | date: 'dd/MM/yyyy HH:mm' }}</td>
                      <td class="text-sm text-muted"><a [routerLink]="['/turnos', v.turnoId]" class="hover:underline">#{{ v.turnoId }}</a></td>
                      <td><app-badge [tone]="v.estado === 'COMPLETADA' ? 'success' : 'danger'">{{ v.estado === 'COMPLETADA' ? 'Completada' : 'Anulada' }}</app-badge></td>
                      <td class="text-right text-sm font-semibold text-ink">{{ v.total | number: '1.2-2' }}</td>
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
export class VentasPage {
  private readonly service = inject(VentasService);
  protected readonly list = new PagedList<VentaResumen, VentaFilters>((f) => this.service.list(f));
}
