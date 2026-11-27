import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { errorMessage } from '../../../core/api/api-error';
import { Page } from '../../../core/api/page.model';
import { ToastService } from '../../../core/ui/toast';
import { Badge, BadgeTone } from '../../../shared/ui/badge';
import { Btn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { EmptyState } from '../../../shared/ui/empty-state';
import { FilterSelect } from '../../../shared/ui/filter-select';
import { tamanoPagina } from '../../../shared/ui/page-size';
import { Paginator } from '../../../shared/ui/paginator';
import { StatCard } from '../../../shared/ui/stat-card';
import { TableSkeleton } from '../../../shared/ui/table-skeleton';
import { Alerta, AlertaFiltro, TIPO_ALERTA_LABEL, TipoAlerta } from '../data/alerta.model';
import { AlertasService } from '../data/alertas.service';

const TONO: Record<TipoAlerta, BadgeTone> = { VENCIMIENTO: 'warning', VENCIDO: 'danger', STOCK_BAJO: 'info' };

/** Qué alertas se listan: las activas sin leer, todas las activas o también las ya resueltas. */
type Vista = 'pendientes' | 'activas' | 'todas';

@Component({
  selector: 'app-alertas-page',
  imports: [DatePipe, DecimalPipe, Badge, Btn, Card, EmptyState, FilterSelect, Paginator, StatCard, TableSkeleton],
  template: `
    <div class="space-y-6">
      <div class="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h2 class="text-lg font-semibold text-ink">Alertas del inventario</h2>
          <p class="mt-1 text-sm text-muted">
            El sistema revisa los lotes y el stock todos los días. Una alerta se resuelve sola cuando el lote se agota o el stock se repone.
          </p>
        </div>
        <div class="flex flex-wrap gap-2">
          <button appBtn variant="secondary" icon="activity" [disabled]="ocupado()" (click)="revisar()">Revisar ahora</button>
          <button appBtn icon="check-circle" [disabled]="ocupado() || (service.conteo()?.total ?? 0) === 0" (click)="leerTodas()">
            Marcar todas como leídas
          </button>
        </div>
      </div>

      @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }

      <div class="grid gap-4 sm:grid-cols-3">
        <app-stat-card label="Lotes por vencer" [value]="service.conteo()?.vencimiento ?? 0" hint="Sin leer" />
        <app-stat-card label="Lotes vencidos con stock" [value]="service.conteo()?.vencido ?? 0" hint="Sin leer" />
        <app-stat-card label="Productos bajo el mínimo" [value]="service.conteo()?.stockBajo ?? 0" hint="Sin leer" />
      </div>

      <div class="flex flex-col gap-3 sm:flex-row">
        <app-filter-select class="sm:w-56" icon="filter" label="Tipo de alerta" (changed)="cambiarTipo($event)">
          <option value="">Todos los tipos</option>
          <option value="VENCIMIENTO">Por vencer</option>
          <option value="VENCIDO">Vencidos</option>
          <option value="STOCK_BAJO">Stock bajo</option>
        </app-filter-select>
        <app-filter-select class="sm:w-56" icon="eye" label="Qué mostrar" (changed)="cambiarVista($event)">
          <option value="">Sin leer</option>
          <option value="activas">Activas (leídas y sin leer)</option>
          <option value="todas">Todas, incluidas las resueltas</option>
        </app-filter-select>
      </div>

      <app-card class="overflow-hidden">
        @if (pagina(); as p) {
          @if (p.content.length === 0) {
            <app-empty-state title="Sin alertas" message="No hay alertas para los filtros elegidos." />
          } @else {
            <div class="overflow-x-auto">
              <table class="dt">
                <thead>
                  <tr><th>Tipo</th><th>Producto</th><th>Detalle</th><th class="text-right">Stock</th><th>Fecha</th><th class="text-right">Acciones</th></tr>
                </thead>
                <tbody>
                  @for (a of p.content; track a.id) {
                    <tr [class.opacity-60]="a.leida || a.resuelta">
                      <td>
                        <app-badge [tone]="tono(a)">{{ etiqueta(a) }}</app-badge>
                        @if (a.resuelta) { <p class="mt-1 text-xs text-muted">Resuelta</p> }
                      </td>
                      <td>
                        <p class="text-sm font-semibold text-ink">{{ a.producto }}</p>
                        @if (a.numeroLote) { <p class="mt-0.5 font-mono text-xs text-muted">Lote {{ a.numeroLote }}</p> }
                      </td>
                      <td class="max-w-md text-sm text-ink">
                        {{ a.mensaje }}
                        @if (a.fechaReferencia) {
                          <p class="mt-0.5 text-xs text-muted">Vence: {{ a.fechaReferencia | date: 'dd/MM/yyyy' }}</p>
                        }
                      </td>
                      <td class="text-right text-sm">{{ a.cantidad | number: '1.0-4' }}</td>
                      <td class="text-sm text-muted">
                        {{ a.fecha | date: 'dd/MM/yyyy HH:mm' }}
                        @if (a.leida && a.leidaPor) { <p class="mt-0.5 text-xs">Leída por {{ a.leidaPor }}</p> }
                      </td>
                      <td class="text-right">
                        @if (!a.leida) {
                          <button appBtn variant="secondary" class="!h-8 !px-3 !text-xs" [disabled]="ocupado()" (click)="leer(a)">
                            Marcar leída
                          </button>
                        }
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
            @if (p.totalElements > 10) {
              <app-paginator [page]="p.page" [size]="p.size" [totalPages]="p.totalPages" [totalElements]="p.totalElements" (pageChange)="irA($event)" (sizeChange)="cambiarTamano($event)" />
            }
          }
        } @else if (!error()) {
          <app-table-skeleton />
        }
      </app-card>
    </div>
  `,
})
export class AlertasPage {
  protected readonly service = inject(AlertasService);
  private readonly toast = inject(ToastService);

  protected readonly pagina = signal<Page<Alerta> | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly ocupado = signal(false);

  private tipo: TipoAlerta | undefined;
  private vista: Vista = 'pendientes';
  private page = 0;
  private size = tamanoPagina(20);

  constructor() {
    this.cargar();
  }

  protected tono(a: Alerta): BadgeTone {
    return a.resuelta ? 'neutral' : TONO[a.tipo];
  }

  protected etiqueta(a: Alerta): string {
    return TIPO_ALERTA_LABEL[a.tipo];
  }

  protected cambiarTipo(valor: string): void {
    this.tipo = (valor || undefined) as TipoAlerta | undefined;
    this.page = 0;
    this.cargar();
  }

  protected cambiarVista(valor: string): void {
    this.vista = (valor || 'pendientes') as Vista;
    this.page = 0;
    this.cargar();
  }

  protected cambiarTamano(size: number): void {
    this.size = size;
    this.page = 0;
    this.cargar();
  }

  protected irA(page: number): void {
    this.page = page;
    this.cargar();
  }

  protected leer(a: Alerta): void {
    this.ocupado.set(true);
    this.service.marcarLeida(a.id).subscribe({
      next: () => this.recargarTodo(),
      error: (e) => this.fallo(e, 'No se pudo marcar la alerta como leída'),
    });
  }

  protected leerTodas(): void {
    this.ocupado.set(true);
    this.service.marcarTodasLeidas().subscribe({
      next: () => this.recargarTodo(),
      error: (e) => this.fallo(e, 'No se pudieron marcar las alertas como leídas'),
    });
  }

  protected revisar(): void {
    this.ocupado.set(true);
    this.service.generar().subscribe({
      next: (r) => {
        const nuevas = r.vencimiento + r.vencido + r.stockBajo;
        this.toast.success(
          nuevas === 0
            ? `Revisión completa: no hay alertas nuevas${r.resueltas ? ` (${r.resueltas} resueltas)` : ''}.`
            : `Revisión completa: ${nuevas} alertas nuevas${r.resueltas ? ` y ${r.resueltas} resueltas` : ''}.`,
        );
        this.recargarTodo();
      },
      error: (e) => this.fallo(e, 'No se pudo revisar el inventario'),
    });
  }

  private recargarTodo(): void {
    this.ocupado.set(false);
    this.cargar();
    this.service.refrescarConteo().subscribe({ error: () => undefined });
  }

  private fallo(e: unknown, mensaje: string): void {
    this.ocupado.set(false);
    this.error.set(errorMessage(e, mensaje));
  }

  private cargar(): void {
    const filtro: AlertaFiltro = {
      tipo: this.tipo,
      leida: this.vista === 'pendientes' ? false : undefined,
      incluirResueltas: this.vista === 'todas' ? true : undefined,
      page: this.page,
      size: this.size,
    };
    this.error.set(null);
    this.service.listar(filtro).subscribe({
      next: (p) => this.pagina.set(p),
      error: (e) => this.error.set(errorMessage(e, 'No se pudieron cargar las alertas')),
    });
  }
}
