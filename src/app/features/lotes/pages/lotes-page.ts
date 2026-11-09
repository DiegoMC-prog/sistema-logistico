import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { errorMessage } from '../../../core/api/api-error';
import { PagedList } from '../../../core/api/paged-list';
import { AuthService } from '../../../core/auth/auth.service';
import { PERMISOS } from '../../../core/auth/permisos';
import { Btn, IconBtn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { ConfirmDialog } from '../../../shared/ui/confirm-dialog';
import { EmptyState } from '../../../shared/ui/empty-state';
import { Field } from '../../../shared/ui/field';
import { Modal } from '../../../shared/ui/modal';
import { Paginator } from '../../../shared/ui/paginator';
import { SearchInput } from '../../../shared/ui/search-input';
import { TableSkeleton } from '../../../shared/ui/table-skeleton';
import { Lote, LoteFilters } from '../data/lote.model';
import { LotesService } from '../data/lotes.service';

@Component({
  selector: 'app-lotes-page',
  imports: [DatePipe, DecimalPipe, ReactiveFormsModule, Btn, IconBtn, Card, ConfirmDialog, EmptyState, Field, Modal, Paginator, SearchInput, TableSkeleton],
  template: `
    <div class="space-y-6">
      <div>
        <h2 class="text-lg font-semibold text-ink">Gestión de lotes</h2>
        <p class="mt-1 text-sm text-muted">Los lotes nuevos se crean al registrar una entrada de inventario.</p>
      </div>

      <app-card class="overflow-hidden">
        <div class="flex flex-col gap-3 border-b border-border p-4 lg:flex-row lg:items-center">
          <app-search-input placeholder="Buscar por producto o número de lote…" (searched)="list.setFilters({ search: $event })" />
          <label class="flex items-center gap-2 text-sm text-muted lg:ml-auto">Vence desde
            <input type="date" class="field-input !h-10" (change)="list.setFilters({ venceDesde: $any($event.target).value || undefined })" />
          </label>
          <label class="flex items-center gap-2 text-sm text-muted">Hasta
            <input type="date" class="field-input !h-10" (change)="list.setFilters({ venceHasta: $any($event.target).value || undefined })" />
          </label>
        </div>

        @if (list.error()) { <p class="alert-error m-4" role="alert">{{ list.error() }}</p> }

        @if (list.data(); as d) {
          @if (d.content.length === 0) {
            <app-empty-state title="Sin lotes" message="No hay lotes con esos filtros." />
          } @else {
            <div class="overflow-x-auto">
              <table class="dt">
                <thead><tr><th>Producto</th><th>Lote</th><th>Vencimiento</th><th class="text-right">Costo unitario</th><th class="text-right">Desc. vencimiento</th>@if (puedeEditar) {<th class="text-right">Acciones</th>}</tr></thead>
                <tbody>
                  @for (l of d.content; track l.id) {
                    <tr>
                      <td class="text-sm font-semibold text-ink">{{ l.producto.nombre }}</td>
                      <td class="font-mono text-xs text-muted">{{ l.numeroLote }}</td>
                      <td class="text-sm text-muted">{{ l.fechaVencimiento ? (l.fechaVencimiento | date: 'dd/MM/yyyy') : 'Sin vencimiento' }}</td>
                      <td class="text-right text-sm">{{ l.costoUnitarioCompra | number: '1.2-4' }}</td>
                      <td class="text-right text-sm">{{ (l.porcentajeDescuentoVencimiento ?? 0) | number: '1.0-2' }}%</td>
                      @if (puedeEditar) {
                        <td>
                          <div class="flex justify-end">
                            <button appIconBtn icon="edit" [label]="'Editar lote ' + l.numeroLote" (click)="openForm(l)"></button>
                            <button appIconBtn icon="trash" danger [label]="'Eliminar lote ' + l.numeroLote" (click)="toDelete.set(l)"></button>
                          </div>
                        </td>
                      }
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

    @if (editing(); as l) {
      <app-modal [title]="'Editar lote ' + l.numeroLote" [description]="l.producto.nombre" (closed)="editing.set(null)">
        <form [formGroup]="form" (ngSubmit)="save()" id="lote-form">
          <div class="grid gap-4 p-5 sm:grid-cols-2">
            @if (formError()) { <p class="alert-error sm:col-span-2" role="alert">{{ formError() }}</p> }
            <app-field label="Número de lote" [control]="form.controls.numeroLote" required>
              <input class="field-input" formControlName="numeroLote" />
            </app-field>
            <app-field label="Fecha de vencimiento" [control]="form.controls.fechaVencimiento" optional>
              <input type="date" class="field-input" formControlName="fechaVencimiento" />
            </app-field>
            <app-field label="Costo unitario de compra" [control]="form.controls.costoUnitarioCompra" required>
              <input type="number" step="any" min="0" class="field-input" formControlName="costoUnitarioCompra" />
            </app-field>
            <app-field label="% descuento por vencimiento" [control]="form.controls.porcentajeDescuentoVencimiento" optional>
              <input type="number" step="any" min="0" max="100" class="field-input" formControlName="porcentajeDescuentoVencimiento" />
            </app-field>
          </div>
        </form>
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="editing.set(null)">Cancelar</button>
          <button appBtn type="submit" form="lote-form" [disabled]="saving()">Guardar cambios</button>
        </ng-container>
      </app-modal>
    }

    @if (toDelete(); as l) {
      <app-confirm-dialog title="¿Eliminar lote?"
        [message]="'Vas a eliminar el lote ' + l.numeroLote + ' de «' + l.producto.nombre + '». Si tiene movimientos, el servidor lo rechazará.'"
        confirmLabel="Eliminar lote" [busy]="deleting()" [error]="deleteError()"
        (confirmed)="remove(l)" (cancelled)="toDelete.set(null)" />
    }
  `,
})
export class LotesPage {
  private readonly service = inject(LotesService);
  private readonly fb = inject(FormBuilder).nonNullable;
  /** Editar y eliminar exigen CATALOGO_GESTIONAR; con INVENTARIO_VER solo se consulta. */
  protected readonly puedeEditar = inject(AuthService).hasPermission(PERMISOS.catalogo);

  protected readonly list = new PagedList<Lote, LoteFilters>((f) => this.service.list(f));
  protected readonly editing = signal<Lote | null>(null);
  protected readonly saving = signal(false);
  protected readonly formError = signal<string | null>(null);
  protected readonly toDelete = signal<Lote | null>(null);
  protected readonly deleting = signal(false);
  protected readonly deleteError = signal<string | null>(null);

  protected readonly form = this.fb.group({
    numeroLote: ['', [Validators.required, Validators.maxLength(60)]],
    fechaVencimiento: [''],
    costoUnitarioCompra: [0, [Validators.required, Validators.min(0)]],
    porcentajeDescuentoVencimiento: [null as number | null, [Validators.min(0), Validators.max(100)]],
  });

  protected openForm(l: Lote): void {
    this.formError.set(null);
    this.form.reset({
      numeroLote: l.numeroLote,
      fechaVencimiento: l.fechaVencimiento ?? '',
      costoUnitarioCompra: l.costoUnitarioCompra,
      porcentajeDescuentoVencimiento: l.porcentajeDescuentoVencimiento,
    });
    this.editing.set(l);
  }

  protected save(): void {
    const l = this.editing();
    if (!l) return;
    if (this.form.invalid) return this.form.markAllAsTouched();
    const v = this.form.getRawValue();
    this.saving.set(true);
    this.formError.set(null);
    this.service
      .update(l.id, {
        numeroLote: v.numeroLote.trim(),
        fechaVencimiento: v.fechaVencimiento || undefined,
        costoUnitarioCompra: v.costoUnitarioCompra,
        porcentajeDescuentoVencimiento: v.porcentajeDescuentoVencimiento ?? undefined,
      })
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.editing.set(null);
          this.list.reload();
        },
        error: (e) => {
          this.saving.set(false);
          this.formError.set(errorMessage(e));
        },
      });
  }

  protected remove(l: Lote): void {
    this.deleting.set(true);
    this.deleteError.set(null);
    this.service.delete(l.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.toDelete.set(null);
        this.list.reload();
      },
      error: (e) => {
        this.deleting.set(false);
        this.deleteError.set(errorMessage(e, 'No se pudo eliminar el lote'));
      },
    });
  }
}
