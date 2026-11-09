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
import { Icon } from '../../../shared/ui/icon';
import { Modal } from '../../../shared/ui/modal';
import { Paginator } from '../../../shared/ui/paginator';
import { SearchInput } from '../../../shared/ui/search-input';
import { TableSkeleton } from '../../../shared/ui/table-skeleton';
import { StatCard } from '../../../shared/ui/stat-card';
import { Toggle } from '../../../shared/ui/toggle';
import { Deposito, DepositoFilters } from '../data/deposito.model';
import { DepositosService } from '../data/depositos.service';

@Component({
  selector: 'app-depositos-page',
  imports: [ReactiveFormsModule, TableSkeleton, FilterSelect, Badge, Btn, IconBtn, Card, ConfirmDialog, EmptyState, Field, Icon, Modal, Paginator, SearchInput, StatCard, Toggle],
  template: `
    <div class="space-y-6">
      <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h2 class="text-lg font-semibold text-ink">Gestión de depósitos</h2>
          <p class="mt-1 text-sm text-muted">Administra las instalaciones de almacenamiento y su estado operativo.</p>
        </div>
        @if (puede.crear) {
          <button appBtn icon="plus" (click)="openForm(null)">Nuevo depósito</button>
        }
      </div>

      @if (actionError()) {
        <p class="alert-error" role="alert">{{ actionError() }}</p>
      }

      <div class="grid gap-4 sm:grid-cols-3">
        <app-stat-card label="Total de depósitos" [value]="counts()?.total" />
        <app-stat-card label="Activos" tone="success" [value]="counts()?.activos" />
        <app-stat-card label="Inactivos" tone="muted" [value]="counts()?.inactivos" />
      </div>

      <app-card class="overflow-hidden">
        <div class="flex flex-col gap-3 border-b border-border p-4 lg:flex-row lg:items-center">
          <app-search-input placeholder="Buscar depósitos…" (searched)="list.setFilters({ search: $event })" />
          <app-filter-select
            class="lg:ml-auto lg:w-status"
            icon="check-circle"
            label="Filtrar por estado"
            (changed)="list.setFilters({ estado: $any($event) || undefined })"
          >
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
                <thead>
                  <tr><th>Depósito</th><th>Descripción</th><th>Estado</th><th class="text-right">Acciones</th></tr>
                </thead>
                <tbody>
                  @for (dep of d.content; track dep.id) {
                    <tr>
                      <td class="min-w-product">
                        <div class="flex items-center gap-3">
                          <div class="grid size-10 place-items-center rounded-lg bg-success-soft text-success">
                            <app-icon name="location" />
                          </div>
                          <div>
                            <p class="text-sm font-semibold text-ink">{{ dep.nombre }}</p>
                            <p class="mt-0.5 font-mono text-xs text-muted">DEP-{{ pad(dep.id) }}</p>
                          </div>
                        </div>
                      </td>
                      <td class="max-w-md text-sm text-muted">{{ dep.descripcion || 'Sin descripción' }}</td>
                      <td>
                        <div class="flex items-center gap-3">
                          <app-toggle
                            [checked]="isActivo(dep.estado)"
                            [disabled]="togglingId() === dep.id || !puede.activar"
                            [label]="'Cambiar estado de ' + dep.nombre"
                            (checkedChange)="toggle(dep, $event)"
                          />
                          <app-badge [tone]="isActivo(dep.estado) ? 'success' : 'neutral'">
                            {{ isActivo(dep.estado) ? 'Activo' : 'Inactivo' }}
                          </app-badge>
                        </div>
                      </td>
                      <td>
                        <div class="flex justify-end">
                          @if (puede.editar) { <button appIconBtn icon="edit" [label]="'Editar ' + dep.nombre" (click)="openForm(dep)"></button> }
                          @if (puede.eliminar) { <button appIconBtn icon="trash" danger [label]="'Eliminar ' + dep.nombre" (click)="toDelete.set(dep)"></button> }
                        </div>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
            <app-paginator
              [page]="d.page" [size]="d.size" [totalPages]="d.totalPages" [totalElements]="d.totalElements"
              (pageChange)="list.load($event)"
            />
          }
        } @else if (list.loading()) {
          <app-table-skeleton />
        }
      </app-card>
    </div>

    @if (formOpen()) {
      <app-modal
        [title]="editing() ? 'Editar depósito' : 'Nuevo depósito'"
        [description]="editing() ? 'Actualiza los datos de la instalación.' : 'Registra una nueva instalación de almacenamiento.'"
        (closed)="formOpen.set(false)"
      >
        <form [formGroup]="form" (ngSubmit)="save()" id="deposito-form">
          <div class="space-y-5 p-5">
            @if (formError()) {
              <p class="alert-error" role="alert">{{ formError() }}</p>
            }
            <app-field label="Nombre del depósito" [control]="form.controls.nombre" required>
              <input class="field-input" formControlName="nombre" placeholder="Ej. Centro de distribución sur" />
            </app-field>
            <app-field label="Descripción" [control]="form.controls.descripcion" optional>
              <textarea class="field-input" formControlName="descripcion" maxlength="500"
                placeholder="Describe el propósito o la zona de servicio…"></textarea>
            </app-field>
          </div>
        </form>
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="formOpen.set(false)">Cancelar</button>
          <button appBtn type="submit" form="deposito-form" [disabled]="saving()">
            {{ editing() ? 'Guardar cambios' : 'Crear depósito' }}
          </button>
        </ng-container>
      </app-modal>
    }

    @if (toDelete(); as dep) {
      <app-confirm-dialog
        title="¿Eliminar depósito?"
        [message]="'Vas a eliminar «' + dep.nombre + '». Esta acción no se puede deshacer.'"
        confirmLabel="Eliminar depósito"
        [busy]="deleting()"
        [error]="deleteError()"
        (confirmed)="remove(dep)"
        (cancelled)="toDelete.set(null)"
      />
    }
  `,
})
export class DepositosPage {
  private readonly service = inject(DepositosService);
  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly isActivo = isActivo;

