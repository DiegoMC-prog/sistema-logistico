import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, input } from '@angular/core';
import { Badge } from '../../../shared/ui/badge';
import { Venta, nombreMetodo } from '../data/venta.model';

/** Nota de venta lista para mostrar o reimprimir. */
@Component({
  selector: 'app-nota-venta',
  imports: [DatePipe, DecimalPipe, Badge],
  template: `
    @let v = venta();
    <div class="space-y-4 text-sm">
      <div class="flex flex-wrap items-start justify-between gap-2">
        <div>
          <p class="font-mono text-base font-bold text-ink">{{ v.numeroVenta }}</p>
          <p class="text-muted">{{ v.fechaVenta | date: 'dd/MM/yyyy HH:mm' }} · Turno #{{ v.turnoId }}</p>
          <p class="text-muted">Cliente: {{ v.cliente?.nombre ?? 'Sin cliente (precio general)' }}</p>
        </div>
        <app-badge [tone]="v.estado === 'COMPLETADA' ? 'success' : 'danger'">{{ v.estado === 'COMPLETADA' ? 'Completada' : 'Anulada' }}</app-badge>
      </div>

      @if (v.anulacion; as a) {
        <p class="alert-error">Anulada el {{ a.fecha | date: 'dd/MM/yyyy HH:mm' }}: {{ a.motivo }}</p>
      }

      <div class="overflow-x-auto">
        <table class="dt">
          <thead><tr><th>Producto</th><th class="text-right">Cant.</th><th class="text-right">Precio</th><th class="text-right">Desc.</th><th class="text-right">Subtotal</th></tr></thead>
          <tbody>
            @for (l of v.lineas; track l.id) {
              <tr>
                <td>
                  <p class="font-semibold text-ink">{{ l.producto }}</p>
                  @if (l.precioModificado) { <p class="text-xs text-warning-dark">Precio ajustado: {{ l.motivoPrecio }}</p> }
                </td>
                <td class="text-right">{{ l.cantidad }}</td>
                <td class="text-right">{{ l.precioUnitario | number: '1.2-2' }}</td>
                <td class="text-right">{{ l.descuento | number: '1.2-2' }}</td>
                <td class="text-right font-semibold">{{ l.subtotal | number: '1.2-2' }}</td>
              </tr>
            }
          </tbody>
        </table>
      </div>

      <dl class="ml-auto max-w-xs space-y-1">
        <div class="flex justify-between"><dt class="text-muted">Subtotal</dt><dd>{{ v.subtotal | number: '1.2-2' }}</dd></div>
        <div class="flex justify-between"><dt class="text-muted">Descuento por vencimiento</dt><dd>−{{ v.descuentoTotal | number: '1.2-2' }}</dd></div>
        <div class="flex justify-between border-t border-border pt-1 text-base font-bold text-ink"><dt>Total</dt><dd>{{ v.total | number: '1.2-2' }}</dd></div>
      </dl>

      <div>
        <p class="mb-1 text-xs font-semibold uppercase tracking-wide text-muted">Pagos</p>
        <ul class="divide-y divide-border rounded-lg border border-border">
          @for (p of v.pagos; track $index) {
            <li class="flex justify-between px-3 py-2">
              <span>{{ nombre(p.metodoPago) }}@if (p.referencia) { <span class="text-muted"> · {{ p.referencia }}</span> }</span>
              <span class="font-semibold">{{ p.monto | number: '1.2-2' }}</span>
            </li>
          } @empty {
            <li class="px-3 py-2 text-muted">Sin pagos (total 0).</li>
          }
        </ul>
        @if (v.efectivoRecibido !== null) {
          <p class="mt-2 text-right text-muted">
            Efectivo recibido {{ v.efectivoRecibido | number: '1.2-2' }} · Vuelto {{ (v.vuelto ?? 0) | number: '1.2-2' }}
          </p>
        }
      </div>
    </div>
  `,
})
export class NotaVenta {
  readonly venta = input.required<Venta>();
  protected readonly nombre = nombreMetodo;
}
