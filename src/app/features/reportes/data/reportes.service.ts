import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, from, map, switchMap, throwError } from 'rxjs';
import { API_URL } from '../../../core/api/api-url';

export interface ReporteResponse {
  desde: string | null;
  hasta: string | null;
  totales: Record<string, unknown>;
  filas: Record<string, unknown>[];
}

export type ParamsReporte = Record<string, string>;

@Injectable({ providedIn: 'root' })
export class ReportesService {
  private readonly http = inject(HttpClient);

  consultar(familia: string, path: string, params: ParamsReporte): Observable<ReporteResponse> {
    return this.http.get<ReporteResponse>(`${API_URL}/reportes/${familia}/${path}`, { params });
  }

  /** Descarga el detalle como CSV. Va con `fetch`/blob porque un enlace directo no envía el token. */
  descargarCsv(familia: string, path: string, params: ParamsReporte, nombreSugerido: string): Observable<void> {
    return this.http
      .get(`${API_URL}/reportes/${familia}/${path}`, { params: { ...params, formato: 'csv' }, responseType: 'blob', observe: 'response' })
      .pipe(
        map((resp) => {
          const nombre = nombreDeCabecera(resp.headers.get('Content-Disposition')) ?? nombreSugerido;
          const url = URL.createObjectURL(resp.body as Blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = nombre;
          a.click();
          setTimeout(() => URL.revokeObjectURL(url), 1000);
        }),
        // Con responseType blob, el cuerpo del error también llega como Blob: se vuelve a leer como JSON.
        catchError((err: unknown) =>
          err instanceof HttpErrorResponse && err.error instanceof Blob
            ? from(err.error.text()).pipe(
                switchMap((texto) => {
                  let cuerpo: unknown = null;
                  try {
                    cuerpo = JSON.parse(texto);
                  } catch {
                    /* el cuerpo no era JSON */
                  }
                  return throwError(() => new HttpErrorResponse({ error: cuerpo, status: err.status, statusText: err.statusText, url: err.url ?? undefined }));
                }),
              )
            : throwError(() => err),
        ),
      );
  }
}

function nombreDeCabecera(cabecera: string | null): string | null {
  const m = /filename\*?=(?:UTF-8'')?"?([^";]+)"?/i.exec(cabecera ?? '');
  return m ? decodeURIComponent(m[1]) : null;
}
