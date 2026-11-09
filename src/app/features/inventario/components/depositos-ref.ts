import { Injectable, inject, signal } from '@angular/core';
import { Deposito } from '../../depositos/data/deposito.model';
import { DepositosService } from '../../depositos/data/depositos.service';

/** Lista de depósitos activos compartida por los formularios de inventario. */
@Injectable({ providedIn: 'root' })
export class DepositosRef {
  readonly depositos = signal<Deposito[]>([]);

  constructor() {
    inject(DepositosService)
      .list({ size: 100, estado: 'ACTIVO' })
      .subscribe({ next: (p) => this.depositos.set(p.content) });
  }
}
