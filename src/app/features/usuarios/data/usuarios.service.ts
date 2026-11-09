import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { withToast } from '../../../core/ui/toast';
import { CrudService } from '../../../core/api/crud.service';
import { EditarUsuarioRequest, Usuario, UsuarioFilters, UsuarioRequest } from './usuario.model';

/** `update` acepta solo nombre y email, que es lo que edita el backend (PUT /usuarios/{id}). */
@Injectable({ providedIn: 'root' })
export class UsuariosService extends CrudService<Usuario, UsuarioRequest | EditarUsuarioRequest, UsuarioFilters> {
  constructor() {
    super('usuarios');
  }

  activar(id: number): Observable<Usuario> {
    return this.http.patch<Usuario>(`${this.baseUrl}/${id}/activar`, null);
  }

  desactivar(id: number): Observable<Usuario> {
    return this.http.patch<Usuario>(`${this.baseUrl}/${id}/desactivar`, null);
  }

  asignarRoles(id: number, rolIds: number[]): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.baseUrl}/${id}/roles`, { rolIds });
  }

  restablecerPassword(id: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${id}/restablecer-password`, null, withToast('Contraseña restablecida'));
  }
}
