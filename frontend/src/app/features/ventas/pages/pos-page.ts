import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Observable, catchError, throwError, timer, switchMap } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { errorMessage } from '../../../core/api/api-error';
import { AuthService } from '../../../core/auth/auth.service';
import { PERMISOS } from '../../../core/auth/permisos';
import { Btn, IconBtn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { Icon } from '../../../shared/ui/icon';
import { Modal } from '../../../shared/ui/modal';
import { SearchInput } from '../../../shared/ui/search-input';
import { Caja } from '../../cajas/data/caja.model';
import { CajasService } from '../../cajas/data/cajas.service';
import { Cliente } from '../../clientes/data/cliente.model';
import { ClientesService } from '../../clientes/data/clientes.service';
import { MetodoPago } from '../../metodos-pago/data/metodo-pago.model';
import { MetodosPagoService } from '../../metodos-pago/data/metodos-pago.service';
import { PreciosService } from '../../precios/data/precios.service';
import { Turno } from '../../turnos/data/turno.model';
import { TurnosService } from '../../turnos/data/turnos.service';
import { NotaVenta } from '../components/nota-venta';
import { ItemElegido, ProductPicker } from '../components/product-picker';
import { Venta, VentaRequest, round2 } from '../data/venta.model';
import { VentasService, nuevaClave } from '../data/ventas.service';

interface Linea {
  uid: number;
  presentacionId: number;
  nombre: string;
  unidad: string;
  cantidad: number;
  precio: number | null;
  precioManual: number | null;
  motivo: string;
  error: string | null;
  cargando: boolean;
}

interface PagoFila {
  metodoPagoId: number | null;
  monto: number;
  referencia: string;
}

@Component({
  selector: 'app-pos-page',
  imports: [DecimalPipe, FormsModule, RouterLink, Btn, IconBtn, Card, Icon, Modal, NotaVenta, ProductPicker, SearchInput],
  template: `
    @if (cargandoTurno()) {
      <p class="text-sm text-muted">Verificando turno de caja…</p>
    } @else if (!turno()) {
      <app-card class="mx-auto block max-w-lg space-y-4 p-6">
        <h2 class="text-lg font-semibold text-ink">No tienes un turno de caja abierto</h2>
        <p class="text-sm text-muted">Sin turno abierto no se puede vender. Abre uno para comenzar.</p>
        @if (errorTurno()) { <p class="alert-error" role="alert">{{ errorTurno() }}</p> }
        @if (puedeAbrir) {
          <label class="block text-sm font-medium text-ink">Caja
            <select class="field-input mt-1" [ngModel]="cajaSel()" (ngModelChange)="cajaSel.set($event)">
              <option [ngValue]="null" disabled>Selecciona una caja</option>
              @for (c of cajas(); track c.id) { <option [ngValue]="c.id">{{ c.nombre }}</option> }
            </select>
          </label>
          <label class="block text-sm font-medium text-ink">Monto de apertura
            <input type="number" min="0" step="0.01" class="field-input mt-1" [ngModel]="apertura()" (ngModelChange)="apertura.set($event)" />
          </label>
          <button appBtn icon="wallet" [disabled]="!cajaSel() || abriendo()" (click)="abrirTurno()">Abrir turno</button>
        } @else {
          <p class="text-sm text-danger">Tu usuario no tiene permiso para abrir turnos. Pide a un supervisor que lo haga.</p>
        }
      </app-card>
    } @else {
      <div class="grid gap-6 xl:grid-cols-[1fr_26rem]">
        <div class="space-y-6">
          <app-card class="block space-y-3 p-5">
            <div class="flex flex-wrap items-center justify-between gap-2 text-sm">
              <span class="text-muted">Turno <a [routerLink]="['/turnos', turno()!.id]" class="font-semibold text-accent hover:underline">#{{ turno()!.id }}</a> · {{ turno()!.cajaNombre }}</span>
              <span class="text-muted">Efectivo esperado: <strong class="text-ink">{{ turno()!.efectivoEsperado | number: '1.2-2' }}</strong></span>
            </div>
            <app-product-picker (picked)="agregar($event)" />
          </app-card>

          <app-card class="overflow-hidden">
            @if (lineas().length === 0) {
              <div class="p-10 text-center text-sm text-muted">
                <app-icon name="cart" class="mx-auto mb-2 size-8" />El carrito está vacío. Escanea un producto o búscalo por nombre.
              </div>
            } @else {
              <div class="overflow-x-auto">
                <table class="dt">
                  <thead><tr><th>Producto</th><th class="w-28">Cantidad</th><th class="text-right">Precio</th><th class="text-right">Subtotal</th><th></th></tr></thead>
                  <tbody>
                    @for (l of lineas(); track l.uid) {
                      <tr>
                        <td>
                          <p class="text-sm font-semibold text-ink">{{ l.nombre }} <span class="font-normal text-muted">({{ l.unidad }})</span></p>
                          @if (l.error) { <p class="text-xs text-danger">{{ l.error }}</p> }
                          @if (puedeModificarPrecio && l.precioManual !== null) {
                            <input class="field-input !h-8 mt-1 text-xs" placeholder="Motivo del cambio de precio (obligatorio)"
                              [ngModel]="l.motivo" (ngModelChange)="patch(l.uid, { motivo: $event })" />
                          }
                        </td>
                        <td>
                          <input type="number" min="0.0001" step="any" class="field-input !h-9" [ngModel]="l.cantidad"
                            (change)="cambiarCantidad(l, $any($event.target).value)" />
                        </td>
                        <td class="text-right text-sm">
                          @if (puedeModificarPrecio) {
                            <input type="number" min="0" step="0.01" class="field-input !h-9 w-24 text-right"
                              [ngModel]="l.precioManual ?? l.precio" (change)="cambiarPrecio(l, $any($event.target).value)" />
                          } @else {
                            {{ l.cargando ? '…' : (l.precio === null ? '—' : (l.precio | number: '1.2-2')) }}
                          }
                        </td>
                        <td class="text-right text-sm font-semibold text-ink">{{ subtotal(l) | number: '1.2-2' }}</td>
                        <td class="text-right"><button appIconBtn icon="trash" danger [label]="'Quitar ' + l.nombre" (click)="quitar(l.uid)"></button></td>
                      </tr>
                    }
                  </tbody>
                </table>
              </div>
            }
          </app-card>
        </div>

        <div class="space-y-6">
          <app-card class="block space-y-3 p-5">
            <p class="text-sm font-semibold text-ink">Cliente</p>
            @if (cliente(); as c) {
              <div class="flex items-center justify-between rounded-lg bg-subtle p-3 text-sm">
                <span><strong class="text-ink">{{ c.nombre }}</strong> <span class="text-muted">· {{ c.tipoCliente.nombre }}</span></span>
                <button type="button" class="text-xs text-accent hover:underline" (click)="elegirCliente(null)">Quitar</button>
              </div>
              @if (c.creditoHabilitado) {
                <p class="text-xs text-muted">Crédito habilitado · límite {{ c.limiteCredito | number: '1.2-2' }}. Puedes cobrar todo o parte con «CRÉDITO (fiado)».</p>
              } @else {
                <p class="text-xs text-muted">Este cliente no tiene crédito habilitado.</p>
              }
            } @else {
              <app-search-input class="!max-w-none" placeholder="Buscar cliente (opcional)…" (searched)="buscarCliente($event)" />
              @if (clientes().length > 0) {
                <ul class="divide-y divide-border rounded-lg border border-border text-sm">
                  @for (c of clientes(); track c.id) {
                    <li><button type="button" class="w-full px-3 py-2 text-left hover:bg-subtle" (click)="elegirCliente(c)">
                      {{ c.nombre }} <span class="text-muted">· {{ c.documento || c.tipoCliente.nombre }}</span></button></li>
                  }
                </ul>
              }
            }
          </app-card>

          <app-card class="block space-y-4 p-5">
            <div class="flex items-baseline justify-between">
              <span class="text-sm font-semibold text-ink">Total (vista previa)</span>
              <span class="text-3xl font-bold text-ink">{{ total() | number: '1.2-2' }}</span>
            </div>
            <p class="text-xs text-muted">El total final lo calcula el servidor (descuentos por vencimiento, redondeo).</p>

            <div class="space-y-2">
              <div class="flex items-center justify-between">
                <p class="text-sm font-semibold text-ink">Pagos</p>
                <span class="flex gap-2">
                  <button appBtn variant="secondary" class="!h-8 !px-3 !text-xs" type="button" (click)="pagoExacto()" [disabled]="!efectivoId()">Exacto en efectivo</button>
                  <button appBtn variant="secondary" icon="plus" class="!h-8 !px-3 !text-xs" type="button" (click)="agregarPago()">Pago</button>
                </span>
              </div>
              @for (p of pagos(); track $index) {
                <div class="grid grid-cols-[1fr_6rem_auto] items-center gap-2">
                  <select class="field-input !h-9" [ngModel]="p.metodoPagoId" (ngModelChange)="patchPago($index, { metodoPagoId: $event })">
                    <option [ngValue]="null" disabled>Método</option>
                    @for (m of metodosDisponibles(); track m.id) { <option [ngValue]="m.id">{{ m.nombre === 'CREDITO' ? 'CRÉDITO (fiado)' : m.nombre }}</option> }
                  </select>
                  <input type="number" min="0" step="0.01" class="field-input !h-9 text-right" [ngModel]="p.monto" (ngModelChange)="patchPago($index, { monto: +$event })" />
                  <button appIconBtn icon="close" label="Quitar pago" (click)="quitarPago($index)"></button>
                  @if (p.metodoPagoId !== efectivoId() && p.metodoPagoId !== creditoId()) {
                    <input class="field-input !h-8 col-span-3 text-xs" placeholder="Referencia (opcional)" [ngModel]="p.referencia" (ngModelChange)="patchPago($index, { referencia: $event })" />
                  }
                </div>
              }
              @if (pagos().length > 0) {
                <p class="text-xs" [class]="faltante() > 0 ? 'text-danger' : 'text-muted'">
                  @if (faltante() > 0) { Falta {{ faltante() | number: '1.2-2' }} }
                  @else if (cambio() > 0) { Vuelto estimado {{ cambio() | number: '1.2-2' }} (solo si hay efectivo) }
                  @else { Pagos cubren el total }
                </p>
              }
            </div>

            @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }

            <button appBtn class="w-full !h-12 !text-base" icon="receipt" [disabled]="cobrando() || lineas().length === 0" (click)="cobrar()">
              {{ cobrando() ? 'Procesando…' : 'Cobrar' }}
            </button>
          </app-card>
        </div>
      </div>
    }

    @if (nota(); as n) {
      <app-modal title="Venta registrada" size="lg" (closed)="nuevaVenta()">
        <div class="p-5"><app-nota-venta [venta]="n" /></div>
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="imprimir()">Imprimir</button>
          <button appBtn type="button" (click)="nuevaVenta()">Nueva venta</button>
        </ng-container>
      </app-modal>
    }
  `,
})
export class PosPage {
  private readonly auth = inject(AuthService);
  private readonly turnos = inject(TurnosService);
  private readonly cajasService = inject(CajasService);
  private readonly clientesService = inject(ClientesService);
  private readonly metodosService = inject(MetodosPagoService);
  private readonly preciosService = inject(PreciosService);
  private readonly ventasService = inject(VentasService);

  protected readonly puedeAbrir = this.auth.hasPermission(PERMISOS.cajaAbrir);
  protected readonly puedeModificarPrecio = this.auth.hasPermission(PERMISOS.ventaPrecioModificar);

  // Turno
  protected readonly turno = signal<Turno | null>(null);
  protected readonly cargandoTurno = signal(true);
  protected readonly errorTurno = signal<string | null>(null);
  protected readonly cajas = signal<Caja[]>([]);
  protected readonly cajaSel = signal<number | null>(null);
  protected readonly apertura = signal(0);
  protected readonly abriendo = signal(false);

  // Venta en curso
  protected readonly lineas = signal<Linea[]>([]);
  protected readonly cliente = signal<Cliente | null>(null);
  protected readonly clientes = signal<Cliente[]>([]);
  protected readonly metodos = signal<MetodoPago[]>([]);
  protected readonly pagos = signal<PagoFila[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly cobrando = signal(false);
  protected readonly nota = signal<Venta | null>(null);

  protected readonly creditoId = computed(() => this.metodos().find((m) => m.nombre === 'CREDITO')?.id ?? null);
  /** CRÉDITO solo se ofrece con un cliente que tenga el crédito habilitado. */
  protected readonly metodosDisponibles = computed(() =>
    this.metodos().filter((m) => m.nombre !== 'CREDITO' || this.cliente()?.creditoHabilitado === true),
  );
  protected readonly efectivoId = computed(() => this.metodos().find((m) => m.nombre === 'EFECTIVO')?.id ?? null);
  protected readonly total = computed(() => round2(this.lineas().reduce((s, l) => s + this.subtotal(l), 0)));
  private readonly pagado = computed(() => round2(this.pagos().reduce((s, p) => s + (p.monto || 0), 0)));
  protected readonly faltante = computed(() => Math.max(0, round2(this.total() - this.pagado())));
  protected readonly cambio = computed(() => Math.max(0, round2(this.pagado() - this.total())));

  private uid = 0;
  private ultimoCuerpo = '';
  private claveActual = nuevaClave();

  constructor() {
    this.cargarTurno();
    this.metodosService.paraCobro().subscribe({ next: (m) => this.metodos.set(m) });
  }

  // --- Turno ---

  private cargarTurno(): void {
    this.cargandoTurno.set(true);
    // GET /turnos filtra automáticamente a los turnos propios.
    this.turnos.list({ estado: 'ABIERTO', size: 1 }).subscribe({
      next: (p) => {
        const abierto = p.content[0];
        if (!abierto) return this.sinTurno();
        this.turnos.get(abierto.id).subscribe({
          next: (t) => {
            this.turno.set(t);
            this.cargandoTurno.set(false);
          },
          error: (e) => {
            this.errorTurno.set(errorMessage(e, 'No se pudo cargar el turno'));
            this.cargandoTurno.set(false);
          },
        });
      },
      error: (e) => {
        this.errorTurno.set(errorMessage(e, 'No se pudo verificar el turno'));
        this.cargandoTurno.set(false);
      },
    });
  }

  private sinTurno(): void {
    this.turno.set(null);
    this.cargandoTurno.set(false);
    if (this.puedeAbrir) {
      this.cajasService.list({ size: 100, estado: 'ACTIVO' }).subscribe({ next: (p) => this.cajas.set(p.content) });
    }
  }

  protected abrirTurno(): void {
    const caja = this.cajaSel();
    if (!caja) return;
    this.abriendo.set(true);
    this.errorTurno.set(null);
    this.turnos.abrir(caja, this.apertura() || 0).subscribe({
      next: (t) => {
        this.abriendo.set(false);
        this.turno.set(t);
      },
      error: (e) => {
        this.abriendo.set(false);
        this.errorTurno.set(errorMessage(e));
      },
    });
  }

  private refrescarTurno(): void {
    const t = this.turno();
    if (t) this.turnos.get(t.id).subscribe({ next: (n) => this.turno.set(n) });
  }

  // --- Carrito ---

  protected subtotal(l: Linea): number {
    const precio = l.precioManual ?? l.precio ?? 0;
    return round2(l.cantidad * precio);
  }

  protected agregar(item: ItemElegido): void {
    const existente = this.lineas().find((l) => l.presentacionId === item.presentacionId && l.precioManual === null);
    if (existente) return this.cambiarCantidad(existente, String(existente.cantidad + 1));
    const linea: Linea = {
      uid: ++this.uid, presentacionId: item.presentacionId, nombre: item.nombre, unidad: item.unidad,
      cantidad: 1, precio: null, precioManual: null, motivo: '', error: null, cargando: true,
    };
    this.lineas.update((ls) => [...ls, linea]);
    this.cargarPrecio(linea.uid);
  }

  protected patch(uid: number, cambios: Partial<Linea>): void {
    this.lineas.update((ls) => ls.map((l) => (l.uid === uid ? { ...l, ...cambios } : l)));
  }

  protected quitar(uid: number): void {
    this.lineas.update((ls) => ls.filter((l) => l.uid !== uid));
  }

  protected cambiarCantidad(l: Linea, valor: string): void {
    const cantidad = Number(valor);
    if (!(cantidad > 0)) return this.patch(l.uid, { cantidad: l.cantidad }); // restablece la vista
    this.patch(l.uid, { cantidad, cargando: true });
    this.cargarPrecio(l.uid); // la cantidad puede activar un precio por volumen
  }

  protected cambiarPrecio(l: Linea, valor: string): void {
    const n = Number(valor);
    if (valor === '' || !(n >= 0) || n === l.precio) return this.patch(l.uid, { precioManual: null, motivo: '' });
    this.patch(l.uid, { precioManual: n });
  }

  private cargarPrecio(uid: number): void {
    const l = this.lineas().find((x) => x.uid === uid);
    if (!l) return;
    this.preciosService.aplicable(l.presentacionId, l.cantidad, this.cliente()?.tipoCliente.id).subscribe({
      next: (r) => this.patch(uid, { precio: r.precio, error: null, cargando: false }),
      error: (e) => this.patch(uid, { precio: null, cargando: false, error: errorMessage(e, 'Sin precio aplicable') }),
    });
  }

  // --- Cliente ---

  protected buscarCliente(texto: string): void {
    if (!texto) return void this.clientes.set([]);
    this.clientesService.list({ search: texto, size: 5, estado: 'ACTIVO' }).subscribe({
      next: (p) => this.clientes.set(p.content),
    });
  }

  protected elegirCliente(c: Cliente | null): void {
    this.cliente.set(c);
    this.clientes.set([]);
    if (!c?.creditoHabilitado) this.pagos.update((ps) => ps.filter((p) => p.metodoPagoId !== this.creditoId()));
    // El tipo de cliente cambia el precio aplicable de todas las líneas.
    this.lineas.update((ls) => ls.map((l) => ({ ...l, cargando: true })));
    this.lineas().forEach((l) => this.cargarPrecio(l.uid));
  }

  // --- Pagos ---

  protected agregarPago(): void {
    const usados = new Set(this.pagos().map((p) => p.metodoPagoId));
    const libre = this.metodosDisponibles().find((m) => !usados.has(m.id));
    this.pagos.update((ps) => [...ps, { metodoPagoId: libre?.id ?? null, monto: this.faltante(), referencia: '' }]);
  }

  protected pagoExacto(): void {
    this.pagos.set([{ metodoPagoId: this.efectivoId(), monto: this.total(), referencia: '' }]);
  }

  protected patchPago(i: number, cambios: Partial<PagoFila>): void {
    this.pagos.update((ps) => ps.map((p, idx) => (idx === i ? { ...p, ...cambios } : p)));
  }

  protected quitarPago(i: number): void {
    this.pagos.update((ps) => ps.filter((_, idx) => idx !== i));
  }

  // --- Cobro ---

  private validar(): string | null {
    const ls = this.lineas();
    if (ls.length === 0) return 'Agrega al menos un producto';
    if (ls.some((l) => l.cargando)) return 'Espera a que se carguen los precios';
    const sinPrecio = ls.find((l) => l.precio === null && l.precioManual === null);
    if (sinPrecio) return `«${sinPrecio.nombre}» no tiene precio aplicable`;
    if (ls.some((l) => l.precioManual !== null && !l.motivo.trim())) return 'Indica el motivo de cada precio modificado';
    const ps = this.pagos();
    if (ps.some((p) => p.metodoPagoId === null)) return 'Selecciona el método de cada pago';
    const ids = ps.map((p) => p.metodoPagoId);
    if (new Set(ids).size !== ids.length) return 'No repitas el método de pago';
    if (this.total() > 0 && this.faltante() > 0) return `Los pagos no cubren el total: faltan ${this.faltante().toFixed(2)}`;
    return null;
  }

  protected cobrar(): void {
    if (this.cobrando()) return;
    const problema = this.validar();
    if (problema) return void this.error.set(problema);

    const body: VentaRequest = {
      clienteId: this.cliente()?.id,
      items: this.lineas().map((l) => ({
        presentacionId: l.presentacionId,
        cantidad: l.cantidad,
        ...(l.precioManual !== null ? { precioUnitario: l.precioManual, motivoPrecio: l.motivo.trim() } : {}),
      })),
      pagos: this.pagos()
        .filter((p) => p.monto > 0)
        .map((p) => ({ metodoPagoId: p.metodoPagoId!, monto: p.monto, ...(p.referencia.trim() ? { referencia: p.referencia.trim() } : {}) })),
    };

    // Misma clave solo si el contenido es idéntico (reintento); venta distinta = clave nueva.
    const firma = JSON.stringify(body);
    if (firma !== this.ultimoCuerpo) {
      this.ultimoCuerpo = firma;
      this.claveActual = nuevaClave();
    }

    this.cobrando.set(true);
    this.error.set(null);
    this.enviar(body, this.claveActual).subscribe({
      next: (venta) => {
        this.cobrando.set(false);
        this.nota.set(venta);
        this.refrescarTurno();
      },
      error: (e) => {
        this.cobrando.set(false);
        this.error.set(errorMessage(e, 'No se pudo registrar la venta'));
      },
    });
  }

  /** Un 409 "recurso ocupado… vuelva a intentarlo" se reintenta una vez con la misma clave. */
  private enviar(body: VentaRequest, clave: string): Observable<Venta> {
    return this.ventasService.crear(body, clave).pipe(
      catchError((e: unknown) => {
        const ocupado = e instanceof HttpErrorResponse && e.status === 409 && /ocupado|vuelva a intentar/i.test(errorMessage(e, ''));
        return ocupado
          ? timer(600).pipe(switchMap(() => this.ventasService.crear(body, clave)))
          : throwError(() => e);
      }),
    );
  }

  protected nuevaVenta(): void {
    this.nota.set(null);
    this.lineas.set([]);
    this.pagos.set([]);
    this.cliente.set(null);
    this.clientes.set([]);
    this.error.set(null);
    this.ultimoCuerpo = '';
    this.claveActual = nuevaClave();
  }

  protected imprimir(): void {
    globalThis.print();
  }
}
