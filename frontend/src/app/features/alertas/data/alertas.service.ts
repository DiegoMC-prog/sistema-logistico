import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { API_URL, toParams } from '../../../core/api/api-url';
import { Page } from '../../../core/api/page.model';
import { withToast } from '../../../core/ui/toast';
import { Alerta, AlertaFiltro, ConteoAlertas, ResultadoGeneracion } from './alerta.model';

@Injectable({ providedIn: 'root' })
export class AlertasService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/alertas`;

  /** Último conteo de alertas sin leer: lo muestra la campana y lo refresca la pantalla de alertas. */
  readonly conteo = signal<ConteoAlertas | null>(null);

  listar(filtro: AlertaFiltro): Observable<Page<Alerta>> {
    return this.http.get<Page<Alerta>>(this.url, { params: toParams(filtro) });
  }

  refrescarConteo(): Observable<ConteoAlertas> {
    return this.http.get<ConteoAlertas>(`${this.url}/conteo`).pipe(tap((c) => this.conteo.set(c)));
  }

  marcarLeida(id: number): Observable<Alerta> {
    return this.http.patch<Alerta>(`${this.url}/${id}/leida`, null, withToast(null));
  }

  marcarTodasLeidas(): Observable<{ marcadas: number }> {
    return this.http.patch<{ marcadas: number }>(`${this.url}/leer-todas`, null, withToast('Alertas marcadas como leídas'));
  }

  /** Revisa lotes y stock en el momento, sin esperar a la tarea diaria. */
  generar(): Observable<ResultadoGeneracion> {
    return this.http.post<ResultadoGeneracion>(`${this.url}/generar`, null, withToast(null));
  }
}
