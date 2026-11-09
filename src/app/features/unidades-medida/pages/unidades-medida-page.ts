import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { errorMessage } from '../../../core/api/api-error';
import { PagedList } from '../../../core/api/paged-list';
import { AuthService } from '../../../core/auth/auth.service';
import { MODULOS } from '../../../core/auth/permisos';
import { Badge } from '../../../shared/ui/badge';
import { Btn, IconBtn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { ConfirmDialog } from '../../../shared/ui/confirm-dialog';
import { EmptyState } from '../../../shared/ui/empty-state';
import { Field } from '../../../shared/ui/field';
import { Icon } from '../../../shared/ui/icon';
import { Modal } from '../../../shared/ui/modal';
import { Paginator } from '../../../shared/ui/paginator';
import { SearchInput } from '../../../shared/ui/search-input';
import { TableSkeleton } from '../../../shared/ui/table-skeleton';
import { StatCard } from '../../../shared/ui/stat-card';
import { UnidadMedida } from '../data/unidad-medida.model';
import { UnidadesMedidaService } from '../data/unidades-medida.service';

@Component({
  selector: 'app-unidades-medida-page',
  imports: [ReactiveFormsModule, TableSkeleton, Badge, Btn, IconBtn, Card, ConfirmDialog, EmptyState, Field, Icon, Modal, Paginator, SearchInput, StatCard],
  template: `
    <div class="space-y-6">
      <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h2 class="text-lg font-semibold text-ink">Gestión de unidades de medida</h2>
          <p class="mt-1 text-sm text-muted">Define las unidades usadas en las presentaciones de tus productos.</p>
        </div>
        @if (puede.crear) {
          <button appBtn icon="plus" (click)="openForm(null)">Nueva unidad</button>
        }
      </div>

      <div class="grid gap-4 sm:grid-cols-3">
        <app-stat-card label="Total de unidades" [value]="list.data()?.totalElements" />
      </div>

      <app-card class="overflow-hidden">
        <div class="flex items-center justify-between gap-3 border-b border-border p-4">
          <app-search-input placeholder="Buscar unidades…" (searched)="list.setFilters({ search: $event })" />
          <p class="hidden shrink-0 text-sm text-muted sm:block">{{ list.data()?.totalElements ?? 0 }} resultados</p>
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
                  <tr><th>ID</th><th>Nombre</th><th>Abreviatura</th><th class="text-right">Acciones</th></tr>
                </thead>
                <tbody>
                  @for (u of d.content; track u.id) {
                    <tr>
                      <td class="w-24 font-mono text-xs font-semibold text-muted">#{{ pad(u.id) }}</td>
                      <td>
                        <div class="flex items-center gap-3">
                          <div class="grid size-9 place-items-center rounded-lg bg-info-soft text-info">
                            <app-icon name="grid" class="size-4" />
                          </div>
                          <span class="text-sm font-semibold text-ink">{{ u.nombre }}</span>
                        </div>
                      </td>
                      <td><app-badge>{{ u.abreviatura }}</app-badge></td>
                      <td>
                        <div class="flex justify-end">
                          @if (puede.editar) { <button appIconBtn icon="edit" [label]="'Editar ' + u.nombre" (click)="openForm(u)"></button> }
                          @if (puede.eliminar) { <button appIconBtn icon="trash" danger [label]="'Eliminar ' + u.nombre" (click)="toDelete.set(u)"></button> }
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
        [title]="editing() ? 'Editar unidad de medida' : 'Nueva unidad de medida'"
        description="Por ejemplo: Kilogramo (kg), Caja (cj)."
        (closed)="formOpen.set(false)"
      >
        <form [formGroup]="form" (ngSubmit)="save()" id="unidad-form">
          <div class="space-y-5 p-5">
            @if (formError()) {
              <p class="alert-error" role="alert">{{ formError() }}</p>
            }
            <app-field label="Nombre" [control]="form.controls.nombre" required>
              <input class="field-input" formControlName="nombre" placeholder="Ej. Kilogramo" />
            </app-field>
            <app-field label="Abreviatura" [control]="form.controls.abreviatura" helper="Máximo 10 caracteres" required>
              <input class="field-input" formControlName="abreviatura" placeholder="Ej. kg" />
            </app-field>
          </div>
        </form>
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="formOpen.set(false)">Cancelar</button>
          <button appBtn type="submit" form="unidad-form" [disabled]="saving()">
            {{ editing() ? 'Guardar cambios' : 'Crear unidad' }}
          </button>
        </ng-container>
      </app-modal>
    }

    @if (toDelete(); as u) {
      <app-confirm-dialog
        title="¿Eliminar unidad de medida?"
        [message]="'Vas a eliminar «' + u.nombre + '». Esta acción no se puede deshacer.'"
        confirmLabel="Eliminar unidad"
        [busy]="deleting()"
        [error]="deleteError()"
        (confirmed)="remove(u)"
        (cancelled)="toDelete.set(null)"
      />
    }
  `,
})
export class UnidadesMedidaPage {
  private readonly service = inject(UnidadesMedidaService);
  private readonly fb = inject(FormBuilder).nonNullable;

  protected readonly puede = inject(AuthService).crud(MODULOS.catalogo);
  protected readonly list = new PagedList<UnidadMedida>((f) => this.service.list(f));

  protected readonly formOpen = signal(false);
  protected readonly editing = signal<UnidadMedida | null>(null);
  protected readonly saving = signal(false);
  protected readonly formError = signal<string | null>(null);
  protected readonly toDelete = signal<UnidadMedida | null>(null);
  protected readonly deleting = signal(false);
  protected readonly deleteError = signal<string | null>(null);

  protected readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    abreviatura: ['', [Validators.required, Validators.maxLength(10)]],
  });

  protected pad(id: number): string {
    return String(id).padStart(3, '0');
  }

  protected openForm(u: UnidadMedida | null): void {
    this.editing.set(u);
    this.formError.set(null);
    this.form.reset({ nombre: u?.nombre ?? '', abreviatura: u?.abreviatura ?? '' });
    this.formOpen.set(true);
  }

  protected save(): void {
    if (this.form.invalid) return this.form.markAllAsTouched();
    const v = this.form.getRawValue();
    const body = { nombre: v.nombre.trim(), abreviatura: v.abreviatura.trim() };
    const current = this.editing();
    this.saving.set(true);
    this.formError.set(null);
    (current ? this.service.update(current.id, body) : this.service.create(body)).subscribe({
      next: () => {
        this.saving.set(false);
        this.formOpen.set(false);
        current ? this.list.reload() : this.list.load(0);
      },
      error: (e) => {
        this.saving.set(false);
        this.formError.set(errorMessage(e));
      },
    });
  }

  protected remove(u: UnidadMedida): void {
    this.deleting.set(true);
    this.deleteError.set(null);
    this.service.delete(u.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.toDelete.set(null);
        this.list.reload();
      },
      error: (e) => {
        this.deleting.set(false);
        this.deleteError.set(errorMessage(e, 'No se pudo eliminar la unidad'));
      },
    });
  }
}
