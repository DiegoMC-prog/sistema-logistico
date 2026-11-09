import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../../core/api/api-url';
import { Presentacion } from './producto.model';

export interface PresentacionRequest {
  unidadMedidaId: number;
  codigoBarras: string;
  factorConversion: number;
  esUnidadBase: boolean;
}

/** PUT /presentaciones/{id} no acepta esUnidadBase. */
export type EditarPresentacionRequest = Omit<PresentacionRequest, 'esUnidadBase'>;

@Injectable({ providedIn: 'root' })
export class PresentacionesService {
  private readonly http = inject(HttpClient);

  list(productoId: number): Observable<Presentacion[]> {
    return this.http.get<Presentacion[]>(`${API_URL}/productos/${productoId}/presentaciones`);
  }

  create(productoId: number, body: PresentacionRequest): Observable<Presentacion> {
    return this.http.post<Presentacion>(`${API_URL}/productos/${productoId}/presentaciones`, body);
  }

  update(id: number, body: EditarPresentacionRequest): Observable<Presentacion> {
    return this.http.put<Presentacion>(`${API_URL}/presentaciones/${id}`, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/presentaciones/${id}`);
  }

  porCodigoBarras(codigo: string): Observable<Presentacion> {
    return this.http.get<Presentacion>(`${API_URL}/presentaciones/codigo-barras/${encodeURIComponent(codigo)}`);
  }
}
