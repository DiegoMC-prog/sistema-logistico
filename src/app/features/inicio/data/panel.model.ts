/**
 * Respuesta de GET /reportes/panel. Cada bloque llega solo si el usuario tiene el permiso del reporte
 * del que sale; los demás no vienen (no se envían como null). `bloques` lista los presentes.
 */
export interface PanelDia {
  ventas: number;
  total: number;
  ticketPromedio: number;
  anuladas: number;
}

export interface PanelVentas {
  hoy: PanelDia;
  ayer: PanelDia;
  /** % de cambio del total de hoy respecto de ayer; null si ayer no hubo ventas. */
  variacionTotal: number | null;
  /** Siempre 7 filas (los días sin ventas en cero); la última es hoy. */
  ultimos7Dias: { fecha: string; ventas: number; total: number }[];
}

export interface PanelCaja {
  turnosAbiertos: number;
  turnos: {
    turnoId: number;
    caja: string;
    cajero: string;
    fechaApertura: string;
    ventas: number;
    totalVentas: number;
    efectivoEsperado: number;
  }[];
}

export interface PanelInventario {
  productosBajoMinimo: number;
  sinStock: number;
  criticos: { productoId: number; producto: string; stock: number; stockMinimo: number; faltante: number }[];
  lotesPorVencer7Dias: number;
  lotesVencidos: number;
  valorEnRiesgo: number;
}

export interface PanelCompras {
  pedidosPendientes: number;
  atrasados: number;
  montoPendiente: number;
  masAtrasados: { pedidoId: number; numeroPedido: string; proveedor: string; diasAtraso: number; montoPendiente: number }[];
}

export interface PanelUtilidad {
  utilidadHoy: number;
  /** null si hoy no hubo ventas. */
  margenHoy: number | null;
  utilidad7Dias: number;
  margen7Dias: number | null;
}

export interface Panel {
  generadoEn: string;
  bloques: string[];
  ventas?: PanelVentas;
  caja?: PanelCaja;
  inventario?: PanelInventario;
  compras?: PanelCompras;
  utilidad?: PanelUtilidad;
}
