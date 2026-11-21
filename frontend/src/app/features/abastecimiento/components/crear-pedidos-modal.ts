import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, input, OnInit, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { errorMessage } from '../../../core/api/api-error';
import { Btn } from '../../../shared/ui/button';
import { Modal } from '../../../shared/ui/modal';
import { Deposito } from '../../depositos/data/deposito.model';
import { DepositosService } from '../../depositos/data/depositos.service';
import { Escenario, ESCENARIO_LABEL, ESCENARIOS, PedidoGenerado, resultado, Sugerencia } from '../data/abastecimiento.model';
import { AbastecimientoService } from '../data/abastecimiento.service';

interface Linea {
  sugerencia: Sugerencia;
  cantidad: number;
}

/** Convierte las sugerencias elegidas en pedidos BORRADOR: uno por proveedor. */
@Component({
  selector: 'app-crear-pedidos-modal',
  imports: [DecimalPipe, FormsModule, RouterLink, Btn, Modal],
  template: `
    <app-modal
      title="Crear pedidos desde sugerencias"
      description="Se crea un pedido en borrador por cada proveedor. Puede revisarlo y enviarlo desde Pedidos."
      size="lg"
      (closed)="closed.emit()"
    >
      @if (creados(); as lista) {
        <div class="space-y-3 p-5">
          <p class="text-sm text-ink">Se crearon {{ lista.length }} pedido(s) en borrador:</p>
          <ul class="divide-y divide-border rounded-lg border border-border">
            @for (p of lista; track p.pedidoId) {
              <li class="flex items-center justify-between gap-3 p-3 text-sm">
                <div>
                  <p class="font-semibold text-ink">{{ p.numeroPedido }} · {{ p.proveedor }}</p>
                  <p class="text-xs text-muted">{{ p.lineas }} producto(s) · total {{ p.total | number: '1.2-2' }}</p>
                </div>
                <a class="text-sm font-semibold text-accent hover:underline" [routerLink]="['/pedidos', p.pedidoId]">Abrir pedido</a>
              </li>
            }
          </ul>
        </div>
      } @else {
        <div class="space-y-5 p-5">
          @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }

          <div class="grid gap-4 sm:grid-cols-2">
            <label class="flex flex-col gap-2">
              <span class="field-label">Depósito de destino <span class="text-danger">*</span></span>
              <select class="field-input" [(ngModel)]="depositoId">
                <option [ngValue]="null">Elija un depósito</option>
                @for (d of depositos(); track d.id) {
                  <option [ngValue]="d.id">{{ d.nombre }}</option>
                }
              </select>
            </label>
            <label class="flex flex-col gap-2">
              <span class="field-label">Escenario para las cantidades</span>
              <select class="field-input" [ngModel]="escenarioSel()" (ngModelChange)="cambiarEscenario($event)">
                @for (e of escenarios; track e) {
                  <option [ngValue]="e">{{ etiquetaEscenario[e] }}</option>
                }
              </select>
            </label>
          </div>

          <div class="overflow-x-auto rounded-lg border border-border">
            <table class="dt">
              <thead><tr><th>Producto</th><th>Proveedor</th><th class="text-right">Costo</th><th class="w-36 text-right">Cantidad</th></tr></thead>
              <tbody>
                @for (l of lineas(); track l.sugerencia.id; let i = $index) {
                  <tr>
                    <td class="text-sm font-semibold text-ink">{{ l.sugerencia.producto }}</td>
                    <td class="text-sm text-muted">{{ l.sugerencia.proveedor }}</td>
                    <td class="text-right text-sm">{{ l.sugerencia.costoUnitario | number: '1.2-4' }}</td>
                    <td class="text-right">
                      <input
                        type="number"
                        min="1"
                        step="1"
                        class="field-input !h-9 text-right"
                        [ngModel]="l.cantidad"
                        (ngModelChange)="cambiarCantidad(i, $event)"
                        [attr.aria-label]="'Cantidad de ' + l.sugerencia.producto"
                      />
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
          <p class="text-xs text-muted">
            {{ proveedores() }} proveedor(es) · total estimado {{ total() | number: '1.2-2' }}. Las cantidades van en unidad base.
          </p>

          <label class="flex flex-col gap-2">
            <span class="field-label">Observación <span class="font-normal text-muted">(opcional)</span></span>
            <textarea class="field-input min-h-20 py-2" maxlength="2000" [(ngModel)]="observacion"></textarea>
          </label>
        </div>
      }
      <div modal-footer class="flex gap-3">
        @if (creados()) {
          <button appBtn (click)="closed.emit()">Listo</button>
        } @else {
          <button appBtn variant="secondary" (click)="closed.emit()">Cancelar</button>
          <button appBtn icon="truck" [disabled]="enviando() || !valido()" (click)="crear()">
            {{ enviando() ? 'Creando…' : 'Crear pedidos' }}
          </button>
        }
      </div>
    </app-modal>
  `,
})
export class CrearPedidosModal implements OnInit {
  private readonly service = inject(AbastecimientoService);
  private readonly depositosService = inject(DepositosService);

