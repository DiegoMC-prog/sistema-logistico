import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { errorMessage } from '../../../core/api/api-error';
import { Page } from '../../../core/api/page.model';
import { AuthService } from '../../../core/auth/auth.service';
import { PERMISOS } from '../../../core/auth/permisos';
import { ToastService } from '../../../core/ui/toast';
import { Badge } from '../../../shared/ui/badge';
import { Btn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { ConfirmDialog } from '../../../shared/ui/confirm-dialog';
import { Modal } from '../../../shared/ui/modal';
import { Pedido, Recepcion, etiquetaEstadoPedido, toneEstadoPedido } from '../data/pedido.model';
import { PedidosService } from '../data/pedidos.service';

interface FilaRecepcion {
  lineaId: number;
  producto: string;
  pendiente: number;
  cantidad: number;
  numeroLote: string;
  vence: string;
  descuento: number | null;
}

type Accion = 'enviar' | 'cancelar' | 'eliminar';

@Component({
  selector: 'app-pedido-detalle-page',
  imports: [DatePipe, DecimalPipe, FormsModule, RouterLink, Badge, Btn, Card, ConfirmDialog, Modal],
  template: `
    <div class="space-y-6">
      <a routerLink="/pedidos" class="text-sm font-medium text-accent hover:underline">← Volver a pedidos</a>

      @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }

      @if (pedido(); as p) {
        <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-start">
          <div>
            <h2 class="text-lg font-semibold text-ink">
              {{ p.numeroPedido }}
              <app-badge class="ml-2" [tone]="tone(p.estado)">{{ etiqueta(p.estado) }}</app-badge>
            </h2>
            <p class="mt-1 text-sm text-muted">{{ p.proveedor.nombre }} → {{ p.depositoDestino.nombre }}</p>
            <p class="text-sm text-muted">
              Pedido el {{ p.fechaPedido | date: 'dd/MM/yyyy' }}
              @if (p.fechaEntregaEstimada) { · entrega estimada {{ p.fechaEntregaEstimada | date: 'dd/MM/yyyy' }} }
            </p>
            @if (p.observacion) { <p class="mt-1 text-sm text-muted">{{ p.observacion }}</p> }
          </div>

          <div class="flex flex-wrap gap-3">
            @if (p.estado === 'BORRADOR') {
              @if (puede.editar) {
                <a [routerLink]="['/pedidos', p.id, 'editar']" class="inline-flex h-10 items-center rounded-lg border border-border bg-surface px-4 text-sm font-semibold text-ink hover:bg-subtle">Editar</a>
              }
              @if (puede.enviar) {
                <button appBtn icon="truck" [disabled]="p.items.length === 0 || trabajando()" [attr.title]="p.items.length === 0 ? 'Agrega al menos una línea para enviar' : null" (click)="accion.set('enviar')">Enviar</button>
              }
            }
            @if ((p.estado === 'ENVIADO' || p.estado === 'RECIBIDO_PARCIAL') && puede.recibir) {
              <button appBtn icon="download" (click)="abrirRecepcion()">Recibir mercadería</button>
            }
            @if ((p.estado === 'BORRADOR' || p.estado === 'ENVIADO') && puede.cancelar) {
              <button appBtn variant="secondary" icon="close" (click)="accion.set('cancelar')">Cancelar pedido</button>
            }
            @if (p.estado === 'BORRADOR' && puede.eliminar) {
              <button appBtn variant="danger" icon="trash" (click)="accion.set('eliminar')">Eliminar</button>
            }
          </div>
        </div>

        <app-card class="overflow-hidden">
          <div class="overflow-x-auto">
            <table class="dt">
              <thead><tr><th>Producto</th><th class="text-right">Pedido</th><th>Progreso</th><th class="text-right">Costo unitario</th><th class="text-right">Subtotal</th></tr></thead>
              <tbody>
                @for (i of p.items; track i.id) {
                  <tr>
                    <td class="text-sm font-semibold text-ink">{{ i.producto.nombre }}</td>
                    <td class="text-right text-sm">{{ i.cantidadPedida | number: '1.0-4' }}</td>
                    <td class="min-w-40">
                      <div class="h-2 overflow-hidden rounded-full bg-subtle">
                        <div class="h-full rounded-full bg-success" [style.width.%]="progreso(i.cantidadRecibida, i.cantidadPedida)"></div>
                      </div>
                      <p class="mt-1 text-xs text-muted">{{ i.cantidadRecibida | number: '1.0-4' }} / {{ i.cantidadPedida | number: '1.0-4' }}
                        @if (i.cantidadPendiente > 0 && p.estado !== 'BORRADOR' && p.estado !== 'CANCELADO') { · faltan {{ i.cantidadPendiente | number: '1.0-4' }} }</p>
                    </td>
                    <td class="text-right text-sm">{{ i.costoUnitario | number: '1.2-4' }}</td>
                    <td class="text-right text-sm font-semibold">{{ i.subtotal | number: '1.2-2' }}</td>
                  </tr>
                } @empty {
                  <tr><td colspan="5" class="text-sm text-muted">El pedido no tiene líneas.</td></tr>
                }
              </tbody>
            </table>
          </div>
          <p class="border-t border-border p-4 text-right text-base font-bold text-ink">Total: {{ p.total | number: '1.2-2' }}</p>
        </app-card>

        <app-card class="block p-5">
          <p class="text-sm font-semibold text-ink">Recepciones</p>
          @if (recepciones().length === 0) {
            <p class="mt-2 text-sm text-muted">Aún no hay recepciones.</p>
          } @else {
            <ul class="mt-3 space-y-3">
              @for (r of recepciones(); track r.id) {
                <li class="rounded-lg border border-border p-3 text-sm">
                  <p class="flex flex-wrap justify-between gap-2">
                    <span class="font-semibold text-ink">Recepción #{{ r.id }}@if (r.numeroGuia) { <span class="font-normal text-muted"> · guía {{ r.numeroGuia }}</span> }</span>
                    <span class="text-muted">{{ r.fechaRecepcion | date: 'dd/MM/yyyy HH:mm' }} · {{ r.usuario.nombre }}</span>
                  </p>
                  @if (r.observacion) { <p class="text-muted">{{ r.observacion }}</p> }
                  <ul class="mt-2 divide-y divide-border">
                    @for (i of r.items; track i.lineaId) {
                      <li class="flex justify-between py-1.5">
                        <span>{{ i.producto.nombre }} <span class="font-mono text-xs text-muted">· lote {{ i.lote.numeroLote }}</span></span>
                        <span class="font-semibold">{{ i.cantidad | number: '1.0-4' }}</span>
                      </li>
                    }
                  </ul>
                </li>
              }
            </ul>
          }
        </app-card>
      } @else if (!error()) {
        <p class="text-sm text-muted">Cargando…</p>
      }
    </div>

    @if (recepcionOpen()) {
      <app-modal title="Recibir mercadería" description="Cada línea recibida crea un lote nuevo con el costo pactado. Las cantidades van en unidad base." size="lg" (closed)="recepcionOpen.set(false)">
        <div class="space-y-4 p-5">
          @if (modalError()) { <p class="alert-error" role="alert">{{ modalError() }}</p> }
          <div class="grid gap-4 sm:grid-cols-2">
            <label class="block text-sm font-medium text-ink">N.º de guía <span class="font-normal text-muted">(opcional)</span>
              <input class="field-input mt-1" maxlength="60" [ngModel]="guia()" (ngModelChange)="guia.set($event)" />
            </label>
            <label class="block text-sm font-medium text-ink">Observación <span class="font-normal text-muted">(opcional)</span>
              <input class="field-input mt-1" maxlength="300" [ngModel]="obs()" (ngModelChange)="obs.set($event)" />
            </label>
          </div>
          <div class="space-y-3">
            @for (f of filas(); track f.lineaId; let i = $index) {
              <div class="space-y-2 rounded-lg border border-border p-3">
                <p class="flex justify-between text-sm"><span class="font-semibold text-ink">{{ f.producto }}</span><span class="text-muted">Pendiente: {{ f.pendiente | number: '1.0-4' }}</span></p>
                <div class="grid gap-3 sm:grid-cols-4">
                  <label class="text-xs text-muted">Cantidad recibida
                    <input type="number" min="0" [max]="f.pendiente" step="any" class="field-input !h-10 mt-1" [ngModel]="f.cantidad" (ngModelChange)="patch(i, { cantidad: +$event })" />
                  </label>
                  <label class="text-xs text-muted">N.º de lote (opcional)
                    <input class="field-input !h-10 mt-1" maxlength="80" [ngModel]="f.numeroLote" (ngModelChange)="patch(i, { numeroLote: $event })" />
                  </label>
                  <label class="text-xs text-muted">Vencimiento (opcional)
                    <input type="date" class="field-input !h-10 mt-1" [ngModel]="f.vence" (ngModelChange)="patch(i, { vence: $event })" />
                  </label>
                  <label class="text-xs text-muted">% desc. vencimiento
                    <input type="number" min="0" max="100" step="any" class="field-input !h-10 mt-1" [ngModel]="f.descuento" (ngModelChange)="patch(i, { descuento: $event === '' || $event === null ? null : +$event })" />
                  </label>
                </div>
              </div>
            } @empty {
              <p class="text-sm text-muted">No quedan líneas pendientes.</p>
            }
          </div>
          <p class="text-xs text-muted">Las líneas con cantidad 0 no se envían.</p>
        </div>
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="recepcionOpen.set(false)">Cancelar</button>
          <button appBtn type="button" [disabled]="trabajando()" (click)="recibir()">Registrar recepción</button>
        </ng-container>
      </app-modal>
    }

    @if (accion(); as a) {
      <app-confirm-dialog
        [title]="a === 'enviar' ? '¿Enviar pedido?' : a === 'cancelar' ? '¿Cancelar pedido?' : '¿Eliminar borrador?'"
        [message]="a === 'enviar' ? 'Una vez enviado ya no se podrá editar.' : a === 'cancelar' ? 'El pedido quedará cancelado. Solo es posible mientras no tenga recepciones.' : 'Se eliminará el borrador. Esta acción no se puede deshacer.'"
        [confirmLabel]="a === 'enviar' ? 'Enviar pedido' : a === 'cancelar' ? 'Cancelar pedido' : 'Eliminar borrador'"
        [icon]="a === 'enviar' ? 'truck' : a === 'cancelar' ? 'close' : 'trash'"
        [busy]="trabajando()"
        [error]="accionError()"
        (confirmed)="ejecutar(a)"
        (cancelled)="accion.set(null)"
      />
    }
  `,
})
export class PedidoDetallePage {
  private readonly service = inject(PedidosService);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));

  protected readonly tone = toneEstadoPedido;
  protected readonly etiqueta = etiquetaEstadoPedido;
  protected readonly puede = {
    editar: this.auth.hasPermission(PERMISOS.pedidoEditar),
    enviar: this.auth.hasPermission(PERMISOS.pedidoEnviar),
    cancelar: this.auth.hasPermission(PERMISOS.pedidoCancelar),
    eliminar: this.auth.hasPermission(PERMISOS.pedidoEliminar),
    recibir: this.auth.hasPermission(PERMISOS.pedidoRecibir),
  };

  protected readonly pedido = signal<Pedido | null>(null);
  protected readonly recepciones = signal<Recepcion[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly trabajando = signal(false);

  protected readonly accion = signal<Accion | null>(null);
  protected readonly accionError = signal<string | null>(null);

  protected readonly recepcionOpen = signal(false);
  protected readonly modalError = signal<string | null>(null);
  protected readonly guia = signal('');
  protected readonly obs = signal('');
  protected readonly filas = signal<FilaRecepcion[]>([]);
  protected readonly hayPendientes = computed(() => this.filas().length > 0);

  constructor() {
    this.cargar();
  }

  protected progreso(recibido: number, pedido: number): number {
    return pedido > 0 ? Math.min(100, (recibido / pedido) * 100) : 0;
  }

  private cargar(): void {
    this.service.get(this.id).subscribe({
      next: (p) => {
        this.error.set(null);
        this.pedido.set(p);
      },
      error: (e) => this.error.set(errorMessage(e, 'No se pudo cargar el pedido')),
    });
    this.service.recepciones(this.id).subscribe({
      next: (r) => this.recepciones.set(Array.isArray(r) ? r : (r as Page<Recepcion>).content),
      error: () => this.recepciones.set([]),
    });
  }

  protected ejecutar(a: Accion): void {
    this.trabajando.set(true);
    this.accionError.set(null);
    const req$: Observable<unknown> = a === 'enviar' ? this.service.enviar(this.id) : a === 'cancelar' ? this.service.cancelar(this.id) : this.service.delete(this.id);
    req$.subscribe({
      next: () => {
        this.trabajando.set(false);
        this.accion.set(null);
        if (a === 'eliminar') return void this.router.navigate(['/pedidos']);
        this.cargar();
      },
      error: (e) => {
        this.trabajando.set(false);
        this.accionError.set(errorMessage(e, 'No se pudo completar la acción'));
      },
    });
  }

  protected abrirRecepcion(): void {
    const p = this.pedido();
    if (!p) return;
    this.modalError.set(null);
    this.guia.set('');
    this.obs.set('');
    // Solo las líneas con pendiente, precargadas con lo que falta.
    this.filas.set(
      p.items
        .filter((i) => i.cantidadPendiente > 0)
        .map((i) => ({
          lineaId: i.id, producto: i.producto.nombre ?? `Producto #${i.producto.id}`, pendiente: i.cantidadPendiente,
          cantidad: i.cantidadPendiente, numeroLote: '', vence: '', descuento: null,
        })),
    );
    this.recepcionOpen.set(true);
  }

  protected patch(i: number, cambios: Partial<FilaRecepcion>): void {
    this.filas.update((fs) => fs.map((f, idx) => (idx === i ? { ...f, ...cambios } : f)));
  }

  protected recibir(): void {
    if (this.trabajando()) return;
    const items = this.filas().filter((f) => f.cantidad > 0);
    if (items.length === 0) return void this.modalError.set('Indica la cantidad recibida de al menos una línea');
    const excede = items.find((f) => f.cantidad > f.pendiente);
    if (excede) return void this.modalError.set(`«${excede.producto}»: no puedes recibir más de lo pendiente (${excede.pendiente}).`);

    this.trabajando.set(true);
    this.modalError.set(null);
    this.service
      .recibir(this.id, {
        numeroGuia: this.guia().trim() || undefined,
        observacion: this.obs().trim() || undefined,
        items: items.map((f) => ({
          lineaId: f.lineaId,
          cantidad: f.cantidad,
          ...(f.numeroLote.trim() ? { numeroLote: f.numeroLote.trim() } : {}),
          ...(f.vence ? { fechaVencimiento: f.vence } : {}),
          ...(f.descuento !== null ? { porcentajeDescuentoVencimiento: f.descuento } : {}),
        })),
      })
      .subscribe({
        next: (r) => {
          this.trabajando.set(false);
          this.recepcionOpen.set(false);
          this.toast.info(`El pedido quedó ${etiquetaEstadoPedido(r.estadoPedido).toLowerCase()}.`, { title: `Recepción #${r.id}` });
          this.cargar();
        },
        error: (e) => {
          this.trabajando.set(false);
          this.modalError.set(errorMessage(e, 'No se pudo registrar la recepción'));
        },
      });
  }
}
