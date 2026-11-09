import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL, toParams } from '../../../core/api/api-url';
import { Page } from '../../../core/api/page.model';
import { withToast } from '../../../core/ui/toast';
import { CambioRequest, CambioResponse, Devolucion, DevolucionFilters, DevolucionRequest } from './devolucion.model';

@Injectable({ providedIn: 'root' })
export class DevolucionesService {
  private readonly http = inject(HttpClient);

  list(filters: DevolucionFilters = {}): Observable<Page<Devolucion>> {
    return this.http.get<Page<Devolucion>>(`${API_URL}/devoluciones`, { params: toParams(filters) });
  }

  get(id: number): Observable<Devolucion> {
    return this.http.get<Devolucion>(`${API_URL}/devoluciones/${id}`);
  }

  porVenta(ventaId: number): Observable<Devolucion[] | Page<Devolucion>> {
    return this.http.get<Devolucion[] | Page<Devolucion>>(`${API_URL}/ventas/${ventaId}/devoluciones`);
  }

  crear(ventaId: number, body: DevolucionRequest, idempotencyKey: string): Observable<Devolucion> {
    return this.http.post<Devolucion>(`${API_URL}/ventas/${ventaId}/devoluciones`, body, {
      headers: new HttpHeaders({ 'Idempotency-Key': idempotencyKey }),
      ...withToast('Devolución registrada'),
    });
  }

  cambio(ventaId: number, body: CambioRequest, idempotencyKey: string): Observable<CambioResponse> {
    return this.http.post<CambioResponse>(`${API_URL}/ventas/${ventaId}/cambios`, body, {
      headers: new HttpHeaders({ 'Idempotency-Key': idempotencyKey }),
      ...withToast('Cambio registrado'),
    });
  }
}