  protected readonly puede = inject(AuthService).crud(MODULOS.deposito);
  protected readonly list = new PagedList<Deposito, DepositoFilters>((f) => this.service.list(f));
  protected readonly counts = signal<EstadoCounts | null>(null);

  protected readonly formOpen = signal(false);
  protected readonly editing = signal<Deposito | null>(null);
  protected readonly saving = signal(false);
  protected readonly formError = signal<string | null>(null);
  protected readonly actionError = signal<string | null>(null);
  protected readonly togglingId = signal<number | null>(null);
  protected readonly toDelete = signal<Deposito | null>(null);
  protected readonly deleting = signal(false);
  protected readonly deleteError = signal<string | null>(null);

  protected readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    descripcion: ['', Validators.maxLength(500)],
  });

  constructor() {
    this.loadCounts();
  }

  private loadCounts(): void {
    estadoCounts((f) => this.service.list(f)).subscribe({ next: (c) => this.counts.set(c) });
  }

  protected pad(id: number): string {
    return String(id).padStart(3, '0');
  }

  protected openForm(dep: Deposito | null): void {
    this.editing.set(dep);
    this.formError.set(null);
    this.form.reset({ nombre: dep?.nombre ?? '', descripcion: dep?.descripcion ?? '' });
    this.formOpen.set(true);
  }

  protected save(): void {
    if (this.form.invalid) return this.form.markAllAsTouched();
    const v = this.form.getRawValue();
    const body = { nombre: v.nombre.trim(), descripcion: v.descripcion.trim() };
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

  protected toggle(dep: Deposito, activar: boolean): void {
    this.togglingId.set(dep.id);
    this.actionError.set(null);
    (activar ? this.service.activar(dep.id) : this.service.desactivar(dep.id)).subscribe({
      next: () => {
        this.togglingId.set(null);
        this.list.reload();
        this.loadCounts();
      },
      error: (e) => {
        this.togglingId.set(null);
        this.actionError.set(errorMessage(e, 'No se pudo cambiar el estado'));
        this.list.reload(); // restablece la posición del interruptor
      },
    });
  }

  protected remove(dep: Deposito): void {
    this.deleting.set(true);
    this.deleteError.set(null);
    this.service.delete(dep.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.toDelete.set(null);
        this.list.reload();
        this.loadCounts();
      },
      error: (e) => {
        this.deleting.set(false);
        this.deleteError.set(errorMessage(e, 'No se pudo eliminar el depósito'));
      },
    });
  }
}
