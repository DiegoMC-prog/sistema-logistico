import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { errorMessage } from '../../../core/api/api-error';
import { EstadoCounts, estadoCounts, isActivo } from '../../../core/api/estado';
import { PagedList } from '../../../core/api/paged-list';
import { AuthService } from '../../../core/auth/auth.service';
import { PERMISOS } from '../../../core/auth/permisos';
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
import { Proveedor, ProveedorFilters } from '../data/proveedor.model';
import { ProveedoresService } from '../data/proveedores.service';

@Component({
  selector: 'app-proveedores-page',
  imports: [ReactiveFormsModule, TableSkeleton, FilterSelect, Badge, Btn, IconBtn, Card, ConfirmDialog, EmptyState, Field, Modal, Paginator, SearchInput, StatCard, Toggle],
  template: `
    <div class="space-y-6">
      <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h2 class="text-lg font-semibold text-ink">Gestión de proveedores</h2>
          <p class="mt-1 text-sm text-muted">Un proveedor inactivo no se puede usar en pedidos nuevos. Si tiene pedidos, desactívalo en lugar de eliminarlo.</p>
        </div>
        @if (puede.crear) {
          <button appBtn icon="plus" (click)="openForm(null)">Nuevo proveedor</button>
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
          <app-search-input placeholder="Buscar por empresa, contacto o correo…" (searched)="list.setFilters({ search: $event })" />
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
                <thead><tr><th>Empresa</th><th>Contacto</th><th>Estado</th><th class="text-right">Acciones</th></tr></thead>
                <tbody>
                  @for (p of d.content; track p.id) {
                    <tr>
                      <td class="text-sm font-semibold text-ink">{{ p.nombreEmpresa }}</td>
                      <td class="text-sm text-muted">
                        <p class="text-ink">{{ p.nombreContacto }}</p>
                        <p>{{ p.telefono || '—' }} · {{ p.email || '—' }}</p>
                      </td>
                      <td>
                        <div class="flex items-center gap-3">
                          <app-toggle [checked]="isActivo(p.estado)" [disabled]="togglingId() === p.id || !puede.activar"
                            [label]="'Cambiar estado de ' + p.nombreEmpresa" (checkedChange)="toggle(p, $event)" />
                          <app-badge [tone]="isActivo(p.estado) ? 'success' : 'neutral'">{{ isActivo(p.estado) ? 'Activo' : 'Inactivo' }}</app-badge>
                        </div>
                      </td>
                      <td>
                        <div class="flex justify-end">
                          @if (puede.editar) { <button appIconBtn icon="edit" [label]="'Editar ' + p.nombreEmpresa" (click)="openForm(p)"></button> }
                          @if (puede.eliminar) { <button appIconBtn icon="trash" danger [label]="'Eliminar ' + p.nombreEmpresa" (click)="toDelete.set(p)"></button> }
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
      <app-modal [title]="editing() ? 'Editar proveedor' : 'Nuevo proveedor'" (closed)="formOpen.set(false)">
        <form [formGroup]="form" (ngSubmit)="save()" id="proveedor-form">
          <div class="space-y-4 p-5">
            @if (formError()) { <p class="alert-error" role="alert">{{ formError() }}</p> }
            <app-field label="Empresa" [control]="form.controls.nombreEmpresa" required>
              <input class="field-input" formControlName="nombreEmpresa" placeholder="Ej. Distribuidora Sur" />
            </app-field>
            <app-field label="Nombre del contacto" [control]="form.controls.nombreContacto" required>
              <input class="field-input" formControlName="nombreContacto" placeholder="Ej. Ana Pérez" />
            </app-field>
            <div class="grid gap-4 sm:grid-cols-2">
              <app-field label="Teléfono" [control]="form.controls.telefono" optional>
                <input class="field-input" formControlName="telefono" />
              </app-field>
              <app-field label="Correo electrónico" [control]="form.controls.email" optional>
                <input type="email" class="field-input" formControlName="email" />
              </app-field>
            </div>
          </div>
        </form>
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="formOpen.set(false)">Cancelar</button>
          <button appBtn type="submit" form="proveedor-form" [disabled]="saving()">{{ editing() ? 'Guardar cambios' : 'Crear proveedor' }}</button>
        </ng-container>
      </app-modal>
    }

    @if (toDelete(); as p) {
      <app-confirm-dialog title="¿Eliminar proveedor?"
        [message]="'Vas a eliminar a «' + p.nombreEmpresa + '». Si tiene pedidos, el servidor lo rechazará: desactívalo en su lugar.'"
        confirmLabel="Eliminar proveedor" [busy]="deleting()" [error]="deleteError()"
        (confirmed)="remove(p)" (cancelled)="toDelete.set(null)" />
    }
  `,
})
export class ProveedoresPage {
  private readonly service = inject(ProveedoresService);
  private readonly fb = inject(FormBuilder).nonNullable;
  private readonly auth = inject(AuthService);
  protected readonly isActivo = isActivo;
  protected readonly puede = {
    crear: this.auth.hasPermission(PERMISOS.proveedorCrear),
    editar: this.auth.hasPermission(PERMISOS.proveedorEditar),
    eliminar: this.auth.hasPermission(PERMISOS.proveedorEliminar),
    activar: this.auth.hasPermission(PERMISOS.proveedorActivar),
  };

