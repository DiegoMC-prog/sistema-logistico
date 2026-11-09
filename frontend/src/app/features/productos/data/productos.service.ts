import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CrudService } from '../../../core/api/crud.service';
import { Producto, ProductoFilters, ProductoRequest } from './producto.model';

@Injectable({ providedIn: 'root' })
export class ProductosService extends CrudService<Producto, ProductoRequest, ProductoFilters> {
  constructor() {
    super('productos');
  }

  activar(id: number): Observable<Producto> {
    return this.http.patch<Producto>(`${this.baseUrl}/${id}/activar`, null);
  }

  desactivar(id: number): Observable<Producto> {
    return this.http.patch<Producto>(`${this.baseUrl}/${id}/desactivar`, null);
  }
}
