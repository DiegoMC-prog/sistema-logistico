import { Component, computed, inject, signal } from '@angular/core';
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
import { Deposito } from '../../depositos/data/deposito.model';
import { DepositosService } from '../../depositos/data/depositos.service';
import { Caja, CajaFilters } from '../data/caja.model';
import { CajasService } from '../data/cajas.service';

@Component({
  selector: 'app-cajas-page',
  imports: [ReactiveFormsModule, TableSkeleton, FilterSelect, Badge, Btn, IconBtn, Card, ConfirmDialog, EmptyState, Field, Modal, Paginator, SearchInput, StatCard, Toggle],
  template: `
    <div class="space-y-6">
      <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h2 class="text-lg font-semibold text-ink">Gestión de cajas</h2>
          <p class="mt-1 text-sm text-muted">Una caja con turno abierto no se puede desactivar, eliminar ni mover de depósito.</p>
        </div>
        @if (puede.crear) {
          <button appBtn icon="plus" (click)="openForm(null)">Nueva caja</button>
        }
      </div>

      @if (actionError()) {
        <p class="alert-error" role="alert">{{ actionError() }}</p>
      }

      <div class="grid gap-4 sm:grid-cols-3">
        <app-stat-card label="Total" [value]="counts()?.total" />
        <app-stat-card label="Activas" tone="success" [value]="counts()?.activos" />
        <app-stat-card label="Inactivas" tone="muted" [value]="counts()?.inactivos" />
      </div>

      <app-card class="overflow-hidden">
        <div class="flex flex-col gap-3 border-b border-border p-4 lg:flex-row lg:items-center">
          <app-search-input placeholder="Buscar cajas…" (searched)="list.setFilters({ search: $event })" />
          <app-filter-select class="lg:ml-auto lg:w-status" icon="location" label="Filtrar por depósito"
            (changed)="list.setFilters({ depositoId: $any($event) ? +$event : undefined })">
            <option value="">Todos los depósitos</option>
            @for (d of depositos(); track d.id) { <option [value]="d.id">{{ d.nombre }}</option> }
          </app-filter-select>
          <app-filter-select class="lg:w-status" icon="check-circle" label="Filtrar por estado"
            (changed)="list.setFilters({ estado: $any($event) || undefined })">
            <option value="">Cualquier estado</option>
            <option value="ACTIVO">Activas</option>
            <option value="INACTIVO">Inactivas</option>
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
                <thead><tr><th>Caja</th><th>Depósito</th><th>Estado</th><th class="text-right">Acciones</th></tr></thead>
                <tbody>
                  @for (c of d.content; track c.id) {
                    <tr>
                      <td><span class="text-sm font-semibold text-ink">{{ c.nombre }}</span></td>
                      <td class="text-sm text-muted">{{ depositoNombre(c.depositoId) }}</td>
                      <td>
                        <div class="flex items-center gap-3">
                          <app-toggle [checked]="isActivo(c.estado)" [disabled]="togglingId() === c.id || !puede.activar"
                            [label]="'Cambiar estado de ' + c.nombre" (checkedChange)="toggle(c, $event)" />
                          <app-badge [tone]="isActivo(c.estado) ? 'success' : 'neutral'">{{ isActivo(c.estado) ? 'Activa' : 'Inactiva' }}</app-badge>
                        </div>
                      </td>
                      <td>
                        <div class="flex justify-end">
                          @if (puede.editar) { <button appIconBtn icon="edit" [label]="'Editar ' + c.nombre" (click)="openForm(c)"></button> }
                          @if (puede.eliminar) { <button appIconBtn icon="trash" danger [label]="'Eliminar ' + c.nombre" (click)="toDelete.set(c)"></button> }
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
      <app-modal [title]="editing() ? 'Editar caja' : 'Nueva caja'" (closed)="formOpen.set(false)">
        <form [formGroup]="form" (ngSubmit)="save()" id="caja-form">
          <div class="space-y-4 p-5">
            @if (formError()) { <p class="alert-error" role="alert">{{ formError() }}</p> }
            <app-field label="Nombre" [control]="form.controls.nombre" required>
              <input class="field-input" formControlName="nombre" placeholder="Ej. Caja 1" />
            </app-field>
            <app-field label="Depósito" [control]="form.controls.depositoId" required>
              <select class="field-input" formControlName="depositoId">
                <option [ngValue]="null" disabled>Selecciona un depósito</option>
                @for (d of depositosActivos(); track d.id) { <option [ngValue]="d.id">{{ d.nombre }}</option> }
              </select>
            </app-field>
          </div>
        </form>
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="formOpen.set(false)">Cancelar</button>
          <button appBtn type="submit" form="caja-form" [disabled]="saving()">{{ editing() ? 'Guardar cambios' : 'Crear caja' }}</button>
        </ng-container>
      </app-modal>
    }

    @if (toDelete(); as c) {
      <app-confirm-dialog title="¿Eliminar caja?"
        [message]="'Vas a eliminar «' + c.nombre + '». Si tiene turnos históricos, desactívala en su lugar.'"
        confirmLabel="Eliminar caja" [busy]="deleting()" [error]="deleteError()"
        (confirmed)="remove(c)" (cancelled)="toDelete.set(null)" />
    }
  `,
})
export class CajasPage {
  private readonly service = inject(CajasService);
  private readonly depositosService = inject(DepositosService);
  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly isActivo = isActivo;

