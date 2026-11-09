import { DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { errorMessage } from '../../../core/api/api-error';
import { Btn } from '../../../shared/ui/button';
import { Modal } from '../../../shared/ui/modal';
import { MetodoPago } from '../../metodos-pago/data/metodo-pago.model';
import { MetodosPagoService } from '../../metodos-pago/data/metodos-pago.service';
import { Venta } from '../../ventas/data/venta.model';
import { nuevaClave } from '../../ventas/data/ventas.service';
import { Devolucion, DevolucionRequest } from '../data/devolucion.model';
import { DevolucionesService } from '../data/devoluciones.service';

interface FilaDevolucion {
  ventaDetalleId: number;
  producto: string;
  vendida: number;
  cantidad: number;
  reingresaStock: boolean;
}

/** Devolución (parcial o total) de una venta, con o sin reembolso. */
@Component({
  selector: 'app-devolucion-modal',
  imports: [DecimalPipe, FormsModule, Btn, Modal],
  template: `
    <app-modal title="Registrar devolución" [description]="'Venta ' + venta().numeroVenta" size="lg" (closed)="cerrar()">
      @if (resultado(); as r) {
        <div class="space-y-3 p-5 text-sm">
          <p class="alert-success">Devolución {{ r.numeroDevolucion }} registrada.</p>
          <p>Monto reembolsado: <strong class="text-ink">{{ r.montoReembolsado | number: '1.2-2' }}</strong>
            @if (r.metodoPago) { <span class="text-muted"> ({{ r.metodoPago.nombre }})</span> }</p>
        </div>
      } @else {
        <div class="space-y-4 p-5">
          @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }

          <div class="overflow-x-auto">
            <table class="dt">
              <thead><tr><th>Producto</th><th class="text-right">Vendida</th><th class="w-28">A devolver</th><th>Reingresa stock</th></tr></thead>
              <tbody>
                @for (f of filas(); track f.ventaDetalleId; let i = $index) {
                  <tr>
                    <td class="text-sm font-semibold text-ink">{{ f.producto }}</td>
                    <td class="text-right text-sm">{{ f.vendida }}</td>
                    <td><input type="number" min="0" [max]="f.vendida" step="any" class="field-input !h-9" [ngModel]="f.cantidad" (ngModelChange)="patch(i, { cantidad: +$event })" /></td>
                    <td><input type="checkbox" [ngModel]="f.reingresaStock" (ngModelChange)="patch(i, { reingresaStock: $event })" aria-label="Reingresa stock" /></td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
          <p class="text-xs text-muted">Desmarca «reingresa stock» para producto dañado o vencido. El reembolso es proporcional a lo que el cliente realmente pagó.</p>

          <label class="block text-sm font-medium text-ink">Motivo
            <input class="field-input mt-1" maxlength="200" [ngModel]="motivo()" (ngModelChange)="motivo.set($event)" />
          </label>

          <label class="flex items-center gap-2 text-sm font-medium text-ink">
            <input type="checkbox" [ngModel]="reembolsar()" (ngModelChange)="reembolsar.set($event)" /> Reembolsar dinero al cliente
          </label>
          @if (reembolsar()) {
            <label class="block text-sm font-medium text-ink">Método de reembolso
              <select class="field-input mt-1" [ngModel]="metodoId()" (ngModelChange)="metodoId.set($event)">
                <option [ngValue]="null" disabled>Selecciona un método</option>
                @for (m of metodos(); track m.id) { <option [ngValue]="m.id">{{ m.nombre }}</option> }
              </select>
            </label>
          }
        </div>
      }
      @if (resultado()) {
        <ng-container modal-footer><button appBtn type="button" (click)="cerrar()">Cerrar</button></ng-container>
      } @else {
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="cerrar()">Cancelar</button>
          <button appBtn type="button" [disabled]="enviando()" (click)="enviar()">Registrar devolución</button>
        </ng-container>
      }
    </app-modal>
  `,
})
export class DevolucionModal implements OnInit {
  private readonly service = inject(DevolucionesService);
  private readonly metodosService = inject(MetodosPagoService);

  readonly venta = input.required<Venta>();
  readonly closed = output<void>();
  readonly done = output<Devolucion>();

  protected readonly metodos = signal<MetodoPago[]>([]);
  protected readonly motivo = signal('');
  protected readonly reembolsar = signal(true);
  protected readonly metodoId = signal<number | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly resultado = signal<Devolucion | null>(null);
  protected readonly filas = signal<FilaDevolucion[]>([]);
  protected readonly hayCambios = computed(() => this.filas().some((f) => f.cantidad > 0));

  private ultimoCuerpo = '';
  private clave = nuevaClave();

  ngOnInit(): void {
    this.filas.set(
      this.venta().lineas.map((l) => ({
        ventaDetalleId: l.id, producto: l.producto, vendida: l.cantidad, cantidad: 0, reingresaStock: true,
      })),
    );
  }

  constructor() {
    this.metodosService.paraCobro().subscribe({ next: (m) => this.metodos.set(m) });
  }

  protected patch(i: number, cambios: Partial<FilaDevolucion>): void {
    this.filas.update((fs) => fs.map((f, idx) => (idx === i ? { ...f, ...cambios } : f)));
  }

  protected cerrar(): void {
    this.closed.emit();
  }

  protected enviar(): void {
    const items = this.filas().filter((f) => f.cantidad > 0);
    if (items.length === 0) return void this.error.set('Indica la cantidad a devolver de al menos una línea');
    if (items.some((f) => f.cantidad > f.vendida)) return void this.error.set('No puedes devolver más de lo vendido');
    if (!this.motivo().trim()) return void this.error.set('Indica el motivo de la devolución');
    if (this.reembolsar() && this.metodoId() === null) return void this.error.set('Selecciona el método de reembolso');

    const body: DevolucionRequest = {
      motivo: this.motivo().trim(),
      reembolsar: this.reembolsar(),
      ...(this.reembolsar() ? { metodoPagoId: this.metodoId()! } : {}),
      items: items.map((f) => ({ ventaDetalleId: f.ventaDetalleId, cantidad: f.cantidad, reingresaStock: f.reingresaStock })),
    };
    const firma = JSON.stringify(body);
    if (firma !== this.ultimoCuerpo) {
      this.ultimoCuerpo = firma;
      this.clave = nuevaClave();
    }

    this.enviando.set(true);
    this.error.set(null);
    this.service.crear(this.venta().id, body, this.clave).subscribe({
      next: (d) => {
        this.enviando.set(false);
        this.resultado.set(d);
        this.done.emit(d);
      },
      error: (e) => {
        this.enviando.set(false);
        this.error.set(errorMessage(e, 'No se pudo registrar la devolución'));
      },
    });
  }
}
