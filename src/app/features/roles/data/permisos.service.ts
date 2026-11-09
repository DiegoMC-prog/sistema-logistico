import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../../core/api/api-url';
import { Permiso } from './roles.service';

@Injectable({ providedIn: 'root' })
export class PermisosService {
  private readonly http = inject(HttpClient);

  /** El backend devuelve la lista completa, sin paginar. */
  list(): Observable<Permiso[]> {
    return this.http.get<Permiso[]>(`${API_URL}/permisos`);
  }
}
