export interface ProductoRequest {
  codigoBarras: string;
  nombre: string;
  descripcion?: string;
  imagen?: string;
  categoriaId: number;
}

export interface ProductoResponse {
  id: number;
  codigoBarras: string;
  nombre: string;
  descripcion?: string;
  imagen?: string;
  categoriaId: number;
  categoriaNombre?: string;
  estado: boolean;
}

export interface ProductoStats {
  totalProductos: number;
  activos: number;
  inactivos: number;
}
