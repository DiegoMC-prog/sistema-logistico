import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL, toParams } from '../../../core/api/api-url';
import { Page } from '../../../core/api/page.model';
import { withToast } from '../../../core/ui/toast';
import { Venta, VentaFilters, VentaRequest, VentaResumen } from './venta.model';

@Injectable({ providedIn: 'root' })
export class VentasService {
  private readonly http = inject(HttpClient);

  list(filters: VentaFilters = {}): Observable<Page<VentaResumen>> {
    return this.http.get<Page<VentaResumen>>(`${API_URL}/ventas`, { params: toParams(filters) });
  }

  get(id: number): Observable<Venta> {
    return this.http.get<Venta>(`${API_URL}/ventas/${id}`);
  }

  /** 201 al crear, 200 si es un reintento con la misma Idempotency-Key: ambos son éxito. */
  crear(body: VentaRequest, idempotencyKey: string): Observable<Venta> {
    return this.http.post<Venta>(`${API_URL}/ventas`, body, {
      headers: new HttpHeaders({ 'Idempotency-Key': idempotencyKey }),
      ...withToast('Venta registrada correctamente'),
    });
  }

  anular(id: number, motivo: string): Observable<Venta> {
    return this.http.post<Venta>(`${API_URL}/ventas/${id}/anular`, { motivo }, withToast('Venta anulada'));
  }
}

/** Clave de idempotencia: 8 a 64 caracteres (letras, números, `.`, `-`, `_`). */
export function nuevaClave(): string {
  return globalThis.crypto?.randomUUID?.() ?? `k-${Date.now()}-${Math.random().toString(36).slice(2, 12)}`;
}
