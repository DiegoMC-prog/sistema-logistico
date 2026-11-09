import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CrudService } from '../../../core/api/crud.service';
import { Page } from '../../../core/api/page.model';
import { withToast } from '../../../core/ui/toast';
import { Pedido, PedidoFilters, PedidoRequest, Recepcion, RecepcionRequest } from './pedido.model';

@Injectable({ providedIn: 'root' })
export class PedidosService extends CrudService<Pedido, PedidoRequest, PedidoFilters> {
  constructor() {
    super('pedidos');
  }

  enviar(id: number): Observable<Pedido> {
    return this.http.post<Pedido>(`${this.baseUrl}/${id}/enviar`, null, withToast('Pedido enviado'));
  }

  cancelar(id: number): Observable<Pedido> {
    return this.http.post<Pedido>(`${this.baseUrl}/${id}/cancelar`, null, withToast('Pedido cancelado'));
  }

  recepciones(id: number): Observable<Recepcion[] | Page<Recepcion>> {
    return this.http.get<Recepcion[] | Page<Recepcion>>(`${this.baseUrl}/${id}/recepciones`);
  }

  recibir(id: number, body: RecepcionRequest): Observable<Recepcion> {
    return this.http.post<Recepcion>(`${this.baseUrl}/${id}/recepciones`, body, withToast('Recepción registrada'));
  }
}
