import { DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { PagedList } from '../../../core/api/paged-list';
import { Card } from '../../../shared/ui/card';
import { EmptyState } from '../../../shared/ui/empty-state';
import { FilterSelect } from '../../../shared/ui/filter-select';
import { Paginator } from '../../../shared/ui/paginator';
import { SearchInput } from '../../../shared/ui/search-input';
import { TableSkeleton } from '../../../shared/ui/table-skeleton';
import { Deposito } from '../../depositos/data/deposito.model';
import { DepositosService } from '../../depositos/data/depositos.service';
import { Stock, StockFilters } from '../data/inventario.model';
import { InventarioService } from '../data/inventario.service';

@Component({
  selector: 'app-stock-page',
  imports: [DecimalPipe, Card, EmptyState, FilterSelect, Paginator, SearchInput, TableSkeleton],
  template: `
    <div class="space-y-6">
      <div>
        <h2 class="text-lg font-semibold text-ink">Existencias</h2>
        <p class="mt-1 text-sm text-muted">Cantidades en unidad base, por lote y depósito.</p>
      </div>

      <app-card class="overflow-hidden">
        <div class="flex flex-col gap-3 border-b border-border p-4 lg:flex-row lg:items-center">
          <app-search-input placeholder="Buscar producto…" (searched)="list.setFilters({ search: $event })" />
          <app-filter-select class="lg:w-status" icon="location" label="Filtrar por depósito"
            (changed)="list.setFilters({ depositoId: $any($event) ? +$event : undefined })">
            <option value="">Todos los depósitos</option>
            @for (d of depositos(); track d.id) { <option [value]="d.id">{{ d.nombre }}</option> }
          </app-filter-select>
          <label class="flex items-center gap-2 text-sm text-ink">
            <input type="checkbox" (change)="list.setFilters({ conStock: $any($event.target).checked || undefined })" /> Solo con stock
          </label>
          <label class="flex items-center gap-2 text-sm text-muted lg:ml-auto">Vence hasta
            <input type="date" class="field-input !h-10" (change)="list.setFilters({ venceHasta: $any($event.target).value || undefined })" />
          </label>
        </div>

        @if (list.error()) { <p class="alert-error m-4" role="alert">{{ list.error() }}</p> }

        @if (list.data(); as d) {
          @if (d.content.length === 0) {
            <app-empty-state title="Sin existencias" message="No hay stock con esos filtros." />
          } @else {
            <div class="overflow-x-auto">
              <table class="dt">
                <thead><tr><th>Producto</th><th>Depósito</th><th>Lote</th><th class="text-right">Stock actual</th></tr></thead>
                <tbody>
                  @for (s of d.content; track s.id) {
                    <tr>
                      <td class="text-sm font-semibold text-ink">{{ s.producto.nombre }}</td>
                      <td class="text-sm text-muted">{{ s.deposito.nombre }}</td>
                      <td class="font-mono text-xs text-muted">{{ s.lote.numeroLote }}</td>
                      <td class="text-right text-sm font-semibold" [class]="s.stockActual > 0 ? 'text-ink' : 'text-danger'">
                        {{ s.stockActual | number: '1.0-4' }} <span class="font-normal text-muted">{{ s.unidadBase }}</span>
                      </td>
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
export class StockPage {
  private readonly service = inject(InventarioService);
  protected readonly list = new PagedList<Stock, StockFilters>((f) => this.service.stock(f));
  protected readonly depositos = signal<Deposito[]>([]);

  constructor() {
    inject(DepositosService).list({ size: 100 }).subscribe({ next: (p) => this.depositos.set(p.content) });
  }
}
