import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { API_URL, toParams } from '../../../core/api/api-url';
import { Page } from '../../../core/api/page.model';
import { withToast } from '../../../core/ui/toast';
import {
  CalculoReorden,
  CostoFiltro,
  HistoricoCosto,
  CrearPedidosRequest,
  ParametrosReorden,
  ParametrosReordenRequest,
  PedidoGenerado,
  Sugerencia,
  SugerenciaFiltro,
  VariacionCosto,
  VariacionProveedor,
} from './abastecimiento.model';

@Injectable({ providedIn: 'root' })
export class AbastecimientoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_URL}/abastecimiento`;

  /** Último recálculo, o null si todavía no se ejecutó ninguno (el backend responde 204). */
  ultimoCalculo(): Observable<CalculoReorden | null> {
    return this.http
      .get<CalculoReorden>(`${this.url}/calculo`, { observe: 'response' })
      .pipe(map((r: HttpResponse<CalculoReorden>) => (r.status === 204 ? null : r.body)));
  }

  recalcular(): Observable<CalculoReorden> {
    return this.http.post<CalculoReorden>(`${this.url}/calculo`, null, withToast('Sugerencias recalculadas'));
  }

  sugerencias(filtro: SugerenciaFiltro): Observable<Page<Sugerencia>> {
    return this.http.get<Page<Sugerencia>>(`${this.url}/sugerencias`, { params: toParams(filtro) });
  }

  crearPedidos(body: CrearPedidosRequest): Observable<PedidoGenerado[]> {
    return this.http.post<PedidoGenerado[]>(`${this.url}/pedidos`, body, withToast(null));
  }

  descartar(id: number): Observable<Sugerencia> {
    return this.http.patch<Sugerencia>(`${this.url}/sugerencias/${id}/descartar`, null, withToast('Sugerencia descartada'));
  }

  parametros(productoId: number): Observable<ParametrosReorden> {
    return this.http.get<ParametrosReorden>(`${this.url}/parametros/${productoId}`);
  }

  /** RF-35: variación de costo por proveedor y producto, primero las mayores alzas. */
  variacionesCosto(filtro: CostoFiltro): Observable<Page<VariacionCosto>> {
    return this.http.get<Page<VariacionCosto>>(`${this.url}/costos/variaciones`, { params: toParams(filtro) });
  }

  historialCostos(productoId: number, proveedorId?: number): Observable<HistoricoCosto[]> {
    return this.http.get<HistoricoCosto[]>(`${this.url}/costos/historial`, { params: toParams({ productoId, proveedorId }) });
  }

  variacionProveedores(): Observable<VariacionProveedor[]> {
    return this.http.get<VariacionProveedor[]>(`${this.url}/costos/proveedores`);
  }

  guardarParametros(productoId: number, body: ParametrosReordenRequest): Observable<ParametrosReorden> {
    return this.http.put<ParametrosReorden>(
      `${this.url}/parametros/${productoId}`,
      body,
      withToast('Parámetros guardados: se aplican en el próximo recálculo'),
    );
  }
}
