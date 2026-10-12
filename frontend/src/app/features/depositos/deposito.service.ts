import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.development';
import { Deposito, DepositoRequest, DepositoStats } from './deposito.model';
import { PageResponse } from '../../shared/models/page.model';

@Injectable({
  providedIn: 'root',
})
export class DepositoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/depositos`;

  listar(page = 0, size = 10, nombre = ''): Observable<PageResponse<Deposito>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (nombre.trim()) {
      params = params.set('nombre', nombre.trim());
    }

    return this.http.get<PageResponse<Deposito>>(this.apiUrl, { params });
  }

  obtenerEstadisticas(): Observable<DepositoStats> {
    return this.http.get<DepositoStats>(`${this.apiUrl}/stats`);
  }

  obtenerPorId(id: number): Observable<Deposito> {
    return this.http.get<Deposito>(`${this.apiUrl}/${id}`);
  }

  crear(request: DepositoRequest): Observable<Deposito> {
    return this.http.post<Deposito>(this.apiUrl, request);
  }

  actualizar(id: number, request: DepositoRequest): Observable<Deposito> {
    return this.http.put<Deposito>(`${this.apiUrl}/${id}`, request);
  }

  cambiarEstado(id: number, estado: boolean): Observable<Deposito> {
    const params = new HttpParams().set('estado', estado.toString());
    return this.http.patch<Deposito>(`${this.apiUrl}/${id}/estado`, null, { params });
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
