import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CrudService } from '../../../core/api/crud.service';
import { Proveedor, ProveedorFilters, ProveedorRequest } from './proveedor.model';

@Injectable({ providedIn: 'root' })
export class ProveedoresService extends CrudService<Proveedor, ProveedorRequest, ProveedorFilters> {
  constructor() {
    super('proveedores');
  }

  activar(id: number): Observable<Proveedor> {
    return this.http.patch<Proveedor>(`${this.baseUrl}/${id}/activar`, null);
  }

  desactivar(id: number): Observable<Proveedor> {
    return this.http.patch<Proveedor>(`${this.baseUrl}/${id}/desactivar`, null);
  }
}
