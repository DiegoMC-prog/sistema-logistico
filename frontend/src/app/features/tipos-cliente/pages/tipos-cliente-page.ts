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
import { TipoCliente, TipoClienteFilters, esTipoSistema } from '../data/tipo-cliente.model';
import { TiposClienteService } from '../data/tipos-cliente.service';

@Component({
  selector: 'app-tipos-cliente-page',
  imports: [ReactiveFormsModule, TableSkeleton, FilterSelect, Badge, Btn, IconBtn, Card, ConfirmDialog, EmptyState, Field, Modal, Paginator, SearchInput, StatCard, Toggle],
  template: `
    <div class="space-y-6">
      <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h2 class="text-lg font-semibold text-ink">Tipos de cliente</h2>
          <p class="mt-1 text-sm text-muted">Los precios pueden definirse por tipo de cliente. «GENERAL» es un tipo de sistema.</p>
        </div>
        @if (puede.crear) {
          <button appBtn icon="plus" (click)="openForm(null)">Nuevo tipo</button>
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
          <app-search-input placeholder="Buscar tipos…" (searched)="list.setFilters({ search: $event })" />
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
                <thead><tr><th>Nombre</th><th>Estado</th><th class="text-right">Acciones</th></tr></thead>
                <tbody>
                  @for (t of d.content; track t.id) {
                    <tr>
                      <td>
                        <span class="text-sm font-semibold text-ink">{{ t.nombre }}</span>
                        @if (sistema(t)) { <app-badge tone="info" class="ml-2">Sistema</app-badge> }
                      </td>
                      <td>
                        <div class="flex items-center gap-3">
                          <app-toggle [checked]="isActivo(t.estado)" [disabled]="sistema(t) || togglingId() === t.id || !puede.activar"
                            [label]="'Cambiar estado de ' + t.nombre" (checkedChange)="toggle(t, $event)" />
                          <app-badge [tone]="isActivo(t.estado) ? 'success' : 'neutral'">{{ isActivo(t.estado) ? 'Activo' : 'Inactivo' }}</app-badge>
                        </div>
                      </td>
                      <td>
                        <div class="flex justify-end">
                          @if (!sistema(t)) {
                            @if (puede.editar) { <button appIconBtn icon="edit" [label]="'Editar ' + t.nombre" (click)="openForm(t)"></button> }
                            @if (puede.eliminar) { <button appIconBtn icon="trash" danger [label]="'Eliminar ' + t.nombre" (click)="toDelete.set(t)"></button> }
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
      <app-modal [title]="editing() ? 'Editar tipo de cliente' : 'Nuevo tipo de cliente'" (closed)="formOpen.set(false)">
        <form [formGroup]="form" (ngSubmit)="save()" id="tipo-form">
          <div class="space-y-4 p-5">
            @if (formError()) { <p class="alert-error" role="alert">{{ formError() }}</p> }
            <app-field label="Nombre" [control]="form.controls.nombre" helper="Se guarda en mayúsculas" required>
              <input class="field-input" formControlName="nombre" placeholder="Ej. Mayorista" />
            </app-field>
          </div>
        </form>
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="formOpen.set(false)">Cancelar</button>
          <button appBtn type="submit" form="tipo-form" [disabled]="saving()">{{ editing() ? 'Guardar cambios' : 'Crear tipo' }}</button>
        </ng-container>
      </app-modal>
    }

    @if (toDelete(); as t) {
      <app-confirm-dialog title="¿Eliminar tipo de cliente?"
        [message]="'Vas a eliminar «' + t.nombre + '». Si está en uso, desactívalo en su lugar.'"
        confirmLabel="Eliminar tipo" [busy]="deleting()" [error]="deleteError()"
        (confirmed)="remove(t)" (cancelled)="toDelete.set(null)" />
    }
  `,
})
export class TiposClientePage {
  private readonly service = inject(TiposClienteService);
  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly isActivo = isActivo;
  protected readonly sistema = esTipoSistema;

  protected readonly puede = inject(AuthService).crud(MODULOS.cliente);
  protected readonly list = new PagedList<TipoCliente, TipoClienteFilters>((f) => this.service.list(f));
  protected readonly counts = signal<EstadoCounts | null>(null);

  protected readonly formOpen = signal(false);
  protected readonly editing = signal<TipoCliente | null>(null);
  protected readonly saving = signal(false);
  protected readonly formError = signal<string | null>(null);
  protected readonly actionError = signal<string | null>(null);
  protected readonly togglingId = signal<number | null>(null);
  protected readonly toDelete = signal<TipoCliente | null>(null);
  protected readonly deleting = signal(false);
  protected readonly deleteError = signal<string | null>(null);

  protected readonly form = this.fb.group({ nombre: ['', [Validators.required, Validators.maxLength(100)]] });

  constructor() {
    this.loadCounts();
  }

  private loadCounts(): void {
    estadoCounts((f) => this.service.list(f)).subscribe({ next: (c) => this.counts.set(c) });
  }

  protected openForm(t: TipoCliente | null): void {
    this.editing.set(t);
    this.formError.set(null);
    this.form.reset({ nombre: t?.nombre ?? '' });
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

  protected toggle(t: TipoCliente, activar: boolean): void {
    this.togglingId.set(t.id);
    this.actionError.set(null);
    (activar ? this.service.activar(t.id) : this.service.desactivar(t.id)).subscribe({
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

  protected remove(t: TipoCliente): void {
    this.deleting.set(true);
    this.deleteError.set(null);
    this.service.delete(t.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.toDelete.set(null);
        this.list.reload();
        this.loadCounts();
      },
      error: (e) => {
        this.deleting.set(false);
        this.deleteError.set(errorMessage(e, 'No se pudo eliminar el tipo de cliente'));
      },
    });
  }
}
