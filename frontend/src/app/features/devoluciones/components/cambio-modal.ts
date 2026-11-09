import { DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { errorMessage } from '../../../core/api/api-error';
import { Btn, IconBtn } from '../../../shared/ui/button';
import { Modal } from '../../../shared/ui/modal';
import { MetodoPago } from '../../metodos-pago/data/metodo-pago.model';
import { MetodosPagoService } from '../../metodos-pago/data/metodos-pago.service';
import { ItemElegido, ProductPicker } from '../../ventas/components/product-picker';
import { Venta } from '../../ventas/data/venta.model';
import { nuevaClave } from '../../ventas/data/ventas.service';
import { CambioRequest, CambioResponse } from '../data/devolucion.model';
import { DevolucionesService } from '../data/devoluciones.service';

interface Devuelta {
  ventaDetalleId: number;
  producto: string;
  vendida: number;
  cantidad: number;
}

interface Nueva extends ItemElegido {
  cantidad: number;
}

interface PagoFila {
  metodoPagoId: number | null;
  monto: number;
}

/** Cambio por otro producto: devuelve y vende lo nuevo en una sola operación. */
@Component({
  selector: 'app-cambio-modal',
  imports: [DecimalPipe, FormsModule, Btn, IconBtn, Modal, ProductPicker],
  template: `
    <app-modal title="Cambio por otro producto" [description]="'Venta ' + venta().numeroVenta" size="lg" (closed)="closed.emit()">
      @if (resultado(); as r) {
        <div class="space-y-3 p-5 text-sm">
          <p class="alert-success">Cambio registrado: devolución {{ r.devolucion.numeroDevolucion }}
            @if (r.devolucion.ventaCambio) { · nueva venta {{ r.devolucion.ventaCambio.numeroVenta }} }</p>
          <dl class="grid gap-2 sm:grid-cols-3">
            <div class="rounded-lg bg-subtle p-3"><dt class="text-muted">Crédito aplicado</dt><dd class="text-lg font-bold text-ink">{{ r.creditoAplicado | number: '1.2-2' }}</dd></div>
            <div class="rounded-lg bg-subtle p-3"><dt class="text-muted">Diferencia cobrada</dt><dd class="text-lg font-bold text-ink">{{ r.diferenciaCobrada | number: '1.2-2' }}</dd></div>
            <div class="rounded-lg p-3" [class]="r.saldoEntregadoEnEfectivo > 0 ? 'bg-warning-soft' : 'bg-subtle'">
              <dt class="text-muted">Entregar al cliente en efectivo</dt>
              <dd class="text-lg font-bold" [class]="r.saldoEntregadoEnEfectivo > 0 ? 'text-warning-dark' : 'text-ink'">{{ r.saldoEntregadoEnEfectivo | number: '1.2-2' }}</dd>
            </div>
          </dl>
        </div>
      } @else {
        <div class="space-y-5 p-5">
          @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }

          <section>
            <p class="mb-2 text-xs font-semibold uppercase tracking-wide text-muted">1. Productos que devuelve</p>
            <div class="overflow-x-auto">
              <table class="dt">
                <thead><tr><th>Producto</th><th class="text-right">Vendida</th><th class="w-28">A devolver</th></tr></thead>
                <tbody>
                  @for (f of devueltas(); track f.ventaDetalleId; let i = $index) {
                    <tr>
                      <td class="text-sm font-semibold text-ink">{{ f.producto }}</td>
                      <td class="text-right text-sm">{{ f.vendida }}</td>
                      <td><input type="number" min="0" [max]="f.vendida" step="any" class="field-input !h-9" [ngModel]="f.cantidad" (ngModelChange)="patchDevuelta(i, +$event)" /></td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          </section>

          <section class="space-y-3">
            <p class="text-xs font-semibold uppercase tracking-wide text-muted">2. Productos nuevos</p>
            <app-product-picker (picked)="agregarNueva($event)" />
            @for (n of nuevas(); track n.presentacionId; let i = $index) {
              <div class="flex items-center gap-3 rounded-lg border border-border px-3 py-2 text-sm">
                <span class="flex-1 text-ink">{{ n.nombre }} <span class="text-muted">({{ n.unidad }})</span></span>
                <input type="number" min="0.0001" step="any" class="field-input !h-9 w-24" [ngModel]="n.cantidad" (ngModelChange)="patchNueva(i, +$event)" />
                <button appIconBtn icon="trash" danger label="Quitar" (click)="quitarNueva(i)"></button>
              </div>
            }
          </section>

          <section class="space-y-2">
            <div class="flex items-center justify-between">
              <p class="text-xs font-semibold uppercase tracking-wide text-muted">3. Pago de la diferencia (solo si lo nuevo vale más)</p>
              <button appBtn variant="secondary" icon="plus" class="!h-8 !px-3 !text-xs" type="button" (click)="agregarPago()">Pago</button>
            </div>
            @for (p of pagos(); track $index; let i = $index) {
              <div class="grid grid-cols-[1fr_7rem_auto] items-center gap-2">
                <select class="field-input !h-9" [ngModel]="p.metodoPagoId" (ngModelChange)="patchPago(i, { metodoPagoId: $event })">
                  <option [ngValue]="null" disabled>Método</option>
                  @for (m of metodos(); track m.id) { <option [ngValue]="m.id">{{ m.nombre }}</option> }
                </select>
                <input type="number" min="0" step="0.01" class="field-input !h-9 text-right" [ngModel]="p.monto" (ngModelChange)="patchPago(i, { monto: +$event })" />
                <button appIconBtn icon="close" label="Quitar pago" (click)="quitarPago(i)"></button>
              </div>
            }
            <p class="text-xs text-muted">Si el crédito alcanza o sobra, no envíes pagos (el servidor los rechaza).</p>
          </section>

          <label class="block text-sm font-medium text-ink">Motivo
            <input class="field-input mt-1" maxlength="200" [ngModel]="motivo()" (ngModelChange)="motivo.set($event)" />
          </label>
        </div>
      }
      @if (resultado()) {
        <ng-container modal-footer><button appBtn type="button" (click)="closed.emit()">Cerrar</button></ng-container>
      } @else {
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="closed.emit()">Cancelar</button>
          <button appBtn type="button" [disabled]="enviando()" (click)="enviar()">Registrar cambio</button>
        </ng-container>
      }
    </app-modal>
  `,
})
export class CambioModal implements OnInit {
  private readonly service = inject(DevolucionesService);
  private readonly metodosService = inject(MetodosPagoService);

  readonly venta = input.required<Venta>();
  readonly closed = output<void>();
  readonly done = output<CambioResponse>();

  protected readonly metodos = signal<MetodoPago[]>([]);
  protected readonly devueltas = signal<Devuelta[]>([]);
  protected readonly nuevas = signal<Nueva[]>([]);
  protected readonly pagos = signal<PagoFila[]>([]);
  protected readonly motivo = signal('');
  protected readonly error = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly resultado = signal<CambioResponse | null>(null);

  private ultimoCuerpo = '';
  private clave = nuevaClave();

  ngOnInit(): void {
    this.devueltas.set(
      this.venta().lineas.map((l) => ({ ventaDetalleId: l.id, producto: l.producto, vendida: l.cantidad, cantidad: 0 })),
    );
  }

  constructor() {
    this.metodosService.paraCobro().subscribe({ next: (m) => this.metodos.set(m) });
  }

  protected patchDevuelta(i: number, cantidad: number): void {
    this.devueltas.update((fs) => fs.map((f, idx) => (idx === i ? { ...f, cantidad } : f)));
  }

  protected agregarNueva(item: ItemElegido): void {
    const i = this.nuevas().findIndex((n) => n.presentacionId === item.presentacionId);
    if (i >= 0) return this.patchNueva(i, this.nuevas()[i].cantidad + 1);
    this.nuevas.update((ns) => [...ns, { ...item, cantidad: 1 }]);
  }

  protected patchNueva(i: number, cantidad: number): void {
    this.nuevas.update((ns) => ns.map((n, idx) => (idx === i ? { ...n, cantidad } : n)));
  }

  protected quitarNueva(i: number): void {
    this.nuevas.update((ns) => ns.filter((_, idx) => idx !== i));
  }

  protected agregarPago(): void {
    this.pagos.update((ps) => [...ps, { metodoPagoId: null, monto: 0 }]);
  }

  protected patchPago(i: number, cambios: Partial<PagoFila>): void {
    this.pagos.update((ps) => ps.map((p, idx) => (idx === i ? { ...p, ...cambios } : p)));
  }

  protected quitarPago(i: number): void {
    this.pagos.update((ps) => ps.filter((_, idx) => idx !== i));
  }

  protected enviar(): void {
    const devueltos = this.devueltas().filter((f) => f.cantidad > 0);
    if (devueltos.length === 0) return void this.error.set('Indica qué producto devuelve el cliente');
    if (devueltos.some((f) => f.cantidad > f.vendida)) return void this.error.set('No puedes devolver más de lo vendido');
    if (this.nuevas().length === 0) return void this.error.set('Agrega al menos un producto nuevo');
    if (this.nuevas().some((n) => !(n.cantidad > 0))) return void this.error.set('Las cantidades nuevas deben ser mayores a cero');
    if (!this.motivo().trim()) return void this.error.set('Indica el motivo del cambio');
    const pagos = this.pagos().filter((p) => p.monto > 0);
    if (pagos.some((p) => p.metodoPagoId === null)) return void this.error.set('Selecciona el método de cada pago');

    const body: CambioRequest = {
      motivo: this.motivo().trim(),
      devueltos: devueltos.map((f) => ({ ventaDetalleId: f.ventaDetalleId, cantidad: f.cantidad })),
      nuevos: this.nuevas().map((n) => ({ presentacionId: n.presentacionId, cantidad: n.cantidad })),
      ...(pagos.length ? { pagos: pagos.map((p) => ({ metodoPagoId: p.metodoPagoId!, monto: p.monto })) } : {}),
    };
    const firma = JSON.stringify(body);
    if (firma !== this.ultimoCuerpo) {
      this.ultimoCuerpo = firma;
      this.clave = nuevaClave();
    }

    this.enviando.set(true);
    this.error.set(null);
    this.service.cambio(this.venta().id, body, this.clave).subscribe({
      next: (r) => {
        this.enviando.set(false);
        this.resultado.set(r);
        this.done.emit(r);
      },
      error: (e) => {
        this.enviando.set(false);
        this.error.set(errorMessage(e, 'No se pudo registrar el cambio'));
      },
    });
  }
}
