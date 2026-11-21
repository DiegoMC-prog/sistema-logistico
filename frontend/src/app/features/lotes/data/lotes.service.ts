import { Injectable } from '@angular/core';
import { CrudService } from '../../../core/api/crud.service';
import { Observable } from 'rxjs';
import { AjusteCostoLote, Lote, LoteFilters, LoteRequest } from './lote.model';

/** Los lotes se crean desde una entrada de inventario: aquí solo se consultan, editan y eliminan. */
@Injectable({ providedIn: 'root' })
export class LotesService extends CrudService<Lote, LoteRequest, LoteFilters> {
  constructor() {
    super('lotes');
  }

  /** R10: correcciones del costo del lote, la más reciente primero. */
  ajustesCosto(id: number): Observable<AjusteCostoLote[]> {
    return this.http.get<AjusteCostoLote[]>(`${this.baseUrl}/${id}/ajustes-costo`);
  }
}
