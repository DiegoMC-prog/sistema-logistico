import { Component, inject, OnInit, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { errorMessage } from '../../../core/api/api-error';
import { Btn } from '../../../shared/ui/button';
import { Modal } from '../../../shared/ui/modal';
import { Categoria } from '../../categorias/data/categoria.model';
import { CategoriasService } from '../../categorias/data/categorias.service';
import { Producto } from '../../productos/data/producto.model';
import { ProductosService } from '../../productos/data/productos.service';
import { AlcanceRecargo, Recargo } from '../data/recargo.model';
import { RecargosService } from '../data/recargos.service';

/** Activa un recargo por escasez. El porcentaje se escribe en % y viaja como fracción. */
@Component({
  selector: 'app-recargo-form-modal',
  imports: [FormsModule, Btn, Modal],
  template: `
    <app-modal
      title="Activar recargo por escasez"
      description="Se suma al precio que corresponda (general, por tipo de cliente o por cantidad). El de un producto gana sobre el de su categoría."
      (closed)="closed.emit()"
    >
      <div class="space-y-4 p-5">
        @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }

        <fieldset class="flex flex-col gap-2">
          <legend class="field-label mb-2">Aplicar a</legend>
          <div class="flex gap-4 text-sm text-ink">
            <label class="flex items-center gap-2">
              <input type="radio" name="alcance" value="PRODUCTO" [ngModel]="alcance()" (ngModelChange)="alcance.set($event)" /> Un producto
            </label>
            <label class="flex items-center gap-2">
              <input type="radio" name="alcance" value="CATEGORIA" [ngModel]="alcance()" (ngModelChange)="alcance.set($event)" /> Una categoría completa
            </label>
          </div>
        </fieldset>

        @if (alcance() === 'PRODUCTO') {
          <label class="flex flex-col gap-2">
            <span class="field-label">Buscar producto <span class="text-danger">*</span></span>
            <input class="field-input" placeholder="Escriba al menos 2 letras" [ngModel]="busqueda" (ngModelChange)="buscarProducto($event)" />
          </label>
          @if (productos().length > 0) {
            <select class="field-input" size="5" [(ngModel)]="productoId" aria-label="Producto">
              @for (p of productos(); track p.id) {
                <option [ngValue]="p.id">{{ p.nombre }} · {{ p.categoria.nombre }}</option>
              }
            </select>
          } @else if (busqueda.trim().length >= 2) {
            <p class="text-xs text-muted">No se encontraron productos activos.</p>
          }
        } @else {
          <label class="flex flex-col gap-2">
            <span class="field-label">Categoría <span class="text-danger">*</span></span>
            <select class="field-input" [(ngModel)]="categoriaId">
              <option [ngValue]="null">Elija una categoría</option>
              @for (c of categorias(); track c.id) {
                <option [ngValue]="c.id">{{ c.nombre }}</option>
              }
            </select>
          </label>
        }

        <label class="flex flex-col gap-2">
          <span class="field-label">Recargo (%) <span class="text-danger">*</span></span>
          <input type="number" min="0.1" max="100" step="0.5" class="field-input" [(ngModel)]="porcentaje" placeholder="Por ejemplo 10" />
          <span class="text-xs text-muted">Un precio de 2,00 con 10 % se cobra 2,20. Se redondea a centavos.</span>
        </label>

        <label class="flex flex-col gap-2">
          <span class="field-label">Motivo <span class="text-danger">*</span></span>
          <textarea class="field-input min-h-20 py-2" maxlength="500" [(ngModel)]="motivo" placeholder="Por ejemplo: desabastecimiento del proveedor por temporada"></textarea>
          <span class="text-xs text-muted">Queda registrado junto con su usuario y la fecha.</span>
        </label>
      </div>
      <div modal-footer>
        <button appBtn variant="secondary" (click)="closed.emit()">Cancelar</button>
        <button appBtn icon="alert" [disabled]="guardando()" (click)="guardar()">{{ guardando() ? 'Activando…' : 'Activar recargo' }}</button>
      </div>
    </app-modal>
  `,
})
export class RecargoFormModal implements OnInit {
  private readonly service = inject(RecargosService);
  private readonly productosService = inject(ProductosService);
  private readonly categoriasService = inject(CategoriasService);

  readonly closed = output<void>();
  readonly saved = output<Recargo>();

  protected readonly alcance = signal<AlcanceRecargo>('PRODUCTO');
  protected readonly productos = signal<Producto[]>([]);
  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected busqueda = '';
  protected productoId: number | null = null;
  protected categoriaId: number | null = null;
  protected porcentaje: number | null = null;
  protected motivo = '';
  private busquedaTimer: ReturnType<typeof setTimeout> | undefined;

  ngOnInit(): void {
    this.categoriasService.list({ size: 100 }).subscribe({
      next: (p) => this.categorias.set(p.content),
      error: () => this.categorias.set([]),
    });
  }

  protected buscarProducto(texto: string): void {
    this.busqueda = texto;
    clearTimeout(this.busquedaTimer);
    if (texto.trim().length < 2) {
      this.productos.set([]);
      return;
    }
    this.busquedaTimer = setTimeout(() => {
      this.productosService.list({ search: texto.trim(), estado: 'ACTIVO', size: 20 }).subscribe({
        next: (p) => {
          this.productos.set(p.content);
          if (p.content.length === 1) this.productoId = p.content[0].id;
        },
        error: () => this.productos.set([]),
      });
    }, 300);
  }

  protected guardar(): void {
    const alcance = this.alcance();
    if (alcance === 'PRODUCTO' && this.productoId == null) {
      this.error.set('Elija el producto');
      return;
    }
    if (alcance === 'CATEGORIA' && this.categoriaId == null) {
      this.error.set('Elija la categoría');
      return;
    }
    const pct = Number(this.porcentaje);
    if (!pct || pct <= 0 || pct > 100) {
      this.error.set('El recargo debe estar entre 0,1 % y 100 %');
      return;
    }
    if (!this.motivo.trim()) {
      this.error.set('Indique el motivo del recargo');
      return;
    }
    this.guardando.set(true);
    this.error.set(null);
    this.service
      .crear({
        alcance,
        productoId: alcance === 'PRODUCTO' ? this.productoId! : undefined,
        categoriaId: alcance === 'CATEGORIA' ? this.categoriaId! : undefined,
        porcentaje: Math.round(pct * 100) / 10000,
        motivo: this.motivo.trim(),
      })
      .subscribe({
        next: (r) => {
          this.guardando.set(false);
          this.saved.emit(r);
        },
        error: (e) => {
          this.guardando.set(false);
          this.error.set(errorMessage(e, 'No se pudo activar el recargo'));
        },
      });
  }
}
