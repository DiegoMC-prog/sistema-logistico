import { Estado } from '../../../core/api/estado';
import { PageParams } from '../../../core/api/page.model';

export interface Producto {
  id: number;
  nombre: string;
  descripcion: string | null;
  categoria: { id: number; nombre: string };
  stockMinimo: number;
  esPerecedero: boolean;
  estado: Estado;
}

export interface ProductoRequest {
  nombre: string;
  descripcion?: string;
  categoriaId: number;
  stockMinimo: number;
  esPerecedero: boolean;
}

export interface ProductoFilters extends PageParams {
  categoriaId?: number;
  estado?: Estado;
  esPerecedero?: boolean;
}

export interface Presentacion {
  id: number;
  productoId: number;
  unidadMedida: { id: number; nombre: string; abreviatura: string };
  codigoBarras: string;
  factorConversion: number;
  esUnidadBase: boolean;
}
