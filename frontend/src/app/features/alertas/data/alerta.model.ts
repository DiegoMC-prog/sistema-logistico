export type TipoAlerta = 'VENCIMIENTO' | 'VENCIDO' | 'STOCK_BAJO';

export interface Alerta {
  id: number;
  tipo: TipoAlerta;
  productoId: number;
  producto: string;
  loteId: number | null;
  numeroLote: string | null;
  /** Vencimiento del lote (yyyy-MM-dd); null en STOCK_BAJO. */
  fechaReferencia: string | null;
  /** Existencias (unidades base) al generar la alerta. */
  cantidad: number;
  mensaje: string;
  fecha: string;
  leida: boolean;
  leidaPor: string | null;
  leidaAt: string | null;
  /** La condición ya no se cumple (lote agotado, stock repuesto…). */
  resuelta: boolean;
}

export interface ConteoAlertas {
  total: number;
  vencimiento: number;
  vencido: number;
  stockBajo: number;
}

export interface AlertaFiltro {
  leida?: boolean;
  tipo?: TipoAlerta;
  incluirResueltas?: boolean;
  page?: number;
  size?: number;
}

export interface ResultadoGeneracion {
  vencimiento: number;
  vencido: number;
  stockBajo: number;
  resueltas: number;
}

export const TIPO_ALERTA_LABEL: Record<TipoAlerta, string> = {
  VENCIMIENTO: 'Por vencer',
  VENCIDO: 'Vencido',
  STOCK_BAJO: 'Stock bajo',
};
