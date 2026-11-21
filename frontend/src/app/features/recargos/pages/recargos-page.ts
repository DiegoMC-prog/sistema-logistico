import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { errorMessage } from '../../../core/api/api-error';
import { Page } from '../../../core/api/page.model';
import { Badge } from '../../../shared/ui/badge';
import { Btn, IconBtn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { ConfirmDialog } from '../../../shared/ui/confirm-dialog';
import { EmptyState } from '../../../shared/ui/empty-state';
import { FilterSelect } from '../../../shared/ui/filter-select';
import { Paginator } from '../../../shared/ui/paginator';
import { SearchInput } from '../../../shared/ui/search-input';
import { TableSkeleton } from '../../../shared/ui/table-skeleton';
import { RecargoFormModal } from '../components/recargo-form-modal';
import { ALCANCE_LABEL, AlcanceRecargo, Recargo, RecargoFiltro } from '../data/recargo.model';
import { RecargosService } from '../data/recargos.service';

/** Recargos por escasez (RF-35): activos arriba; los desactivados quedan como historial con su auditoría. */
@Component({
  selector: 'app-recargos-page',
  imports: [DatePipe, DecimalPipe, Badge, Btn, IconBtn, Card, ConfirmDialog, EmptyState, FilterSelect, Paginator, SearchInput, TableSkeleton, RecargoFormModal],
  template: `
    <div class="space-y-6">
      <div class="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h2 class="text-lg font-semibold text-ink">Recargos por escasez</h2>
          <p class="mt-1 text-sm text-muted">
            Mientras un recargo está activo, el punto de venta cobra el precio que corresponda más ese porcentaje.
            Si un producto tiene recargo propio, se usa ese y no el de su categoría.
          </p>
        </div>
        <button appBtn icon="plus" (click)="formAbierto.set(true)">Activar recargo</button>
      </div>

      @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }

      <div class="flex flex-col gap-3 sm:flex-row">
        <app-search-input class="sm:w-72" placeholder="Buscar producto, categoría o motivo" (searched)="buscar($event)" />
        <app-filter-select class="sm:w-52" icon="filter" label="Estado" (changed)="cambiarEstado($event)">
          <option value="">Activos e historial</option>
          <option value="true">Solo activos</option>
          <option value="false">Solo desactivados</option>
        </app-filter-select>
        <app-filter-select class="sm:w-52" icon="tag" label="Aplicado a" (changed)="cambiarAlcance($event)">
          <option value="">Productos y categorías</option>
          <option value="PRODUCTO">Productos</option>
          <option value="CATEGORIA">Categorías</option>
        </app-filter-select>
      </div>

      <app-card class="overflow-hidden">
        @if (pagina(); as p) {
          @if (p.content.length === 0) {
            <app-empty-state title="Sin recargos" message="No hay recargos para los filtros elegidos." />
          } @else {
            <div class="overflow-x-auto">
              <table class="dt">
                <thead>
                  <tr>
                    <th>Aplicado a</th>
                    <th class="text-right">Recargo</th>
                    <th>Motivo</th>
                    <th>Activado</th>
                    <th>Estado</th>
                    <th class="text-right"><span class="sr-only">Acciones</span></th>
                  </tr>
                </thead>
                <tbody>
                  @for (r of p.content; track r.id) {
                    <tr [class.opacity-70]="!r.activo">
                      <td>
                        <p class="text-sm font-semibold text-ink">{{ r.alcance === 'PRODUCTO' ? r.producto : r.categoria }}</p>
                        <p class="mt-0.5 text-xs text-muted">{{ alcanceLabel[r.alcance] }}</p>
                      </td>
                      <td class="text-right text-sm font-bold text-ink">+{{ r.porcentaje * 100 | number: '1.0-2' }} %</td>
                      <td class="max-w-sm !whitespace-normal text-sm">{{ r.motivo }}</td>
                      <td class="text-sm">
                        <p>{{ r.activadoAt | date: 'dd/MM/yyyy HH:mm' }}</p>
                        <p class="text-xs text-muted">por {{ r.activadoPor }}</p>
                      </td>
                      <td>
                        @if (r.activo) {
                          <app-badge tone="danger">Activo</app-badge>
                        } @else {
                          <app-badge tone="neutral">Desactivado</app-badge>
                          <p class="mt-1 text-xs text-muted">{{ r.desactivadoAt | date: 'dd/MM/yyyy HH:mm' }} por {{ r.desactivadoPor }}</p>
                        }
                      </td>
                      <td class="text-right">
                        @if (r.activo) {
                          <button appIconBtn icon="close" label="Desactivar recargo" danger (click)="aDesactivar.set(r)"></button>
                        }
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
            @if (p.totalPages > 1) {
              <app-paginator [page]="p.page" [size]="p.size" [totalPages]="p.totalPages" [totalElements]="p.totalElements" (pageChange)="irA($event)" />
            }
          }
        } @else if (!error()) {
          <app-table-skeleton />
        }
      </app-card>
    </div>

    @if (formAbierto()) {
      <app-recargo-form-modal (saved)="creado()" (closed)="formAbierto.set(false)" />
    }

    @if (aDesactivar(); as r) {
      <app-confirm-dialog
        title="¿Desactivar el recargo?"
        [message]="'Desde ahora ' + (r.alcance === 'PRODUCTO' ? r.producto : 'los productos de ' + r.categoria) + ' se venderán sin el recargo. Queda en el historial.'"
        confirmLabel="Desactivar"
        icon="close"
        [busy]="desactivando()"
        [error]="errorDesactivar()"
        (confirmed)="desactivar(r)"
        (cancelled)="cerrarDesactivar()"
      />
    }
  `,
})
export class RecargosPage {
  private readonly service = inject(RecargosService);

  protected readonly alcanceLabel = ALCANCE_LABEL;
  protected readonly pagina = signal<Page<Recargo> | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly formAbierto = signal(false);
  protected readonly aDesactivar = signal<Recargo | null>(null);
  protected readonly desactivando = signal(false);
  protected readonly errorDesactivar = signal<string | null>(null);

  private filtro: RecargoFiltro = { page: 0, size: 20 };

  constructor() {
    this.cargar();
  }

  protected buscar(texto: string): void {
    this.filtro = { ...this.filtro, search: texto || undefined, page: 0 };
    this.cargar();
  }

  protected cambiarEstado(valor: string): void {
    this.filtro = { ...this.filtro, activo: valor === '' ? undefined : valor === 'true', page: 0 };
    this.cargar();
  }

  protected cambiarAlcance(valor: string): void {
    this.filtro = { ...this.filtro, alcance: (valor || undefined) as AlcanceRecargo | undefined, page: 0 };
    this.cargar();
  }

  protected irA(page: number): void {
    this.filtro = { ...this.filtro, page };
    this.cargar();
  }

  protected creado(): void {
    this.formAbierto.set(false);
    this.cargar();
  }

  protected cerrarDesactivar(): void {
    this.aDesactivar.set(null);
    this.errorDesactivar.set(null);
  }

  protected desactivar(r: Recargo): void {
    this.desactivando.set(true);
    this.errorDesactivar.set(null);
    this.service.desactivar(r.id).subscribe({
      next: () => {
        this.desactivando.set(false);
        this.aDesactivar.set(null);
        this.cargar();
      },
      error: (e) => {
        this.desactivando.set(false);
        this.errorDesactivar.set(errorMessage(e, 'No se pudo desactivar el recargo'));
      },
    });
  }

  private cargar(): void {
    this.service.listar(this.filtro).subscribe({
      next: (p) => this.pagina.set(p),
      error: (e) => this.error.set(errorMessage(e, 'No se pudieron cargar los recargos')),
    });
  }
}
