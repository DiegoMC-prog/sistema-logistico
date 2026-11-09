import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { errorMessage } from '../../../core/api/api-error';
import { Page } from '../../../core/api/page.model';
import { AuthService } from '../../../core/auth/auth.service';
import { PERMISOS } from '../../../core/auth/permisos';
import { Btn } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { Modal } from '../../../shared/ui/modal';
import { CambioModal } from '../../devoluciones/components/cambio-modal';
import { DevolucionModal } from '../../devoluciones/components/devolucion-modal';
import { Devolucion } from '../../devoluciones/data/devolucion.model';
import { DevolucionesService } from '../../devoluciones/data/devoluciones.service';
import { NotaVenta } from '../components/nota-venta';
import { Venta } from '../data/venta.model';
import { VentasService } from '../data/ventas.service';

@Component({
  selector: 'app-venta-detalle-page',
  imports: [DatePipe, DecimalPipe, FormsModule, RouterLink, Btn, Card, Modal, NotaVenta, DevolucionModal, CambioModal],
  template: `
    <div class="space-y-6">
      <a routerLink="/ventas" class="text-sm font-medium text-accent hover:underline">← Volver a ventas</a>

      @if (error()) { <p class="alert-error" role="alert">{{ error() }}</p> }

      @if (venta(); as v) {
        <div class="flex flex-wrap justify-end gap-3">
          <button appBtn variant="secondary" icon="receipt" (click)="imprimir()">Imprimir</button>
          @if (v.estado === 'COMPLETADA') {
            @if (puedeDevolver) {
              <button appBtn variant="secondary" icon="undo" (click)="devolucionOpen.set(true)">Devolver</button>
            }
            @if (puedeDevolver && puedeVender) {
              <button appBtn variant="secondary" icon="cart" (click)="cambioOpen.set(true)">Cambiar producto</button>
            }
            @if (puedeAnular) {
              <button appBtn variant="danger" icon="close" (click)="abrirAnular()">Anular</button>
            }
          }
        </div>

        <app-card class="block p-5"><app-nota-venta [venta]="v" /></app-card>

        <app-card class="block p-5">
          <p class="text-sm font-semibold text-ink">Devoluciones de esta venta</p>
          @if (devoluciones().length === 0) {
            <p class="mt-2 text-sm text-muted">Sin devoluciones.</p>
          } @else {
            <ul class="mt-3 divide-y divide-border text-sm">
              @for (d of devoluciones(); track d.id) {
                <li class="flex flex-wrap justify-between gap-2 py-2">
                  <span><span class="font-mono text-xs font-semibold text-ink">{{ d.numeroDevolucion }}</span>
                    <span class="ml-2 text-muted">{{ d.fecha | date: 'dd/MM/yyyy HH:mm' }} · {{ d.motivo }}</span></span>
                  <span class="font-semibold">{{ d.montoReembolsado | number: '1.2-2' }}</span>
                </li>
              }
            </ul>
          }
        </app-card>
      } @else if (!error()) {
        <p class="text-sm text-muted">Cargando…</p>
      }
    </div>

    @if (anularOpen()) {
      <app-modal title="Anular venta" description="Solo es posible mientras el turno de la venta siga abierto; después usa una devolución." (closed)="anularOpen.set(false)">
        <div class="space-y-4 p-5">
          @if (anularError()) { <p class="alert-error" role="alert">{{ anularError() }}</p> }
          <label class="block text-sm font-medium text-ink">Motivo
            <input class="field-input mt-1" maxlength="200" [ngModel]="motivo()" (ngModelChange)="motivo.set($event)" />
          </label>
        </div>
        <ng-container modal-footer>
          <button appBtn variant="secondary" type="button" (click)="anularOpen.set(false)">Cancelar</button>
          <button appBtn variant="danger" type="button" [disabled]="anulando()" (click)="anular()">Anular venta</button>
        </ng-container>
      </app-modal>
    }

    @if (devolucionOpen() && venta(); as v) {
      <app-devolucion-modal [venta]="v" (closed)="cerrarDialogos()" (done)="recargar()" />
    }
    @if (cambioOpen() && venta(); as v) {
      <app-cambio-modal [venta]="v" (closed)="cerrarDialogos()" (done)="recargar()" />
    }
  `,
})
export class VentaDetallePage {
  private readonly ventas = inject(VentasService);
  private readonly devolucionesService = inject(DevolucionesService);
  private readonly auth = inject(AuthService);
  private readonly id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));

  protected readonly puedeAnular = this.auth.hasPermission(PERMISOS.ventaAnular);
  protected readonly puedeDevolver = this.auth.hasPermission(PERMISOS.ventaDevolver);
  protected readonly puedeVender = this.auth.hasPermission(PERMISOS.ventaCrear);

  protected readonly venta = signal<Venta | null>(null);
  protected readonly devoluciones = signal<Devolucion[]>([]);
  protected readonly error = signal<string | null>(null);

  protected readonly anularOpen = signal(false);
  protected readonly anulando = signal(false);
  protected readonly anularError = signal<string | null>(null);
  protected readonly motivo = signal('');
  protected readonly devolucionOpen = signal(false);
  protected readonly cambioOpen = signal(false);

  constructor() {
    this.cargar();
  }

  private cargar(): void {
    this.ventas.get(this.id).subscribe({
      next: (v) => {
        this.error.set(null);
        this.venta.set(v);
      },
      error: (e) => this.error.set(errorMessage(e, 'No se pudo cargar la venta')),
    });
    this.devolucionesService.porVenta(this.id).subscribe({
      next: (r) => this.devoluciones.set(Array.isArray(r) ? r : (r as Page<Devolucion>).content),
      error: () => this.devoluciones.set([]), // sin permiso de ver devoluciones: no es un error de la página
    });
  }

  /** Tras devolver o cambiar, la nota y la lista cambian; el modal sigue abierto mostrando el resultado. */
  protected recargar(): void {
    this.cargar();
  }

  protected cerrarDialogos(): void {
    this.devolucionOpen.set(false);
    this.cambioOpen.set(false);
  }

  protected abrirAnular(): void {
    this.motivo.set('');
    this.anularError.set(null);
    this.anularOpen.set(true);
  }

  protected anular(): void {
    if (!this.motivo().trim()) return void this.anularError.set('Indica el motivo de la anulación');
    this.anulando.set(true);
    this.anularError.set(null);
    this.ventas.anular(this.id, this.motivo().trim()).subscribe({
      next: (v) => {
        this.anulando.set(false);
        this.anularOpen.set(false);
        this.venta.set(v);
      },
      error: (e) => {
        this.anulando.set(false);
        this.anularError.set(errorMessage(e, 'No se pudo anular la venta'));
      },
    });
  }

  protected imprimir(): void {
    globalThis.print();
  }
}
