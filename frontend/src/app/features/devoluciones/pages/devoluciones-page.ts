import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { errorMessage } from '../../../core/api/api-error';
import { PagedList } from '../../../core/api/paged-list';
import { Btn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { EmptyState } from '../../../shared/ui/empty-state';
import { Modal } from '../../../shared/ui/modal';
import { Paginator } from '../../../shared/ui/paginator';
import { TableSkeleton } from '../../../shared/ui/table-skeleton';
import { Devolucion, DevolucionFilters } from '../data/devolucion.model';
import { DevolucionesService } from '../data/devoluciones.service';

@Component({
  selector: 'app-devoluciones-page',
  imports: [DatePipe, DecimalPipe, RouterLink, Btn, Card, EmptyState, Modal, Paginator, TableSkeleton],
  template: `
    <div class="space-y-6">
      <div>
        <h2 class="text-lg font-semibold text-ink">Devoluciones y cambios</h2>
        <p class="mt-1 text-sm text-muted">Para registrar una devolución abre la venta correspondiente desde el historial de ventas.</p>
      </div>

      <app-card class="overflow-hidden">
        <div class="flex flex-col gap-3 border-b border-border p-4 sm:flex-row sm:items-center sm:justify-end">
          <label class="flex items-center gap-2 text-sm text-muted">Desde
            <input type="date" class="field-input !h-10" (change)="list.setFilters({ desde: $any($event.target).value || undefined })" />
          </label>
          <label class="flex items-center gap-2 text-sm text-muted">Hasta
            <input type="date" class="field-input !h-10" (change)="list.setFilters({ hasta: $any($event.target).value || undefined })" />
          </label>
        </div>

        @if (list.error()) { <p class="alert-error m-4" role="alert">{{ list.error() }}</p> }

        @if (list.data(); as d) {
          @if (d.content.length === 0) {
            <app-empty-state title="Sin devoluciones" message="No hay devoluciones con esos filtros." />
          } @else {
            <div class="overflow-x-auto">
              <table class="dt">
                <thead><tr><th>Número</th><th>Fecha</th><th>Venta</th><th>Motivo</th><th class="text-right">Reembolsado</th><th></th></tr></thead>
                <tbody>
                  @for (x of d.content; track x.id) {
                    <tr>
                      <td class="font-mono text-xs font-semibold text-ink">{{ x.numeroDevolucion }}</td>
                      <td class="text-sm text-muted">{{ x.fecha | date: 'dd/MM/yyyy HH:mm' }}</td>
                      <td><a [routerLink]="['/ventas', x.ventaId]" class="text-sm text-accent hover:underline">Ver venta #{{ x.ventaId }}</a></td>
                      <td class="max-w-xs truncate text-sm text-muted">{{ x.motivo }}</td>
                      <td class="text-right text-sm font-semibold text-ink">{{ x.montoReembolsado | number: '1.2-2' }}</td>
                      <td class="text-right"><button appBtn variant="ghost" class="!h-8 !px-3 !text-xs" (click)="ver(x)">Detalle</button></td>
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

    @if (abierta()) {
      <app-modal [title]="detalle()?.numeroDevolucion ?? 'Detalle de devolución'" (closed)="abierta.set(false)">
        <div class="space-y-3 p-5 text-sm">
          @if (detalleError()) { <p class="alert-error" role="alert">{{ detalleError() }}</p> }
          @if (detalle(); as d) {
            <p class="text-muted">{{ d.fecha | date: 'dd/MM/yyyy HH:mm' }} · Turno #{{ d.turnoId }}</p>
            <p><span class="text-muted">Motivo:</span> {{ d.motivo }}</p>
            <p><span class="text-muted">Reembolsado:</span> <strong class="text-ink">{{ d.montoReembolsado | number: '1.2-2' }}</strong>
              @if (d.metodoPago) { ({{ d.metodoPago.nombre }}) }</p>
            @if (d.ventaCambio; as vc) {
              <p><span class="text-muted">Venta nueva del cambio:</span>
                <a [routerLink]="['/ventas', vc.id]" class="text-accent hover:underline">{{ vc.numeroVenta }}</a> ({{ vc.total | number: '1.2-2' }})</p>
            }
            @if (d.lineas?.length) {
              <ul class="divide-y divide-border rounded-lg border border-border">
                @for (l of d.lineas; track $index) {
                  <li class="flex justify-between px-3 py-2">
                    <span>Línea #{{ l.ventaDetalleId }} · lote {{ l.loteId }}</span>
                    <span>{{ l.cantidadBase }} {{ l.reingresaStock ? '(reingresa)' : '(no reingresa)' }}</span>
                  </li>
                }
              </ul>
            }
          } @else if (!detalleError()) {
            <p class="text-muted">Cargando…</p>
          }
        </div>
      </app-modal>
    }
  `,
})
export class DevolucionesPage {
  private readonly service = inject(DevolucionesService);
  protected readonly list = new PagedList<Devolucion, DevolucionFilters>((f) => this.service.list(f));

  protected readonly abierta = signal(false);
  protected readonly detalle = signal<Devolucion | null>(null);
  protected readonly detalleError = signal<string | null>(null);

  /** Los listados vienen sin método de pago ni venta nueva: el detalle completo se pide aparte. */
  protected ver(x: Devolucion): void {
    this.detalle.set(null);
    this.detalleError.set(null);
    this.abierta.set(true);
    this.service.get(x.id).subscribe({
      next: (d) => this.detalle.set(d),
      error: (e) => this.detalleError.set(errorMessage(e, 'No se pudo cargar la devolución')),
    });
  }
}
