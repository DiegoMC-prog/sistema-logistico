import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CrudService } from '../../../core/api/crud.service';

export interface Rol {
  id: number;
  nombre: string;
}

export interface RolRequest {
  nombre: string;
}

export interface Permiso {
  id: number;
  /** `MODULO_ACCION`. */
  nombre: string;
  modulo: string;
  accion: string;
  /** VER, CREAR, EDITAR y ELIMINAR van en columnas fijas de la matriz; el resto en «Otras». */
  estandar: boolean;
  descripcion: string | null;
}

export interface RolDetalle extends Rol {
  permisos: Permiso[];
}

@Injectable({ providedIn: 'root' })
export class RolesService extends CrudService<Rol, RolRequest> {
  constructor() {
    super('roles');
  }

  /** El detalle (con permisos) viene de GET /roles/{id}. */
  detalle(id: number): Observable<RolDetalle> {
    return this.http.get<RolDetalle>(`${this.baseUrl}/${id}`);
  }

  asignarPermisos(id: number, permisoIds: number[]): Observable<RolDetalle> {
    return this.http.put<RolDetalle>(`${this.baseUrl}/${id}/permisos`, { permisoIds });
  }
}
