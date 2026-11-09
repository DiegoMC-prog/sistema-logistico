import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CrudService } from '../../../core/api/crud.service';
import { TipoCliente, TipoClienteFilters, TipoClienteRequest } from './tipo-cliente.model';

@Injectable({ providedIn: 'root' })
export class TiposClienteService extends CrudService<TipoCliente, TipoClienteRequest, TipoClienteFilters> {
  constructor() {
    super('tipos-cliente');
  }

  activar(id: number): Observable<TipoCliente> {
    return this.http.patch<TipoCliente>(`${this.baseUrl}/${id}/activar`, null);
  }

  desactivar(id: number): Observable<TipoCliente> {
    return this.http.patch<TipoCliente>(`${this.baseUrl}/${id}/desactivar`, null);
  }
}
