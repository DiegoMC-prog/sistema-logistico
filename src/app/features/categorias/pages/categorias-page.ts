import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { errorMessage } from '../../../core/api/api-error';
import { PagedList } from '../../../core/api/paged-list';
import { AuthService } from '../../../core/auth/auth.service';
import { MODULOS } from '../../../core/auth/permisos';
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
import { Categoria } from '../data/categoria.model';
import { CategoriasService } from '../data/categorias.service';

@Component({
  selector: 'app-categorias-page',
  imports: [ReactiveFormsModule, TableSkeleton, Btn, IconBtn, Card, ConfirmDialog, EmptyState, Field, Icon, Modal, Paginator, SearchInput, StatCard],
  template: `
    <div class="space-y-6">
      <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h2 class="text-lg font-semibold text-ink">Gestión de categorías</h2>
          <p class="mt-1 text-sm text-muted">Crea y organiza las categorías de tu catálogo de productos.</p>
        </div>
        @if (puede.crear) {
          <button appBtn icon="plus" (click)="openForm(null)">Nueva categoría</button>
        }
      </div>

      @if (actionError()) {
        <p class="alert-error" role="alert">{{ actionError() }}</p>
      }

      <div class="grid gap-4 sm:grid-cols-3">
        <app-stat-card label="Total de categorías" [value]="list.data()?.totalElements" />
      </div>

      <app-card class="overflow-hidden">
        <div class="flex items-center justify-between gap-3 border-b border-border p-4">
          <app-search-input placeholder="Buscar categorías…" (searched)="list.setFilters({ search: $event })" />
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
                  <tr><th>ID</th><th>Nombre</th><th class="text-right">Acciones</th></tr>
                </thead>
                <tbody>
                  @for (c of d.content; track c.id) {
                    <tr>
                      <td class="w-24 font-mono text-xs font-semibold text-muted">#{{ pad(c.id) }}</td>
                      <td>
                        <div class="flex items-center gap-3">
                          <div class="grid size-9 place-items-center rounded-lg bg-info-soft text-info">
                            <app-icon name="clipboard" class="size-4" />
                          </div>
                          <span class="text-sm font-semibold text-ink">{{ c.nombre }}</span>
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
        [title]="editing() ? 'Editar categoría' : 'Nueva categoría'"
        [description]="editing() ? 'Actualiza el nombre de la categoría.' : 'Agrega una categoría para organizar tus productos.'"
        (closed)="closeForm()"
      >
        <form [formGroup]="form" (ngSubmit)="save()" id="categoria-form">
          <div class="space-y-4 p-5">
            @if (formError()) {
              <p class="alert-error" role="alert">{{ formError() }}</p>
            }
            <app-field label="Nombre de la categoría" [control]="form.controls.nombre" required>
              <input class="field-input" formControlName="nombre" placeholder="Ej. Materias primas" />
            </app-field>
          </div>
        </form>
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="closeForm()">Cancelar</button>
          <button appBtn type="submit" form="categoria-form" [disabled]="saving()">
            {{ editing() ? 'Guardar cambios' : 'Crear categoría' }}
          </button>
        </ng-container>
      </app-modal>
    }

    @if (toDelete(); as c) {
      <app-confirm-dialog
        title="¿Eliminar categoría?"
        [message]="'Vas a eliminar «' + c.nombre + '». Esta acción no se puede deshacer.'"
        confirmLabel="Eliminar categoría"
        [busy]="deleting()"
        [error]="deleteError()"
        (confirmed)="remove(c)"
        (cancelled)="toDelete.set(null)"
      />
    }
  `,
})
export class CategoriasPage {
  private readonly service = inject(CategoriasService);
  private readonly fb = inject(FormBuilder).nonNullable;

  protected readonly puede = inject(AuthService).crud(MODULOS.catalogo);
  protected readonly list = new PagedList<Categoria>((f) => this.service.list(f));

  protected readonly formOpen = signal(false);
  protected readonly editing = signal<Categoria | null>(null);
  protected readonly saving = signal(false);
  protected readonly formError = signal<string | null>(null);
  protected readonly actionError = signal<string | null>(null);
  protected readonly toDelete = signal<Categoria | null>(null);
  protected readonly deleting = signal(false);
  protected readonly deleteError = signal<string | null>(null);

  protected readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
  });

  protected pad(id: number): string {
    return String(id).padStart(3, '0');
  }

  protected openForm(c: Categoria | null): void {
    this.editing.set(c);
    this.formError.set(null);
    this.form.reset({ nombre: c?.nombre ?? '' });
    this.formOpen.set(true);
  }

  protected closeForm(): void {
    this.formOpen.set(false);
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
        this.closeForm();
        current ? this.list.reload() : this.list.load(0);
      },
      error: (e) => {
        this.saving.set(false);
        this.formError.set(errorMessage(e));
      },
    });
  }

  protected remove(c: Categoria): void {
    this.deleting.set(true);
    this.deleteError.set(null);
    this.service.delete(c.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.toDelete.set(null);
        this.list.reload();
      },
      error: (e) => {
        this.deleting.set(false);
        this.deleteError.set(errorMessage(e, 'No se pudo eliminar la categoría'));
      },
    });
  }
}
