import { Component, inject, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { errorMessage } from '../../../core/api/api-error';
import { Btn, IconBtn } from '../../../shared/ui/button';
import { Lote } from '../../lotes/data/lote.model';
import { LotesService } from '../../lotes/data/lotes.service';
import { ItemElegido, ProductPicker } from '../../ventas/components/product-picker';
import { ItemEntradaRequest, MovimientoInv } from '../data/inventario.model';
import { InventarioService } from '../data/inventario.service';
import { DepositosRef } from './depositos-ref';

interface FilaEntrada {
  uid: number;
  productoId: number;
  presentacionId: number;
  nombre: string;
  unidad: string;
  cantidad: number;
  modo: 'nuevo' | 'existente';
  loteId: number | null;
  lotes: Lote[];
  numeroLote: string;
  vence: string;
  costo: number;
  descuento: number | null;
}

/** Entrada de mercadería: cada ítem va a un lote existente o crea uno nuevo. */
@Component({
  selector: 'app-entrada-form',
  imports: [FormsModule, Btn, IconBtn, ProductPicker],
  template: `
    <div class="space-y-5">
      @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }

      <div class="grid gap-4 sm:grid-cols-2">
        <label class="block text-sm font-medium text-ink">Depósito de destino
          <select class="field-input mt-1" [ngModel]="depositoId()" (ngModelChange)="depositoId.set($event)">
            <option [ngValue]="null" disabled>Selecciona un depósito</option>
            @for (d of depositos(); track d.id) { <option [ngValue]="d.id">{{ d.nombre }}</option> }
          </select>
        </label>
        <label class="block text-sm font-medium text-ink">Observación <span class="font-normal text-muted">(opcional)</span>
          <input class="field-input mt-1" maxlength="200" [ngModel]="observacion()" (ngModelChange)="observacion.set($event)" />
        </label>
      </div>

      <section class="space-y-3">
        <p class="text-xs font-semibold uppercase tracking-wide text-muted">Agregar producto</p>
        <app-product-picker (picked)="agregar($event)" />
      </section>

      @for (f of filas(); track f.uid) {
        <div class="space-y-3 rounded-lg border border-border p-4">
          <div class="flex items-center justify-between gap-3">
            <p class="text-sm font-semibold text-ink">{{ f.nombre }} <span class="font-normal text-muted">({{ f.unidad }})</span></p>
            <button appIconBtn icon="trash" danger [label]="'Quitar ' + f.nombre" (click)="quitar(f.uid)"></button>
          </div>
          <div class="grid gap-3 sm:grid-cols-4">
            <label class="text-xs text-muted">Cantidad ({{ f.unidad }})
              <input type="number" min="0.0001" step="any" class="field-input !h-10 mt-1" [ngModel]="f.cantidad" (ngModelChange)="patch(f.uid, { cantidad: +$event })" />
            </label>
            <label class="text-xs text-muted">Lote
              <select class="field-input !h-10 mt-1" [ngModel]="f.modo" (ngModelChange)="patch(f.uid, { modo: $event })">
                <option value="nuevo">Crear lote nuevo</option>
                <option value="existente">Lote existente</option>
              </select>
            </label>
            @if (f.modo === 'existente') {
              <label class="text-xs text-muted sm:col-span-2">Elegir lote
                <select class="field-input !h-10 mt-1" [ngModel]="f.loteId" (ngModelChange)="patch(f.uid, { loteId: $event })">
                  <option [ngValue]="null" disabled>{{ f.lotes.length ? 'Selecciona un lote' : 'Este producto no tiene lotes' }}</option>
                  @for (l of f.lotes; track l.id) { <option [ngValue]="l.id">{{ l.numeroLote }}{{ l.fechaVencimiento ? ' · vence ' + l.fechaVencimiento : '' }}</option> }
                </select>
              </label>
            } @else {
              <label class="text-xs text-muted">N.º de lote <span>(opcional)</span>
                <input class="field-input !h-10 mt-1" maxlength="80" [ngModel]="f.numeroLote" (ngModelChange)="patch(f.uid, { numeroLote: $event })" />
              </label>
              <label class="text-xs text-muted">Vencimiento
                <input type="date" class="field-input !h-10 mt-1" [ngModel]="f.vence" (ngModelChange)="patch(f.uid, { vence: $event })" />
              </label>
              <label class="text-xs text-muted">Costo unitario
                <input type="number" min="0" step="any" class="field-input !h-10 mt-1" [ngModel]="f.costo" (ngModelChange)="patch(f.uid, { costo: +$event })" />
              </label>
              <label class="text-xs text-muted">% desc. vencimiento
                <input type="number" min="0" max="100" step="any" class="field-input !h-10 mt-1" [ngModel]="f.descuento" (ngModelChange)="patch(f.uid, { descuento: $event === '' || $event === null ? null : +$event })" />
              </label>
            }
          </div>
        </div>
      } @empty {
        <p class="text-sm text-muted">Aún no agregaste productos.</p>
      }

      <div class="flex justify-end">
        <button appBtn icon="download" [disabled]="enviando()" (click)="enviar()">Registrar entrada</button>
      </div>
    </div>
  `,
})
export class EntradaForm {
  private readonly service = inject(InventarioService);
  private readonly lotesService = inject(LotesService);

  readonly depositos = inject(DepositosRef).depositos;
  readonly done = output<MovimientoInv>();

  protected readonly depositoId = signal<number | null>(null);
  protected readonly observacion = signal('');
  protected readonly filas = signal<FilaEntrada[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly enviando = signal(false);
  private uid = 0;

  protected agregar(item: ItemElegido): void {
    const fila: FilaEntrada = {
      uid: ++this.uid, productoId: item.productoId, presentacionId: item.presentacionId, nombre: item.nombre, unidad: item.unidad,
      cantidad: 1, modo: 'nuevo', loteId: null, lotes: [], numeroLote: '', vence: '', costo: 0, descuento: null,
    };
    this.filas.update((fs) => [...fs, fila]);
    // Lotes existentes del producto (requiere permiso de ver lotes: si falta, solo se ofrece lote nuevo).
    this.lotesService.list({ productoId: item.productoId, size: 100 }).subscribe({
      next: (p) => this.patch(fila.uid, { lotes: p.content }),
      error: () => undefined,
    });
  }

  protected patch(uid: number, cambios: Partial<FilaEntrada>): void {
    this.filas.update((fs) => fs.map((f) => (f.uid === uid ? { ...f, ...cambios } : f)));
  }

  protected quitar(uid: number): void {
    this.filas.update((fs) => fs.filter((f) => f.uid !== uid));
  }

  protected enviar(): void {
    const dep = this.depositoId();
    if (!dep) return void this.error.set('Selecciona el depósito de destino');
    const filas = this.filas();
    if (filas.length === 0) return void this.error.set('Agrega al menos un producto');
    for (const f of filas) {
      if (!(f.cantidad > 0)) return void this.error.set(`La cantidad de «${f.nombre}» debe ser mayor a cero`);
      if (f.modo === 'existente' && f.loteId === null) return void this.error.set(`Elige el lote de «${f.nombre}»`);
      if (f.modo === 'nuevo' && !(f.costo >= 0)) return void this.error.set(`Indica el costo de «${f.nombre}»`);
    }

    const items: ItemEntradaRequest[] = filas.map((f) =>
      f.modo === 'existente'
        ? { loteId: f.loteId!, presentacionId: f.presentacionId, cantidad: f.cantidad }
        : {
            productoId: f.productoId,
            presentacionId: f.presentacionId,
            cantidad: f.cantidad,
            lote: {
              ...(f.numeroLote.trim() ? { numeroLote: f.numeroLote.trim() } : {}),
              ...(f.vence ? { fechaVencimiento: f.vence } : {}),
              costoUnitarioCompra: f.costo,
              ...(f.descuento !== null ? { porcentajeDescuentoVencimiento: f.descuento } : {}),
            },
          },
    );

    this.enviando.set(true);
    this.error.set(null);
    this.service.entrada({ depositoId: dep, observacion: this.observacion().trim() || undefined, items }).subscribe({
      next: (m) => {
        this.enviando.set(false);
        this.filas.set([]);
        this.observacion.set('');
        this.done.emit(m);
      },
      error: (e) => {
        this.enviando.set(false);
        this.error.set(errorMessage(e, 'No se pudo registrar la entrada'));
      },
    });
  }
}
