import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { errorMessage } from '../../../core/api/api-error';
import { EstadoCounts, estadoCounts, isActivo } from '../../../core/api/estado';
import { PagedList } from '../../../core/api/paged-list';
import { AuthService } from '../../../core/auth/auth.service';
import { MODULOS } from '../../../core/auth/permisos';
import { Badge } from '../../../shared/ui/badge';
import { Btn, IconBtn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { ConfirmDialog } from '../../../shared/ui/confirm-dialog';
import { EmptyState } from '../../../shared/ui/empty-state';
import { Field } from '../../../shared/ui/field';
import { FilterSelect } from '../../../shared/ui/filter-select';
import { Modal } from '../../../shared/ui/modal';
import { Paginator } from '../../../shared/ui/paginator';
import { SearchInput } from '../../../shared/ui/search-input';
import { StatCard } from '../../../shared/ui/stat-card';
import { TableSkeleton } from '../../../shared/ui/table-skeleton';
import { Toggle } from '../../../shared/ui/toggle';
import { MetodoPago, MetodoPagoFilters, esMetodoSistema } from '../data/metodo-pago.model';
import { MetodosPagoService } from '../data/metodos-pago.service';

@Component({
  selector: 'app-metodos-pago-page',
  imports: [ReactiveFormsModule, TableSkeleton, FilterSelect, Badge, Btn, IconBtn, Card, ConfirmDialog, EmptyState, Field, Modal, Paginator, SearchInput, StatCard, Toggle],
  template: `
    <div class="space-y-6">
      <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h2 class="text-lg font-semibold text-ink">Métodos de pago</h2>
          <p class="mt-1 text-sm text-muted">«EFECTIVO» y «CREDITO_DEVOLUCION» son de sistema; el crédito de devolución no se ofrece al cobrar.</p>
        </div>
        @if (puede.crear) {
          <button appBtn icon="plus" (click)="openForm(null)">Nuevo método</button>
        }
      </div>

      @if (actionError()) {
        <p class="alert-error" role="alert">{{ actionError() }}</p>
      }

      <div class="grid gap-4 sm:grid-cols-3">
        <app-stat-card label="Total" [value]="counts()?.total" />
        <app-stat-card label="Activos" tone="success" [value]="counts()?.activos" />
        <app-stat-card label="Inactivos" tone="muted" [value]="counts()?.inactivos" />
      </div>

      <app-card class="overflow-hidden">
        <div class="flex flex-col gap-3 border-b border-border p-4 lg:flex-row lg:items-center">
          <app-search-input placeholder="Buscar métodos…" (searched)="list.setFilters({ search: $event })" />
          <app-filter-select class="lg:ml-auto lg:w-status" icon="check-circle" label="Filtrar por estado"
            (changed)="list.setFilters({ estado: $any($event) || undefined })">
            <option value="">Cualquier estado</option>
            <option value="ACTIVO">Activos</option>
            <option value="INACTIVO">Inactivos</option>
          </app-filter-select>
        </div>

        @if (list.error()) {
          <p class="alert-error m-4" role="alert">{{ list.error() }}</p>
        }

        @if (list.data(); as d) {
          @if (d.content.length === 0) {
            <app-empty-state />
          } @else {
            <div class="overflow-x-auto">
              <table class="dt">
                <thead><tr><th>Nombre</th><th>Uso</th><th>Estado</th><th class="text-right">Acciones</th></tr></thead>
                <tbody>
                  @for (m of d.content; track m.id) {
                    <tr>
                      <td>
                        <span class="text-sm font-semibold text-ink">{{ m.nombre }}</span>
                        @if (sistema(m)) { <app-badge tone="info" class="ml-2">Sistema</app-badge> }
                      </td>
                      <td class="text-sm text-muted">{{ m.paraCobro ? 'Cobro y reembolso' : 'Solo interno' }}</td>
                      <td>
                        <div class="flex items-center gap-3">
                          <app-toggle [checked]="isActivo(m.estado)" [disabled]="sistema(m) || togglingId() === m.id || !puede.activar"
                            [label]="'Cambiar estado de ' + m.nombre" (checkedChange)="toggle(m, $event)" />
                          <app-badge [tone]="isActivo(m.estado) ? 'success' : 'neutral'">{{ isActivo(m.estado) ? 'Activo' : 'Inactivo' }}</app-badge>
                        </div>
                      </td>
                      <td>
                        <div class="flex justify-end">
                          @if (!sistema(m)) {
                            @if (puede.editar) { <button appIconBtn icon="edit" [label]="'Editar ' + m.nombre" (click)="openForm(m)"></button> }
                            @if (puede.eliminar) { <button appIconBtn icon="trash" danger [label]="'Eliminar ' + m.nombre" (click)="toDelete.set(m)"></button> }
                          }
                        </div>
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

    @if (formOpen()) {
      <app-modal [title]="editing() ? 'Editar método de pago' : 'Nuevo método de pago'" (closed)="formOpen.set(false)">
        <form [formGroup]="form" (ngSubmit)="save()" id="metodo-form">
          <div class="space-y-4 p-5">
            @if (formError()) { <p class="alert-error" role="alert">{{ formError() }}</p> }
            <app-field label="Nombre" [control]="form.controls.nombre" helper="Se guarda en mayúsculas" required>
              <input class="field-input" formControlName="nombre" placeholder="Ej. Billetera digital" />
            </app-field>
          </div>
        </form>
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="formOpen.set(false)">Cancelar</button>
          <button appBtn type="submit" form="metodo-form" [disabled]="saving()">{{ editing() ? 'Guardar cambios' : 'Crear método' }}</button>
        </ng-container>
      </app-modal>
    }

    @if (toDelete(); as m) {
      <app-confirm-dialog title="¿Eliminar método de pago?"
        [message]="'Vas a eliminar «' + m.nombre + '». Si ya se usó en ventas, desactívalo en su lugar.'"
        confirmLabel="Eliminar método" [busy]="deleting()" [error]="deleteError()"
        (confirmed)="remove(m)" (cancelled)="toDelete.set(null)" />
    }
  `,
})
export class MetodosPagoPage {
  private readonly service = inject(MetodosPagoService);
  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly isActivo = isActivo;
  protected readonly sistema = esMetodoSistema;

  protected readonly puede = inject(AuthService).crud(MODULOS.metodoPago);
  protected readonly list = new PagedList<MetodoPago, MetodoPagoFilters>((f) => this.service.list(f));
  protected readonly counts = signal<EstadoCounts | null>(null);

  protected readonly formOpen = signal(false);
  protected readonly editing = signal<MetodoPago | null>(null);
  protected readonly saving = signal(false);
  protected readonly formError = signal<string | null>(null);
  protected readonly actionError = signal<string | null>(null);
  protected readonly togglingId = signal<number | null>(null);
  protected readonly toDelete = signal<MetodoPago | null>(null);
  protected readonly deleting = signal(false);
  protected readonly deleteError = signal<string | null>(null);

  protected readonly form = this.fb.group({ nombre: ['', [Validators.required, Validators.maxLength(100)]] });

  constructor() {
    this.loadCounts();
  }

  private loadCounts(): void {
    estadoCounts((f) => this.service.list(f)).subscribe({ next: (c) => this.counts.set(c) });
  }

  protected openForm(m: MetodoPago | null): void {
    this.editing.set(m);
    this.formError.set(null);
    this.form.reset({ nombre: m?.nombre ?? '' });
    this.formOpen.set(true);
  }

  protected save(): void {
    if (this.form.invalid) return this.form.markAllAsTouched();
    const body = { nombre: this.form.getRawValue().nombre.trim() };
    const current = this.editing();
    this.saving.set(true);
    this.formError.set(null);
    (current ? this.service.update(current.id, body) : this.service.create(body)).subscribe({
      next: () => {
        this.saving.set(false);
        this.formOpen.set(false);
        current ? this.list.reload() : this.list.load(0);
        this.loadCounts();
      },
      error: (e) => {
        this.saving.set(false);
        this.formError.set(errorMessage(e));
      },
    });
  }

  protected toggle(m: MetodoPago, activar: boolean): void {
    this.togglingId.set(m.id);
    this.actionError.set(null);
    (activar ? this.service.activar(m.id) : this.service.desactivar(m.id)).subscribe({
      next: () => {
        this.togglingId.set(null);
        this.list.reload();
        this.loadCounts();
      },
      error: (e) => {
        this.togglingId.set(null);
        this.actionError.set(errorMessage(e, 'No se pudo cambiar el estado'));
        this.list.reload();
      },
    });
  }

  protected remove(m: MetodoPago): void {
    this.deleting.set(true);
    this.deleteError.set(null);
    this.service.delete(m.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.toDelete.set(null);
        this.list.reload();
        this.loadCounts();
      },
      error: (e) => {
        this.deleting.set(false);
        this.deleteError.set(errorMessage(e, 'No se pudo eliminar el método de pago'));
      },
    });
  }
}
