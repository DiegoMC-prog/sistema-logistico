import { Injectable } from '@angular/core';
import { CrudService } from '../../../core/api/crud.service';
import { Categoria, CategoriaRequest } from './categoria.model';

@Injectable({ providedIn: 'root' })
export class CategoriasService extends CrudService<Categoria, CategoriaRequest> {
  constructor() {
    super('categorias');
  }
}