  protected readonly list = new PagedList<Proveedor, ProveedorFilters>((f) => this.service.list(f));
  protected readonly counts = signal<EstadoCounts | null>(null);

  protected readonly formOpen = signal(false);
  protected readonly editing = signal<Proveedor | null>(null);
  protected readonly saving = signal(false);
  protected readonly formError = signal<string | null>(null);
  protected readonly actionError = signal<string | null>(null);
  protected readonly togglingId = signal<number | null>(null);
  protected readonly toDelete = signal<Proveedor | null>(null);
  protected readonly deleting = signal(false);
  protected readonly deleteError = signal<string | null>(null);

  protected readonly form = this.fb.group({
    nombreEmpresa: ['', [Validators.required, Validators.maxLength(150)]],
    nombreContacto: ['', [Validators.required, Validators.maxLength(150)]],
    telefono: ['', Validators.maxLength(30)],
    email: ['', [Validators.email, Validators.maxLength(150)]],
  });

  constructor() {
    this.loadCounts();
  }

  private loadCounts(): void {
    estadoCounts((f) => this.service.list(f)).subscribe({ next: (c) => this.counts.set(c) });
  }

  protected openForm(p: Proveedor | null): void {
    this.editing.set(p);
    this.formError.set(null);
    this.form.reset({
      nombreEmpresa: p?.nombreEmpresa ?? '',
      nombreContacto: p?.nombreContacto ?? '',
      telefono: p?.telefono ?? '',
      email: p?.email ?? '',
    });
    this.formOpen.set(true);
  }

  protected save(): void {
    if (this.form.invalid) return this.form.markAllAsTouched();
    const v = this.form.getRawValue();
    const body = {
      nombreEmpresa: v.nombreEmpresa.trim(),
      nombreContacto: v.nombreContacto.trim(),
      telefono: v.telefono.trim() || undefined,
      email: v.email.trim() || undefined,
    };
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

  protected toggle(p: Proveedor, activar: boolean): void {
    this.togglingId.set(p.id);
    this.actionError.set(null);
    (activar ? this.service.activar(p.id) : this.service.desactivar(p.id)).subscribe({
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

  protected remove(p: Proveedor): void {
    this.deleting.set(true);
    this.deleteError.set(null);
    this.service.delete(p.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.toDelete.set(null);
        this.list.reload();
        this.loadCounts();
      },
      error: (e) => {
        this.deleting.set(false);
        this.deleteError.set(errorMessage(e, 'No se pudo eliminar el proveedor'));
      },
    });
  }
}