  protected readonly puede = inject(AuthService).crud(MODULOS.caja);
  protected readonly list = new PagedList<Caja, CajaFilters>((f) => this.service.list(f));
  protected readonly counts = signal<EstadoCounts | null>(null);
  protected readonly depositos = signal<Deposito[]>([]);
  protected readonly depositosActivos = computed(() => {
    const actual = this.editing()?.depositoId;
    return this.depositos().filter((d) => d.estado === 'ACTIVO' || d.id === actual);
  });

  protected readonly formOpen = signal(false);
  protected readonly editing = signal<Caja | null>(null);
  protected readonly saving = signal(false);
  protected readonly formError = signal<string | null>(null);
  protected readonly actionError = signal<string | null>(null);
  protected readonly togglingId = signal<number | null>(null);
  protected readonly toDelete = signal<Caja | null>(null);
  protected readonly deleting = signal(false);
  protected readonly deleteError = signal<string | null>(null);

  protected readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    depositoId: [null as number | null, Validators.required],
  });

  constructor() {
    this.loadCounts();
    this.depositosService.list({ size: 100 }).subscribe({ next: (p) => this.depositos.set(p.content) });
  }

  protected depositoNombre(id: number): string {
    return this.depositos().find((d) => d.id === id)?.nombre ?? `#${id}`;
  }

  private loadCounts(): void {
    estadoCounts((f) => this.service.list(f)).subscribe({ next: (c) => this.counts.set(c) });
  }

  protected openForm(c: Caja | null): void {
    this.editing.set(c);
    this.formError.set(null);
    this.form.reset({ nombre: c?.nombre ?? '', depositoId: c?.depositoId ?? null });
    this.formOpen.set(true);
  }

  protected save(): void {
    if (this.form.invalid) return this.form.markAllAsTouched();
    const v = this.form.getRawValue();
    const body = { nombre: v.nombre.trim(), depositoId: v.depositoId! };
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

  protected toggle(c: Caja, activar: boolean): void {
    this.togglingId.set(c.id);
    this.actionError.set(null);
    (activar ? this.service.activar(c.id) : this.service.desactivar(c.id)).subscribe({
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

  protected remove(c: Caja): void {
    this.deleting.set(true);
    this.deleteError.set(null);
    this.service.delete(c.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.toDelete.set(null);
        this.list.reload();
        this.loadCounts();
      },
      error: (e) => {
        this.deleting.set(false);
        this.deleteError.set(errorMessage(e, 'No se pudo eliminar la caja'));
      },
    });
  }
}
