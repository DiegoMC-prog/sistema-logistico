import { Injectable } from '@angular/core';
import { CrudService } from '../../../core/api/crud.service';
import { Lote, LoteFilters, LoteRequest } from './lote.model';

/** Los lotes se crean desde una entrada de inventario: aquí solo se consultan, editan y eliminan. */
@Injectable({ providedIn: 'root' })
export class LotesService extends CrudService<Lote, LoteRequest, LoteFilters> {
  constructor() {
    super('lotes');
  }
}
