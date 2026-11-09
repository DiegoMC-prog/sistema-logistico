import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { errorMessage } from '../../../core/api/api-error';
import { Btn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { EmptyState } from '../../../shared/ui/empty-state';
import { StatCard } from '../../../shared/ui/stat-card';
import { TableSkeleton } from '../../../shared/ui/table-skeleton';
import { Deudor } from '../data/credito.model';
import { CreditosService } from '../data/creditos.service';

@Component({
  selector: 'app-deudores-page',
  imports: [DecimalPipe, Btn, Card, EmptyState, StatCard, TableSkeleton],
  template: `
    <div class="space-y-6">
      <div>
        <h2 class="text-lg font-semibold text-ink">Cuentas por cobrar</h2>
        <p class="mt-1 text-sm text-muted">Clientes con ventas a crédito pendientes de pago. Abre uno para ver su detalle y registrar abonos.</p>
      </div>

      @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }

      <div class="grid gap-4 sm:grid-cols-2">
        <app-stat-card label="Clientes con deuda" [value]="deudores()?.length ?? 0" />
        <app-stat-card label="Total por cobrar" tone="success" [value]="(total() | number: '1.2-2')" />
      </div>

      <app-card class="overflow-hidden">
        @if (deudores(); as lista) {
          @if (lista.length === 0) {
            <app-empty-state />
          } @else {
            <div class="overflow-x-auto">
              <table class="dt">
                <thead><tr><th>Cliente</th><th class="text-right">Ventas pendientes</th><th class="text-right">Deuda</th><th class="text-right">Límite</th><th class="text-right">Acciones</th></tr></thead>
                <tbody>
                  @for (d of lista; track d.clienteId) {
                    <tr>
                      <td>
                        <p class="text-sm font-semibold text-ink">{{ d.clienteNombre }}</p>
                        <p class="mt-0.5 font-mono text-xs text-muted">{{ d.documento || 'Sin documento' }}</p>
                      </td>
                      <td class="text-right text-sm">{{ d.cuentasPendientes }}</td>
                      <td class="text-right text-sm font-semibold text-ink">{{ d.deudaTotal | number: '1.2-2' }}</td>
                      <td class="text-right text-sm text-muted">{{ d.limiteCredito | number: '1.2-2' }}</td>
                      <td class="text-right"><button appBtn variant="secondary" class="!h-8 !px-3 !text-xs" (click)="abrir(d)">Ver cuenta</button></td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        } @else if (!error()) {
          <app-table-skeleton />
        }
      </app-card>
    </div>
  `,
})
export class DeudoresPage {
  private readonly service = inject(CreditosService);
  private readonly router = inject(Router);

  protected readonly deudores = signal<Deudor[] | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly total = computed(() => (this.deudores() ?? []).reduce((s, d) => s + d.deudaTotal, 0));

  constructor() {
    this.service.deudores().subscribe({
      next: (d) => this.deudores.set(d),
      error: (e) => this.error.set(errorMessage(e, 'No se pudieron cargar las cuentas por cobrar')),
    });
  }

  protected abrir(d: Deudor): void {
    this.router.navigate(['/creditos', d.clienteId]);
  }
}
