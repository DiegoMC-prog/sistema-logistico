import { Component, effect, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { errorMessage } from '../../../core/api/api-error';
import { AuthService } from '../../../core/auth/auth.service';
import { MODULOS } from '../../../core/auth/permisos';
import { Badge } from '../../../shared/ui/badge';
import { Btn, IconBtn } from '../../../shared/ui/button';
import { ConfirmDialog } from '../../../shared/ui/confirm-dialog';
import { Field } from '../../../shared/ui/field';
import { Icon } from '../../../shared/ui/icon';
import { Toggle } from '../../../shared/ui/toggle';
import { UnidadMedida } from '../../unidades-medida/data/unidad-medida.model';
import { UnidadesMedidaService } from '../../unidades-medida/data/unidades-medida.service';
import { PresentacionesService } from '../data/presentaciones.service';
import { Presentacion } from '../data/producto.model';

/** Lista y gestiona las presentaciones (unidad + código de barras) de un producto. */
@Component({
  selector: 'app-presentaciones-panel',
  imports: [ReactiveFormsModule, Badge, Btn, IconBtn, ConfirmDialog, Field, Icon, Toggle],
  template: `
    <div class="flex items-center justify-between">
      <p class="text-xs font-semibold uppercase tracking-wide text-muted">Presentaciones</p>
      @if (!formOpen() && puede.crear) {
        <button appBtn variant="secondary" icon="plus" class="!h-8 !px-3 !text-xs" (click)="openForm(null)">
          Agregar
        </button>
      }
    </div>

    @if (error()) {
      <p class="alert-error mt-2" role="alert">{{ error() }}</p>
    }

    @if (formOpen()) {
      <form [formGroup]="form" (ngSubmit)="save()" class="mt-3 space-y-4 rounded-lg border border-border bg-table-head p-4">
        <p class="text-sm font-semibold text-ink">{{ editing() ? 'Editar presentación' : 'Nueva presentación' }}</p>
        @if (formError()) {
          <p class="alert-error" role="alert">{{ formError() }}</p>
        }
        <div class="grid gap-4 sm:grid-cols-2">
          <app-field label="Código de barras" [control]="form.controls.codigoBarras" required>
            <input class="field-input" formControlName="codigoBarras" maxlength="64" placeholder="Ej. 7501031311309" />
          </app-field>
          <app-field label="Unidad de medida" [control]="form.controls.unidadMedidaId" required>
            <select class="field-input" formControlName="unidadMedidaId">
              <option [ngValue]="null" disabled>Selecciona una unidad</option>
              @for (u of unidades(); track u.id) {
                <option [ngValue]="u.id">{{ u.nombre }} ({{ u.abreviatura }})</option>
              }
            </select>
          </app-field>
          <app-field label="Factor de conversión" [control]="form.controls.factorConversion" helper="Unidades base que contiene (mayor a 0)" required>
            <input type="number" step="any" min="0" class="field-input" formControlName="factorConversion" />
          </app-field>
          @if (!editing()) {
            <div class="flex items-center justify-between rounded-lg border border-border bg-surface p-3">
              <span class="text-sm font-semibold text-ink">Unidad base</span>
              <app-toggle
                label="Es unidad base"
                [checked]="form.controls.esUnidadBase.value"
                (checkedChange)="form.controls.esUnidadBase.setValue($event)"
              />
            </div>
          }
        </div>
        <div class="flex justify-end gap-3">
          <button appBtn variant="secondary" type="button" (click)="formOpen.set(false)">Cancelar</button>
          <button appBtn type="submit" [disabled]="saving()">{{ editing() ? 'Guardar' : 'Agregar' }}</button>
        </div>
      </form>
    }

    @if (items() === null) {
      <p class="mt-2 text-sm text-muted">Cargando…</p>
    } @else if (items()!.length === 0) {
      <p class="mt-2 text-sm text-muted">Este producto aún no tiene presentaciones.</p>
    } @else {
      <ul class="mt-3 divide-y divide-border rounded-lg border border-border">
        @for (pr of items(); track pr.id) {
          <li class="flex flex-wrap items-center justify-between gap-3 px-4 py-3 text-sm">
            <span class="flex items-center gap-2 font-mono text-xs font-semibold text-muted">
              <app-icon name="barcode" class="size-4" />{{ pr.codigoBarras }}
            </span>
            <span class="text-ink">{{ pr.unidadMedida.nombre }} ({{ pr.unidadMedida.abreviatura }}) · ×{{ pr.factorConversion }}</span>
            <span class="flex items-center gap-1">
              @if (pr.esUnidadBase) {
                <app-badge tone="info">Unidad base</app-badge>
              }
              @if (puede.editar) { <button appIconBtn icon="edit" [label]="'Editar ' + pr.codigoBarras" (click)="openForm(pr)"></button> }
              @if (puede.eliminar) { <button appIconBtn icon="trash" danger [label]="'Eliminar ' + pr.codigoBarras" (click)="toDelete.set(pr)"></button> }
            </span>
          </li>
        }
      </ul>
    }

    @if (toDelete(); as pr) {
      <app-confirm-dialog
        title="¿Eliminar presentación?"
        [message]="'Vas a eliminar la presentación con código ' + pr.codigoBarras + '.'"
        confirmLabel="Eliminar presentación"
        [busy]="deleting()"
        [error]="deleteError()"
        (confirmed)="remove(pr)"
        (cancelled)="toDelete.set(null)"
      />
    }
  `,
})
export class PresentacionesPanel {
  private readonly service = inject(PresentacionesService);
  private readonly unidadesService = inject(UnidadesMedidaService);
  private readonly fb = inject(FormBuilder).nonNullable;

  protected readonly puede = inject(AuthService).crud(MODULOS.catalogo);
  readonly productoId = input.required<number>();

  protected readonly items = signal<Presentacion[] | null>(null);
  protected readonly unidades = signal<UnidadMedida[]>([]);
  protected readonly error = signal<string | null>(null);

  protected readonly formOpen = signal(false);
  protected readonly editing = signal<Presentacion | null>(null);
  protected readonly saving = signal(false);
  protected readonly formError = signal<string | null>(null);

  protected readonly toDelete = signal<Presentacion | null>(null);
  protected readonly deleting = signal(false);
  protected readonly deleteError = signal<string | null>(null);

  protected readonly form = this.fb.group({
    codigoBarras: ['', [Validators.required, Validators.maxLength(64)]],
    unidadMedidaId: [null as number | null, Validators.required],
    factorConversion: [1, [Validators.required, Validators.min(0.0001)]],
    esUnidadBase: [false],
  });

  constructor() {
    effect(() => {
      this.productoId(); // recarga si cambia el producto
      this.formOpen.set(false);
      this.load();
    });
    this.unidadesService.list({ size: 200 }).subscribe({
      next: (p) => this.unidades.set(p.content),
      error: (e) => this.error.set(errorMessage(e, 'No se pudieron cargar las unidades de medida')),
    });
  }

  private load(): void {
    this.items.set(null);
    this.error.set(null);
    this.service.list(this.productoId()).subscribe({
      next: (r) => this.items.set(r),
      error: (e) => {
        this.items.set([]);
        this.error.set(errorMessage(e, 'No se pudieron cargar las presentaciones'));
      },
    });
  }

  protected openForm(pr: Presentacion | null): void {
    this.editing.set(pr);
    this.formError.set(null);
    this.form.reset({
      codigoBarras: pr?.codigoBarras ?? '',
      unidadMedidaId: pr?.unidadMedida.id ?? null,
      factorConversion: pr?.factorConversion ?? 1,
      // La primera presentación de un producto normalmente es la unidad base.
      esUnidadBase: pr ? pr.esUnidadBase : this.items()?.length === 0,
    });
    this.formOpen.set(true);
  }

  protected save(): void {
    if (this.form.invalid) return this.form.markAllAsTouched();
    const v = this.form.getRawValue();
    const base = {
      unidadMedidaId: v.unidadMedidaId!,
      codigoBarras: v.codigoBarras.trim(),
      factorConversion: v.factorConversion,
    };
    const current = this.editing();
    this.saving.set(true);
    this.formError.set(null);
    (current
      ? this.service.update(current.id, base)
      : this.service.create(this.productoId(), { ...base, esUnidadBase: v.esUnidadBase })
    ).subscribe({
      next: () => {
        this.saving.set(false);
        this.formOpen.set(false);
        this.load();
      },
      error: (e) => {
        this.saving.set(false);
        this.formError.set(errorMessage(e));
      },
    });
  }

  protected remove(pr: Presentacion): void {
    this.deleting.set(true);
    this.deleteError.set(null);
    this.service.delete(pr.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.toDelete.set(null);
        this.load();
      },
      error: (e) => {
        this.deleting.set(false);
        this.deleteError.set(errorMessage(e, 'No se pudo eliminar la presentación'));
      },
    });
  }
}
