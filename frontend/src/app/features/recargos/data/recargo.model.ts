export type AlcanceRecargo = 'PRODUCTO' | 'CATEGORIA';

/** Recargo por escasez (RF-35). El porcentaje viaja como fracción: 0.15 = +15 %. */
export interface Recargo {
  id: number;
  alcance: AlcanceRecargo;
  productoId: number | null;
  producto: string | null;
  categoriaId: number | null;
  categoria: string | null;
  porcentaje: number;
  motivo: string;
  activo: boolean;
  activadoPor: string;
  activadoAt: string;
  desactivadoPor: string | null;
  desactivadoAt: string | null;
}

export interface RecargoFiltro {
  activo?: boolean;
  alcance?: AlcanceRecargo;
  search?: string;
  page?: number;
  size?: number;
}

export interface RecargoRequest {
  alcance: AlcanceRecargo;
  productoId?: number;
  categoriaId?: number;
  porcentaje: number;
  motivo: string;
}

export const ALCANCE_LABEL: Record<AlcanceRecargo, string> = {
  PRODUCTO: 'Producto',
  CATEGORIA: 'Categoría',
};
