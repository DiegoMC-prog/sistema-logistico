import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, from, map, switchMap, throwError } from 'rxjs';
import { API_URL } from './api-url';

/** Descarga de archivos generados por el backend. Va con blob porque un enlace directo no envía el token. */
@Injectable({ providedIn: 'root' })
export class DescargasService {
  private readonly http = inject(HttpClient);

  /** @param ruta ruta de la API sin el prefijo, p. ej. `ventas/5/pdf` */
  descargar(ruta: string, nombreSugerido: string): Observable<void> {
    return this.http.get(`${API_URL}/${ruta}`, { responseType: 'blob', observe: 'response' }).pipe(
      map((resp) => {
        const nombre = /filename="?([^";]+)"?/.exec(resp.headers.get('Content-Disposition') ?? '')?.[1] ?? nombreSugerido;
        const url = URL.createObjectURL(resp.body as Blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = nombre;
        a.click();
        setTimeout(() => URL.revokeObjectURL(url), 1000);
      }),
      // Con responseType blob el cuerpo del error también llega como Blob: se vuelve a leer como JSON
      catchError((err: HttpErrorResponse) =>
        err.error instanceof Blob
          ? from(err.error.text()).pipe(
              switchMap((texto) => {
                let cuerpo: unknown = null;
                try {
                  cuerpo = JSON.parse(texto);
                } catch {
                  /* respuesta sin JSON */
                }
                return throwError(() => new HttpErrorResponse({ error: cuerpo, status: err.status, statusText: err.statusText }));
              }),
            )
          : throwError(() => err),
      ),
    );
  }
}
