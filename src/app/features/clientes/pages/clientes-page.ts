import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { errorMessage } from '../../../core/api/api-error';
import { EstadoCounts, estadoCounts, isActivo } from '../../../core/api/estado';
import { PagedList } from '../../../core/api/paged-list';
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
import { TipoCliente } from '../../tipos-cliente/data/tipo-cliente.model';
import { TiposClienteService } from '../../tipos-cliente/data/tipos-cliente.service';
import { Cliente, ClienteFilters } from '../data/cliente.model';
import { ClientesService } from '../data/clientes.service';

@Component({
  selector: 'app-clientes-page',
  imports: [ReactiveFormsModule, TableSkeleton, FilterSelect, Badge, Btn, IconBtn, Card, ConfirmDialog, EmptyState, Field, Modal, Paginator, SearchInput, StatCard, Toggle],
  template: `
    <div class="space-y-6">
      <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h2 class="text-lg font-semibold text-ink">Gestión de clientes</h2>
          <p class="mt-1 text-sm text-muted">Un cliente inactivo no puede comprar. Sin tipo, queda como «GENERAL».</p>
        </div>
        <button appBtn icon="plus" (click)="openForm(null)">Nuevo cliente</button>
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
          <app-search-input placeholder="Buscar por nombre, documento, email o teléfono…" (searched)="list.setFilters({ search: $event })" />
          <app-filter-select class="lg:ml-auto lg:w-status" icon="tag" label="Filtrar por tipo"
            (changed)="list.setFilters({ tipoClienteId: $any($event) ? +$event : undefined })">
            <option value="">Todos los tipos</option>
            @for (t of tipos(); track t.id) { <option [value]="t.id">{{ t.nombre }}</option> }
          </app-filter-select>
          <app-filter-select class="lg:w-status" icon="check-circle" label="Filtrar por estado"
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
                <thead><tr><th>Cliente</th><th>Tipo</th><th>Contacto</th><th>Estado</th><th class="text-right">Acciones</th></tr></thead>
                <tbody>
                  @for (c of d.content; track c.id) {
                    <tr>
                      <td>
                        <p class="text-sm font-semibold text-ink">{{ c.nombre }}</p>
                        <p class="mt-0.5 font-mono text-xs text-muted">{{ c.documento || 'Sin documento' }}</p>
                      </td>
                      <td><app-badge tone="info">{{ c.tipoCliente.nombre }}</app-badge></td>
                      <td class="text-sm text-muted">
                        <p>{{ c.telefono || '—' }}</p>
                        <p>{{ c.email || '—' }}</p>
                      </td>
                      <td>
                        <div class="flex items-center gap-3">
                          <app-toggle [checked]="isActivo(c.estado)" [disabled]="togglingId() === c.id"
                            [label]="'Cambiar estado de ' + c.nombre" (checkedChange)="toggle(c, $event)" />
                          <app-badge [tone]="isActivo(c.estado) ? 'success' : 'neutral'">{{ isActivo(c.estado) ? 'Activo' : 'Inactivo' }}</app-badge>
                        </div>
                      </td>
                      <td>
                        <div class="flex justify-end">
                          <button appIconBtn icon="edit" [label]="'Editar ' + c.nombre" (click)="openForm(c)"></button>
                          <button appIconBtn icon="trash" danger [label]="'Eliminar ' + c.nombre" (click)="toDelete.set(c)"></button>
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
      <app-modal [title]="editing() ? 'Editar cliente' : 'Nuevo cliente'" size="lg" (closed)="formOpen.set(false)">
        <form [formGroup]="form" (ngSubmit)="save()" id="cliente-form">
          <div class="grid gap-4 p-5 sm:grid-cols-2">
            @if (formError()) { <p class="alert-error sm:col-span-2" role="alert">{{ formError() }}</p> }
            <app-field class="sm:col-span-2" label="Nombre" [control]="form.controls.nombre" required>
              <input class="field-input" formControlName="nombre" placeholder="Ej. Juan Pérez" />
            </app-field>
            <app-field label="Documento" [control]="form.controls.documento" optional helper="Debe ser único">
              <input class="field-input" formControlName="documento" />
            </app-field>
            <app-field label="Tipo de cliente" [control]="form.controls.tipoClienteId" optional>
              <select class="field-input" formControlName="tipoClienteId">
                <option [ngValue]="null">GENERAL (por defecto)</option>
                @for (t of tiposActivos(); track t.id) { <option [ngValue]="t.id">{{ t.nombre }}</option> }
              </select>
            </app-field>
            <app-field label="Teléfono" [control]="form.controls.telefono" optional>
              <input class="field-input" formControlName="telefono" />
            </app-field>
            <app-field label="Correo electrónico" [control]="form.controls.email" optional>
              <input type="email" class="field-input" formControlName="email" />
            </app-field>
          </div>
        </form>
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="formOpen.set(false)">Cancelar</button>
          <button appBtn type="submit" form="cliente-form" [disabled]="saving()">{{ editing() ? 'Guardar cambios' : 'Crear cliente' }}</button>
        </ng-container>
      </app-modal>
    }

    @if (toDelete(); as c) {
      <app-confirm-dialog title="¿Eliminar cliente?"
        [message]="'Vas a eliminar a «' + c.nombre + '». Si tiene ventas registradas, desactívalo en su lugar.'"
        confirmLabel="Eliminar cliente" [busy]="deleting()" [error]="deleteError()"
        (confirmed)="remove(c)" (cancelled)="toDelete.set(null)" />
    }
  `,
})
export class ClientesPage {
  private readonly service = inject(ClientesService);
  private readonly tiposService = inject(TiposClienteService);
  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly isActivo = isActivo;

