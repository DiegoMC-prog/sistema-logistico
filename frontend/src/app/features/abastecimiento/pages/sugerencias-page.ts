import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { errorMessage } from '../../../core/api/api-error';
import { Page } from '../../../core/api/page.model';
import { AuthService } from '../../../core/auth/auth.service';
import { PERMISOS } from '../../../core/auth/permisos';
import { Badge, BadgeTone } from '../../../shared/ui/badge';
import { Btn, IconBtn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { EmptyState } from '../../../shared/ui/empty-state';
import { FilterSelect } from '../../../shared/ui/filter-select';
import { Paginator } from '../../../shared/ui/paginator';
import { SearchInput } from '../../../shared/ui/search-input';
import { StatCard } from '../../../shared/ui/stat-card';
import { TableSkeleton } from '../../../shared/ui/table-skeleton';
import { CrearPedidosModal } from '../components/crear-pedidos-modal';
import { ParametrosModal } from '../components/parametros-modal';
import {
  ACCION_LABEL,
  AccionSugerencia,
  CalculoReorden,
  ESCENARIO_LABEL,
  ESCENARIOS,
  Escenario,
  ESTADO_LABEL,
  EstadoSugerencia,
  resultado,
  ResultadoEscenario,
  Sugerencia,
  SugerenciaFiltro,
} from '../data/abastecimiento.model';
import { AbastecimientoService } from '../data/abastecimiento.service';

const TONO_ESTADO: Record<EstadoSugerencia, BadgeTone> = {
  REORDENAR: 'warning',
  SIN_NECESIDAD: 'success',
  DATOS_INSUFICIENTES: 'neutral',
  SIN_DEMANDA: 'neutral',
  SIN_PROVEEDOR: 'danger',
};

const TONO_ACCION: Record<AccionSugerencia, BadgeTone> = {
  PENDIENTE: 'info',
  PEDIDO_CREADO: 'success',
  DESCARTADA: 'neutral',
};

@Component({
  selector: 'app-sugerencias-page',
  imports: [
    DatePipe,
    DecimalPipe,
    RouterLink,
    Badge,
    Btn,
    IconBtn,
    Card,
    EmptyState,
    FilterSelect,
    Paginator,
    SearchInput,
    StatCard,
    TableSkeleton,
    CrearPedidosModal,
    ParametrosModal,
  ],
  template: `
    <div class="space-y-6">
      <div class="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <h2 class="text-lg font-semibold text-ink">Reabastecimiento sugerido</h2>
          @if (calculo(); as c) {
            <p class="mt-1 text-sm text-muted">
              Último cálculo: {{ c.fecha | date: 'dd/MM/yyyy HH:mm' }} ({{ c.origen === 'MANUAL' ? 'manual, por ' + c.ejecutadoPor : 'automático' }})
              · ventas de los últimos {{ c.diasVentana }} días · historial mínimo {{ c.historialMinimoDias }} días.
            </p>
          } @else if (cargado()) {
            <p class="mt-1 text-sm text-muted">Todavía no se calcularon sugerencias. El cálculo corre cada noche o puede ejecutarlo ahora.</p>
          }
        </div>
        @if (puedeGestionar) {
          <button appBtn variant="secondary" icon="activity" [disabled]="recalculando()" (click)="recalcular()">
            {{ recalculando() ? 'Calculando…' : 'Recalcular ahora' }}
          </button>
        }
      </div>

      @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }

      @if (calculo(); as c) {
        <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <app-stat-card label="A reordenar" [value]="c.porEstado.REORDENAR" hint="Posición ≤ punto de reorden" />
          <app-stat-card label="Stock suficiente" tone="success" [value]="c.porEstado.SIN_NECESIDAD" />
          <app-stat-card label="Datos insuficientes" tone="muted" [value]="c.porEstado.DATOS_INSUFICIENTES" hint="Falta historial o costo" />
          <app-stat-card label="Sin demanda o proveedor" tone="muted" [value]="c.porEstado.SIN_DEMANDA + c.porEstado.SIN_PROVEEDOR" />
        </div>
      }

      <div class="flex flex-col gap-3 lg:flex-row lg:items-center">
        <app-search-input class="lg:w-72" placeholder="Buscar producto" (searched)="buscar($event)" />
        <app-filter-select class="lg:w-56" icon="filter" label="Estado" (changed)="cambiarEstado($event)">
          <option value="">Todos los estados</option>
          <option value="REORDENAR">Reordenar</option>
          <option value="SIN_NECESIDAD">Stock suficiente</option>
          <option value="DATOS_INSUFICIENTES">Datos insuficientes</option>
          <option value="SIN_DEMANDA">Sin demanda</option>
          <option value="SIN_PROVEEDOR">Sin proveedor</option>
        </app-filter-select>
        <app-filter-select class="lg:w-48" icon="tag" label="Acción" (changed)="cambiarAccion($event)">
          <option value="">Todas las acciones</option>
          <option value="PENDIENTE">Pendientes</option>
          <option value="PEDIDO_CREADO">Con pedido</option>
          <option value="DESCARTADA">Descartadas</option>
        </app-filter-select>
        <div class="flex rounded-lg border border-border bg-surface p-1 lg:ml-auto" role="group" aria-label="Escenario">
          @for (e of escenarios; track e) {
            <button
              type="button"
              class="h-8 rounded-md px-3 text-xs font-semibold transition"
              [class]="escenario() === e ? 'bg-brand text-white' : 'text-muted hover:bg-subtle hover:text-ink'"
              [attr.aria-pressed]="escenario() === e"
              (click)="escenario.set(e)"
            >
              {{ etiquetaEscenario[e] }}
            </button>
          }
        </div>
      </div>

      @if (puedeGestionar && seleccion().size > 0) {
        <div class="flex flex-col gap-3 rounded-xl border border-accent bg-accent-soft/40 p-4 sm:flex-row sm:items-center sm:justify-between">
          <p class="text-sm text-ink">{{ seleccion().size }} sugerencia(s) elegidas · escenario {{ etiquetaEscenario[escenario()].toLowerCase() }}</p>
          <div class="flex gap-2">
            <button appBtn variant="ghost" (click)="limpiarSeleccion()">Quitar selección</button>
            <button appBtn icon="truck" (click)="abrirCrearPedidos()">Crear pedidos</button>
          </div>
        </div>
      }

      <app-card class="overflow-hidden">
        @if (pagina(); as p) {
          @if (p.content.length === 0) {
            <app-empty-state title="Sin sugerencias" message="No hay productos para los filtros elegidos." />
          } @else {
            <div class="overflow-x-auto">
              <table class="dt">
                <thead>
                  <tr>
                    @if (puedeGestionar) { <th class="w-10"><span class="sr-only">Elegir</span></th> }
                    <th>Producto</th>
                    <th>Proveedor</th>
                    <th class="text-right">Posición</th>
                    <th class="text-right">Punto de reorden</th>
                    <th class="text-right">EOQ</th>
                    <th class="text-right">Pedir</th>
                    <th>Estado</th>
                    <th class="text-right">Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  @for (s of p.content; track s.id) {
                    @let r = res(s);
                    <tr>
                      @if (puedeGestionar) {
                        <td>
                          @if (seleccionable(s)) {
                            <input
                              type="checkbox"
                              [checked]="seleccion().has(s.id)"
                              (change)="alternar(s)"
                              [attr.aria-label]="'Elegir ' + s.producto"
                            />
                          }
                        </td>
                      }
                      <td>
                        <p class="text-sm font-semibold text-ink">{{ s.producto }}</p>
                        <p class="mt-0.5 text-xs text-muted">{{ s.categoria }}@if (s.perecedero) { · perecedero }</p>
                      </td>
                      <td class="text-sm" [class.text-muted]="!s.proveedor">{{ s.proveedor ?? 'Sin proveedor' }}</td>
                      <td class="text-right text-sm">
                        {{ s.stockActual + s.enTransito | number: '1.0-1' }}
                        @if (s.enTransito > 0) {
                          <p class="text-xs text-muted">{{ s.enTransito | number: '1.0-1' }} en camino</p>
                        }
                      </td>
                      <td class="text-right text-sm">{{ calculable(s) ? (r.puntoReorden | number: '1.0-1') : '—' }}</td>
                      <td class="text-right text-sm">{{ calculable(s) ? (r.eoq | number: '1.0-1') : '—' }}</td>
                      <td class="text-right">
                        @if (r.cantidad > 0) {
                          <p class="text-sm font-bold text-ink">{{ r.cantidad | number: '1.0-0' }}</p>
                        } @else {
                          <p class="text-sm text-muted">—</p>
                        }
                        @if (calculable(s)) {
                          <p class="text-[11px] text-muted" title="Pesimista / estándar / optimista">
                            {{ s.pesimista.cantidad | number: '1.0-0' }} / {{ s.estandar.cantidad | number: '1.0-0' }} / {{ s.optimista.cantidad | number: '1.0-0' }}
                          </p>
                        }
                      </td>
                      <td>
                        <div class="flex flex-col items-start gap-1">
                          <app-badge [tone]="tonoEstado[s.estado]">{{ etiquetaEstado[s.estado] }}</app-badge>
                          @if (s.diasAnticipo > 0) {
                            <app-badge tone="danger" [attr.title]="'El costo del proveedor subió: se adelanta la compra ' + s.diasAnticipo + ' días'">
                              Alza {{ s.variacionCosto! * 100 | number: '1.0-1' }} %
                            </app-badge>
                          }
                          @if (s.accion !== 'PENDIENTE') {
                            @if (s.pedidoId) {
                              <a class="text-xs font-semibold text-accent hover:underline" [routerLink]="['/pedidos', s.pedidoId]">{{ s.numeroPedido }}</a>
                            } @else {
                              <app-badge [tone]="tonoAccion[s.accion]">{{ etiquetaAccion[s.accion] }}</app-badge>
                            }
                          }
                        </div>
                      </td>
                      <td class="text-right">
                        <div class="flex justify-end gap-1">
                          <button appIconBtn [icon]="abierta() === s.id ? 'close' : 'eye'" [label]="abierta() === s.id ? 'Ocultar detalle' : 'Ver detalle'" (click)="alternarDetalle(s)"></button>
                          @if (puedeGestionar) {
                            <button appIconBtn icon="settings" label="Parámetros del producto" (click)="parametros.set(s)"></button>
                            @if (s.accion === 'PENDIENTE' && s.estado === 'REORDENAR') {
                              <button appIconBtn icon="trash" label="Descartar sugerencia" danger (click)="descartar(s)"></button>
                            }
                          }
                        </div>
                      </td>
                    </tr>
                    @if (abierta() === s.id) {
                      <tr class="bg-table-head">
                        <td [attr.colspan]="puedeGestionar ? 9 : 8" class="!whitespace-normal">
                          <div class="space-y-4 py-2">
                            <p class="text-sm text-ink">{{ s.motivo }}</p>
                            <dl class="grid gap-3 text-sm sm:grid-cols-3 lg:grid-cols-6">
                              <div><dt class="text-xs text-muted">Demanda diaria (σ)</dt><dd class="font-semibold text-ink">{{ s.demandaDiaria | number: '1.0-2' }} ({{ s.desviacionDemanda | number: '1.0-2' }})</dd></div>
                              <div><dt class="text-xs text-muted">Lead time, días (σ)</dt><dd class="font-semibold text-ink">{{ s.leadTimeDias | number: '1.0-1' }} ({{ s.desviacionLeadTime | number: '1.0-1' }}) · {{ s.muestrasLeadTime }} pedidos</dd></div>
                              <div><dt class="text-xs text-muted">Último costo de compra</dt><dd class="font-semibold text-ink">{{ s.costoUnitario | number: '1.2-4' }}</dd></div>
                              <div>
                                <dt class="text-xs text-muted">Variación reciente del costo</dt>
                                <dd class="font-semibold" [class]="s.variacionCosto == null ? 'text-muted' : s.variacionCosto > 0 ? 'text-danger' : 'text-success'">
                                  @if (s.variacionCosto == null) { Sin datos } @else { {{ s.variacionCosto > 0 ? '+' : '' }}{{ s.variacionCosto * 100 | number: '1.0-1' }} % }
                                  @if (s.diasAnticipo > 0) { <span class="text-xs font-normal text-muted">· compra adelantada {{ s.diasAnticipo }} días</span> }
                                </dd>
                              </div>
                              <div><dt class="text-xs text-muted">Costo por pedido (S)</dt><dd class="font-semibold text-ink">{{ s.costoPedido | number: '1.2-2' }}</dd></div>
                              <div><dt class="text-xs text-muted">Almacenaje anual (H)</dt><dd class="font-semibold text-ink">{{ s.costoAlmacenAnual | number: '1.2-4' }}</dd></div>
                              <div><dt class="text-xs text-muted">Nivel de servicio (z)</dt><dd class="font-semibold text-ink">{{ s.nivelServicio * 100 | number: '1.0-1' }} % ({{ s.factorZ | number: '1.2-2' }})</dd></div>
                              <div><dt class="text-xs text-muted">Stock vendible</dt><dd class="font-semibold text-ink">{{ s.stockActual | number: '1.0-1' }}</dd></div>
                              <div><dt class="text-xs text-muted">En camino</dt><dd class="font-semibold text-ink">{{ s.enTransito | number: '1.0-1' }}</dd></div>
                              <div><dt class="text-xs text-muted">Historial de ventas</dt><dd class="font-semibold text-ink">{{ s.diasHistorial }} días</dd></div>
                              @if (s.vidaUtilDias != null) {
                                <div><dt class="text-xs text-muted">Vida útil media</dt><dd class="font-semibold text-ink">{{ s.vidaUtilDias | number: '1.0-0' }} días</dd></div>
                              }
                            </dl>
                            @if (calculable(s)) {
                              <div class="overflow-x-auto rounded-lg border border-border bg-surface">
                                <table class="dt">
                                  <thead><tr><th>Escenario</th><th class="text-right">EOQ</th><th class="text-right">Stock de seguridad</th><th class="text-right">Punto de reorden</th><th class="text-right">Cantidad a pedir</th></tr></thead>
                                  <tbody>
                                    @for (e of escenarios; track e) {
                                      @let x = resDe(s, e);
                                      <tr [class.font-semibold]="e === escenario()">
                                        <td class="text-sm">{{ etiquetaEscenario[e] }}</td>
                                        <td class="text-right text-sm">{{ x.eoq | number: '1.0-1' }}</td>
                                        <td class="text-right text-sm">{{ x.stockSeguridad | number: '1.0-1' }}</td>
                                        <td class="text-right text-sm">{{ x.puntoReorden | number: '1.0-1' }}</td>
                                        <td class="text-right text-sm">{{ x.cantidad | number: '1.0-0' }}</td>
                                      </tr>
                                    }
                                  </tbody>
                                </table>
                              </div>
                              <p class="text-xs text-muted">
                                Pesimista: demanda y lead time una desviación estándar por encima de la media; optimista: una por debajo.
                              </p>
                            }
                          </div>
                        </td>
                      </tr>
                    }
                  }
                </tbody>
              </table>
            </div>
            @if (p.totalPages > 1) {
              <app-paginator [page]="p.page" [size]="p.size" [totalPages]="p.totalPages" [totalElements]="p.totalElements" (pageChange)="irA($event)" />
            }
          }
        } @else if (!error()) {
          <app-table-skeleton />
        }
      </app-card>
    </div>

    @if (crearAbierto()) {
      <app-crear-pedidos-modal
        [sugerencias]="elegidas()"
        [escenario]="escenario()"
        (creadosChange)="pedidosCreados()"
        (closed)="crearAbierto.set(false)"
      />
    }

    @if (parametros(); as s) {
      <app-parametros-modal [productoId]="s.productoId" [producto]="s.producto" (saved)="parametros.set(null)" (closed)="parametros.set(null)" />
    }
  `,
})
export class SugerenciasPage {
  private readonly service = inject(AbastecimientoService);
  protected readonly puedeGestionar = inject(AuthService).hasPermission(PERMISOS.reordenGestionar);

  protected readonly escenarios = ESCENARIOS;
  protected readonly etiquetaEscenario = ESCENARIO_LABEL;
  protected readonly etiquetaEstado = ESTADO_LABEL;
  protected readonly etiquetaAccion = ACCION_LABEL;
  protected readonly tonoEstado = TONO_ESTADO;
  protected readonly tonoAccion = TONO_ACCION;

  protected readonly calculo = signal<CalculoReorden | null>(null);
  protected readonly cargado = signal(false);
  protected readonly pagina = signal<Page<Sugerencia> | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly recalculando = signal(false);
  protected readonly escenario = signal<Escenario>('ESTANDAR');
  protected readonly abierta = signal<number | null>(null);
  protected readonly parametros = signal<Sugerencia | null>(null);
  protected readonly crearAbierto = signal(false);

  /** Sugerencias elegidas para convertir en pedido; se conservan al cambiar de página. */
  private readonly elegidasMap = signal<ReadonlyMap<number, Sugerencia>>(new Map());
  protected readonly seleccion = computed(() => new Set(this.elegidasMap().keys()));
  protected readonly elegidas = computed(() => [...this.elegidasMap().values()]);

  private filtro: SugerenciaFiltro = { estado: undefined, page: 0, size: 20 };

  constructor() {
    this.cargarCalculo();
    this.cargar();
  }

  protected res(s: Sugerencia): ResultadoEscenario {
    return resultado(s, this.escenario());
  }

  protected resDe(s: Sugerencia, e: Escenario): ResultadoEscenario {
    return resultado(s, e);
  }

  /** Solo REORDENAR y SIN_NECESIDAD tienen EOQ y punto de reorden calculados. */
  protected calculable(s: Sugerencia): boolean {
    return s.estado === 'REORDENAR' || s.estado === 'SIN_NECESIDAD';
  }

  protected seleccionable(s: Sugerencia): boolean {
    return s.accion === 'PENDIENTE' && s.proveedorId != null && this.calculable(s);
  }

  protected alternar(s: Sugerencia): void {
    const next = new Map(this.elegidasMap());
    next.has(s.id) ? next.delete(s.id) : next.set(s.id, s);
    this.elegidasMap.set(next);
  }

  protected limpiarSeleccion(): void {
    this.elegidasMap.set(new Map());
  }

  protected alternarDetalle(s: Sugerencia): void {
    this.abierta.set(this.abierta() === s.id ? null : s.id);
  }

  protected abrirCrearPedidos(): void {
    this.crearAbierto.set(true);
  }

  protected pedidosCreados(): void {
    this.limpiarSeleccion();
    this.cargar();
  }

  protected buscar(texto: string): void {
    this.filtro = { ...this.filtro, search: texto || undefined, page: 0 };
    this.cargar();
  }

  protected cambiarEstado(valor: string): void {
    this.filtro = { ...this.filtro, estado: (valor || undefined) as EstadoSugerencia | undefined, page: 0 };
    this.cargar();
  }

  protected cambiarAccion(valor: string): void {
    this.filtro = { ...this.filtro, accion: (valor || undefined) as AccionSugerencia | undefined, page: 0 };
    this.cargar();
  }

  protected irA(page: number): void {
    this.filtro = { ...this.filtro, page };
    this.cargar();
  }

  protected recalcular(): void {
    this.recalculando.set(true);
    this.error.set(null);
    this.service.recalcular().subscribe({
      next: (c) => {
        this.recalculando.set(false);
        this.calculo.set(c);
        // Las sugerencias viejas dejan de ser las vigentes: la selección ya no aplica
        this.limpiarSeleccion();
        this.cargar();
      },
      error: (e) => {
        this.recalculando.set(false);
        this.error.set(errorMessage(e, 'No se pudieron recalcular las sugerencias'));
      },
    });
  }

  protected descartar(s: Sugerencia): void {
    this.service.descartar(s.id).subscribe({
      next: () => {
        const next = new Map(this.elegidasMap());
        next.delete(s.id);
        this.elegidasMap.set(next);
        this.cargar();
      },
      error: (e) => this.error.set(errorMessage(e, 'No se pudo descartar la sugerencia')),
    });
  }

  private cargarCalculo(): void {
    this.service.ultimoCalculo().subscribe({
      next: (c) => {
        this.calculo.set(c);
        this.cargado.set(true);
      },
      error: (e) => {
        this.cargado.set(true);
        this.error.set(errorMessage(e, 'No se pudo consultar el último cálculo'));
      },
    });
  }

  private cargar(): void {
    this.service.sugerencias(this.filtro).subscribe({
      next: (p) => this.pagina.set(p),
      error: (e) => this.error.set(errorMessage(e, 'No se pudieron cargar las sugerencias')),
    });
  }
}
