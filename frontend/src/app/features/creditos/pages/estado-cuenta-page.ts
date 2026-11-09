import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { errorMessage } from '../../../core/api/api-error';
import { Badge } from '../../../shared/ui/badge';
import { Btn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { Field } from '../../../shared/ui/field';
import { Modal } from '../../../shared/ui/modal';
import { StatCard } from '../../../shared/ui/stat-card';
import { MetodoPago } from '../../metodos-pago/data/metodo-pago.model';
import { MetodosPagoService } from '../../metodos-pago/data/metodos-pago.service';
import { Abono, EstadoCuenta, EstadoCuentaCliente } from '../data/credito.model';
import { CreditosService } from '../data/creditos.service';

@Component({
  selector: 'app-estado-cuenta-page',
  imports: [DatePipe, DecimalPipe, ReactiveFormsModule, RouterLink, Badge, Btn, Card, Field, Modal, StatCard],
  template: `
    <div class="space-y-6">
      <a routerLink="/creditos" class="text-sm font-medium text-accent hover:underline print:hidden">← Volver a cuentas por cobrar</a>

      @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }

      @if (estado(); as e) {
        <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
          <div>
            <h2 class="text-lg font-semibold text-ink">{{ e.clienteNombre }}</h2>
            <p class="mt-1 text-sm text-muted">
              @if (e.creditoHabilitado) { Crédito habilitado · límite {{ e.limiteCredito | number: '1.2-2' }} }
              @else { Crédito no habilitado actualmente }
            </p>
          </div>
          <button appBtn icon="dollar" [disabled]="e.deudaTotal <= 0" (click)="abrirAbono()">Registrar abono</button>
        </div>

        <div class="grid gap-4 sm:grid-cols-3">
          <app-stat-card label="Deuda total" [value]="(e.deudaTotal | number: '1.2-2')" />
          <app-stat-card label="Crédito disponible" tone="success" [value]="(e.disponible | number: '1.2-2')" />
          <app-stat-card label="Límite de crédito" tone="muted" [value]="(e.limiteCredito | number: '1.2-2')" />
        </div>

        <app-card class="block overflow-hidden">
          <p class="border-b border-border p-4 text-sm font-semibold text-ink">Ventas a crédito</p>
          @if (e.cuentas.length === 0) {
            <p class="p-4 text-sm text-muted">Este cliente no tiene ventas a crédito.</p>
          } @else {
            <div class="overflow-x-auto">
              <table class="dt">
                <thead><tr><th>Venta</th><th>Fecha</th><th class="text-right">Original</th><th class="text-right">Saldo</th><th>Estado</th></tr></thead>
                <tbody>
                  @for (c of e.cuentas; track c.id) {
                    <tr>
                      <td><a [routerLink]="['/ventas', c.ventaId]" class="font-mono text-sm text-accent hover:underline">{{ c.numeroVenta }}</a></td>
                      <td class="text-sm text-muted">{{ c.fecha | date: 'dd/MM/yyyy HH:mm' }}</td>
                      <td class="text-right text-sm">{{ c.montoOriginal | number: '1.2-2' }}</td>
                      <td class="text-right text-sm font-semibold text-ink">{{ c.saldo | number: '1.2-2' }}</td>
                      <td><app-badge [tone]="tono(c.estado)">{{ etiqueta(c.estado) }}</app-badge></td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </app-card>

        <app-card class="block overflow-hidden">
          <p class="border-b border-border p-4 text-sm font-semibold text-ink">Abonos</p>
          @if (e.abonos.length === 0) {
            <p class="p-4 text-sm text-muted">Aún no hay abonos.</p>
          } @else {
            <div class="overflow-x-auto">
              <table class="dt">
                <thead><tr><th>Número</th><th>Fecha</th><th>Método</th><th class="text-right">Monto</th><th class="text-right">Comprobante</th></tr></thead>
                <tbody>
                  @for (a of e.abonos; track a.id) {
                    <tr>
                      <td class="font-mono text-sm">{{ a.numeroAbono }}</td>
                      <td class="text-sm text-muted">{{ a.fecha | date: 'dd/MM/yyyy HH:mm' }}</td>
                      <td class="text-sm">{{ a.metodoPago }}</td>
                      <td class="text-right text-sm font-semibold text-ink">{{ a.monto | number: '1.2-2' }}</td>
                      <td class="text-right"><button appBtn variant="secondary" class="!h-8 !px-3 !text-xs" (click)="comprobante.set(a)">Ver</button></td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </app-card>
      } @else if (!error()) {
        <p class="text-sm text-muted">Cargando…</p>
      }
    </div>

    @if (abonoOpen()) {
      <app-modal title="Registrar abono" description="Se aplica a las deudas más antiguas primero y entra a tu turno de caja abierto." (closed)="abonoOpen.set(false)">
        <form [formGroup]="form" (ngSubmit)="guardar()" id="abono-form">
          <div class="space-y-4 p-5">
            @if (modalError()) { <p class="alert-error" role="alert">{{ modalError() }}</p> }
            <app-field label="Monto" [control]="form.controls.monto" required [helper]="'Deuda pendiente: ' + (estado()?.deudaTotal ?? 0).toFixed(2)">
              <input type="number" step="0.01" min="0.01" class="field-input" formControlName="monto" />
            </app-field>
            <app-field label="Método de pago" [control]="form.controls.metodoPagoId" required>
              <select class="field-input" formControlName="metodoPagoId">
                <option [ngValue]="null" disabled>Selecciona un método</option>
                @for (m of metodos(); track m.id) { <option [ngValue]="m.id">{{ m.nombre }}</option> }
              </select>
            </app-field>
            <app-field label="Referencia" [control]="form.controls.referencia" optional>
              <input class="field-input" formControlName="referencia" maxlength="100" />
            </app-field>
          </div>
        </form>
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="abonoOpen.set(false)">Cancelar</button>
          <button appBtn type="submit" form="abono-form" [disabled]="saving()">Registrar abono</button>
        </ng-container>
      </app-modal>
    }

    @if (comprobante(); as a) {
      <app-modal title="Comprobante de abono" (closed)="comprobante.set(null)">
        <div class="space-y-3 p-5 text-sm">
          <p class="text-center text-base font-bold text-ink">{{ a.numeroAbono }}</p>
          <dl class="space-y-1">
            <div class="flex justify-between"><dt class="text-muted">Cliente</dt><dd class="text-ink">{{ a.clienteNombre }}</dd></div>
            <div class="flex justify-between"><dt class="text-muted">Fecha</dt><dd>{{ a.fecha | date: 'dd/MM/yyyy HH:mm' }}</dd></div>
            <div class="flex justify-between"><dt class="text-muted">Cobrado por</dt><dd>{{ a.usuario }}</dd></div>
            <div class="flex justify-between"><dt class="text-muted">Método</dt><dd>{{ a.metodoPago }}</dd></div>
            @if (a.referencia) { <div class="flex justify-between"><dt class="text-muted">Referencia</dt><dd>{{ a.referencia }}</dd></div> }
            <div class="flex justify-between border-t border-border pt-2 text-base font-bold text-ink"><dt>Monto abonado</dt><dd>{{ a.monto | number: '1.2-2' }}</dd></div>
          </dl>
          <p class="pt-2 text-xs font-semibold text-muted">Aplicado a</p>
          <ul class="divide-y divide-border">
            @for (ap of a.aplicaciones; track ap.cuentaId) {
              <li class="flex justify-between py-1"><span class="font-mono">{{ ap.numeroVenta }}</span><span>{{ ap.monto | number: '1.2-2' }}</span></li>
            }
          </ul>
        </div>
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="imprimir()">Imprimir</button>
          <button appBtn type="button" (click)="comprobante.set(null)">Cerrar</button>
        </ng-container>
      </app-modal>
    }
  `,
})
export class EstadoCuentaPage {
  private readonly service = inject(CreditosService);
  private readonly metodosService = inject(MetodosPagoService);
  private readonly fb = inject(FormBuilder).nonNullable;
  private readonly id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));

  protected readonly estado = signal<EstadoCuentaCliente | null>(null);
  protected readonly metodos = signal<MetodoPago[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly modalError = signal<string | null>(null);
  protected readonly abonoOpen = signal(false);
  protected readonly saving = signal(false);
  protected readonly comprobante = signal<Abono | null>(null);

  protected readonly form = this.fb.group({
    monto: [0, [Validators.required, Validators.min(0.01)]],
    metodoPagoId: [null as number | null, Validators.required],
    referencia: ['', Validators.maxLength(100)],
  });

  constructor() {
    this.cargar();
    // El crédito no se puede pagar con crédito: solo los métodos que mueven dinero.
    this.metodosService.paraCobro().subscribe({
      next: (m) => this.metodos.set(m.filter((x) => x.nombre !== 'CREDITO')),
    });
  }

  private cargar(): void {
    this.service.estadoCuenta(this.id).subscribe({
      next: (e) => {
        this.error.set(null);
        this.estado.set(e);
      },
      error: (e) => this.error.set(errorMessage(e, 'No se pudo cargar el estado de cuenta')),
    });
  }

  protected tono(e: EstadoCuenta): 'warning' | 'success' | 'neutral' {
    return e === 'PENDIENTE' ? 'warning' : e === 'PAGADA' ? 'success' : 'neutral';
  }

  protected etiqueta(e: EstadoCuenta): string {
    return e === 'PENDIENTE' ? 'Pendiente' : e === 'PAGADA' ? 'Pagada' : 'Anulada';
  }

  protected abrirAbono(): void {
    this.modalError.set(null);
    const efectivo = this.metodos().find((m) => m.nombre === 'EFECTIVO')?.id ?? null;
    this.form.reset({ monto: this.estado()?.deudaTotal ?? 0, metodoPagoId: efectivo, referencia: '' });
    this.abonoOpen.set(true);
  }

  protected guardar(): void {
    if (this.form.invalid) return this.form.markAllAsTouched();
    const v = this.form.getRawValue();
    this.saving.set(true);
    this.modalError.set(null);
    this.service
      .abonar(this.id, { metodoPagoId: v.metodoPagoId!, monto: Number(v.monto), ...(v.referencia.trim() ? { referencia: v.referencia.trim() } : {}) })
      .subscribe({
        next: (abono) => {
          this.saving.set(false);
          this.abonoOpen.set(false);
          this.comprobante.set(abono);
          this.cargar();
        },
        error: (e) => {
          this.saving.set(false);
          this.modalError.set(errorMessage(e));
        },
      });
  }

  protected imprimir(): void {
    globalThis.print();
  }
}
