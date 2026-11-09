import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../../core/api/api-url';
import { withToast } from '../../../core/ui/toast';
import { Abono, AbonoRequest, Deudor, EstadoCuentaCliente } from './credito.model';

@Injectable({ providedIn: 'root' })
export class CreditosService {
  private readonly http = inject(HttpClient);

  deudores(): Observable<Deudor[]> {
    return this.http.get<Deudor[]>(`${API_URL}/creditos/deudores`);
  }

  estadoCuenta(clienteId: number): Observable<EstadoCuentaCliente> {
    return this.http.get<EstadoCuentaCliente>(`${API_URL}/clientes/${clienteId}/estado-cuenta`);
  }

  /** Exige un turno de caja abierto: el abono entra a la caja del turno. */
  abonar(clienteId: number, body: AbonoRequest): Observable<Abono> {
    return this.http.post<Abono>(`${API_URL}/clientes/${clienteId}/abonos`, body, withToast('Abono registrado'));
  }
}
