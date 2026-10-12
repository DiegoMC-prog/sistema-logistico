export interface PageResponse<T> {
  content: T[];
  totalElements: number; // Total de registros
  totalPages: number;    // Total de páginas
  size: number;          // Tamaño de página
  page: number;          // Índice de página actual (0-indexed)
  number: number;        // Alias idéntico a 'page'
  first: boolean;        // Es primera página
  last: boolean;         // Es última página
}

// Alias para compatibilidad
export type SpringPage<T> = PageResponse<T>;