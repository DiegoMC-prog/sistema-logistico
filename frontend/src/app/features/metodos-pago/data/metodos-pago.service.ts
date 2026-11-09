import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { CrudService } from '../../../core/api/crud.service';
import { MetodoPago, MetodoPagoFilters, MetodoPagoRequest, esUtilizable } from './metodo-pago.model';

@Injectable({ providedIn: 'root' })
export class MetodosPagoService extends CrudService<MetodoPago, MetodoPagoRequest, MetodoPagoFilters> {
  constructor() {
    super('metodos-pago');
  }

  activar(id: number): Observable<MetodoPago> {
    return this.http.patch<MetodoPago>(`${this.baseUrl}/${id}/activar`, null);
  }

  desactivar(id: number): Observable<MetodoPago> {
    return this.http.patch<MetodoPago>(`${this.baseUrl}/${id}/desactivar`, null);
  }

  /** Métodos activos y aptos para cobro (selector de pagos y reembolsos). */
  paraCobro(): Observable<MetodoPago[]> {
    return this.list({ size: 100, estado: 'ACTIVO' }).pipe(map((p) => p.content.filter(esUtilizable)));
  }
}
