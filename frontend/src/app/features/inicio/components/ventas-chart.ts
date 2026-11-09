import { DecimalPipe, formatDate, formatNumber } from '@angular/common';
import { Component, computed, input, signal } from '@angular/core';

export interface PuntoVentas {
  fecha: string;
  ventas: number;
  total: number;
}

const LOCALE = 'en-US';
const W = 640;
const H = 240;
const M = { l: 56, r: 12, t: 20, b: 32 };
const GROSOR_MAX = 24; // las columnas no pasan de 24 px: el resto del espacio es aire
const RADIO = 4;

/** Paso «redondo» (1, 2 o 5 × 10ⁿ) para que los ticks del eje queden en números limpios. */
function pasoLimpio(max: number, ticks: number): number {
  if (max <= 0) return 1;
  const crudo = max / ticks;
  const mag = 10 ** Math.floor(Math.log10(crudo));
  const n = crudo / mag;
  return (n <= 1 ? 1 : n <= 2 ? 2 : n <= 5 ? 5 : 10) * mag;
}

/**
 * Columnas del total vendido por día. Una sola serie: sin leyenda (el título dice qué se grafica),
 * con tooltip al pasar el cursor o enfocar con teclado, y una vista de tabla equivalente.
 */
@Component({
  selector: 'app-ventas-chart',
  imports: [DecimalPipe],
  template: `
    <div class="relative">
      <svg [attr.viewBox]="'0 0 ' + W + ' ' + H" class="block h-auto w-full" role="img" [attr.aria-label]="resumen()">
        <!-- Rejilla y eje Y: hairline, sólida y recesiva -->
        @for (t of ticks(); track t.valor) {
          <line [attr.x1]="M.l" [attr.x2]="W - M.r" [attr.y1]="t.y" [attr.y2]="t.y" class="grid" />
          <text [attr.x]="M.l - 8" [attr.y]="t.y + 4" text-anchor="end" class="axis">{{ t.etiqueta }}</text>
        }

        @for (c of columnas(); track c.fecha; let i = $index) {
          <!-- Franja de la columna activa -->
          @if (activo() === i) {
            <rect [attr.x]="c.slotX" [attr.y]="M.t" [attr.width]="c.slotW" [attr.height]="H - M.t - M.b" class="franja" />
          }
          @if (c.alto > 0) {
            <path [attr.d]="c.path" class="barra" />
          }
          <text [attr.x]="c.x + c.ancho / 2" [attr.y]="H - 10" text-anchor="middle" class="axis">{{ c.etiqueta }}</text>
        }

        <!-- Valor de hoy: etiqueta selectiva en la punta de la última columna -->
        @if (hoy(); as h) {
          @if (h.alto > 0) {
            <text [attr.x]="h.x + h.ancho / 2" [attr.y]="h.y - 6" text-anchor="middle" class="valor">{{ h.total | number: '1.0-0' }}</text>
          }
        }

        <!-- Línea base -->
        <line [attr.x1]="M.l" [attr.x2]="W - M.r" [attr.y1]="H - M.b" [attr.y2]="H - M.b" class="base" />

        <!-- Zonas de interacción: más grandes que las marcas -->
        @for (c of columnas(); track c.fecha; let i = $index) {
          <rect [attr.x]="c.slotX" [attr.y]="M.t" [attr.width]="c.slotW" [attr.height]="H - M.t - M.b" fill="transparent" tabindex="0"
            [attr.aria-label]="c.descripcion" class="hit"
            (mouseenter)="activo.set(i)" (mouseleave)="activo.set(null)" (focus)="activo.set(i)" (blur)="activo.set(null)" />
        }
      </svg>

      @if (tip(); as t) {
        <div class="tip pointer-events-none absolute z-10 rounded-lg border border-border bg-surface px-3 py-2 text-xs shadow-lg"
          [style.left.%]="t.left" [style.top.%]="t.top" role="status">
          <p class="font-semibold text-ink">{{ t.fecha }}</p>
          <p class="mt-0.5 text-muted">{{ t.ventas }} {{ t.ventas === 1 ? 'venta' : 'ventas' }}</p>
          <p class="text-ink"><span class="swatch"></span>Total {{ t.total | number: '1.2-2' }}</p>
        </div>
      }
    </div>

    <button type="button" class="mt-3 text-xs font-medium text-accent hover:underline" [attr.aria-expanded]="tabla()" (click)="tabla.set(!tabla())">
      {{ tabla() ? 'Ocultar tabla' : 'Ver como tabla' }}
    </button>
    @if (tabla()) {
      <table class="dt mt-2">
        <thead><tr><th>Día</th><th class="text-right">Ventas</th><th class="text-right">Total</th></tr></thead>
        <tbody>
          @for (p of datos(); track p.fecha) {
            <tr>
              <td class="text-sm">{{ fechaLarga(p.fecha) }}</td>
              <td class="text-right text-sm">{{ p.ventas }}</td>
              <td class="text-right text-sm tabular-nums">{{ p.total | number: '1.2-2' }}</td>
            </tr>
          }
        </tbody>
      </table>
    }
  `,
  styles: `
    :host {
      display: block;
      --series-1: #2a78d6;
      --viz-surface: #fcfcfb;
    }
    :host-context(html.dark) {
      --series-1: #3987e5;
      --viz-surface: #1a1a19;
    }
    .barra { fill: var(--series-1); }
    .grid { stroke: var(--color-border); stroke-width: 1; }
    .base { stroke: var(--color-faint); stroke-width: 1; }
    .axis { fill: var(--color-muted); font-size: 11px; }
    .valor { fill: var(--color-ink); font-size: 12px; font-weight: 600; }
    .franja { fill: var(--color-subtle); }
    .hit { outline: none; }
    .hit:focus-visible { stroke: var(--color-accent); stroke-width: 2; }
    .swatch { display: inline-block; width: 8px; height: 8px; margin-right: 6px; border-radius: 2px; background: var(--series-1); }
    .tip { transform: translate(-50%, calc(-100% - 8px)); min-width: 9rem; }
  `,
})
export class VentasChart {
  protected readonly W = W;
  protected readonly H = H;
  protected readonly M = M;

