package com.tienda.backend.pedido.application.service;

import com.tienda.backend.pedido.application.model.RecepcionComando;
import com.tienda.backend.pedido.application.model.RecepcionDetallada;
import com.tienda.backend.pedido.application.port.in.RegistrarRecepcionUseCase;
import com.tienda.backend.pedido.application.port.out.PedidoRepositoryPort;
import com.tienda.backend.pedido.application.port.out.StockPedidoPort;
import com.tienda.backend.pedido.application.port.out.StockPedidoPort.ItemIngreso;
import com.tienda.backend.pedido.domain.exception.PedidoDatosInvalidosException;
import com.tienda.backend.pedido.domain.exception.PedidoNotFoundException;
import com.tienda.backend.pedido.domain.model.LineaPedido;
import com.tienda.backend.pedido.domain.model.PedidoProveedor;
import com.tienda.backend.pedido.domain.model.Recepcion;
import com.tienda.backend.pedido.domain.model.Recepcion.LineaRecepcion;
import com.tienda.backend.shared.application.port.out.UsuarioActualPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecepcionService implements RegistrarRecepcionUseCase {
    private static final String REFERENCIA_PEDIDO = "PEDIDO_PROVEEDOR";
    private static final int GUIA_MAX = 80;
    private static final int OBSERVACION_MAX = 2000;

    private final PedidoRepositoryPort pedidoRepositoryPort;
    private final StockPedidoPort stockPedidoPort;
    private final UsuarioActualPort usuarioActualPort;
    private final PedidoVistaFactory vistaFactory;

    /**
     * Todo ocurre en una transacción: si el ingreso de stock falla (depósito inactivo, número de lote repetido…)
     * el pedido y la recepción quedan como estaban.
     */
    @Override
    @Transactional
    public RecepcionDetallada registrar(RecepcionComando comando) {
        // 1. Validar. El pedido se lee con bloqueo: dos recepciones simultáneas no pueden pasarse de lo pendiente.
        PedidoProveedor pedido = pedidoRepositoryPort.findByIdParaActualizar(comando.pedidoId())
                .orElseThrow(() -> new PedidoNotFoundException(comando.pedidoId()));
        pedido.exigirRecibible();

        List<RecepcionComando.Item> items = comando.items() == null ? List.of() : comando.items();
        if (items.isEmpty()) {
            throw new PedidoDatosInvalidosException("La recepción debe tener al menos una línea");
        }
        String guia = limpiar(comando.numeroGuia(), GUIA_MAX, "El número de guía");
        String observacion = limpiar(comando.observacion(), OBSERVACION_MAX, "La observación");

        // 2. Aplicar lo recibido al pedido (valida que cada línea exista y que no se exceda lo pendiente)
        Set<Long> vistas = new HashSet<>();
        List<ItemIngreso> ingresos = new ArrayList<>();
        for (RecepcionComando.Item item : items) {
            if (item.lineaId() == null) {
                throw new PedidoDatosInvalidosException("Cada línea de la recepción debe indicar la línea del pedido");
            }
            if (!vistas.add(item.lineaId())) {
                throw new PedidoDatosInvalidosException(
                        "La línea " + item.lineaId() + " está repetida en la recepción");
            }
            LineaPedido linea = pedido.linea(item.lineaId());
            pedido.recibir(item.lineaId(), item.cantidad());
            ingresos.add(new ItemIngreso(linea.getProductoId(), item.cantidad(), item.numeroLote(),
                    item.fechaVencimiento(), linea.getCostoUnitario(), item.porcentajeDescuentoVencimiento()));
        }

        // 3. Ingresar el stock: crea un lote por línea. El movimiento queda referenciado al pedido.
        String detalle = "Recepción del pedido " + pedido.getNumeroPedido() + (guia == null ? "" : " (guía " + guia + ")");
        List<Long> loteIds = stockPedidoPort.ingresar(pedido.getDepositoDestinoId(), detalle, pedido.getId(), ingresos);
        if (loteIds.size() != items.size()) {
            throw new IllegalStateException("El inventario devolvió " + loteIds.size() + " lotes para "
                    + items.size() + " líneas de recepción");
        }

        // 4. Guardar el pedido (cantidades y estado) y la recepción con el lote de cada línea
        PedidoProveedor guardado = pedidoRepositoryPort.save(pedido);
        List<LineaRecepcion> lineas = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            lineas.add(new LineaRecepcion(null, items.get(i).lineaId(), loteIds.get(i), items.get(i).cantidad()));
        }
        Recepcion recepcion = pedidoRepositoryPort.saveRecepcion(new Recepcion(null, guardado.getId(),
                usuarioActualPort.id(), LocalDateTime.now(), guia, observacion, lineas));
        return vistaFactory.construir(guardado, recepcion);
    }

    private static String limpiar(String valor, int max, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpio = valor.trim();
        if (limpio.length() > max) {
            throw new PedidoDatosInvalidosException(campo + " no puede superar los " + max + " caracteres");
        }
        return limpio;
    }
}
