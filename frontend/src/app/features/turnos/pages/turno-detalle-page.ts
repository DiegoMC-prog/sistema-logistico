import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { errorMessage } from '../../../core/api/api-error';
import { AuthService } from '../../../core/auth/auth.service';
import { PERMISOS } from '../../../core/auth/permisos';
import { Badge } from '../../../shared/ui/badge';
import { Btn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { Field } from '../../../shared/ui/field';
import { Modal } from '../../../shared/ui/modal';
import { StatCard } from '../../../shared/ui/stat-card';
import { MovimientoCaja, Turno } from '../data/turno.model';
import { TurnosService } from '../data/turnos.service';

@Component({
  selector: 'app-turno-detalle-page',
  imports: [DatePipe, DecimalPipe, ReactiveFormsModule, RouterLink, Badge, Btn, Card, Field, Modal, StatCard],
  template: `
    <div class="space-y-6">
      <a routerLink="/turnos" class="text-sm font-medium text-accent hover:underline">← Volver a turnos</a>

      @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }

      @if (turno(); as t) {
        <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
          <div>
            <h2 class="text-lg font-semibold text-ink">
              Turno #{{ t.id }} · {{ t.cajaNombre }}
              <app-badge class="ml-2" [tone]="t.estado === 'ABIERTO' ? 'success' : 'neutral'">{{ t.estado === 'ABIERTO' ? 'Abierto' : 'Cerrado' }}</app-badge>
            </h2>
            <p class="mt-1 text-sm text-muted">
              Abierto el {{ t.fechaApertura | date: 'dd/MM/yyyy HH:mm' }}
              @if (t.fechaCierre) { · cerrado el {{ t.fechaCierre | date: 'dd/MM/yyyy HH:mm' }} }
            </p>
          </div>
          @if (t.estado === 'ABIERTO') {
            <div class="flex gap-3">
              @if (puedeMovimiento) {
                <button appBtn variant="secondary" icon="wallet" (click)="openMovimiento()">Registrar movimiento</button>
              }
              @if (puedeCerrar) {
                <button appBtn variant="danger" icon="clock" (click)="openCierre()">Cerrar turno</button>
              }
            </div>
          }
        </div>

        <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <app-stat-card label="Monto de apertura" [value]="(t.montoApertura | number: '1.2-2')" />
          <app-stat-card label="Ingresos" tone="success" [value]="(t.ingresos | number: '1.2-2')" />
          <app-stat-card label="Egresos" tone="muted" [value]="(t.egresos | number: '1.2-2')" />
          <app-stat-card label="Efectivo esperado" [value]="(t.efectivoEsperado | number: '1.2-2')"
            hint="Apertura + ingresos − egresos + cobrado en efectivo + abonos en efectivo − reembolsos en efectivo" />
        </div>

        @if (t.estado === 'CERRADO') {
          <app-card class="block p-5">
            <p class="text-sm font-semibold text-ink">Cuadre de caja</p>
            <dl class="mt-3 grid gap-4 text-sm sm:grid-cols-3">
              <div><dt class="text-muted">Esperado</dt><dd class="text-lg font-bold text-ink">{{ t.efectivoEsperado | number: '1.2-2' }}</dd></div>
              <div><dt class="text-muted">Declarado</dt><dd class="text-lg font-bold text-ink">{{ (t.montoCierreDeclarado ?? 0) | number: '1.2-2' }}</dd></div>
              <div>
                <dt class="text-muted">Diferencia</dt>
                <dd class="text-lg font-bold" [class]="(t.diferencia ?? 0) < 0 ? 'text-danger' : (t.diferencia ?? 0) > 0 ? 'text-warning-dark' : 'text-success'">
                  {{ (t.diferencia ?? 0) | number: '1.2-2' }}
                  <span class="text-xs font-normal text-muted">{{ (t.diferencia ?? 0) < 0 ? 'faltante' : (t.diferencia ?? 0) > 0 ? 'sobrante' : 'cuadrada' }}</span>
                </dd>
              </div>
            </dl>
          </app-card>
        }

        <div class="grid gap-6 lg:grid-cols-2">
          <app-card class="block p-5">
            <p class="text-sm font-semibold text-ink">Cobros por método de pago</p>
            @if (t.cobrosPorMetodo.length === 0) {
              <p class="mt-3 text-sm text-muted">Aún no hay cobros.</p>
            } @else {
              <ul class="mt-3 divide-y divide-border text-sm">
                @for (c of t.cobrosPorMetodo; track c.metodoPagoId) {
                  <li class="flex justify-between py-2"><span class="text-muted">{{ c.metodoPago }}</span><span class="font-semibold text-ink">{{ c.total | number: '1.2-2' }}</span></li>
                }
              </ul>
            }
            <dl class="mt-4 space-y-1 border-t border-border pt-3 text-sm">
              <div class="flex justify-between"><dt class="text-muted">Efectivo cobrado</dt><dd>{{ t.efectivoCobrado | number: '1.2-2' }}</dd></div>
              <div class="flex justify-between"><dt class="text-muted">Abonos de crédito en efectivo</dt><dd>{{ t.abonosEfectivo | number: '1.2-2' }}</dd></div>
              <div class="flex justify-between"><dt class="text-muted">Reembolsos en efectivo</dt><dd>{{ t.reembolsosEfectivo | number: '1.2-2' }}</dd></div>
            </dl>
          </app-card>

          <app-card class="block p-5">
            <p class="text-sm font-semibold text-ink">Movimientos de caja</p>
            @if (movimientos().length === 0) {
              <p class="mt-3 text-sm text-muted">Sin ingresos ni egresos manuales.</p>
            } @else {
              <ul class="mt-3 divide-y divide-border text-sm">
                @for (m of movimientos(); track m.id) {
                  <li class="flex items-center justify-between gap-3 py-2">
                    <span>
                      <app-badge [tone]="m.tipo === 'INGRESO' ? 'success' : 'warning'">{{ m.tipo === 'INGRESO' ? 'Ingreso' : 'Egreso' }}</app-badge>
                      <span class="ml-2 text-muted">{{ m.motivo }}</span>
                    </span>
                    <span class="font-semibold text-ink">{{ m.monto | number: '1.2-2' }}</span>
                  </li>
                }
              </ul>
            }
          </app-card>
        </div>
      } @else if (!error()) {
        <p class="text-sm text-muted">Cargando…</p>
      }
    </div>

    @if (movOpen()) {
      <app-modal title="Registrar movimiento" description="Los movimientos no se editan ni se borran: un error se corrige con otro movimiento." (closed)="movOpen.set(false)">
        <form [formGroup]="movForm" (ngSubmit)="guardarMovimiento()" id="mov-form">
          <div class="space-y-4 p-5">
            @if (modalError()) { <p class="alert-error" role="alert">{{ modalError() }}</p> }
            <app-field label="Tipo" [control]="movForm.controls.tipo" required>
              <select class="field-input" formControlName="tipo">
                <option value="INGRESO">Ingreso</option>
                <option value="EGRESO">Egreso</option>
              </select>
            </app-field>
            <app-field label="Monto" [control]="movForm.controls.monto" required>
              <input type="number" step="0.01" min="0.01" class="field-input" formControlName="monto" />
            </app-field>
            <app-field label="Motivo" [control]="movForm.controls.motivo" required>
              <input class="field-input" formControlName="motivo" maxlength="200" />
            </app-field>
          </div>
        </form>
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="movOpen.set(false)">Cancelar</button>
          <button appBtn type="submit" form="mov-form" [disabled]="saving()">Registrar</button>
        </ng-container>
      </app-modal>
    }

    @if (cierreOpen()) {
      <app-modal title="Cerrar turno" description="Cuenta el efectivo de la caja y declara el monto." (closed)="cierreOpen.set(false)">
        <form [formGroup]="cierreForm" (ngSubmit)="cerrar()" id="cierre-form">
          <div class="space-y-4 p-5">
            @if (modalError()) { <p class="alert-error" role="alert">{{ modalError() }}</p> }
            <app-field label="Monto declarado (efectivo contado)" [control]="cierreForm.controls.montoDeclarado" required>
              <input type="number" step="0.01" min="0" class="field-input" formControlName="montoDeclarado" />
            </app-field>
          </div>
        </form>
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="cierreOpen.set(false)">Cancelar</button>
          <button appBtn variant="danger" type="submit" form="cierre-form" [disabled]="saving()">Cerrar turno</button>
        </ng-container>
      </app-modal>
    }
  `,
})
export class TurnoDetallePage {
  private readonly service = inject(TurnosService);
  private readonly fb = inject(FormBuilder).nonNullable;
  private readonly auth = inject(AuthService);
  private readonly id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));

  protected readonly puedeMovimiento = this.auth.hasPermission(PERMISOS.cajaMovimiento);
  protected readonly puedeCerrar = this.auth.hasPermission(PERMISOS.cajaCerrar);

  protected readonly turno = signal<Turno | null>(null);
  protected readonly movimientos = signal<MovimientoCaja[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly saving = signal(false);
  protected readonly modalError = signal<string | null>(null);
  protected readonly movOpen = signal(false);
  protected readonly cierreOpen = signal(false);
  protected readonly abierto = computed(() => this.turno()?.estado === 'ABIERTO');

  protected readonly movForm = this.fb.group({
    tipo: ['INGRESO' as 'INGRESO' | 'EGRESO', Validators.required],
    monto: [0, [Validators.required, Validators.min(0.01)]],
    motivo: ['', [Validators.required, Validators.maxLength(200)]],
  });
  protected readonly cierreForm = this.fb.group({
    montoDeclarado: [0, [Validators.required, Validators.min(0)]],
  });

  constructor() {
    this.refresh();
  }

  /** Recarga resumen y movimientos: el efectivo esperado cambia tras cada operación. */
  private refresh(): void {
    this.service.get(this.id).subscribe({
      next: (t) => {
        this.error.set(null);
        this.turno.set(t);
      },
      error: (e) => this.error.set(errorMessage(e, 'No se pudo cargar el turno')),
    });
    this.service.movimientos(this.id).subscribe({
      next: (r) => this.movimientos.set(Array.isArray(r) ? r : r.content),
      error: () => this.movimientos.set([]),
    });
  }

  protected openMovimiento(): void {
    this.modalError.set(null);
    this.movForm.reset({ tipo: 'INGRESO', monto: 0, motivo: '' });
    this.movOpen.set(true);
  }

  protected guardarMovimiento(): void {
    if (this.movForm.invalid) return this.movForm.markAllAsTouched();
    const v = this.movForm.getRawValue();
    this.saving.set(true);
    this.modalError.set(null);
    this.service.registrarMovimiento(this.id, { tipo: v.tipo, monto: v.monto, motivo: v.motivo.trim() }).subscribe({
      next: () => {
        this.saving.set(false);
        this.movOpen.set(false);
        this.refresh();
      },
      error: (e) => {
        this.saving.set(false);
        this.modalError.set(errorMessage(e));
      },
    });
  }

  protected openCierre(): void {
    this.modalError.set(null);
    this.cierreForm.reset({ montoDeclarado: this.turno()?.efectivoEsperado ?? 0 });
    this.cierreOpen.set(true);
  }

  protected cerrar(): void {
    if (this.cierreForm.invalid) return this.cierreForm.markAllAsTouched();
    this.saving.set(true);
    this.modalError.set(null);
    this.service.cerrar(this.id, this.cierreForm.getRawValue().montoDeclarado).subscribe({
      next: (t) => {
        this.saving.set(false);
        this.cierreOpen.set(false);
        this.turno.set(t); // trae esperado, declarado y diferencia: pantalla de cuadre
        this.refresh();
      },
      error: (e) => {
        this.saving.set(false);
        this.modalError.set(errorMessage(e));
      },
    });
  }
}
