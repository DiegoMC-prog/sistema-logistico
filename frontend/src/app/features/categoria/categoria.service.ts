import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.development';
import { Categoria, CategoriaStats } from './categoria.model';
import { PageResponse } from '../../shared/models/page.model';

@Injectable({
    providedIn: 'root'
})
export class CategoriaService {
    private readonly http = inject(HttpClient);
    private readonly apiUrl = `${environment.apiUrl}/categorias`;

    listar(page = 0, size = 10, nombre = ''): Observable<PageResponse<Categoria>> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());

        if (nombre.trim()) {
            params = params.set('nombre', nombre.trim());
        }

        return this.http.get<PageResponse<Categoria>>(this.apiUrl, { params });
    }

    obtenerEstadisticas(): Observable<CategoriaStats> {
        return this.http.get<CategoriaStats>(`${this.apiUrl}/stats`);
    }

    crear(nombre: string): Observable<Categoria> {
        return this.http.post<Categoria>(this.apiUrl, { nombre });
    }

    actualizar(id: number, nombre: string): Observable<Categoria> {
        return this.http.put<Categoria>(`${this.apiUrl}/${id}`, { nombre });
    }

    eliminar(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }
}