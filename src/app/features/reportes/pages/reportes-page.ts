import { formatDate, formatNumber } from '@angular/common';
import { Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { errorMessage } from '../../../core/api/api-error';
import { Badge, BadgeTone } from '../../../shared/ui/badge';
import { Btn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { EmptyState } from '../../../shared/ui/empty-state';
import { TableSkeleton } from '../../../shared/ui/table-skeleton';
import { Caja } from '../../cajas/data/caja.model';
import { CajasService } from '../../cajas/data/cajas.service';
import { Categoria } from '../../categorias/data/categoria.model';
import { CategoriasService } from '../../categorias/data/categorias.service';
import { DepositosRef } from '../../inventario/components/depositos-ref';
import { ProductoBuscador } from '../../pedidos/components/producto-buscador';
import { Producto } from '../../productos/data/producto.model';
import { Proveedor } from '../../proveedores/data/proveedor.model';
import { ProveedoresService } from '../../proveedores/data/proveedores.service';
import { Col, FiltroDef, ReporteDef, Valores, columnasDe, familiaPorId, totalesDe } from '../data/reportes.config';
import { ParamsReporte, ReporteResponse, ReportesService } from '../data/reportes.service';

const LOCALE = 'en-US';
const MAX_DIAS = 366;

/** yyyy-MM-dd en hora local (toISOString devolvería la fecha UTC, que puede ser otro día). */
const iso = (d: Date): string =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;

const haceDias = (n: number): Date => {
  const d = new Date();
  d.setDate(d.getDate() - n);
  return d;
};

const ESTADO_TONOS: Record<string, BadgeTone> = {
  COMPLETADA: 'success', ANULADA: 'danger', ABIERTO: 'success', CERRADO: 'neutral',
  ANULACION: 'danger', DEVOLUCION: 'warning', CAMBIO: 'info',
  POR_VENCER: 'warning', VENCIDO: 'danger',
  ENVIADO: 'info', RECIBIDO_PARCIAL: 'warning', RECIBIDO: 'success', CANCELADO: 'danger', BORRADOR: 'neutral',
  ENTRADA: 'success', SALIDA: 'danger', TRASLADO: 'info', AJUSTE_POSITIVO: 'success', AJUSTE_NEGATIVO: 'warning',
};

@Component({
  selector: 'app-reportes-page',
  imports: [RouterLink, Badge, Btn, Card, EmptyState, ProductoBuscador, TableSkeleton],
  template: `
    @if (!familia()) {
      <p class="alert-error" role="alert">Ese reporte no existe.</p>
    } @else {
      <div class="space-y-6">
        <nav class="flex flex-wrap gap-2" aria-label="Reportes">
          @for (r of familia()!.reportes; track r.id) {
            <a [routerLink]="['/reportes', familia()!.id, r.id]"
              class="h-10 rounded-lg border px-4 text-sm font-semibold leading-10 transition"
              [class]="def()?.id === r.id ? 'border-brand bg-brand text-white' : 'border-border bg-surface text-ink hover:bg-subtle'">
              {{ r.titulo }}
            </a>
          }
        </nav>

        @if (def(); as d) {
          <p class="text-sm text-muted">{{ d.descripcion }}</p>

          <app-card class="block space-y-4 p-4">
            <div class="flex flex-wrap items-end gap-3">
              @if (d.rango) {
                <label class="flex flex-col gap-1 text-xs text-muted">Desde
                  <input type="date" class="field-input !h-10" [value]="desde()" (change)="cambiarRango('desde', $any($event.target).value)" />
                </label>
                <label class="flex flex-col gap-1 text-xs text-muted">Hasta
                  <input type="date" class="field-input !h-10" [value]="hasta()" (change)="cambiarRango('hasta', $any($event.target).value)" />
                </label>
                <div class="flex flex-wrap gap-2">
                  @for (p of atajos; track p.label) {
                    <button type="button" class="h-10 rounded-lg border border-border bg-surface px-3 text-sm font-medium text-ink transition hover:bg-subtle" (click)="aplicarAtajo(p.dias)">{{ p.label }}</button>
                  }
                  <button type="button" class="h-10 rounded-lg border border-border bg-surface px-3 text-sm font-medium text-ink transition hover:bg-subtle" (click)="mesActual()">Este mes</button>
                </div>
              }

              @for (f of d.filtros; track f.key) {
                @switch (f.tipo) {
                  @case ('check') {
                    <label class="flex h-10 items-center gap-2 text-sm text-ink">
                      <input type="checkbox" class="size-4 accent-[var(--color-accent)]" [checked]="valores()[f.key] === true" (change)="setValor(f.key, $any($event.target).checked)" />
                      {{ f.label }}
                    </label>
                  }
                  @case ('numero') {
                    <label class="flex flex-col gap-1 text-xs text-muted">{{ f.label }}
                      <input type="number" class="field-input !h-10 w-32" [attr.min]="f.min" [attr.max]="f.max" [value]="valores()[f.key] ?? ''" (change)="setValor(f.key, $any($event.target).value)" />
                    </label>
                  }
                  @case ('producto') {
                    <div class="min-w-64 flex-1">
                      <p class="mb-1 text-xs text-muted">{{ f.label }} <span class="text-danger">*</span></p>
                      @if (producto(); as p) {
                        <div class="flex h-10 items-center justify-between rounded-lg border border-border bg-subtle px-3 text-sm">
                          <span class="font-semibold text-ink">{{ p.nombre }}</span>
                          <button type="button" class="text-xs text-accent hover:underline" (click)="quitarProducto()">Cambiar</button>
                        </div>
                      } @else {
                        <app-producto-buscador (picked)="elegirProducto($event)" />
                      }
                    </div>
                  }
                  @default {
                    <label class="flex flex-col gap-1 text-xs text-muted">{{ f.label }}
                      <select class="field-input !h-10" (change)="setValor(f.key, $any($event.target).value)">
                        @if (f.tipo !== 'select') { <option value="">Todos</option> }
                        @for (o of opciones(f); track o.value) {
                          <option [value]="o.value" [selected]="(valores()[f.key] ?? '') === o.value">{{ o.label }}</option>
                        }
                      </select>
                    </label>
                  }
                }
              }

              <button appBtn variant="secondary" icon="download" class="ml-auto" [disabled]="!datos() || cargando() || exportando()" (click)="exportar()">
                {{ exportando() ? 'Exportando…' : 'Exportar CSV' }}
              </button>
            </div>
          </app-card>

          @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }
          @if (pendiente()) {
            <app-card><app-empty-state title="Falta un dato" [message]="pendiente()!" /></app-card>
          }
          @if (datos(); as r) {
            @if (r.totales['truncado'] === true) {
              <p class="rounded-lg border border-warning/30 bg-warning-soft px-4 py-3 text-sm font-semibold text-warning-dark" role="status">
                Hay más registros que el límite elegido: acota el rango o sube el límite para verlos todos.
              </p>
            }

            @if (tarjetas().length > 0) {
              <div class="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
                @for (t of tarjetas(); track t.col.key) {
                  <app-card class="block p-4">
                    <p class="text-xs font-medium text-muted">{{ t.col.label }}</p>
                    <p class="mt-1 text-xl font-bold" [class]="t.clase">{{ t.texto }}</p>
                  </app-card>
                }
              </div>
            }

            <app-card class="overflow-hidden">
              <div class="flex items-center justify-between border-b border-border p-4 text-sm text-muted">
                <span>{{ r.filas.length }} {{ r.filas.length === 1 ? 'fila' : 'filas' }}</span>
                @if (r.desde && r.hasta) { <span>{{ r.desde }} → {{ r.hasta }}</span> }
              </div>
              @if (r.filas.length === 0) {
                <app-empty-state title="Sin datos" message="No hay registros para estos filtros." />
              } @else {
                <div class="max-h-[34rem] overflow-auto">
                  <table class="dt">
                    <thead>
                      <tr>
                        @for (c of columnas(); track c.key) { <th [class]="derecha(c) ? 'text-right' : ''">{{ c.label }}</th> }
                      </tr>
                    </thead>
                    <tbody>
                      @for (fila of r.filas; track $index) {
                        <tr>
                          @for (c of columnas(); track c.key) {
                            <td class="whitespace-nowrap text-sm" [class]="(derecha(c) ? 'text-right ' : '') + clase(c, fila[c.key])">
                              @if (c.tipo === 'estado' && fila[c.key]) {
                                <app-badge [tone]="tono(fila[c.key])">{{ texto(c, fila[c.key]) }}</app-badge>
                              } @else {
                                {{ texto(c, fila[c.key]) }}
                              }
                            </td>
                          }
                        </tr>
                      }
                    </tbody>
                  </table>
                </div>
              }
            </app-card>
          } @else if (cargando()) {
            <app-card class="overflow-hidden"><app-table-skeleton /></app-card>
          }
        }
      </div>
    }
  `,
})
export class ReportesPage {
  private readonly service = inject(ReportesService);
  private readonly categoriasService = inject(CategoriasService);
  private readonly cajasService = inject(CajasService);
  private readonly proveedoresService = inject(ProveedoresService);
  private readonly params = toSignal(inject(ActivatedRoute).paramMap);

  protected readonly familia = computed(() => familiaPorId(this.params()?.get('familia')));
  protected readonly def = computed<ReporteDef | undefined>(() => {
    const f = this.familia();
    return f?.reportes.find((r) => r.id === this.params()?.get('reporte')) ?? f?.reportes[0];
  });

  protected readonly atajos = [
    { label: 'Hoy', dias: 0 },
    { label: '7 días', dias: 6 },
    { label: '30 días', dias: 29 },
  ];

  // Filtros
  protected readonly desde = signal(iso(haceDias(29)));
  protected readonly hasta = signal(iso(new Date()));
  protected readonly valores = signal<Valores>({});
  protected readonly producto = signal<Producto | null>(null);

  // Resultado
  protected readonly datos = signal<ReporteResponse | null>(null);
  protected readonly cargando = signal(false);
  protected readonly exportando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly pendiente = signal<string | null>(null);
  private peticion = 0;

  protected readonly columnas = computed(() => {
    const d = this.def();
    return d ? columnasDe(d, this.valores()) : [];
  });

  protected readonly tarjetas = computed(() => {
    const d = this.def();
    const r = this.datos();
    if (!d || !r) return [];
    return totalesDe(d, this.valores())
      .filter((c) => r.totales[c.key] !== undefined && r.totales[c.key] !== null)
      .map((c) => ({ col: c, texto: this.texto(c, r.totales[c.key]), clase: this.clase(c, r.totales[c.key]) || 'text-ink' }));
  });

  // Catálogos para los filtros
  private readonly categorias = signal<Categoria[]>([]);
  private readonly cajas = signal<Caja[]>([]);
  private readonly proveedores = signal<Proveedor[]>([]);
  private readonly depositos = inject(DepositosRef).depositos;
  private readonly cargados = new Set<string>();

  constructor() {
    effect(() => {
      const d = this.def();
      if (!d) return;
      untracked(() => this.reiniciar(d));
    });
  }

  // --- Filtros ---

  private reiniciar(d: ReporteDef): void {
    const v: Valores = {};
    for (const f of d.filtros) if (f.defecto !== undefined) v[f.key] = f.defecto;
    this.valores.set(v);
    this.producto.set(null);
    this.datos.set(null);
    this.error.set(null);
    this.cargarCatalogos(d);
    this.cargar();
  }

  private cargarCatalogos(d: ReporteDef): void {
    const necesita = (t: string) => d.filtros.some((f) => f.tipo === t) && !this.cargados.has(t);
    if (necesita('categoria')) {
      this.cargados.add('categoria');
      this.categoriasService.list({ size: 200 }).subscribe({ next: (p) => this.categorias.set(p.content) });
    }
    if (necesita('caja')) {
      this.cargados.add('caja');
      this.cajasService.list({ size: 100 }).subscribe({ next: (p) => this.cajas.set(p.content) });
    }
    if (necesita('proveedor')) {
      this.cargados.add('proveedor');
      // Listar proveedores exige PROVEEDOR_VER: sin él el selector queda con la sola opción «Todos».
      this.proveedoresService.list({ size: 100 }).subscribe({ next: (p) => this.proveedores.set(p.content), error: () => undefined });
    }
  }

  protected opciones(f: FiltroDef): { value: string; label: string }[] {
    switch (f.tipo) {
      case 'categoria': return this.categorias().map((c) => ({ value: String(c.id), label: c.nombre }));
      case 'caja': return this.cajas().map((c) => ({ value: String(c.id), label: c.nombre }));
      case 'deposito': return this.depositos().map((x) => ({ value: String(x.id), label: x.nombre }));
      case 'proveedor': return this.proveedores().map((p) => ({ value: String(p.id), label: p.nombreEmpresa }));
      default: return f.opciones ?? [];
    }
  }

  protected setValor(key: string, valor: string | boolean): void {
    this.valores.update((v) => ({ ...v, [key]: valor }));
    this.cargar();
  }

  protected elegirProducto(p: Producto): void {
    this.producto.set(p);
    this.setValor('productoId', String(p.id));
  }

  protected quitarProducto(): void {
    this.producto.set(null);
    this.setValor('productoId', '');
  }

  protected cambiarRango(cual: 'desde' | 'hasta', valor: string): void {
    (cual === 'desde' ? this.desde : this.hasta).set(valor);
    this.cargar();
  }

  protected aplicarAtajo(dias: number): void {
    this.desde.set(iso(haceDias(dias)));
    this.hasta.set(iso(new Date()));
    this.cargar();
  }

  protected mesActual(): void {
    const hoy = new Date();
    this.desde.set(iso(new Date(hoy.getFullYear(), hoy.getMonth(), 1)));
    this.hasta.set(iso(hoy));
    this.cargar();
  }

  // --- Consulta ---

  /** Parámetros de la petición, o el motivo por el que todavía no se puede consultar. */
  private armar(d: ReporteDef): { params: ParamsReporte } | { falta: string } {
    const params: ParamsReporte = {};
    if (d.rango) {
      const desde = this.desde();
      const hasta = this.hasta();
      if (!desde || !hasta) return { falta: 'Elige las fechas «desde» y «hasta».' };
      if (desde > hasta) return { falta: 'La fecha «desde» no puede ser posterior a «hasta».' };
      const dias = (Date.parse(hasta) - Date.parse(desde)) / 86_400_000 + 1;
      if (dias > MAX_DIAS) return { falta: `El rango no puede pasar de ${MAX_DIAS} días.` };
      params['desde'] = desde;
      params['hasta'] = hasta;
    }
    const v = this.valores();
    for (const f of d.filtros) {
      const valor = v[f.key];
      if (f.obligatorio && (valor === undefined || valor === '')) return { falta: `Elige un ${f.label.toLowerCase()} para ver este reporte.` };
      if (valor !== undefined && valor !== '') params[f.key] = String(valor);
    }
    return { params };
  }

  protected cargar(): void {
    const d = this.def();
    if (!d) return;
    const id = ++this.peticion;
    const armado = this.armar(d);
    if ('falta' in armado) {
      this.datos.set(null);
      this.cargando.set(false);
      this.error.set(null);
      this.pendiente.set(armado.falta);
      return;
    }
    this.pendiente.set(null);
    this.cargando.set(true);
    this.error.set(null);
    this.service.consultar(this.familia()!.id, d.path, armado.params).subscribe({
      next: (r) => {
        if (id !== this.peticion) return; // respuesta obsoleta
        this.datos.set(r);
        this.cargando.set(false);
      },
      error: (e) => {
        if (id !== this.peticion) return;
        this.datos.set(null);
        this.cargando.set(false);
        this.error.set(errorMessage(e, 'No se pudo cargar el reporte'));
      },
    });
  }

  protected exportar(): void {
    const d = this.def();
    if (!d) return;
    const armado = this.armar(d);
    if ('falta' in armado) return;
    this.exportando.set(true);
    const sufijo = d.rango ? `-${armado.params['desde']}_${armado.params['hasta']}` : '';
    this.service.descargarCsv(this.familia()!.id, d.path, armado.params, `${this.familia()!.id}-${d.id}${sufijo}.csv`).subscribe({
      next: () => this.exportando.set(false),
      error: (e) => {
        this.exportando.set(false);
        this.error.set(errorMessage(e, 'No se pudo exportar el reporte'));
      },
    });
  }

  // --- Presentación ---

  protected derecha(c: Col): boolean {
    return c.tipo === 'dinero' || c.tipo === 'entero' || c.tipo === 'cantidad' || c.tipo === 'pct';
  }

  protected tono(v: unknown): BadgeTone {
    return ESTADO_TONOS[String(v)] ?? 'neutral';
  }

  protected texto(c: Col, v: unknown): string {
    if (v === null || v === undefined || v === '') return '—';
    switch (c.tipo) {
      case 'dinero': return formatNumber(Number(v), LOCALE, '1.2-2');
      case 'entero': return formatNumber(Number(v), LOCALE, '1.0-0');
      case 'cantidad': return formatNumber(Number(v), LOCALE, '1.0-4');
      case 'pct': return `${formatNumber(Number(v), LOCALE, '1.2-2')}%`;
      case 'fecha': return formatDate(String(v), 'dd/MM/yyyy', LOCALE);
      case 'fechahora': return formatDate(String(v), 'dd/MM/yyyy HH:mm', LOCALE);
      case 'si-no': return v ? 'Sí' : 'No';
      case 'estado': {
        const s = String(v).toLowerCase().replace(/_/g, ' ');
        return s.charAt(0).toUpperCase() + s.slice(1);
      }
      default: return String(v);
    }
  }

  protected clase(c: Col, v: unknown): string {
    const n = typeof v === 'number' ? v : Number.NaN;
    if (!c.resaltar || Number.isNaN(n)) return '';
    switch (c.resaltar) {
      case 'diferencia': return n < 0 ? 'font-semibold text-danger' : n > 0 ? 'font-semibold text-warning-dark' : '';
      case 'costo': return n > 0 ? 'font-semibold text-danger' : n < 0 ? 'font-semibold text-success' : '';
      case 'positivo-malo': return n > 0 ? 'font-semibold text-danger' : '';
    }
  }
}
