import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CrudService } from '../../../core/api/crud.service';
import { Cliente, ClienteFilters, ClienteRequest } from './cliente.model';

@Injectable({ providedIn: 'root' })
export class ClientesService extends CrudService<Cliente, ClienteRequest, ClienteFilters> {
  constructor() {
    super('clientes');
  }

  activar(id: number): Observable<Cliente> {
    return this.http.patch<Cliente>(`${this.baseUrl}/${id}/activar`, null);
  }

  desactivar(id: number): Observable<Cliente> {
    return this.http.patch<Cliente>(`${this.baseUrl}/${id}/desactivar`, null);
  }
}
