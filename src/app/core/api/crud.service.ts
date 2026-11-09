import { HttpClient } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL, toParams } from './api-url';
import { Page, PageParams } from './page.model';

/**
 * Base para los recursos con CRUD paginado del backend.
 * Uso: @Injectable({providedIn:'root'}) class X extends CrudService<Resp, Req> { constructor(){ super('categorias'); } }
 */
export abstract class CrudService<TResponse, TRequest, TFilters extends PageParams = PageParams> {
  protected readonly http = inject(HttpClient);
  protected readonly baseUrl: string;

  protected constructor(resource: string) {
    this.baseUrl = `${API_URL}/${resource}`;
  }

  list(filters: TFilters = {} as TFilters): Observable<Page<TResponse>> {
    return this.http.get<Page<TResponse>>(this.baseUrl, { params: toParams(filters) });
  }

  get(id: number): Observable<TResponse> {
    return this.http.get<TResponse>(`${this.baseUrl}/${id}`);
  }

  create(body: TRequest): Observable<TResponse> {
    return this.http.post<TResponse>(this.baseUrl, body);
  }

  update(id: number, body: TRequest): Observable<TResponse> {
    return this.http.put<TResponse>(`${this.baseUrl}/${id}`, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
