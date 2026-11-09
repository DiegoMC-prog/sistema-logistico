import { Injectable } from '@angular/core';
import { CrudService } from '../../../core/api/crud.service';
import { UnidadMedida, UnidadMedidaRequest } from './unidad-medida.model';

@Injectable({ providedIn: 'root' })
export class UnidadesMedidaService extends CrudService<UnidadMedida, UnidadMedidaRequest> {
  constructor() {
    super('unidades-medida');
  }
}