  protected readonly list = new PagedList<Cliente, ClienteFilters>((f) => this.service.list(f));
  protected readonly counts = signal<EstadoCounts | null>(null);
  protected readonly tipos = signal<TipoCliente[]>([]);

  protected readonly formOpen = signal(false);
  protected readonly editing = signal<Cliente | null>(null);
  protected readonly saving = signal(false);
  protected readonly formError = signal<string | null>(null);
  protected readonly actionError = signal<string | null>(null);
  protected readonly togglingId = signal<number | null>(null);
  protected readonly toDelete = signal<Cliente | null>(null);
  protected readonly deleting = signal(false);
  protected readonly deleteError = signal<string | null>(null);

  protected readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    documento: ['', Validators.maxLength(30)],
    tipoClienteId: [null as number | null],
    telefono: ['', Validators.maxLength(30)],
    email: ['', [Validators.email, Validators.maxLength(150)]],
  });

  constructor() {
    this.loadCounts();
    this.tiposService.list({ size: 100 }).subscribe({ next: (p) => this.tipos.set(p.content) });
  }

  /** Tipos asignables: activos (más el actual del cliente que se edita, aunque esté inactivo). */
  protected tiposActivos(): TipoCliente[] {
    const actual = this.editing()?.tipoCliente.id;
    return this.tipos().filter((t) => t.estado === 'ACTIVO' || t.id === actual);
  }

  private loadCounts(): void {
    estadoCounts((f) => this.service.list(f)).subscribe({ next: (c) => this.counts.set(c) });
  }

  protected openForm(c: Cliente | null): void {
    this.editing.set(c);
    this.formError.set(null);
    this.form.reset({
      nombre: c?.nombre ?? '',
      documento: c?.documento ?? '',
      tipoClienteId: c?.tipoCliente.id ?? null,
      telefono: c?.telefono ?? '',
      email: c?.email ?? '',
    });
    this.formOpen.set(true);
  }

  protected save(): void {
    if (this.form.invalid) return this.form.markAllAsTouched();
    const v = this.form.getRawValue();
    const body = {
      nombre: v.nombre.trim(),
      documento: v.documento.trim() || undefined,
      tipoClienteId: v.tipoClienteId ?? undefined,
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

  protected toggle(c: Cliente, activar: boolean): void {
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

  protected remove(c: Cliente): void {
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
        this.deleteError.set(errorMessage(e, 'No se pudo eliminar el cliente'));
      },
    });
  }
}
