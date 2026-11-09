import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { errorMessage } from '../../../core/api/api-error';
import { Btn, IconBtn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { Field } from '../../../shared/ui/field';
import { DepositosRef } from '../../inventario/components/depositos-ref';
import { Producto } from '../../productos/data/producto.model';
import { Proveedor } from '../../proveedores/data/proveedor.model';
import { ProveedoresService } from '../../proveedores/data/proveedores.service';
import { ProductoBuscador } from '../components/producto-buscador';
import { PedidoRequest } from '../data/pedido.model';
import { PedidosService } from '../data/pedidos.service';

interface Linea {
  productoId: number;
  nombre: string;
  cantidad: number;
  costo: number;
}

/** Crea un pedido (borrador) o edita uno existente: al guardar se reemplazan los datos y las líneas. */
@Component({
  selector: 'app-pedido-form-page',
  imports: [DecimalPipe, FormsModule, ReactiveFormsModule, RouterLink, Btn, IconBtn, Card, Field, ProductoBuscador],
  template: `
    <div class="space-y-6">
      <a [routerLink]="id ? ['/pedidos', id] : ['/pedidos']" class="text-sm font-medium text-accent hover:underline">← Volver</a>

      @if (loadError()) { <p class="alert-error" role="alert">{{ loadError() }}</p> }
      @if (formError()) { <p class="alert-error" role="alert">{{ formError() }}</p> }

      @if (!loadError()) {
        <form [formGroup]="form" (ngSubmit)="guardar()" class="space-y-6">
          <app-card class="block p-5">
            <div class="grid gap-4 sm:grid-cols-2">
              <app-field label="Proveedor" [control]="form.controls.proveedorId" required>
                <select class="field-input" formControlName="proveedorId">
                  <option [ngValue]="null" disabled>Selecciona un proveedor</option>
                  @for (p of proveedoresActivos(); track p.id) { <option [ngValue]="p.id">{{ p.nombreEmpresa }}</option> }
                </select>
              </app-field>
              <app-field label="Depósito de destino" [control]="form.controls.depositoDestinoId" required>
                <select class="field-input" formControlName="depositoDestinoId">
                  <option [ngValue]="null" disabled>Selecciona un depósito</option>
                  @for (d of depositos(); track d.id) { <option [ngValue]="d.id">{{ d.nombre }}</option> }
                </select>
              </app-field>
              <app-field label="Fecha de entrega estimada" [control]="form.controls.fechaEntregaEstimada" optional>
                <input type="date" class="field-input" formControlName="fechaEntregaEstimada" />
              </app-field>
              <app-field label="Observación" [control]="form.controls.observacion" optional>
                <input class="field-input" formControlName="observacion" maxlength="300" />
              </app-field>
            </div>
          </app-card>

          <app-card class="block space-y-4 p-5">
            <div>
              <p class="text-sm font-semibold text-ink">Productos del pedido</p>
              <p class="text-xs text-muted">Las cantidades van en la unidad base del producto. El costo es el pactado y será el costo del lote al recibir. Un borrador puede guardarse sin líneas, pero para enviarlo se necesita al menos una.</p>
            </div>
            <app-producto-buscador (picked)="agregar($event)" />

            @if (lineas().length > 0) {
              <div class="overflow-x-auto">
                <table class="dt">
                  <thead><tr><th>Producto</th><th class="w-32">Cantidad (base)</th><th class="w-32">Costo unitario</th><th class="text-right">Subtotal</th><th></th></tr></thead>
                  <tbody>
                    @for (l of lineas(); track l.productoId; let i = $index) {
                      <tr>
                        <td class="text-sm font-semibold text-ink">{{ l.nombre }}</td>
                        <td><input type="number" min="0.0001" step="any" class="field-input !h-9" [ngModel]="l.cantidad" [ngModelOptions]="{ standalone: true }" (ngModelChange)="patch(i, { cantidad: +$event })" /></td>
                        <td><input type="number" min="0" step="any" class="field-input !h-9" [ngModel]="l.costo" [ngModelOptions]="{ standalone: true }" (ngModelChange)="patch(i, { costo: +$event })" /></td>
                        <td class="text-right text-sm font-semibold">{{ l.cantidad * l.costo | number: '1.2-2' }}</td>
                        <td class="text-right"><button appIconBtn icon="trash" danger [label]="'Quitar ' + l.nombre" (click)="quitar(i)"></button></td>
                      </tr>
                    }
                  </tbody>
                </table>
              </div>
              <p class="text-right text-base font-bold text-ink">Total: {{ total() | number: '1.2-2' }}</p>
            } @else {
              <p class="text-sm text-muted">Aún no agregaste productos.</p>
            }
          </app-card>

          <div class="flex justify-end gap-3">
            <a [routerLink]="id ? ['/pedidos', id] : ['/pedidos']" class="inline-flex h-10 items-center rounded-lg border border-border bg-surface px-4 text-sm font-semibold text-ink hover:bg-subtle">Cancelar</a>
            <button appBtn type="submit" [disabled]="saving() || cargando()">{{ id ? 'Guardar cambios' : 'Crear borrador' }}</button>
          </div>
        </form>
      }
    </div>
  `,
})
export class PedidoFormPage {
  private readonly service = inject(PedidosService);
  private readonly proveedoresService = inject(ProveedoresService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id')) || null;

  protected readonly depositos = inject(DepositosRef).depositos;
  protected readonly proveedores = signal<Proveedor[]>([]);
  protected readonly lineas = signal<Linea[]>([]);
  protected readonly total = computed(() => this.lineas().reduce((s, l) => s + l.cantidad * l.costo, 0));

  protected readonly cargando = signal(!!this.id);
  protected readonly saving = signal(false);
  protected readonly loadError = signal<string | null>(null);
  protected readonly formError = signal<string | null>(null);

  protected readonly form = this.fb.group({
    proveedorId: [null as number | null, Validators.required],
    depositoDestinoId: [null as number | null, Validators.required],
    fechaEntregaEstimada: [''],
    observacion: ['', Validators.maxLength(300)],
  });

  constructor() {
    this.proveedoresService.list({ size: 100 }).subscribe({
      next: (p) => this.proveedores.set(p.content),
      error: (e) => this.formError.set(errorMessage(e, 'No se pudieron cargar los proveedores (se requiere el permiso PROVEEDOR_VER)')),
    });
    if (this.id) this.cargar(this.id);
  }

  private cargar(id: number): void {
    this.service.get(id).subscribe({
      next: (p) => {
        this.cargando.set(false);
        if (p.estado !== 'BORRADOR') return void this.loadError.set('Solo los pedidos en borrador se pueden editar.');
        this.form.reset({
          proveedorId: p.proveedor.id,
          depositoDestinoId: p.depositoDestino.id,
          fechaEntregaEstimada: p.fechaEntregaEstimada ?? '',
          observacion: p.observacion ?? '',
        });
        this.lineas.set(
          p.items.map((i) => ({ productoId: i.producto.id, nombre: i.producto.nombre ?? `Producto #${i.producto.id}`, cantidad: i.cantidadPedida, costo: i.costoUnitario })),
        );
      },
      error: (e) => {
        this.cargando.set(false);
        this.loadError.set(errorMessage(e, 'No se pudo cargar el pedido'));
      },
    });
  }

  /** Proveedores activos (más el actual del pedido que se edita, aunque se haya desactivado). */
  protected proveedoresActivos(): Proveedor[] {
    const actual = this.form.controls.proveedorId.value;
    return this.proveedores().filter((p) => p.estado === 'ACTIVO' || p.id === actual);
  }

  protected agregar(p: Producto): void {
    if (this.lineas().some((l) => l.productoId === p.id)) return void this.formError.set(`«${p.nombre}» ya está en el pedido: no se puede repetir un producto.`);
    this.formError.set(null);
    this.lineas.update((ls) => [...ls, { productoId: p.id, nombre: p.nombre, cantidad: 1, costo: 0 }]);
  }

  protected patch(i: number, cambios: Partial<Linea>): void {
    this.lineas.update((ls) => ls.map((l, idx) => (idx === i ? { ...l, ...cambios } : l)));
  }

  protected quitar(i: number): void {
    this.lineas.update((ls) => ls.filter((_, idx) => idx !== i));
  }

  protected guardar(): void {
    if (this.form.invalid) return this.form.markAllAsTouched();
    const malas = this.lineas().find((l) => !(l.cantidad > 0) || !(l.costo >= 0));
    if (malas) return void this.formError.set(`Revisa cantidad y costo de «${malas.nombre}».`);

    const v = this.form.getRawValue();
    const body: PedidoRequest = {
      proveedorId: v.proveedorId!,
      depositoDestinoId: v.depositoDestinoId!,
      fechaEntregaEstimada: v.fechaEntregaEstimada || undefined,
      observacion: v.observacion.trim() || undefined,
      items: this.lineas().map((l) => ({ productoId: l.productoId, cantidad: l.cantidad, costoUnitario: l.costo })),
    };
    this.saving.set(true);
    this.formError.set(null);
    (this.id ? this.service.update(this.id, body) : this.service.create(body)).subscribe({
      next: (p) => {
        this.saving.set(false);
        this.router.navigate(['/pedidos', p.id]);
      },
      error: (e) => {
        this.saving.set(false);
        this.formError.set(errorMessage(e, 'No se pudo guardar el pedido'));
      },
    });
  }
}
