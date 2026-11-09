import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { errorMessage } from '../../../core/api/api-error';
import { PagedList } from '../../../core/api/paged-list';
import { AuthService } from '../../../core/auth/auth.service';
import { PERMISOS } from '../../../core/auth/permisos';
import { Btn, IconBtn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { ConfirmDialog } from '../../../shared/ui/confirm-dialog';
import { EmptyState } from '../../../shared/ui/empty-state';
import { Field } from '../../../shared/ui/field';
import { Icon } from '../../../shared/ui/icon';
import { Modal } from '../../../shared/ui/modal';
import { Paginator } from '../../../shared/ui/paginator';
import { SearchInput } from '../../../shared/ui/search-input';
import { TableSkeleton } from '../../../shared/ui/table-skeleton';
import { StatCard } from '../../../shared/ui/stat-card';
import { PermisosService } from '../data/permisos.service';
import { Permiso, Rol, RolesService } from '../data/roles.service';

const ETIQUETAS_MODULO: Record<string, string> = {
  USUARIO: 'Usuarios', ROL: 'Roles', CATALOGO: 'Catálogo', DEPOSITO: 'Depósitos', CLIENTE: 'Clientes',
  PRECIO: 'Precios', METODO_PAGO: 'Métodos de pago', INVENTARIO: 'Inventario', CAJA: 'Cajas y turnos',
  VENTA: 'Ventas', PROVEEDOR: 'Proveedores', PEDIDO: 'Pedidos', REPORTE: 'Reportes',
};

interface FilaMatriz {
  modulo: string;
  etiqueta: string;
  ver?: Permiso;
  crear?: Permiso;
  editar?: Permiso;
  eliminar?: Permiso;
  otras: Permiso[];
  todos: Permiso[];
}

const COLUMNAS = { VER: 'ver', CREAR: 'crear', EDITAR: 'editar', ELIMINAR: 'eliminar' } as const;

/** Una fila por módulo: acciones estándar en columnas fijas y el resto en «Otras». */
function construirMatriz(permisos: Permiso[]): FilaMatriz[] {
  const filas = new Map<string, FilaMatriz>();
  for (const p of permisos) {
    let f = filas.get(p.modulo);
    if (!f) {
      f = { modulo: p.modulo, etiqueta: ETIQUETAS_MODULO[p.modulo] ?? p.modulo, otras: [], todos: [] };
      filas.set(p.modulo, f);
    }
    f.todos.push(p);
    const col = p.estandar ? COLUMNAS[p.accion as keyof typeof COLUMNAS] : undefined;
    if (col) f[col] = p;
    else f.otras.push(p);
  }
  return [...filas.values()];
}

@Component({
  selector: 'app-roles-page',
  imports: [ReactiveFormsModule, TableSkeleton, Btn, IconBtn, Card, ConfirmDialog, EmptyState, Field, Icon, Modal, Paginator, SearchInput, StatCard],
  template: `
    <div class="space-y-6">
      <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h2 class="text-lg font-semibold text-ink">Gestión de roles</h2>
          <p class="mt-1 text-sm text-muted">Crea roles y asigna los permisos que tendrá cada uno.</p>
        </div>
        @if (puede.crear) {
          <button appBtn icon="plus" (click)="openForm(null)">Nuevo rol</button>
        }
      </div>

      <div class="grid gap-4 sm:grid-cols-3">
        <app-stat-card label="Total de roles" [value]="list.data()?.totalElements" />
        <app-stat-card label="Permisos disponibles" [value]="permisos().length" />
      </div>

      <app-card class="overflow-hidden">
        <div class="flex items-center justify-between gap-3 border-b border-border p-4">
          <app-search-input placeholder="Buscar roles…" (searched)="list.setFilters({ search: $event })" />
          <p class="hidden shrink-0 text-sm text-muted sm:block">{{ list.data()?.totalElements ?? 0 }} resultados</p>
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
                  <tr><th>ID</th><th>Rol</th><th class="text-right">Acciones</th></tr>
                </thead>
                <tbody>
                  @for (r of d.content; track r.id) {
                    <tr>
                      <td class="w-24 font-mono text-xs font-semibold text-muted">#{{ pad(r.id) }}</td>
                      <td>
                        <div class="flex items-center gap-3">
                          <div class="grid size-9 place-items-center rounded-lg bg-success-soft text-success">
                            <app-icon name="shield" class="size-4" />
                          </div>
                          <span class="text-sm font-semibold text-ink">{{ r.nombre }}</span>
                          @if (esSistema(r)) { <span class="rounded-full bg-info-soft px-2 py-0.5 text-xs font-semibold text-info">Sistema</span> }
                        </div>
                      </td>
                      <td>
                        <div class="flex justify-end">
                          <button appIconBtn icon="key" [label]="'Permisos de ' + r.nombre" (click)="openPermisos(r)"></button>
                          @if (puede.editar && !esSistema(r)) {
                            <button appIconBtn icon="edit" [label]="'Editar ' + r.nombre" (click)="openForm(r)"></button>
                          }
                          @if (puede.eliminar && !esSistema(r)) {
                            <button appIconBtn icon="trash" danger [label]="'Eliminar ' + r.nombre" (click)="toDelete.set(r)"></button>
                          }
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
        [title]="editing() ? 'Editar rol' : 'Nuevo rol'"
        description="El nombre identifica al rol al asignarlo a los usuarios."
        (closed)="formOpen.set(false)"
      >
        <form [formGroup]="form" (ngSubmit)="save()" id="rol-form">
          <div class="space-y-5 p-5">
            @if (formError()) {
              <p class="alert-error" role="alert">{{ formError() }}</p>
            }
            <app-field label="Nombre del rol" [control]="form.controls.nombre" required>
              <input class="field-input" formControlName="nombre" placeholder="Ej. Supervisor" />
            </app-field>
          </div>
        </form>
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="formOpen.set(false)">Cancelar</button>
          <button appBtn type="submit" form="rol-form" [disabled]="saving()">
            {{ editing() ? 'Guardar cambios' : 'Crear rol' }}
          </button>
        </ng-container>
      </app-modal>
    }

    @if (permisosFor(); as r) {
      <app-modal size="lg" [title]="'Permisos de ' + r.nombre" [description]="soloLectura() ? 'Permisos asignados a este rol.' : 'Marca lo que este rol puede hacer.'" (closed)="permisosFor.set(null)">
        <div class="p-5">
          @if (permisosError()) {
            <p class="alert-error mb-4" role="alert">{{ permisosError() }}</p>
          }
          @if (permisosLoading()) {
            <p class="py-8 text-center text-sm text-muted">Cargando…</p>
          } @else {
            <div class="mb-3 flex items-center justify-between text-sm text-muted">
              <span>{{ selected().size }} de {{ permisos().length }} seleccionados</span>
              @if (!soloLectura()) {
                <span class="flex gap-3">
                  <button type="button" class="font-semibold text-ink underline" (click)="selectAll(true)">Todos</button>
                  <button type="button" class="font-semibold text-ink underline" (click)="selectAll(false)">Ninguno</button>
                </span>
              } @else {
                <span>Solo lectura</span>
              }
            </div>
            <div class="max-h-96 overflow-auto rounded-lg border border-border">
              <table class="dt">
                <thead>
                  <tr><th>Módulo</th><th class="text-center">Ver</th><th class="text-center">Crear</th><th class="text-center">Editar</th><th class="text-center">Eliminar</th><th>Otras</th></tr>
                </thead>
                <tbody>
                  @for (f of matriz(); track f.modulo) {
                    <tr>
                      <td>
                        <label class="flex cursor-pointer items-center gap-2 text-sm font-semibold text-ink">
                          <input type="checkbox" class="size-4 accent-[var(--color-accent)]" [disabled]="soloLectura()"
                            [checked]="filaCompleta(f)" [indeterminate]="filaParcial(f)" (change)="toggleFila(f)"
                            [attr.aria-label]="'Marcar todo ' + f.etiqueta" />
                          {{ f.etiqueta }}
                        </label>
                      </td>
                      @for (p of estandar(f); track $index) {
                        <td class="text-center">
                          @if (p) {
                            <input type="checkbox" class="size-4 accent-[var(--color-accent)]" [disabled]="soloLectura()"
                              [checked]="selected().has(p.id)" (change)="togglePermiso(p, f)" [attr.title]="p.descripcion" [attr.aria-label]="p.nombre" />
                          }
                        </td>
                      }
                      <td>
                        <div class="flex flex-wrap gap-x-4 gap-y-1">
                          @for (p of f.otras; track p.id) {
                            <label class="flex cursor-pointer items-center gap-1.5 text-xs text-ink" [attr.title]="p.descripcion">
                              <input type="checkbox" class="size-3.5 accent-[var(--color-accent)]" [disabled]="soloLectura()"
                                [checked]="selected().has(p.id)" (change)="togglePermiso(p, f)" />
                              {{ nombreAccion(p.accion) }}
                            </label>
                          }
                        </div>
                      </td>
                    </tr>
                  } @empty {
                    <tr><td colspan="6" class="text-sm text-muted">No hay permisos disponibles.</td></tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </div>
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="permisosFor.set(null)">{{ soloLectura() ? 'Cerrar' : 'Cancelar' }}</button>
          @if (!soloLectura()) {
            <button appBtn type="button" [disabled]="saving() || permisosLoading()" (click)="savePermisos(r)">
              Guardar permisos
            </button>
          }
        </ng-container>
      </app-modal>
    }

    @if (toDelete(); as r) {
      <app-confirm-dialog
        title="¿Eliminar rol?"
        [message]="'Vas a eliminar el rol «' + r.nombre + '». Esta acción no se puede deshacer.'"
        confirmLabel="Eliminar rol"
        [busy]="deleting()"
        [error]="deleteError()"
        (confirmed)="remove(r)"
        (cancelled)="toDelete.set(null)"
      />
    }
  `,
})
export class RolesPage {
  private readonly service = inject(RolesService);
  private readonly permisosService = inject(PermisosService);
  private readonly fb = inject(FormBuilder).nonNullable;
  private readonly auth = inject(AuthService);
  protected readonly puede = {
    crear: this.auth.hasPermission(PERMISOS.rolCrear),
    editar: this.auth.hasPermission(PERMISOS.rolEditar),
    eliminar: this.auth.hasPermission(PERMISOS.rolEliminar),
    asignarPermisos: this.auth.hasPermission(PERMISOS.rolAsignarPermisos),
  };

  protected readonly list = new PagedList<Rol>((f) => this.service.list(f));
  protected readonly permisos = signal<Permiso[]>([]);
  protected readonly matriz = computed(() => construirMatriz(this.permisos()));

  protected readonly formOpen = signal(false);
  protected readonly editing = signal<Rol | null>(null);
  protected readonly saving = signal(false);
  protected readonly formError = signal<string | null>(null);

  protected readonly permisosFor = signal<Rol | null>(null);
  protected readonly permisosLoading = signal(false);
  protected readonly permisosError = signal<string | null>(null);
  protected readonly selected = signal<Set<number>>(new Set());

  protected readonly toDelete = signal<Rol | null>(null);
  protected readonly deleting = signal(false);
  protected readonly deleteError = signal<string | null>(null);

  protected readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
  });

  constructor() {
    this.permisosService.list().subscribe({ next: (p) => this.permisos.set(p) });
  }

  /** `ADMIN` es un rol de sistema: no se edita, elimina ni cambia de permisos. */
  protected esSistema(r: Rol): boolean {
    return r.nombre === 'ADMIN';
  }

  protected readonly soloLectura = computed(() => {
    const r = this.permisosFor();
    return !this.puede.asignarPermisos || (r !== null && this.esSistema(r));
  });

  protected estandar(f: FilaMatriz): (Permiso | undefined)[] {
    return [f.ver, f.crear, f.editar, f.eliminar];
  }

  protected nombreAccion(accion: string): string {
    return accion.charAt(0) + accion.slice(1).toLowerCase().replace(/_/g, ' ');
  }

  protected filaCompleta(f: FilaMatriz): boolean {
    return f.todos.length > 0 && f.todos.every((p) => this.selected().has(p.id));
  }

  protected filaParcial(f: FilaMatriz): boolean {
    const n = f.todos.filter((p) => this.selected().has(p.id)).length;
    return n > 0 && n < f.todos.length;
  }

  /** Marca o desmarca todo el módulo. */
  protected toggleFila(f: FilaMatriz): void {
    const marcar = !this.filaCompleta(f);
    this.selected.update((s) => {
      const next = new Set(s);
      for (const p of f.todos) marcar ? next.add(p.id) : next.delete(p.id);
      return next;
    });
  }

  protected pad(id: number): string {
    return String(id).padStart(3, '0');
  }

  protected openForm(r: Rol | null): void {
    this.editing.set(r);
    this.formError.set(null);
    this.form.reset({ nombre: r?.nombre ?? '' });
    this.formOpen.set(true);
  }

  protected save(): void {
    if (this.form.invalid) return this.form.markAllAsTouched();
    const body = { nombre: this.form.getRawValue().nombre.trim() };
    const current = this.editing();
    this.saving.set(true);
    this.formError.set(null);
    (current ? this.service.update(current.id, body) : this.service.create(body)).subscribe({
      next: () => {
        this.saving.set(false);
        this.formOpen.set(false);
        current ? this.list.reload() : this.list.load(0);
      },
      error: (e) => {
        this.saving.set(false);
        this.formError.set(errorMessage(e));
      },
    });
  }

  protected openPermisos(r: Rol): void {
    this.permisosFor.set(r);
    this.permisosError.set(null);
    this.permisosLoading.set(true);
    this.selected.set(new Set());
    this.service.detalle(r.id).subscribe({
      next: (d) => {
        this.selected.set(new Set(d.permisos.map((p) => p.id)));
        this.permisosLoading.set(false);
      },
      error: (e) => {
        this.permisosLoading.set(false);
        this.permisosError.set(errorMessage(e, 'No se pudieron cargar los permisos del rol'));
      },
    });
  }

  /** Al marcar crear, editar o eliminar se marca también «ver» del módulo si existe. */
  protected togglePermiso(p: Permiso, f: FilaMatriz): void {
    this.selected.update((s) => {
      const next = new Set(s);
      if (next.has(p.id)) {
        next.delete(p.id);
      } else {
        next.add(p.id);
        if (f.ver && ['CREAR', 'EDITAR', 'ELIMINAR'].includes(p.accion)) next.add(f.ver.id);
      }
      return next;
    });
  }

  protected selectAll(all: boolean): void {
    this.selected.set(all ? new Set(this.permisos().map((p) => p.id)) : new Set());
  }

  protected savePermisos(r: Rol): void {
    this.saving.set(true);
    this.permisosError.set(null);
    this.service.asignarPermisos(r.id, [...this.selected()]).subscribe({
      next: () => {
        this.saving.set(false);
        this.permisosFor.set(null);
      },
      error: (e) => {
        this.saving.set(false);
        this.permisosError.set(errorMessage(e, 'No se pudieron guardar los permisos'));
      },
    });
  }

  protected remove(r: Rol): void {
    this.deleting.set(true);
    this.deleteError.set(null);
    this.service.delete(r.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.toDelete.set(null);
        this.list.reload();
      },
      error: (e) => {
        this.deleting.set(false);
        this.deleteError.set(errorMessage(e, 'No se pudo eliminar el rol'));
      },
    });
  }
}
