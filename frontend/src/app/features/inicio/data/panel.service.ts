import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../../core/api/api-url';
import { Panel } from './panel.model';

@Injectable({ providedIn: 'root' })
export class PanelService {
  private readonly http = inject(HttpClient);

  /** Cualquier usuario autenticado puede pedirlo: el servidor solo incluye los bloques permitidos. */
  get(): Observable<Panel> {
    return this.http.get<Panel>(`${API_URL}/reportes/panel`);
  }
}
