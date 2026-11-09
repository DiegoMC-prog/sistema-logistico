import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { errorMessage } from '../../../core/api/api-error';
import { PagedList } from '../../../core/api/paged-list';
import { Badge } from '../../../shared/ui/badge';
import { Btn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { EmptyState } from '../../../shared/ui/empty-state';
import { FilterSelect } from '../../../shared/ui/filter-select';
import { Modal } from '../../../shared/ui/modal';
import { Paginator } from '../../../shared/ui/paginator';
import { TableSkeleton } from '../../../shared/ui/table-skeleton';
import { Deposito } from '../../depositos/data/deposito.model';
import { DepositosService } from '../../depositos/data/depositos.service';
import { MovimientoFilters, MovimientoInv, TIPOS_MOVIMIENTO, etiquetaMovimiento, toneMovimiento } from '../data/inventario.model';
import { InventarioService } from '../data/inventario.service';

@Component({
  selector: 'app-movimientos-page',
  imports: [DatePipe, DecimalPipe, Badge, Btn, Card, EmptyState, FilterSelect, Modal, Paginator, TableSkeleton],
  template: `
    <div class="space-y-6">
      <div>
        <h2 class="text-lg font-semibold text-ink">Historial de movimientos</h2>
        <p class="mt-1 text-sm text-muted">Registro de toda entrada, salida, traslado y ajuste de inventario.</p>
      </div>

      <app-card class="overflow-hidden">
        <div class="flex flex-col gap-3 border-b border-border p-4 lg:flex-row lg:items-center">
          <app-filter-select class="lg:w-status" icon="activity" label="Filtrar por tipo"
            (changed)="list.setFilters({ tipo: $any($event) || undefined })">
            <option value="">Todos los tipos</option>
            @for (t of tipos; track t.value) { <option [value]="t.value">{{ t.label }}</option> }
          </app-filter-select>
          <app-filter-select class="lg:w-status" icon="location" label="Filtrar por depósito"
            (changed)="list.setFilters({ depositoId: $any($event) ? +$event : undefined })">
            <option value="">Todos los depósitos</option>
            @for (d of depositos(); track d.id) { <option [value]="d.id">{{ d.nombre }}</option> }
          </app-filter-select>
          <label class="flex items-center gap-2 text-sm text-muted lg:ml-auto">Desde
            <input type="date" class="field-input !h-10" (change)="list.setFilters({ desde: $any($event.target).value || undefined })" />
          </label>
          <label class="flex items-center gap-2 text-sm text-muted">Hasta
            <input type="date" class="field-input !h-10" (change)="list.setFilters({ hasta: $any($event.target).value || undefined })" />
          </label>
        </div>

        @if (list.error()) { <p class="alert-error m-4" role="alert">{{ list.error() }}</p> }

        @if (list.data(); as d) {
          @if (d.content.length === 0) {
            <app-empty-state title="Sin movimientos" message="No hay movimientos con esos filtros." />
          } @else {
            <div class="overflow-x-auto">
              <table class="dt">
                <thead><tr><th>#</th><th>Fecha</th><th>Tipo</th><th>Origen → Destino</th><th>Usuario</th><th class="text-right">Ítems</th><th></th></tr></thead>
                <tbody>
                  @for (m of d.content; track m.id) {
                    <tr>
                      <td class="font-mono text-xs text-muted">#{{ m.id }}</td>
                      <td class="text-sm text-muted">{{ m.fecha | date: 'dd/MM/yyyy HH:mm' }}</td>
                      <td><app-badge [tone]="tone(m.tipo)">{{ etiqueta(m.tipo) }}</app-badge></td>
                      <td class="text-sm text-ink">{{ m.depositoOrigen?.nombre ?? '—' }} → {{ m.depositoDestino?.nombre ?? '—' }}</td>
                      <td class="text-sm text-muted">{{ m.usuario?.nombre ?? '—' }}</td>
                      <td class="text-right text-sm">{{ m.items.length }}</td>
                      <td class="text-right"><button appBtn variant="ghost" class="!h-8 !px-3 !text-xs" (click)="ver(m)">Detalle</button></td>
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

    @if (abierto()) {
      <app-modal [title]="detalle() ? 'Movimiento #' + detalle()!.id : 'Detalle del movimiento'" size="lg" (closed)="abierto.set(false)">
        <div class="space-y-3 p-5 text-sm">
          @if (detalleError()) { <p class="alert-error" role="alert">{{ detalleError() }}</p> }
          @if (detalle(); as m) {
            <p><app-badge [tone]="tone(m.tipo)">{{ etiqueta(m.tipo) }}</app-badge>
              <span class="ml-2 text-muted">{{ m.fecha | date: 'dd/MM/yyyy HH:mm' }} · {{ m.usuario?.nombre ?? '—' }}</span></p>
            <p class="text-muted">{{ m.depositoOrigen?.nombre ?? '—' }} → {{ m.depositoDestino?.nombre ?? '—' }}</p>
            @if (m.observacion) { <p><span class="text-muted">Observación:</span> {{ m.observacion }}</p> }
            @if (m.referencia) { <p class="text-muted">Referencia: {{ m.referencia.tipo }} #{{ m.referencia.id }}</p> }
            <ul class="divide-y divide-border rounded-lg border border-border">
              @for (i of m.items; track $index) {
                <li class="flex justify-between px-3 py-2">
                  <span><span class="font-semibold text-ink">{{ i.producto.nombre }}</span> <span class="font-mono text-xs text-muted">· lote {{ i.lote.numeroLote }}</span></span>
                  <span class="font-semibold">{{ i.cantidad | number: '1.0-4' }}</span>
                </li>
              }
            </ul>
          } @else if (!detalleError()) {
            <p class="text-muted">Cargando…</p>
          }
        </div>
      </app-modal>
    }
  `,
})
export class MovimientosPage {
  private readonly service = inject(InventarioService);
  protected readonly tipos = TIPOS_MOVIMIENTO;
  protected readonly tone = toneMovimiento;
  protected readonly etiqueta = etiquetaMovimiento;

  protected readonly list = new PagedList<MovimientoInv, MovimientoFilters>((f) => this.service.movimientos(f));
  protected readonly depositos = signal<Deposito[]>([]);
  protected readonly abierto = signal(false);
  protected readonly detalle = signal<MovimientoInv | null>(null);
  protected readonly detalleError = signal<string | null>(null);

  constructor() {
    inject(DepositosService).list({ size: 100 }).subscribe({ next: (p) => this.depositos.set(p.content) });
  }

  protected ver(m: MovimientoInv): void {
    this.detalle.set(m); // el listado ya trae los ítems; se refresca con el detalle
    this.detalleError.set(null);
    this.abierto.set(true);
    this.service.movimiento(m.id).subscribe({
      next: (d) => this.detalle.set(d),
      error: (e) => this.detalleError.set(errorMessage(e, 'No se pudo cargar el detalle')),
    });
  }
}
