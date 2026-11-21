import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL, toParams } from '../../../core/api/api-url';
import { Page } from '../../../core/api/page.model';
import { withToast } from '../../../core/ui/toast';
import { Recargo, RecargoFiltro, RecargoRequest } from './recargo.model';

@Injectable({ providedIn: 'root' })
export class RecargosService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/recargos-escasez`;

  listar(filtro: RecargoFiltro): Observable<Page<Recargo>> {
    return this.http.get<Page<Recargo>>(this.url, { params: toParams(filtro) });
  }

  crear(body: RecargoRequest): Observable<Recargo> {
    return this.http.post<Recargo>(this.url, body, withToast('Recargo activado: ya se aplica en el punto de venta'));
  }

  desactivar(id: number): Observable<Recargo> {
    return this.http.patch<Recargo>(`${this.url}/${id}/desactivar`, null, withToast('Recargo desactivado'));
  }
}