  readonly sugerencias = input.required<Sugerencia[]>();
  readonly escenario = input<Escenario>('ESTANDAR');
  readonly closed = output<void>();
  /** Se emite al crear los pedidos, para que la lista se recargue. */
  readonly creadosChange = output<PedidoGenerado[]>();

  protected readonly escenarios = ESCENARIOS;
  protected readonly etiquetaEscenario = ESCENARIO_LABEL;
  protected readonly depositos = signal<Deposito[]>([]);
  protected readonly lineas = signal<Linea[]>([]);
  protected readonly escenarioSel = signal<Escenario>('ESTANDAR');
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly creados = signal<PedidoGenerado[] | null>(null);
  protected depositoId: number | null = null;
  protected observacion = '';

  protected readonly total = computed(() =>
    this.lineas().reduce((s, l) => s + (l.cantidad || 0) * l.sugerencia.costoUnitario, 0),
  );
  protected readonly proveedores = computed(() => new Set(this.lineas().map((l) => l.sugerencia.proveedorId)).size);
  protected readonly valido = computed(() => this.lineas().length > 0 && this.lineas().every((l) => l.cantidad > 0));

  ngOnInit(): void {
    this.escenarioSel.set(this.escenario());
    this.armarLineas(this.escenario());
    this.depositosService.list({ size: 100, estado: 'ACTIVO' }).subscribe({
      next: (p) => {
        this.depositos.set(p.content);
        if (p.content.length === 1) this.depositoId = p.content[0].id;
      },
      error: (e) => this.error.set(errorMessage(e, 'No se pudieron cargar los depósitos')),
    });
  }

  protected cambiarEscenario(e: Escenario): void {
    this.escenarioSel.set(e);
    this.armarLineas(e);
  }

  protected cambiarCantidad(i: number, valor: number | null): void {
    this.lineas.update((ls) => ls.map((l, j) => (j === i ? { ...l, cantidad: Math.max(0, Math.ceil(Number(valor) || 0)) } : l)));
  }

  protected crear(): void {
    if (this.depositoId == null) {
      this.error.set('Elija el depósito donde se recibirá la mercadería');
      return;
    }
    this.enviando.set(true);
    this.error.set(null);
    this.service
      .crearPedidos({
        depositoDestinoId: this.depositoId,
        escenario: this.escenarioSel(),
        observacion: this.observacion.trim() || undefined,
        items: this.lineas().map((l) => ({ sugerenciaId: l.sugerencia.id, cantidad: l.cantidad })),
      })
      .subscribe({
        next: (pedidos) => {
          this.enviando.set(false);
          this.creados.set(pedidos);
          this.creadosChange.emit(pedidos);
        },
        error: (e) => {
          this.enviando.set(false);
          this.error.set(errorMessage(e, 'No se pudieron crear los pedidos'));
        },
      });
  }

  private armarLineas(e: Escenario): void {
    this.lineas.set(this.sugerencias().map((s) => ({ sugerencia: s, cantidad: resultado(s, e).cantidad })));
  }
}
