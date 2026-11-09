import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { errorMessage } from '../../../core/api/api-error';
import { PagedList } from '../../../core/api/paged-list';
import { AuthService } from '../../../core/auth/auth.service';
import { PERMISOS } from '../../../core/auth/permisos';
import { Badge } from '../../../shared/ui/badge';
import { Btn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { EmptyState } from '../../../shared/ui/empty-state';
import { Field } from '../../../shared/ui/field';
import { FilterSelect } from '../../../shared/ui/filter-select';
import { Modal } from '../../../shared/ui/modal';
import { Paginator } from '../../../shared/ui/paginator';
import { TableSkeleton } from '../../../shared/ui/table-skeleton';
import { Caja } from '../../cajas/data/caja.model';
import { CajasService } from '../../cajas/data/cajas.service';
import { TurnoFilters, TurnoResumen } from '../data/turno.model';
import { TurnosService } from '../data/turnos.service';

@Component({
  selector: 'app-turnos-page',
  imports: [DatePipe, DecimalPipe, ReactiveFormsModule, RouterLink, Badge, Btn, Card, EmptyState, Field, FilterSelect, Modal, Paginator, TableSkeleton],
  template: `
    <div class="space-y-6">
      <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h2 class="text-lg font-semibold text-ink">Turnos</h2>
          <p class="mt-1 text-sm text-muted">Sin turno abierto no se puede vender ni devolver. Un cajero solo ve sus propios turnos.</p>
        </div>
        @if (puedeAbrir) {
          <button appBtn icon="plus" (click)="openForm()">Abrir turno</button>
        }
      </div>

      <app-card class="overflow-hidden">
        <div class="flex flex-col gap-3 border-b border-border p-4 lg:flex-row lg:items-center">
          <app-filter-select class="lg:w-status" icon="check-circle" label="Filtrar por estado"
            (changed)="list.setFilters({ estado: $any($event) || undefined })">
            <option value="">Cualquier estado</option>
            <option value="ABIERTO">Abiertos</option>
            <option value="CERRADO">Cerrados</option>
          </app-filter-select>
          <app-filter-select class="lg:w-status" icon="grid" label="Filtrar por caja"
            (changed)="list.setFilters({ cajaId: $any($event) ? +$event : undefined })">
            <option value="">Todas las cajas</option>
            @for (c of cajas(); track c.id) { <option [value]="c.id">{{ c.nombre }}</option> }
          </app-filter-select>
          <label class="flex items-center gap-2 text-sm text-muted lg:ml-auto">Desde
            <input type="date" class="field-input !h-10" (change)="list.setFilters({ desde: $any($event.target).value || undefined })" />
          </label>
          <label class="flex items-center gap-2 text-sm text-muted">Hasta
            <input type="date" class="field-input !h-10" (change)="list.setFilters({ hasta: $any($event.target).value || undefined })" />
          </label>
        </div>

        @if (list.error()) {
          <p class="alert-error m-4" role="alert">{{ list.error() }}</p>
        }

        @if (list.data(); as d) {
          @if (d.content.length === 0) {
            <app-empty-state title="Sin turnos" message="No hay turnos con esos filtros." />
          } @else {
            <div class="overflow-x-auto">
              <table class="dt">
                <thead>
                  <tr><th>Turno</th><th>Caja</th><th>Apertura</th><th class="text-right">Monto apertura</th><th>Cierre</th><th class="text-right">Esperado</th><th class="text-right">Declarado</th><th>Estado</th></tr>
                </thead>
                <tbody>
                  @for (t of d.content; track t.id) {
                    <tr>
                      <td><a [routerLink]="['/turnos', t.id]" class="font-mono text-xs font-semibold text-accent hover:underline">#{{ t.id }}</a></td>
                      <td class="text-sm font-semibold text-ink">{{ t.cajaNombre }}</td>
                      <td class="text-sm text-muted">{{ t.fechaApertura | date: 'dd/MM/yyyy HH:mm' }}</td>
                      <td class="text-right text-sm">{{ t.montoApertura | number: '1.2-2' }}</td>
                      <td class="text-sm text-muted">{{ t.fechaCierre ? (t.fechaCierre | date: 'dd/MM/yyyy HH:mm') : '—' }}</td>
                      <td class="text-right text-sm">{{ t.montoCierreEsperado === null ? '—' : (t.montoCierreEsperado | number: '1.2-2') }}</td>
                      <td class="text-right text-sm">{{ t.montoCierreDeclarado === null ? '—' : (t.montoCierreDeclarado | number: '1.2-2') }}</td>
                      <td><app-badge [tone]="t.estado === 'ABIERTO' ? 'success' : 'neutral'">{{ t.estado === 'ABIERTO' ? 'Abierto' : 'Cerrado' }}</app-badge></td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
            <app-paginator [page]="d.page" [size]="d.size" [totalPages]="d.totalPages" [totalElements]="d.totalElements" (pageChange)="list.load($event)" />
          }
        } @else if (list.loading()) {
          <app-table-skeleton />
        }
      </app-card>
    </div>

    @if (formOpen()) {
      <app-modal title="Abrir turno" description="Elige la caja y el efectivo con que inicia." (closed)="formOpen.set(false)">
        <form [formGroup]="form" (ngSubmit)="abrir()" id="turno-form">
          <div class="space-y-4 p-5">
            @if (formError()) { <p class="alert-error" role="alert">{{ formError() }}</p> }
            <app-field label="Caja" [control]="form.controls.cajaId" required>
              <select class="field-input" formControlName="cajaId">
                <option [ngValue]="null" disabled>Selecciona una caja</option>
                @for (c of cajasActivas(); track c.id) { <option [ngValue]="c.id">{{ c.nombre }}</option> }
              </select>
            </app-field>
            <app-field label="Monto de apertura" [control]="form.controls.montoApertura" required>
              <input type="number" step="0.01" min="0" class="field-input" formControlName="montoApertura" />
            </app-field>
          </div>
        </form>
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="formOpen.set(false)">Cancelar</button>
          <button appBtn type="submit" form="turno-form" [disabled]="saving()">Abrir turno</button>
        </ng-container>
      </app-modal>
    }
  `,
})
export class TurnosPage {
  private readonly service = inject(TurnosService);
  private readonly cajasService = inject(CajasService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly puedeAbrir = inject(AuthService).hasPermission(PERMISOS.cajaAbrir);

  protected readonly list = new PagedList<TurnoResumen, TurnoFilters>((f) => this.service.list(f));
  protected readonly cajas = signal<Caja[]>([]);

  protected readonly formOpen = signal(false);
  protected readonly saving = signal(false);
  protected readonly formError = signal<string | null>(null);

  protected readonly form = this.fb.group({
    cajaId: [null as number | null, Validators.required],
    montoApertura: [0, [Validators.required, Validators.min(0)]],
  });

  constructor() {
    this.cajasService.list({ size: 100 }).subscribe({ next: (p) => this.cajas.set(p.content) });
  }

  protected cajasActivas(): Caja[] {
    return this.cajas().filter((c) => c.estado === 'ACTIVO');
  }

  protected openForm(): void {
    this.formError.set(null);
    this.form.reset({ cajaId: null, montoApertura: 0 });
    this.formOpen.set(true);
  }

  protected abrir(): void {
    if (this.form.invalid) return this.form.markAllAsTouched();
    const v = this.form.getRawValue();
    this.saving.set(true);
    this.formError.set(null);
    this.service.abrir(v.cajaId!, v.montoApertura).subscribe({
      next: (t) => {
        this.saving.set(false);
        this.formOpen.set(false);
        this.router.navigate(['/turnos', t.id]);
      },
      error: (e) => {
        this.saving.set(false);
        this.formError.set(errorMessage(e));
      },
    });
  }
}
