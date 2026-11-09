import { DecimalPipe } from '@angular/common';
import { Component, effect, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { errorMessage } from '../../../core/api/api-error';
import { isActivo } from '../../../core/api/estado';
import { Badge } from '../../../shared/ui/badge';
import { Btn, IconBtn } from '../../../shared/ui/button';
import { ConfirmDialog } from '../../../shared/ui/confirm-dialog';
import { Field } from '../../../shared/ui/field';
import { Toggle } from '../../../shared/ui/toggle';
import { TipoCliente } from '../../tipos-cliente/data/tipo-cliente.model';
import { Precio, PrecioAplicable } from '../data/precio.model';
import { PreciosService } from '../data/precios.service';

/** Lista de precios de una presentación + simulador del precio aplicable. */
@Component({
  selector: 'app-precios-panel',
  imports: [DecimalPipe, ReactiveFormsModule, Badge, Btn, IconBtn, ConfirmDialog, Field, Toggle],
  template: `
    <div class="flex items-center justify-between">
      <p class="text-xs font-semibold uppercase tracking-wide text-muted">Precios</p>
      @if (!formOpen()) {
        <button appBtn variant="secondary" icon="plus" class="!h-8 !px-3 !text-xs" (click)="openForm(null)">Agregar precio</button>
      }
    </div>

    @if (error()) { <p class="alert-error mt-2" role="alert">{{ error() }}</p> }

    @if (formOpen()) {
      <form [formGroup]="form" (ngSubmit)="save()" class="mt-3 space-y-4 rounded-lg border border-border bg-table-head p-4">
        <p class="text-sm font-semibold text-ink">{{ editing() ? 'Editar precio' : 'Nuevo precio' }}</p>
        @if (formError()) { <p class="alert-error" role="alert">{{ formError() }}</p> }
        <div class="grid gap-4 sm:grid-cols-3">
          <app-field label="Tipo de cliente" [control]="form.controls.tipoClienteId" optional>
            <select class="field-input" formControlName="tipoClienteId">
              <option [ngValue]="null">General (todos)</option>
              @for (t of tipos(); track t.id) { <option [ngValue]="t.id">{{ t.nombre }}</option> }
            </select>
          </app-field>
          <app-field label="Cantidad mínima" [control]="form.controls.cantidadMinima" required>
            <input type="number" step="any" min="0.0001" class="field-input" formControlName="cantidadMinima" />
          </app-field>
          <app-field label="Precio" [control]="form.controls.precio" required>
            <input type="number" step="any" min="0" class="field-input" formControlName="precio" />
          </app-field>
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
      <p class="mt-2 text-sm text-muted">Esta presentación aún no tiene precios.</p>
    } @else {
      <ul class="mt-3 divide-y divide-border rounded-lg border border-border">
        @for (p of items(); track p.id) {
          <li class="flex flex-wrap items-center justify-between gap-3 px-4 py-3 text-sm">
            <span class="flex items-center gap-2">
              <app-badge [tone]="p.tipoCliente ? 'info' : 'neutral'">{{ p.tipoCliente?.nombre ?? 'General' }}</app-badge>
              <span class="text-muted">desde {{ p.cantidadMinima }}</span>
            </span>
            <span class="font-semibold text-ink">{{ p.precio | number: '1.2-4' }}</span>
            <span class="flex items-center gap-1">
              <app-toggle [checked]="isActivo(p.estado)" [disabled]="togglingId() === p.id"
                label="Cambiar estado del precio" (checkedChange)="toggle(p, $event)" />
              <button appIconBtn icon="edit" label="Editar precio" (click)="openForm(p)"></button>
              <button appIconBtn icon="trash" danger label="Eliminar precio" (click)="toDelete.set(p)"></button>
            </span>
          </li>
        }
      </ul>
    }

    <div class="mt-4 rounded-lg border border-border p-4">
      <p class="text-xs font-semibold uppercase tracking-wide text-muted">Simular precio aplicable</p>
      <div class="mt-3 flex flex-wrap items-end gap-3">
        <label class="flex flex-col gap-1 text-xs text-muted">Cantidad
          <input type="number" step="any" min="0.0001" class="field-input !h-10 w-28" [value]="simCantidad()"
            (input)="simCantidad.set(+$any($event.target).value)" />
        </label>
        <label class="flex flex-col gap-1 text-xs text-muted">Tipo de cliente
          <select class="field-input !h-10" (change)="simTipo.set($any($event.target).value ? +$any($event.target).value : null)">
            <option value="">General</option>
            @for (t of tipos(); track t.id) { <option [value]="t.id">{{ t.nombre }}</option> }
          </select>
        </label>
        <button appBtn variant="secondary" type="button" (click)="simular()">Calcular</button>
        @if (simResultado(); as r) {
          <span class="text-sm text-ink">Se cobraría <strong>{{ r.precio | number: '1.2-4' }}</strong> por unidad</span>
        }
        @if (simError()) { <span class="text-sm text-danger">{{ simError() }}</span> }
      </div>
    </div>

    @if (toDelete(); as p) {
      <app-confirm-dialog title="¿Eliminar precio?" message="Vas a eliminar este precio de la presentación."
        confirmLabel="Eliminar precio" [busy]="deleting()" [error]="deleteError()"
        (confirmed)="remove(p)" (cancelled)="toDelete.set(null)" />
    }
  `,
})
export class PreciosPanel {
  private readonly service = inject(PreciosService);
  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly isActivo = isActivo;

  readonly presentacionId = input.required<number>();
  readonly tipos = input<TipoCliente[]>([]);

  protected readonly items = signal<Precio[] | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly formOpen = signal(false);
  protected readonly editing = signal<Precio | null>(null);
  protected readonly saving = signal(false);
  protected readonly formError = signal<string | null>(null);
  protected readonly togglingId = signal<number | null>(null);
  protected readonly toDelete = signal<Precio | null>(null);
  protected readonly deleting = signal(false);
  protected readonly deleteError = signal<string | null>(null);

  protected readonly simCantidad = signal(1);
  protected readonly simTipo = signal<number | null>(null);
  protected readonly simResultado = signal<PrecioAplicable | null>(null);
  protected readonly simError = signal<string | null>(null);

  protected readonly form = this.fb.group({
    tipoClienteId: [null as number | null],
    cantidadMinima: [1, [Validators.required, Validators.min(0.0001)]],
    precio: [0, [Validators.required, Validators.min(0)]],
  });

  constructor() {
    effect(() => {
      this.presentacionId();
      this.formOpen.set(false);
      this.simResultado.set(null);
      this.load();
    });
  }

  private load(): void {
    this.items.set(null);
    this.error.set(null);
    this.service.list(this.presentacionId(), { size: 100 }).subscribe({
      next: (p) => this.items.set(p.content),
      error: (e) => {
        this.items.set([]);
        this.error.set(errorMessage(e, 'No se pudieron cargar los precios'));
      },
    });
  }

  protected openForm(p: Precio | null): void {
    this.editing.set(p);
    this.formError.set(null);
    this.form.reset({ tipoClienteId: p?.tipoCliente?.id ?? null, cantidadMinima: p?.cantidadMinima ?? 1, precio: p?.precio ?? 0 });
    // El tipo de cliente no se edita: solo al crear.
    if (p) this.form.controls.tipoClienteId.disable();
    else this.form.controls.tipoClienteId.enable();
    this.formOpen.set(true);
  }

  protected save(): void {
    if (this.form.invalid) return this.form.markAllAsTouched();
    const v = this.form.getRawValue();
    const current = this.editing();
    this.saving.set(true);
    this.formError.set(null);
    (current
      ? this.service.update(current.id, { cantidadMinima: v.cantidadMinima, precio: v.precio })
      : this.service.create(this.presentacionId(), {
          tipoClienteId: v.tipoClienteId ?? undefined,
          cantidadMinima: v.cantidadMinima,
          precio: v.precio,
        })
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

  protected toggle(p: Precio, activar: boolean): void {
    this.togglingId.set(p.id);
    (activar ? this.service.activar(p.id) : this.service.desactivar(p.id)).subscribe({
      next: () => {
        this.togglingId.set(null);
        this.load();
      },
      error: (e) => {
        this.togglingId.set(null);
        this.error.set(errorMessage(e, 'No se pudo cambiar el estado'));
        this.load();
      },
    });
  }

  protected remove(p: Precio): void {
    this.deleting.set(true);
    this.deleteError.set(null);
    this.service.delete(p.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.toDelete.set(null);
        this.load();
      },
      error: (e) => {
        this.deleting.set(false);
        this.deleteError.set(errorMessage(e, 'No se pudo eliminar el precio'));
      },
    });
  }

  protected simular(): void {
    this.simResultado.set(null);
    this.simError.set(null);
    if (!(this.simCantidad() > 0)) return void this.simError.set('Ingresa una cantidad mayor a cero');
    this.service.aplicable(this.presentacionId(), this.simCantidad(), this.simTipo()).subscribe({
      next: (r) => this.simResultado.set(r),
      error: (e) => this.simError.set(errorMessage(e, 'No hay precio aplicable')),
    });
  }
}
