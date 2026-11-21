import { Component, inject, input, OnInit, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { errorMessage } from '../../../core/api/api-error';
import { Btn } from '../../../shared/ui/button';
import { Modal } from '../../../shared/ui/modal';
import { Proveedor } from '../../proveedores/data/proveedor.model';
import { ProveedoresService } from '../../proveedores/data/proveedores.service';
import { ParametrosReorden } from '../data/abastecimiento.model';
import { AbastecimientoService } from '../data/abastecimiento.service';

/**
 * Parámetros de reorden de un producto. Un campo vacío usa el valor global; el costo de almacenamiento y el nivel de
 * servicio se editan en porcentaje y viajan como fracción.
 */
@Component({
  selector: 'app-parametros-modal',
  imports: [FormsModule, Btn, Modal],
  template: `
    <app-modal
      [title]="'Parámetros de reorden · ' + producto()"
      description="Deje un campo vacío para usar el valor global. Los cambios se aplican en el próximo recálculo."
      (closed)="closed.emit()"
    >
      <div class="space-y-4 p-5">
        @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }
        @if (datos(); as d) {
          <label class="flex flex-col gap-2">
            <span class="field-label">Costo de emitir un pedido (S)</span>
            <input type="number" min="0" step="0.01" class="field-input" [(ngModel)]="costoPedido" [placeholder]="'Global: ' + d.costoPedidoEfectivo" />
            <span class="text-xs text-muted">Transporte, trámite y tiempo de quien hace el pedido, por cada pedido.</span>
          </label>
          <label class="flex flex-col gap-2">
            <span class="field-label">Costo anual de almacenamiento (% del costo de compra)</span>
            <input type="number" min="0.01" step="0.5" class="field-input" [(ngModel)]="costoAlmacenPct" [placeholder]="'Global: ' + porcentaje(d.costoAlmacenPctEfectivo)" />
            <span class="text-xs text-muted">H = este % × último costo. Incluye capital inmovilizado, espacio y mermas.</span>
          </label>
          <label class="flex flex-col gap-2">
            <span class="field-label">Nivel de servicio (%)</span>
            <input type="number" min="50" max="99.9" step="0.5" class="field-input" [(ngModel)]="nivelServicio" [placeholder]="'Global: ' + porcentaje(d.nivelServicioEfectivo)" />
            <span class="text-xs text-muted">Probabilidad de no quedarse sin stock mientras llega el pedido (50 a 99,9).</span>
          </label>
          <label class="flex flex-col gap-2">
            <span class="field-label">Proveedor</span>
            <select class="field-input" [(ngModel)]="proveedorId">
              <option [ngValue]="null">El del último pedido del producto</option>
              @for (p of proveedores(); track p.id) {
                <option [ngValue]="p.id">{{ p.nombreEmpresa }}</option>
              }
            </select>
          </label>
          <label class="flex items-center gap-2 text-sm text-ink">
            <input type="checkbox" [(ngModel)]="activo" /> Evaluar este producto en las sugerencias de reorden
          </label>
        } @else if (!error()) {
          <p class="text-sm text-muted">Cargando…</p>
        }
      </div>
      <div modal-footer>
        <button appBtn variant="secondary" (click)="closed.emit()">Cancelar</button>
        <button appBtn [disabled]="guardando() || !datos()" (click)="guardar()">{{ guardando() ? 'Guardando…' : 'Guardar' }}</button>
      </div>
    </app-modal>
  `,
})
export class ParametrosModal implements OnInit {
  private readonly service = inject(AbastecimientoService);
  private readonly proveedoresService = inject(ProveedoresService);

  readonly productoId = input.required<number>();
  readonly producto = input.required<string>();
  readonly closed = output<void>();
  readonly saved = output<ParametrosReorden>();

  protected readonly datos = signal<ParametrosReorden | null>(null);
  protected readonly proveedores = signal<Proveedor[]>([]);
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected costoPedido: number | null = null;
  /** En porcentaje (25 = 0.25). */
  protected costoAlmacenPct: number | null = null;
  /** En porcentaje (95 = 0.95). */
  protected nivelServicio: number | null = null;
  protected proveedorId: number | null = null;
  protected activo = true;

  ngOnInit(): void {
    this.service.parametros(this.productoId()).subscribe({
      next: (d) => {
        this.datos.set(d);
        this.costoPedido = d.costoPedido;
        this.costoAlmacenPct = d.costoAlmacenPct == null ? null : this.redondear(d.costoAlmacenPct * 100);
        this.nivelServicio = d.nivelServicio == null ? null : this.redondear(d.nivelServicio * 100);
        this.proveedorId = d.proveedorId;
        this.activo = d.activo;
      },
      error: (e) => this.error.set(errorMessage(e, 'No se pudieron cargar los parámetros')),
    });
    this.proveedoresService.list({ size: 100, estado: 'ACTIVO' }).subscribe({
      next: (p) => this.proveedores.set(p.content),
      error: () => this.proveedores.set([]),
    });
  }

  protected porcentaje(fraccion: number): string {
    return `${this.redondear(fraccion * 100)} %`;
  }

  protected guardar(): void {
    this.guardando.set(true);
    this.error.set(null);
    this.service
      .guardarParametros(this.productoId(), {
        costoPedido: this.vacio(this.costoPedido) ? null : Number(this.costoPedido),
        costoAlmacenPct: this.vacio(this.costoAlmacenPct) ? null : Number(this.costoAlmacenPct) / 100,
        nivelServicio: this.vacio(this.nivelServicio) ? null : Number(this.nivelServicio) / 100,
        proveedorId: this.proveedorId,
        activo: this.activo,
      })
      .subscribe({
        next: (d) => {
          this.guardando.set(false);
          this.saved.emit(d);
        },
        error: (e) => {
          this.guardando.set(false);
          this.error.set(errorMessage(e, 'No se pudieron guardar los parámetros'));
        },
      });
  }

  private vacio(v: number | null | string): boolean {
    return v === null || v === undefined || v === '';
  }

  private redondear(x: number): number {
    return Math.round(x * 100) / 100;
  }
}