  readonly datos = input.required<PuntoVentas[]>();

  protected readonly activo = signal<number | null>(null);
  protected readonly tabla = signal(false);

  private readonly escala = computed(() => {
    const max = Math.max(0, ...this.datos().map((d) => d.total));
    const paso = pasoLimpio(max, 4);
    const tope = Math.max(paso, Math.ceil(max / paso) * paso);
    return { paso, tope };
  });

  protected readonly ticks = computed(() => {
    const { paso, tope } = this.escala();
    const alto = H - M.t - M.b;
    const out: { valor: number; y: number; etiqueta: string }[] = [];
    for (let v = 0; v <= tope + 1e-9; v += paso) {
      out.push({ valor: v, y: M.t + alto - (v / tope) * alto, etiqueta: v >= 10_000 ? `${formatNumber(v / 1000, LOCALE, '1.0-1')}K` : formatNumber(v, LOCALE, '1.0-2') });
    }
    return out;
  });

  protected readonly columnas = computed(() => {
    const d = this.datos();
    const { tope } = this.escala();
    const ancho = W - M.l - M.r;
    const alto = H - M.t - M.b;
    const slot = ancho / Math.max(d.length, 1);
    const grosor = Math.min(GROSOR_MAX, slot * 0.5);
    const base = H - M.b;
    return d.map((p, i) => {
      const h = (p.total / tope) * alto;
      const x = M.l + slot * i + (slot - grosor) / 2;
      const y = base - h;
      const r = Math.min(RADIO, h / 2);
      // Redondeo de 4 px solo en la punta; el extremo en la línea base queda recto.
      const path = h > 0 ? `M${x},${base} V${y + r} a${r},${r} 0 0 1 ${r},${-r} H${x + grosor - r} a${r},${r} 0 0 1 ${r},${r} V${base} Z` : '';
      return {
        fecha: p.fecha, total: p.total, x, y, ancho: grosor, alto: h, path, slotX: M.l + slot * i, slotW: slot,
        etiqueta: formatDate(p.fecha, 'dd/MM', LOCALE),
        descripcion: `${formatDate(p.fecha, 'EEEE dd/MM', LOCALE)}: ${p.ventas} ventas, total ${formatNumber(p.total, LOCALE, '1.2-2')}`,
      };
    });
  });

  protected readonly hoy = computed(() => this.columnas().at(-1) ?? null);

  protected readonly tip = computed(() => {
    const i = this.activo();
    if (i === null) return null;
    const c = this.columnas()[i];
    const p = this.datos()[i];
    if (!c || !p) return null;
    // La burbuja se ancla sobre la columna, sin salirse por los bordes.
    const left = Math.min(88, Math.max(12, ((c.x + c.ancho / 2) / W) * 100));
    return { left, top: (Math.min(c.y, H - M.b - 8) / H) * 100, fecha: formatDate(p.fecha, 'EEE dd/MM', LOCALE), ventas: p.ventas, total: p.total };
  });

  protected readonly resumen = computed(() => {
    const d = this.datos();
    const total = d.reduce((s, p) => s + p.total, 0);
    return `Ventas de los últimos ${d.length} días. Total acumulado ${formatNumber(total, LOCALE, '1.2-2')}.`;
  });

  protected fechaLarga(f: string): string {
    return formatDate(f, 'EEE dd/MM/yyyy', LOCALE);
  }
}
