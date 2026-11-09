import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { errorMessage } from '../../../core/api/api-error';
import { AuthService } from '../../../core/auth/auth.service';
import { PERMISOS, PERMISOS_CAJA } from '../../../core/auth/permisos';
import { NAV_ITEMS } from '../../../core/layout/nav';
import { Btn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { EmptyState } from '../../../shared/ui/empty-state';
import { Icon } from '../../../shared/ui/icon';
import { VentasChart } from '../components/ventas-chart';
import { Panel } from '../data/panel.model';
import { PanelService } from '../data/panel.service';

/** Cada cuánto se refresca el panel: cada llamada consulta varios reportes, así que sin exagerar. */
const REFRESCO_MS = 5 * 60_000;

@Component({
  selector: 'app-inicio-page',
  imports: [DatePipe, DecimalPipe, RouterLink, Btn, Card, EmptyState, Icon, VentasChart],
  template: `
    <div class="space-y-6">
      <div class="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
        <div>
          <h2 class="text-lg font-semibold text-ink">Hola, {{ auth.perfil()?.nombre }}</h2>
          <p class="mt-1 text-sm text-muted">
            @if (panel(); as p) { Actualizado a las {{ p.generadoEn | date: 'HH:mm' }}. } @else { Resumen de tu operación. }
          </p>
        </div>
        <button appBtn variant="secondary" icon="clock" [disabled]="cargando()" (click)="cargar()">
          {{ cargando() ? 'Actualizando…' : 'Actualizar' }}
        </button>
      </div>

      @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }

      @if (panel(); as p) {
        @if (p.ventas; as v) {
          <section class="grid gap-4 lg:grid-cols-3" aria-label="Ventas">
            <app-card class="block p-6">
              <p class="text-sm font-medium text-muted">Ventas de hoy</p>
              <!-- Cifra principal: una sola por vista, en proporcional (tabular solo en columnas) -->
              <p class="mt-2 text-5xl font-bold tracking-tight text-ink">{{ v.hoy.total | number: '1.2-2' }}</p>
              <p class="mt-2 text-sm font-semibold" [class]="deltaClase(v.variacionTotal)">
                @if (v.variacionTotal === null) {
                  <span class="font-normal text-muted">Ayer no hubo ventas para comparar</span>
                } @else {
                  <span aria-hidden="true">{{ v.variacionTotal >= 0 ? '▲' : '▼' }}</span>
                  {{ v.variacionTotal > 0 ? '+' : '' }}{{ v.variacionTotal | number: '1.0-2' }}%
                  <span class="font-normal text-muted">vs ayer ({{ v.ayer.total | number: '1.2-2' }})</span>
                }
              </p>
              <dl class="mt-5 grid grid-cols-3 gap-3 border-t border-border pt-4 text-sm">
                <div><dt class="text-muted">Ventas</dt><dd class="text-lg font-semibold text-ink">{{ v.hoy.ventas }}</dd></div>
                <div><dt class="text-muted">Ticket prom.</dt><dd class="text-lg font-semibold text-ink">{{ v.hoy.ticketPromedio | number: '1.2-2' }}</dd></div>
                <div><dt class="text-muted">Anuladas</dt><dd class="text-lg font-semibold" [class]="v.hoy.anuladas > 0 ? 'text-danger' : 'text-ink'">{{ v.hoy.anuladas }}</dd></div>
              </dl>
            </app-card>

            <app-card class="block p-6 lg:col-span-2">
              <div class="mb-3 flex items-center justify-between">
                <h3 class="text-sm font-semibold text-ink">Ventas de los últimos 7 días</h3>
                <a routerLink="/reportes/ventas/resumen" class="text-xs font-medium text-accent hover:underline">Ver reporte</a>
              </div>
              <app-ventas-chart [datos]="v.ultimos7Dias" />
            </app-card>
          </section>
        }

        @if (p.utilidad; as u) {
          <section class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4" aria-label="Utilidad">
            <app-card class="block p-4"><p class="text-xs font-medium text-muted">Utilidad de hoy</p><p class="mt-1 text-xl font-bold text-ink">{{ u.utilidadHoy | number: '1.2-2' }}</p></app-card>
            <app-card class="block p-4"><p class="text-xs font-medium text-muted">Margen de hoy</p><p class="mt-1 text-xl font-bold text-ink">{{ u.margenHoy === null ? '—' : (u.margenHoy | number: '1.2-2') + '%' }}</p></app-card>
            <app-card class="block p-4"><p class="text-xs font-medium text-muted">Utilidad 7 días</p><p class="mt-1 text-xl font-bold text-ink">{{ u.utilidad7Dias | number: '1.2-2' }}</p></app-card>
            <app-card class="block p-4"><p class="text-xs font-medium text-muted">Margen 7 días</p><p class="mt-1 text-xl font-bold text-ink">{{ u.margen7Dias === null ? '—' : (u.margen7Dias | number: '1.2-2') + '%' }}</p></app-card>
          </section>
        }

        <div class="grid gap-4 xl:grid-cols-2">
          @if (p.caja; as c) {
            <app-card class="block p-6">
              <div class="mb-3 flex items-center justify-between">
                <h3 class="text-sm font-semibold text-ink">Turnos abiertos ahora <span class="ml-1 rounded-full bg-success-soft px-2 py-0.5 text-xs text-success-dark">{{ c.turnosAbiertos }}</span></h3>
                <a routerLink="/reportes/caja/cierres" class="text-xs font-medium text-accent hover:underline">Ver reporte</a>
              </div>
              @if (c.turnos.length === 0) {
                <p class="text-sm text-muted">No hay turnos abiertos.</p>
              } @else {
                <div class="overflow-x-auto">
                  <table class="dt">
                    <thead><tr><th>Caja</th><th>Cajero</th><th>Desde</th><th class="text-right">Ventas</th><th class="text-right">Esperado</th></tr></thead>
                    <tbody>
                      @for (t of c.turnos; track t.turnoId) {
                        <tr>
                          <td class="text-sm font-semibold text-ink">
                            @if (verTurnos) { <a [routerLink]="['/turnos', t.turnoId]" class="hover:underline">{{ t.caja }}</a> } @else { {{ t.caja }} }
                          </td>
                          <td class="text-sm text-muted">{{ t.cajero }}</td>
                          <td class="text-sm text-muted">{{ t.fechaApertura | date: 'HH:mm' }}</td>
                          <td class="text-right text-sm tabular-nums">{{ t.totalVentas | number: '1.2-2' }} <span class="text-xs text-muted">({{ t.ventas }})</span></td>
                          <td class="text-right text-sm font-semibold tabular-nums">{{ t.efectivoEsperado | number: '1.2-2' }}</td>
                        </tr>
                      }
                    </tbody>
                  </table>
                </div>
              }
            </app-card>
          }

          @if (p.inventario; as i) {
            <app-card class="block p-6">
              <div class="mb-3 flex items-center justify-between">
                <h3 class="text-sm font-semibold text-ink">Inventario</h3>
                <a routerLink="/reportes/inventario/stock-bajo" class="text-xs font-medium text-accent hover:underline">Ver reporte</a>
              </div>
              <dl class="grid grid-cols-2 gap-3 text-sm sm:grid-cols-4">
                <div><dt class="text-muted">Bajo mínimo</dt><dd class="text-lg font-semibold" [class]="i.productosBajoMinimo > 0 ? 'text-warning-dark' : 'text-ink'">{{ i.productosBajoMinimo }}</dd></div>
                <div><dt class="text-muted">Sin stock</dt><dd class="text-lg font-semibold" [class]="i.sinStock > 0 ? 'text-danger' : 'text-ink'">{{ i.sinStock }}</dd></div>
                <div><dt class="text-muted">Vencen en 7 días</dt><dd class="text-lg font-semibold" [class]="i.lotesPorVencer7Dias > 0 ? 'text-warning-dark' : 'text-ink'">{{ i.lotesPorVencer7Dias }}</dd></div>
                <div><dt class="text-muted">Vencidos</dt><dd class="text-lg font-semibold" [class]="i.lotesVencidos > 0 ? 'text-danger' : 'text-ink'">{{ i.lotesVencidos }}</dd></div>
              </dl>
              <p class="mt-3 text-xs text-muted">
                Valor en riesgo (por vencer y vencido): <strong class="text-ink">{{ i.valorEnRiesgo | number: '1.2-2' }}</strong>
                · <a routerLink="/reportes/inventario/lotes-por-vencer" class="text-accent hover:underline">ver lotes</a>
              </p>
              @if (i.criticos.length > 0) {
                <p class="mb-2 mt-5 text-xs font-semibold uppercase tracking-wide text-muted">Productos más críticos</p>
                <ul class="space-y-3">
                  @for (c of i.criticos; track c.productoId) {
                    <li>
                      <div class="flex items-baseline justify-between gap-3 text-sm">
                        <span class="truncate font-medium text-ink">{{ c.producto }}</span>
                        <span class="shrink-0 text-xs text-muted">{{ c.stock | number: '1.0-4' }} / {{ c.stockMinimo | number: '1.0-4' }} · faltan {{ c.faltante | number: '1.0-4' }}</span>
                      </div>
                      <!-- El relleno lleva la severidad; la pista es un tono más claro -->
                      <div class="mt-1 h-2 overflow-hidden rounded-full" [class]="c.stock <= 0 ? 'bg-danger-soft' : 'bg-warning-soft'">
                        <div class="h-full rounded-full" [class]="c.stock <= 0 ? 'bg-danger' : 'bg-warning'" [style.width.%]="porcentajeStock(c.stock, c.stockMinimo)"></div>
                      </div>
                    </li>
                  }
                </ul>
              }
            </app-card>
          }

          @if (p.compras; as c) {
            <app-card class="block p-6 xl:col-span-2">
              <div class="mb-3 flex items-center justify-between">
                <h3 class="text-sm font-semibold text-ink">Compras pendientes de recibir</h3>
                <a routerLink="/reportes/compras/pendientes" class="text-xs font-medium text-accent hover:underline">Ver reporte</a>
              </div>
              <dl class="grid grid-cols-3 gap-3 text-sm sm:max-w-xl">
                <div><dt class="text-muted">Pedidos</dt><dd class="text-lg font-semibold text-ink">{{ c.pedidosPendientes }}</dd></div>
                <div><dt class="text-muted">Atrasados</dt><dd class="text-lg font-semibold" [class]="c.atrasados > 0 ? 'text-danger' : 'text-ink'">{{ c.atrasados }}</dd></div>
                <div><dt class="text-muted">Monto pendiente</dt><dd class="text-lg font-semibold text-ink">{{ c.montoPendiente | number: '1.2-2' }}</dd></div>
              </dl>
              @if (c.masAtrasados.length > 0) {
                <div class="mt-4 overflow-x-auto">
                  <table class="dt">
                    <thead><tr><th>Pedido</th><th>Proveedor</th><th class="text-right">Días de atraso</th><th class="text-right">Monto pendiente</th></tr></thead>
                    <tbody>
                      @for (o of c.masAtrasados; track o.pedidoId) {
                        <tr>
                          <td class="font-mono text-xs font-semibold">
                            @if (verPedidos) { <a [routerLink]="['/pedidos', o.pedidoId]" class="text-accent hover:underline">{{ o.numeroPedido }}</a> } @else { {{ o.numeroPedido }} }
                          </td>
                          <td class="text-sm text-ink">{{ o.proveedor }}</td>
                          <td class="text-right text-sm font-semibold text-danger">{{ o.diasAtraso }}</td>
                          <td class="text-right text-sm tabular-nums">{{ o.montoPendiente | number: '1.2-2' }}</td>
                        </tr>
                      }
                    </tbody>
                  </table>
                </div>
              }
            </app-card>
          }
        </div>

        @if (p.bloques.length === 0) {
          <app-card>
            <app-empty-state title="Sin reportes habilitados" message="Tu rol no incluye reportes. Usa los accesos rápidos para ir a tus tareas." />
          </app-card>
        }
      } @else if (cargando() && !error()) {
        <p class="text-sm text-muted">Cargando panel…</p>
      }

      @if (accesos().length > 0) {
        <section aria-label="Accesos rápidos">
          <h3 class="mb-3 text-sm font-semibold text-ink">Accesos rápidos</h3>
          <div class="flex flex-wrap gap-3">
            @for (a of accesos(); track a.path) {
              <a [routerLink]="a.path" class="inline-flex h-11 items-center gap-2 rounded-lg border border-border bg-surface px-4 text-sm font-medium text-ink transition hover:bg-subtle">
                <app-icon [name]="a.icon" class="size-4 text-muted" />{{ a.label }}
              </a>
            }
          </div>
        </section>
      }
    </div>
  `,
})
export class InicioPage {
  protected readonly auth = inject(AuthService);
  private readonly service = inject(PanelService);

  protected readonly panel = signal<Panel | null>(null);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly verTurnos = this.auth.hasAnyPermission(PERMISOS_CAJA);
  protected readonly verPedidos = this.auth.hasPermission(PERMISOS.pedidoVer);

  /** Atajos a las secciones permitidas para el usuario (el cajero, sin reportes, llega directo a lo suyo). */
  protected readonly accesos = computed(() => NAV_ITEMS.filter((i) => this.auth.hasAnyPermission(i.permiso)).slice(0, 8));

  constructor() {
    this.cargar();
    const timer = setInterval(() => {
      if (!document.hidden) this.cargar(true);
    }, REFRESCO_MS);
    inject(DestroyRef).onDestroy(() => clearInterval(timer));
  }

  protected cargar(silencioso = false): void {
    if (this.cargando()) return;
    this.cargando.set(true);
    if (!silencioso) this.error.set(null);
    this.service.get().subscribe({
      next: (p) => {
        this.panel.set(p);
        this.error.set(null);
        this.cargando.set(false);
      },
      error: (e) => {
        this.cargando.set(false);
        // En un refresco automático se conservan los datos que ya se veían.
        if (!silencioso || !this.panel()) this.error.set(errorMessage(e, 'No se pudo cargar el panel'));
      },
    });
  }

  protected deltaClase(v: number | null): string {
    return v === null ? '' : v > 0 ? 'text-success' : v < 0 ? 'text-danger' : 'text-muted';
  }

  protected porcentajeStock(stock: number, minimo: number): number {
    return minimo > 0 ? Math.max(2, Math.min(100, (stock / minimo) * 100)) : 0;
  }
}
