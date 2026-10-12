export interface Categoria {
  id: number;
  nombre: string;
  totalProductos?: number;
}


export interface CategoriaStats {
  totalCategorias: number;
  categoriasEnUso: number;
}
