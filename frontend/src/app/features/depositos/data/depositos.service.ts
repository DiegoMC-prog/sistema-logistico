import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CrudService } from '../../../core/api/crud.service';
import { Deposito, DepositoFilters, DepositoRequest } from './deposito.model';

@Injectable({ providedIn: 'root' })
export class DepositosService extends CrudService<Deposito, DepositoRequest, DepositoFilters> {
  constructor() {
    super('depositos');
  }

  activar(id: number): Observable<Deposito> {
    return this.http.patch<Deposito>(`${this.baseUrl}/${id}/activar`, null);
  }

  desactivar(id: number): Observable<Deposito> {
    return this.http.patch<Deposito>(`${this.baseUrl}/${id}/desactivar`, null);
  }
}
