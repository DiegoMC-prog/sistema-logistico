import { PageParams } from '../../../core/api/page.model';

export type EstadoPedido = 'BORRADOR' | 'ENVIADO' | 'RECIBIDO_PARCIAL' | 'RECIBIDO' | 'CANCELADO';

export interface PedidoItem {
  /** Es el `lineaId` al registrar una recepción. */
  id: number;
  /** En los listados `producto.nombre` viene en null: usar GET /pedidos/{id}. */
  producto: { id: number; nombre: string | null };
  cantidadPedida: number;
  cantidadRecibida: number;
  cantidadPendiente: number;
  costoUnitario: number;
  subtotal: number;
}

export interface Pedido {
  id: number;
  numeroPedido: string;
  estado: EstadoPedido;
  proveedor: { id: number; nombre: string };
  depositoDestino: { id: number; nombre: string };
  fechaPedido: string;
  fechaEntregaEstimada: string | null;
  observacion: string | null;
  total: number;
  items: PedidoItem[];
}

export interface PedidoItemRequest {
  productoId: number;
  /** En la unidad base del producto, no por presentación. */
  cantidad: number;
  costoUnitario: number;
}

export interface PedidoRequest {
  proveedorId: number;
  depositoDestinoId: number;
  fechaEntregaEstimada?: string;
  observacion?: string;
  items: PedidoItemRequest[];
}

export interface PedidoFilters extends PageParams {
  numero?: string;
  proveedorId?: number;
  depositoId?: number;
  estado?: EstadoPedido;
  desde?: string;
  hasta?: string;
}

export interface RecepcionItemRequest {
  lineaId: number;
  cantidad: number;
  numeroLote?: string;
  fechaVencimiento?: string;
  porcentajeDescuentoVencimiento?: number;
}

export interface RecepcionRequest {
  numeroGuia?: string;
  observacion?: string;
  items: RecepcionItemRequest[];
}

export interface Recepcion {
  id: number;
  pedidoId: number;
  numeroPedido: string;
  /** En el POST es el estado del pedido tras esa recepción; en el GET, el estado actual. */
  estadoPedido: EstadoPedido;
  fechaRecepcion: string;
  usuario: { id: number; nombre: string };
  numeroGuia: string | null;
  observacion: string | null;
  items: {
    lineaId: number;
    producto: { id: number; nombre: string };
    lote: { id: number; numeroLote: string };
    cantidad: number;
  }[];
}

export const ESTADOS_PEDIDO: { value: EstadoPedido; label: string }[] = [
  { value: 'BORRADOR', label: 'Borrador' },
  { value: 'ENVIADO', label: 'Enviado' },
  { value: 'RECIBIDO_PARCIAL', label: 'Recibido parcial' },
  { value: 'RECIBIDO', label: 'Recibido' },
  { value: 'CANCELADO', label: 'Cancelado' },
];

export const etiquetaEstadoPedido = (e: string): string => ESTADOS_PEDIDO.find((x) => x.value === e)?.label ?? e;

export const toneEstadoPedido = (e: string): 'success' | 'warning' | 'danger' | 'neutral' | 'info' =>
  ({ BORRADOR: 'neutral', ENVIADO: 'info', RECIBIDO_PARCIAL: 'warning', RECIBIDO: 'success', CANCELADO: 'danger' } as const)[
    e as EstadoPedido
  ] ?? 'neutral';
