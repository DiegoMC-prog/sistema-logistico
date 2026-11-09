import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { errorMessage } from '../../../core/api/api-error';
import { EstadoCounts, estadoCounts, isActivo } from '../../../core/api/estado';
import { PagedList } from '../../../core/api/paged-list';
import { AuthService } from '../../../core/auth/auth.service';
import { PERMISOS } from '../../../core/auth/permisos';
import { Badge } from '../../../shared/ui/badge';
import { Btn, IconBtn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { ConfirmDialog } from '../../../shared/ui/confirm-dialog';
import { EmptyState } from '../../../shared/ui/empty-state';
import { Field } from '../../../shared/ui/field';
import { FilterSelect } from '../../../shared/ui/filter-select';
import { Modal } from '../../../shared/ui/modal';
import { Paginator } from '../../../shared/ui/paginator';
import { SearchInput } from '../../../shared/ui/search-input';
import { TableSkeleton } from '../../../shared/ui/table-skeleton';
import { StatCard } from '../../../shared/ui/stat-card';
import { Toggle } from '../../../shared/ui/toggle';
import { Rol, RolesService } from '../../roles/data/roles.service';
import { Usuario, UsuarioFilters } from '../data/usuario.model';
import { UsuariosService } from '../data/usuarios.service';

@Component({
  selector: 'app-usuarios-page',
  imports: [ReactiveFormsModule, TableSkeleton, FilterSelect, Badge, Btn, IconBtn, Card, ConfirmDialog, EmptyState, Field, Modal, Paginator, SearchInput, StatCard, Toggle],
  template: `
    <div class="space-y-6">
      <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h2 class="text-lg font-semibold text-ink">Gestión de usuarios</h2>
          <p class="mt-1 text-sm text-muted">Crea cuentas, asigna roles y controla el acceso al sistema.</p>
        </div>
        @if (puede.crear) {
          <button appBtn icon="plus" (click)="openForm(null)">Nuevo usuario</button>
        }
      </div>

      @if (actionError()) {
        <p class="alert-error" role="alert">{{ actionError() }}</p>
      }

      <div class="grid gap-4 sm:grid-cols-3">
        <app-stat-card label="Total de usuarios" [value]="counts()?.total" />
        <app-stat-card label="Activos" tone="success" [value]="counts()?.activos" />
        <app-stat-card label="Inactivos" tone="muted" [value]="counts()?.inactivos" />
      </div>

      <app-card class="overflow-hidden">
        <div class="flex flex-col gap-3 border-b border-border p-4 lg:flex-row lg:items-center">
          <app-search-input placeholder="Buscar por nombre, usuario o correo…" (searched)="list.setFilters({ search: $event })" />
          <div class="flex flex-wrap gap-3 lg:ml-auto">
            @if (puedeListarRoles) {
            <app-filter-select class="sm:w-filter" icon="shield" label="Filtrar por rol"
              (changed)="list.setFilters({ rolId: $event ? +$event : undefined })">
              <option value="">Todos los roles</option>
              @for (r of roles(); track r.id) {
                <option [value]="r.id">{{ r.nombre }}</option>
              }
            </app-filter-select>
            }
            <app-filter-select class="sm:w-status" icon="check-circle" label="Filtrar por estado"
              (changed)="list.setFilters({ estado: $any($event) || undefined })">
              <option value="">Cualquier estado</option>
              <option value="ACTIVO">Activos</option>
              <option value="INACTIVO">Inactivos</option>
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
                  <tr><th>Usuario</th><th>Correo</th><th>Roles</th><th>Estado</th><th class="text-right">Acciones</th></tr>
                </thead>
                <tbody>
                  @for (u of d.content; track u.id) {
                    <tr>
                      <td class="min-w-product">
                        <div class="flex items-center gap-3">
                          <div class="grid size-10 shrink-0 place-items-center rounded-full bg-brand text-xs font-bold text-white">
                            {{ initials(u.nombre) }}
                          </div>
                          <div>
                            <p class="text-sm font-semibold text-ink">{{ u.nombre }}</p>
                            <p class="mt-0.5 font-mono text-xs text-muted">{{ u.username }}</p>
                          </div>
                        </div>
                      </td>
                      <td class="text-sm text-muted">{{ u.email }}</td>
                      <td>
                        <div class="flex flex-wrap gap-1">
                          @for (r of u.roles; track r.id) {
                            <app-badge tone="info">{{ r.nombre }}</app-badge>
                          } @empty {
                            <span class="text-xs text-muted">Sin roles</span>
                          }
                        </div>
                      </td>
                      <td>
                        <div class="flex items-center gap-3">
                          <app-toggle
                            [checked]="isActivo(u.estado)"
                            [disabled]="togglingId() === u.id || u.id === auth.perfil()?.id || !puede.activar"
                            [label]="'Cambiar estado de ' + u.nombre"
                            (checkedChange)="toggle(u, $event)"
                          />
                          <app-badge [tone]="isActivo(u.estado) ? 'success' : 'neutral'">
                            {{ isActivo(u.estado) ? 'Activo' : 'Inactivo' }}
                          </app-badge>
                          @if (u.passwordTemporal) {
                            <app-badge tone="warning">Clave temporal</app-badge>
                          }
                        </div>
                      </td>
                      <td>
                        <div class="flex justify-end">
                          @if (puede.asignarRoles) {
                            <button appIconBtn icon="shield" [label]="'Roles de ' + u.nombre" (click)="openRoles(u)"></button>
                          }
                          @if (puede.restablecer) {
                            <button appIconBtn icon="key" [label]="'Restablecer contraseña de ' + u.nombre" (click)="toReset.set(u)"></button>
                          }
                          @if (puede.editar) {
                            <button appIconBtn icon="edit" [label]="'Editar ' + u.nombre" (click)="openForm(u)"></button>
                          }
                          @if (puede.eliminar) {
                            <button appIconBtn icon="trash" danger [label]="'Eliminar ' + u.nombre" (click)="toDelete.set(u)"
                              [disabled]="u.id === auth.perfil()?.id"></button>
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
        [title]="editing() ? 'Editar usuario' : 'Nuevo usuario'"
        [description]="editing() ? 'Actualiza el nombre y el correo.' : 'El nombre de usuario no se podrá cambiar después.'"
        (closed)="formOpen.set(false)"
      >
        <form [formGroup]="form" (ngSubmit)="save()" id="usuario-form">
          <div class="space-y-5 p-5">
            @if (formError()) {
              <p class="alert-error" role="alert">{{ formError() }}</p>
            }
            @if (!editing()) {
              <app-field label="Nombre de usuario" [control]="form.controls.username" required>
                <input class="field-input" formControlName="username" autocomplete="off" placeholder="Ej. amorgan" />
              </app-field>
            }
            <app-field label="Nombre completo" [control]="form.controls.nombre" required>
              <input class="field-input" formControlName="nombre" placeholder="Ej. Alex Morgan" />
            </app-field>
            <app-field label="Correo electrónico" [control]="form.controls.email" required>
              <input type="email" class="field-input" formControlName="email" placeholder="alex@empresa.com" />
            </app-field>
          </div>
        </form>
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="formOpen.set(false)">Cancelar</button>
          <button appBtn type="submit" form="usuario-form" [disabled]="saving()">
            {{ editing() ? 'Guardar cambios' : 'Crear usuario' }}
          </button>
        </ng-container>
      </app-modal>
    }

    @if (rolesFor(); as u) {
      <app-modal [title]="'Roles de ' + u.nombre" description="Selecciona los roles que tendrá este usuario." (closed)="rolesFor.set(null)">
        <div class="space-y-3 p-5">
          @if (rolesError()) {
            <p class="alert-error" role="alert">{{ rolesError() }}</p>
          }
          @for (r of roles(); track r.id) {
            <label class="flex cursor-pointer items-center gap-3 rounded-lg border border-border p-3 hover:bg-subtle">
              <input type="checkbox" class="size-4 accent-[var(--color-accent)]"
                [checked]="selectedRoles().has(r.id)" (change)="toggleRole(r.id)" />
              <span class="text-sm font-semibold text-ink">{{ r.nombre }}</span>
            </label>
          } @empty {
            <p class="text-sm text-muted">No hay roles disponibles.</p>
          }
        </div>
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="rolesFor.set(null)">Cancelar</button>
          <button appBtn type="button" [disabled]="saving()" (click)="saveRoles(u)">Guardar roles</button>
        </ng-container>
      </app-modal>
    }

    @if (toReset(); as u) {
      <app-confirm-dialog
        title="¿Restablecer contraseña?"
        [message]="'Se generará una contraseña temporal para ' + u.nombre + ', que deberá cambiarla al iniciar sesión.'"
        confirmLabel="Restablecer"
        icon="key"
        [busy]="resetting()"
        [error]="resetError()"
        (confirmed)="reset(u)"
        (cancelled)="toReset.set(null)"
      />
    }

    @if (toDelete(); as u) {
      <app-confirm-dialog
        title="¿Eliminar usuario?"
        [message]="'Vas a eliminar a ' + u.nombre + ' (' + u.username + '). Esta acción no se puede deshacer.'"
        confirmLabel="Eliminar usuario"
        [busy]="deleting()"
        [error]="deleteError()"
        (confirmed)="remove(u)"
        (cancelled)="toDelete.set(null)"
      />
    }
  `,
})
export class UsuariosPage {
  private readonly service = inject(UsuariosService);
  private readonly rolesService = inject(RolesService);
  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly auth = inject(AuthService);
  protected readonly puede = {
    crear: this.auth.hasPermission(PERMISOS.usuarioCrear),
    editar: this.auth.hasPermission(PERMISOS.usuarioEditar),
    eliminar: this.auth.hasPermission(PERMISOS.usuarioEliminar),
    activar: this.auth.hasPermission(PERMISOS.usuarioActivar),
    restablecer: this.auth.hasPermission(PERMISOS.usuarioRestablecerPassword),
    asignarRoles: this.auth.hasPermission(PERMISOS.usuarioAsignarRoles),
  };
  /** El backend permite listar roles con ROL_VER, USUARIO_CREAR, USUARIO_EDITAR o USUARIO_ASIGNAR_ROLES. */
  protected readonly puedeListarRoles = this.auth.hasAnyPermission([
    PERMISOS.rolVer, PERMISOS.usuarioCrear, PERMISOS.usuarioEditar, PERMISOS.usuarioAsignarRoles,
  ]);
  protected readonly isActivo = isActivo;

  protected readonly list = new PagedList<Usuario, UsuarioFilters>((f) => this.service.list(f));
  protected readonly counts = signal<EstadoCounts | null>(null);
  protected readonly roles = signal<Rol[]>([]);

  protected readonly actionError = signal<string | null>(null);
  protected readonly togglingId = signal<number | null>(null);

  protected readonly formOpen = signal(false);
  protected readonly editing = signal<Usuario | null>(null);
  protected readonly saving = signal(false);
  protected readonly formError = signal<string | null>(null);

  protected readonly rolesFor = signal<Usuario | null>(null);
  protected readonly selectedRoles = signal<Set<number>>(new Set());
  protected readonly rolesError = signal<string | null>(null);

  protected readonly toReset = signal<Usuario | null>(null);
  protected readonly resetting = signal(false);
  protected readonly resetError = signal<string | null>(null);

  protected readonly toDelete = signal<Usuario | null>(null);
  protected readonly deleting = signal(false);
  protected readonly deleteError = signal<string | null>(null);

  protected readonly form = this.fb.group({
    username: ['', [Validators.required, Validators.maxLength(50)]],
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email]],
  });

  constructor() {
    this.loadCounts();
    if (this.puedeListarRoles) {
      this.rolesService.list({ size: 200 }).subscribe({
        next: (p) => this.roles.set(p.content),
        error: (e) => this.actionError.set(errorMessage(e, 'No se pudieron cargar los roles')),
      });
    }
  }

  private loadCounts(): void {
    estadoCounts((f) => this.service.list(f)).subscribe({ next: (c) => this.counts.set(c) });
  }

  protected initials = (nombre: string): string =>
    nombre.split(/\s+/).filter(Boolean).slice(0, 2).map((p) => p[0].toUpperCase()).join('');

  protected openForm(u: Usuario | null): void {
    this.editing.set(u);
    this.formError.set(null);
    this.form.reset({ username: u?.username ?? '', nombre: u?.nombre ?? '', email: u?.email ?? '' });
    // El username solo se captura al crear.
    if (u) this.form.controls.username.disable();
    else this.form.controls.username.enable();
    this.formOpen.set(true);
  }

  protected save(): void {
    if (this.form.invalid) return this.form.markAllAsTouched();
    const v = this.form.getRawValue();
    const current = this.editing();
    const email = v.email.trim();
    const nombre = v.nombre.trim();
    this.saving.set(true);
    this.formError.set(null);
    const request$ = current
      ? this.service.update(current.id, { nombre, email })
      : this.service.create({ username: v.username.trim(), nombre, email });
    request$.subscribe({
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

  protected openRoles(u: Usuario): void {
    this.rolesError.set(null);
    this.selectedRoles.set(new Set(u.roles.map((r) => r.id)));
    this.rolesFor.set(u);
  }

  protected toggleRole(id: number): void {
    this.selectedRoles.update((s) => {
      const next = new Set(s);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  }

  protected saveRoles(u: Usuario): void {
    this.saving.set(true);
    this.rolesError.set(null);
    this.service.asignarRoles(u.id, [...this.selectedRoles()]).subscribe({
      next: () => {
        this.saving.set(false);
        this.rolesFor.set(null);
        this.list.reload();
      },
      error: (e) => {
        this.saving.set(false);
        this.rolesError.set(errorMessage(e, 'No se pudieron guardar los roles'));
      },
    });
  }

  protected toggle(u: Usuario, activar: boolean): void {
    this.togglingId.set(u.id);
    this.actionError.set(null);
    (activar ? this.service.activar(u.id) : this.service.desactivar(u.id)).subscribe({
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

  protected reset(u: Usuario): void {
    this.resetting.set(true);
    this.resetError.set(null);
    this.service.restablecerPassword(u.id).subscribe({
      next: () => {
        this.resetting.set(false);
        this.toReset.set(null);
        this.list.reload();
      },
      error: (e) => {
        this.resetting.set(false);
        this.resetError.set(errorMessage(e, 'No se pudo restablecer la contraseña'));
      },
    });
  }

  protected remove(u: Usuario): void {
    this.deleting.set(true);
    this.deleteError.set(null);
    this.service.delete(u.id).subscribe({
      next: () => {
        this.deleting.set(false);
        this.toDelete.set(null);
        this.list.reload();
        this.loadCounts();
      },
      error: (e) => {
        this.deleting.set(false);
        this.deleteError.set(errorMessage(e, 'No se pudo eliminar el usuario'));
      },
    });
  }
}
