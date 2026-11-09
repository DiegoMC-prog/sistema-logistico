import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL, toParams } from '../../../core/api/api-url';
import { Page } from '../../../core/api/page.model';
import { withToast } from '../../../core/ui/toast';
import { MovimientoCaja, MovimientoRequest, Turno, TurnoFilters, TurnoResumen } from './turno.model';

@Injectable({ providedIn: 'root' })
export class TurnosService {
  private readonly http = inject(HttpClient);

  list(filters: TurnoFilters = {}): Observable<Page<TurnoResumen>> {
    return this.http.get<Page<TurnoResumen>>(`${API_URL}/turnos`, { params: toParams(filters) });
  }

  get(id: number): Observable<Turno> {
    return this.http.get<Turno>(`${API_URL}/turnos/${id}`);
  }

  abrir(cajaId: number, montoApertura: number): Observable<Turno> {
    return this.http.post<Turno>(`${API_URL}/cajas/${cajaId}/turnos`, { montoApertura }, withToast('Turno abierto'));
  }

  /** 404 = sin turno abierto; 403 = la caja la usa otro cajero. */
  turnoActual(cajaId: number): Observable<Turno> {
    return this.http.get<Turno>(`${API_URL}/cajas/${cajaId}/turno-actual`);
  }

  cerrar(id: number, montoDeclarado: number): Observable<Turno> {
    return this.http.post<Turno>(`${API_URL}/turnos/${id}/cerrar`, { montoDeclarado }, withToast('Turno cerrado'));
  }

  movimientos(id: number): Observable<MovimientoCaja[] | Page<MovimientoCaja>> {
    return this.http.get<MovimientoCaja[] | Page<MovimientoCaja>>(`${API_URL}/turnos/${id}/movimientos`);
  }

  registrarMovimiento(id: number, body: MovimientoRequest): Observable<MovimientoCaja> {
    return this.http.post<MovimientoCaja>(`${API_URL}/turnos/${id}/movimientos`, body, withToast('Movimiento de caja registrado'));
  }
}
