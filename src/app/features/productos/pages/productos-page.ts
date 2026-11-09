import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { errorMessage } from '../../../core/api/api-error';
import { EstadoCounts, estadoCounts, isActivo } from '../../../core/api/estado';
import { PagedList } from '../../../core/api/paged-list';
import { AuthService } from '../../../core/auth/auth.service';
import { MODULOS } from '../../../core/auth/permisos';
import { CategoriasService } from '../../categorias/data/categorias.service';
import { Categoria } from '../../categorias/data/categoria.model';
import { Badge } from '../../../shared/ui/badge';
import { Btn, IconBtn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { ConfirmDialog } from '../../../shared/ui/confirm-dialog';
import { EmptyState } from '../../../shared/ui/empty-state';
import { Field } from '../../../shared/ui/field';
import { FilterSelect } from '../../../shared/ui/filter-select';
import { Icon } from '../../../shared/ui/icon';
import { Modal } from '../../../shared/ui/modal';
import { Paginator } from '../../../shared/ui/paginator';
import { SearchInput } from '../../../shared/ui/search-input';
import { TableSkeleton } from '../../../shared/ui/table-skeleton';
import { StatCard } from '../../../shared/ui/stat-card';
import { Toggle } from '../../../shared/ui/toggle';
import { PresentacionesPanel } from '../components/presentaciones-panel';
import { PresentacionesService } from '../data/presentaciones.service';
import { Producto, ProductoFilters } from '../data/producto.model';
import { ProductosService } from '../data/productos.service';

@Component({
  selector: 'app-productos-page',
  imports: [ReactiveFormsModule, TableSkeleton, FilterSelect, Badge, Btn, IconBtn, Card, ConfirmDialog, EmptyState, Field, Icon, Modal, Paginator, PresentacionesPanel, SearchInput, StatCard, Toggle],
  template: `
    <div class="space-y-6">
      <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h2 class="text-lg font-semibold text-ink">Gestión de productos</h2>
          <p class="mt-1 text-sm text-muted">Administra identificación, clasificación y disponibilidad del catálogo.</p>
        </div>
        @if (puede.crear) {
          <button appBtn icon="plus" (click)="openForm(null)">Nuevo producto</button>
        }
      </div>

      @if (actionError()) {
        <p class="alert-error" role="alert">{{ actionError() }}</p>
      }

      <div class="grid gap-4 sm:grid-cols-3">
        <app-stat-card label="Total de productos" [value]="counts()?.total" />
        <app-stat-card label="Activos" tone="success" [value]="counts()?.activos" />
        <app-stat-card label="Inactivos" tone="muted" [value]="counts()?.inactivos" />
      </div>

      <app-card class="overflow-hidden">
        <div class="flex flex-col gap-3 border-b border-border p-4 lg:flex-row lg:items-center">
          <app-search-input placeholder="Buscar por nombre…" (searched)="list.setFilters({ search: $event })" />
          <label class="relative block sm:w-filter">
            <app-icon name="barcode" class="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-faint" />
            <input
              class="field-input !h-10 pl-10"
              placeholder="Código de barras ↵"
              aria-label="Buscar por código de barras"
              (keydown.enter)="searchBarcode($any($event.target).value); $any($event.target).select()"
            />
          </label>
          <div class="flex flex-wrap gap-3 lg:ml-auto">
            <app-filter-select class="sm:w-filter" icon="tag" label="Filtrar por categoría"
              (changed)="list.setFilters({ categoriaId: toNumber($event) })">
              <option value="">Todas las categorías</option>
              @for (c of categorias(); track c.id) {
                <option [value]="c.id">{{ c.nombre }}</option>
              }
            </app-filter-select>
            <app-filter-select class="sm:w-status" icon="check-circle" label="Filtrar por estado"
              (changed)="list.setFilters({ estado: $any($event) || undefined })">
              <option value="">Cualquier estado</option>
              <option value="ACTIVO">Activos</option>
              <option value="INACTIVO">Inactivos</option>
            </app-filter-select>
            <app-filter-select class="sm:w-filter" icon="clock" label="Filtrar por perecedero"
              (changed)="list.setFilters({ esPerecedero: toBool($event) })">
              <option value="">Perecederos: todos</option>
              <option value="true">Solo perecederos</option>
              <option value="false">No perecederos</option>
            </app-filter-select>
          </div>
        </div>

        @if (list.error()) {
          <p class="alert-error m-4" role="alert">{{ list.error() }}</p>
        }

        @if (list.data(); as d) {
          @if (d.content.length === 0) {
            <app-empty-state />
          } @else {
            <div class="overflow-x-auto">
              <table class="dt">
                <thead>
                  <tr>
                    <th>Producto</th><th>Categoría</th><th>Stock mínimo</th><th>Estado</th>
                    <th class="text-right">Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  @for (p of d.content; track p.id) {
                    <tr>
                      <td class="min-w-product">
                        <div class="flex items-center gap-3">
                          <div class="grid size-11 shrink-0 place-items-center rounded-lg bg-info-soft text-info">
                            <app-icon name="box" />
                          </div>
                          <div>
                            <p class="text-sm font-semibold text-ink">{{ p.nombre }}</p>
                            <p class="mt-1 max-w-description truncate text-xs text-muted">
                              {{ p.descripcion || 'Sin descripción' }}
                            </p>
                          </div>
                        </div>
                      </td>
                      <td class="whitespace-nowrap">
                        <app-badge>{{ p.categoria.nombre }}</app-badge>
                        @if (p.esPerecedero) {
                          <app-badge tone="warning" class="ml-1">Perecedero</app-badge>
                        }
                      </td>
                      <td class="text-sm font-semibold text-ink">{{ p.stockMinimo }}</td>
                      <td>
                        <div class="flex items-center gap-3">
                          <app-toggle
                            [checked]="isActivo(p.estado)"
                            [disabled]="togglingId() === p.id || !puede.activar"
                            [label]="'Cambiar estado de ' + p.nombre"
                            (checkedChange)="toggle(p, $event)"
                          />
                          <app-badge [tone]="isActivo(p.estado) ? 'success' : 'neutral'">
                            {{ isActivo(p.estado) ? 'Activo' : 'Inactivo' }}
                          </app-badge>
                        </div>
                      </td>
                      <td>
                        <div class="flex justify-end">
                          <button appIconBtn icon="eye" [label]="'Ver ' + p.nombre" (click)="openDetail(p)"></button>
                          @if (puede.editar) { <button appIconBtn icon="edit" [label]="'Editar ' + p.nombre" (click)="openForm(p)"></button> }
                          @if (puede.eliminar) { <button appIconBtn icon="trash" danger [label]="'Eliminar ' + p.nombre" (click)="toDelete.set(p)"></button> }
                        </div>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
            <app-paginator
              [page]="d.page" [size]="d.size" [totalPages]="d.totalPages" [totalElements]="d.totalElements"
              (pageChange)="list.load($event)"
            />
          }
        } @else if (list.loading()) {
          <app-table-skeleton />
        }
      </app-card>
    </div>

    @if (formOpen()) {
      <app-modal
        size="lg"
        [title]="editing() ? 'Editar producto' : 'Nuevo producto'"
        [description]="editing() ? 'Actualiza la información del producto.' : 'Completa los campos para agregar un producto al catálogo.'"
        (closed)="formOpen.set(false)"
      >
        <form [formGroup]="form" (ngSubmit)="save()" id="producto-form">
          <div class="space-y-5 p-5">
            @if (formError()) {
              <p class="alert-error" role="alert">{{ formError() }}</p>
            }
            <app-field label="Nombre del producto" [control]="form.controls.nombre" required>
              <input class="field-input" formControlName="nombre" placeholder="Ej. Escáner de código de barras" />
            </app-field>
            <app-field label="Descripción" [control]="form.controls.descripcion" optional>
              <textarea class="field-input" formControlName="descripcion" maxlength="500"
                placeholder="Describe el producto, su presentación u otros detalles…"></textarea>
            </app-field>
            <div class="grid gap-5 md:grid-cols-2">
              <app-field label="Categoría" [control]="form.controls.categoriaId" required>
                <select class="field-input" formControlName="categoriaId">
                  <option [ngValue]="null" disabled>Selecciona una categoría</option>
                  @for (c of categorias(); track c.id) {
                    <option [ngValue]="c.id">{{ c.nombre }}</option>
                  }
                </select>
              </app-field>
              <app-field label="Stock mínimo" [control]="form.controls.stockMinimo" helper="Se alerta al llegar a esta cantidad" required>
                <input type="number" min="0" class="field-input" formControlName="stockMinimo" />
              </app-field>
            </div>
            <div class="flex items-center justify-between rounded-lg border border-border bg-table-head p-4">
              <div>
                <p class="text-sm font-semibold text-ink">Producto perecedero</p>
                <p class="mt-1 text-xs text-muted">Se controla por lote y fecha de vencimiento</p>
              </div>
              <app-toggle
                label="Producto perecedero"
                [checked]="form.controls.esPerecedero.value"
                (checkedChange)="form.controls.esPerecedero.setValue($event)"
              />
            </div>
          </div>
        </form>
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="formOpen.set(false)">Cancelar</button>
          <button appBtn type="submit" form="producto-form" [disabled]="saving()">
            {{ editing() ? 'Guardar cambios' : 'Crear producto' }}
          </button>
        </ng-container>
      </app-modal>
    }

    @if (detail(); as p) {
      <app-modal [title]="p.nombre" [description]="p.categoria.nombre" (closed)="detail.set(null)">
        <div class="space-y-5 p-6">
          <div class="grid gap-4 sm:grid-cols-3">
            <div>
              <p class="text-xs font-semibold uppercase tracking-wide text-muted">Estado</p>
              <p class="mt-1 text-sm font-semibold" [class]="isActivo(p.estado) ? 'text-success' : 'text-muted'">
                {{ isActivo(p.estado) ? 'Activo' : 'Inactivo' }}
              </p>
            </div>
            <div>
              <p class="text-xs font-semibold uppercase tracking-wide text-muted">Stock mínimo</p>
              <p class="mt-1 text-sm font-semibold text-ink">{{ p.stockMinimo }}</p>
            </div>
            <div>
              <p class="text-xs font-semibold uppercase tracking-wide text-muted">Perecedero</p>
              <p class="mt-1 text-sm font-semibold text-ink">{{ p.esPerecedero ? 'Sí' : 'No' }}</p>
            </div>
          </div>
          <div>
            <p class="text-xs font-semibold uppercase tracking-wide text-muted">Descripción</p>
            <p class="mt-2 text-sm leading-6 text-muted">{{ p.descripcion || 'Sin descripción' }}</p>
          </div>
          <div><app-presentaciones-panel [productoId]="p.id" /></div>
        </div>
        <button modal-footer appBtn variant="secondary" (click)="detail.set(null)">Cerrar</button>
      </app-modal>
    }

    @if (toDelete(); as p) {
      <app-confirm-dialog
        title="¿Eliminar producto?"
        [message]="'Vas a eliminar «' + p.nombre + '». Esta acción no se puede deshacer.'"
        confirmLabel="Eliminar producto"
        [busy]="deleting()"
        [error]="deleteError()"
        (confirmed)="remove(p)"
        (cancelled)="toDelete.set(null)"
      />
    }
  `,
})
export class ProductosPage {
  private readonly service = inject(ProductosService);
  private readonly categoriasService = inject(CategoriasService);
  private readonly presentacionesService = inject(PresentacionesService);
  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly isActivo = isActivo;

  protected readonly puede = inject(AuthService).crud(MODULOS.catalogo);
  protected readonly list = new PagedList<Producto, ProductoFilters>((f) => this.service.list(f));
  protected readonly counts = signal<EstadoCounts | null>(null);
  protected readonly categorias = signal<Categoria[]>([]);

  protected readonly formOpen = signal(false);
  protected readonly editing = signal<Producto | null>(null);
  protected readonly saving = signal(false);
  protected readonly formError = signal<string | null>(null);
  protected readonly actionError = signal<string | null>(null);
  protected readonly togglingId = signal<number | null>(null);

  protected readonly detail = signal<Producto | null>(null);

  protected readonly toDelete = signal<Producto | null>(null);
  protected readonly deleting = signal(false);
  protected readonly deleteError = signal<string | null>(null);

  protected readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    descripcion: ['', Validators.maxLength(500)],
    categoriaId: [null as number | null, Validators.required],
    stockMinimo: [0, [Validators.required, Validators.min(0)]],
    esPerecedero: [false],
  });

  constructor() {
    this.loadCounts();
    this.categoriasService.list({ size: 200 }).subscribe({
      next: (p) => this.categorias.set(p.content),
      error: (e) => this.actionError.set(errorMessage(e, 'No se pudieron cargar las categorías')),
    });
  }

  private loadCounts(): void {
    estadoCounts((f) => this.service.list(f)).subscribe({ next: (c) => this.counts.set(c) });
  }

  protected toNumber(v: string): number | undefined {
    return v ? Number(v) : undefined;
  }

  protected toBool(v: string): boolean | undefined {
    return v === '' ? undefined : v === 'true';
  }

  protected openForm(p: Producto | null): void {
    this.editing.set(p);
    this.formError.set(null);
    this.form.reset({
      nombre: p?.nombre ?? '',
      descripcion: p?.descripcion ?? '',
      categoriaId: p?.categoria.id ?? null,
      stockMinimo: p?.stockMinimo ?? 0,
      esPerecedero: p?.esPerecedero ?? false,
    });
    this.formOpen.set(true);
  }

  protected save(): void {
    if (this.form.invalid) return this.form.markAllAsTouched();
    const v = this.form.getRawValue();
    const body = {
      nombre: v.nombre.trim(),
      descripcion: v.descripcion.trim(),
      categoriaId: v.categoriaId!,
      stockMinimo: v.stockMinimo,
      esPerecedero: v.esPerecedero,
    };
    const current = this.editing();
    this.saving.set(true);
    this.formError.set(null);
    (current ? this.service.update(current.id, body) : this.service.create(body)).subscribe({
      next: () => {
        this.saving.set(false);
        this.formOpen.set(false);
        current ? this.list.reload() : this.list.load(0);
        this.loadCounts();
      },
      error: (e) => {
        this.saving.set(false);
        this.formError.set(errorMessage(e));
      },
    });
  }

  protected openDetail(p: Producto): void {
    this.detail.set(p);
  }

  /** Busca una presentación por código de barras y abre el detalle de su producto. */
  protected searchBarcode(codigo: string): void {
    const code = codigo.trim();
    if (!code) return;
    this.actionError.set(null);
    this.presentacionesService.porCodigoBarras(code).subscribe({
      next: (pr) =>
        this.service.get(pr.productoId).subscribe({
          next: (p) => this.openDetail(p),
          error: (e) => this.actionError.set(errorMessage(e, 'No se pudo cargar el producto')),
        }),
      error: (e) =>
        this.actionError.set(
          e?.status === 404 ? `No existe una presentación con el código ${code}` : errorMessage(e),
        ),
    });
  }

  protected toggle(p: Producto, activar: boolean): void {
    this.togglingId.set(p.id);
    this.actionError.set(null);
    (activar ? this.service.activar(p.id) : this.service.desactivar(p.id)).subscribe({
      next: () => {
        this.togglingId.set(null);
        this.list.reload();
        this.loadCounts();
      },
      error: (e) => {
        this.togglingId.set(null);
        this.actionError.set(errorMessage(e, 'No se pudo cambiar el estado'));
        this.list.reload();
      },
    });
  }

  protected remove(p: Producto): void {
    this.deleting.set(true);
    this.deleteError.set(null);
    this.service.delete(p.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.toDelete.set(null);
        this.list.reload();
        this.loadCounts();
      },
      error: (e) => {
        this.deleting.set(false);
        this.deleteError.set(errorMessage(e, 'No se pudo eliminar el producto'));
      },
    });
  }
}
