import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CrudService } from '../../../core/api/crud.service';
import { Caja, CajaFilters, CajaRequest } from './caja.model';

@Injectable({ providedIn: 'root' })
export class CajasService extends CrudService<Caja, CajaRequest, CajaFilters> {
  constructor() {
    super('cajas');
  }

  activar(id: number): Observable<Caja> {
    return this.http.patch<Caja>(`${this.baseUrl}/${id}/activar`, null);
  }

  desactivar(id: number): Observable<Caja> {
    return this.http.patch<Caja>(`${this.baseUrl}/${id}/desactivar`, null);
  }
}
