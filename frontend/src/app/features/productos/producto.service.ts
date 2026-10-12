import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.development';
import { ProductoRequest, ProductoResponse, ProductoStats } from './producto.model';
import { PageResponse } from '../../shared/models/page.model';

@Injectable({
  providedIn: 'root',
})
export class ProductoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/productos`;

  listar(
    page = 0,
    size = 10,
    nombre = '',
    categoriaId?: number | null,
    estado?: boolean | null
  ): Observable<PageResponse<ProductoResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (nombre && nombre.trim()) {
      params = params.set('nombre', nombre.trim());
    }
    if (categoriaId != null) {
      params = params.set('categoriaId', categoriaId.toString());
    }
    if (estado != null) {
      params = params.set('estado', estado.toString());
    }

    return this.http.get<PageResponse<ProductoResponse>>(this.apiUrl, { params });
  }

  obtenerEstadisticas(): Observable<ProductoStats> {
    return this.http.get<ProductoStats>(`${this.apiUrl}/stats`);
  }

  obtenerPorId(id: number): Observable<ProductoResponse> {
    return this.http.get<ProductoResponse>(`${this.apiUrl}/${id}`);
  }

  crear(request: ProductoRequest): Observable<ProductoResponse> {
    return this.http.post<ProductoResponse>(this.apiUrl, request);
  }

  actualizar(id: number, request: ProductoRequest): Observable<ProductoResponse> {
    return this.http.put<ProductoResponse>(`${this.apiUrl}/${id}`, request);
  }

  cambiarEstado(id: number, estado: boolean): Observable<ProductoResponse> {
    const params = new HttpParams().set('estado', estado.toString());
    return this.http.patch<ProductoResponse>(`${this.apiUrl}/${id}/estado`, null, { params });
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
