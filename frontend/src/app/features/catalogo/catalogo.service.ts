import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL, toParams } from '../../core/api/api-url';
import { Page, PageParams } from '../../core/api/page.model';

export interface CatalogoPresentacion {
  id: number;
  abreviatura: string;
  factor: number;
  /** Precio de una unidad, con el recargo incluido; null si no se pidió o no tiene precio. */
  precio: number | null;
  recargo: number | null;
}

export interface CatalogoProducto {
  productoId: number;
  nombre: string;
  categoria: string;
  /** En unidades base; null si no se pidió un depósito. */
  stock: number | null;
  unidadBase: string | null;
  presentaciones: CatalogoPresentacion[];
}

export interface CatalogoFiltro extends PageParams {
  depositoId?: number;
  categoriaId?: number;
  soloConStock?: boolean;
  incluirVencidos?: boolean;
  conPrecios?: boolean;
  tipoClienteId?: number;
}

@Injectable({ providedIn: 'root' })
export class CatalogoService {
  private readonly http = inject(HttpClient);

  listar(filtro: CatalogoFiltro): Observable<Page<CatalogoProducto>> {
    return this.http.get<Page<CatalogoProducto>>(`${API_URL}/catalogo`, { params: toParams(filtro) });
  }
}
