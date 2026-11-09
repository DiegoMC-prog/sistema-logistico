import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL, toParams } from '../../../core/api/api-url';
import { Page } from '../../../core/api/page.model';
import {
  AjusteRequest, EntradaRequest, MovimientoFilters, MovimientoInv, SalidaRequest, Stock, StockFilters, TrasladoRequest,
} from './inventario.model';

@Injectable({ providedIn: 'root' })
export class InventarioService {
  private readonly http = inject(HttpClient);

  stock(filters: StockFilters = {}): Observable<Page<Stock>> {
    return this.http.get<Page<Stock>>(`${API_URL}/inventario`, { params: toParams(filters) });
  }

  movimientos(filters: MovimientoFilters = {}): Observable<Page<MovimientoInv>> {
    return this.http.get<Page<MovimientoInv>>(`${API_URL}/inventario/movimientos`, { params: toParams(filters) });
  }

  movimiento(id: number): Observable<MovimientoInv> {
    return this.http.get<MovimientoInv>(`${API_URL}/inventario/movimientos/${id}`);
  }

  entrada(body: EntradaRequest): Observable<MovimientoInv> {
    return this.http.post<MovimientoInv>(`${API_URL}/inventario/entradas`, body);
  }

  salida(body: SalidaRequest): Observable<MovimientoInv> {
    return this.http.post<MovimientoInv>(`${API_URL}/inventario/salidas`, body);
  }

  traslado(body: TrasladoRequest): Observable<MovimientoInv> {
    return this.http.post<MovimientoInv>(`${API_URL}/inventario/traslados`, body);
  }

  ajuste(body: AjusteRequest): Observable<MovimientoInv> {
    return this.http.post<MovimientoInv>(`${API_URL}/inventario/ajustes`, body);
  }
}
