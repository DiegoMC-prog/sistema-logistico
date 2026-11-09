import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL, toParams } from '../../../core/api/api-url';
import { Page } from '../../../core/api/page.model';
import { Precio, PrecioAplicable, PrecioFilters, PrecioRequest } from './precio.model';

@Injectable({ providedIn: 'root' })
export class PreciosService {
  private readonly http = inject(HttpClient);

  list(presentacionId: number, filters: PrecioFilters = {}): Observable<Page<Precio>> {
    return this.http.get<Page<Precio>>(`${API_URL}/presentaciones/${presentacionId}/precios`, { params: toParams(filters) });
  }

  create(presentacionId: number, body: PrecioRequest): Observable<Precio> {
    return this.http.post<Precio>(`${API_URL}/presentaciones/${presentacionId}/precios`, body);
  }

  /** Solo cantidad mínima y precio: el tipo de cliente y la presentación no se editan. */
  update(id: number, body: Pick<PrecioRequest, 'cantidadMinima' | 'precio'>): Observable<Precio> {
    return this.http.put<Precio>(`${API_URL}/precios/${id}`, body);
  }

  activar(id: number): Observable<Precio> {
    return this.http.patch<Precio>(`${API_URL}/precios/${id}/activar`, null);
  }

  desactivar(id: number): Observable<Precio> {
    return this.http.patch<Precio>(`${API_URL}/precios/${id}/desactivar`, null);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/precios/${id}`);
  }

  aplicable(presentacionId: number, cantidad: number, tipoClienteId?: number | null): Observable<PrecioAplicable> {
    return this.http.get<PrecioAplicable>(`${API_URL}/presentaciones/${presentacionId}/precio-aplicable`, {
      params: toParams({ cantidad, tipoClienteId: tipoClienteId ?? undefined }),
    });
  }
}
