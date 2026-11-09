package com.tienda.backend.pedido.domain.model;

import com.tienda.backend.pedido.domain.exception.PedidoDatosInvalidosException;
import com.tienda.backend.pedido.domain.exception.PedidoEstadoInvalidoException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Pedido de compra a un proveedor. Se arma en BORRADOR (editable), se ENVIA y luego se recibe en una o varias
 * recepciones hasta quedar RECIBIDO. Solo un pedido que no recibió nada se puede cancelar.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PedidoProveedor {
    private static final int OBSERVACION_MAX = 2000;

    @EqualsAndHashCode.Include
    private final Long id;

    private final String numeroPedido;
    private Long proveedorId;
    private Long depositoDestinoId;
    private EstadoPedido estado;
    private final LocalDate fechaPedido;
    private LocalDate fechaEntregaEstimada;
    private String observacion;
    private BigDecimal total;
    private final Long version;
    private List<LineaPedido> lineas;

    /** Reconstrucción desde persistencia. */
    public PedidoProveedor(Long id, String numeroPedido, Long proveedorId, Long depositoDestinoId, EstadoPedido estado,
                           LocalDate fechaPedido, LocalDate fechaEntregaEstimada, String observacion, Long version,
                           List<LineaPedido> lineas) {
        this.id = id;
        this.numeroPedido = numeroPedido;
        this.fechaPedido = fechaPedido;
        this.version = version;
        this.estado = estado;
        asignarDatos(proveedorId, depositoDestinoId, fechaEntregaEstimada, observacion, lineas);
    }

    /** Pedido nuevo: nace en BORRADOR. */
    public static PedidoProveedor nuevo(String numeroPedido, Long proveedorId, Long depositoDestinoId,
                                        LocalDate fechaEntregaEstimada, String observacion, List<LineaPedido> lineas) {
        return new PedidoProveedor(null, numeroPedido, proveedorId, depositoDestinoId, EstadoPedido.BORRADOR,
                LocalDate.now(), fechaEntregaEstimada, observacion, null, lineas);
    }

    /** Solo en BORRADOR: reemplaza los datos y las líneas. */
    public void editar(Long proveedorId, Long depositoDestinoId, LocalDate fechaEntregaEstimada, String observacion,
                       List<LineaPedido> lineas) {
        exigirEstado("editar", EstadoPedido.BORRADOR);
        asignarDatos(proveedorId, depositoDestinoId, fechaEntregaEstimada, observacion, lineas);
    }

    public void enviar() {
        exigirEstado("enviar", EstadoPedido.BORRADOR);
        if (lineas.isEmpty()) {
            throw new PedidoDatosInvalidosException("El pedido debe tener al menos una línea para enviarse");
        }
        this.estado = EstadoPedido.ENVIADO;
    }

    /** Se puede cancelar un BORRADOR o un pedido ENVIADO sin recepciones. */
    public void cancelar() {
        exigirEstado("cancelar", EstadoPedido.BORRADOR, EstadoPedido.ENVIADO);
        this.estado = EstadoPedido.CANCELADO;
    }

    public boolean esBorrador() {
        return estado == EstadoPedido.BORRADOR;
    }

    /** Se puede recibir un pedido ENVIADO o RECIBIDO_PARCIAL. */
    public void exigirRecibible() {
        exigirEstado("recibir", EstadoPedido.ENVIADO, EstadoPedido.RECIBIDO_PARCIAL);
    }

    public LineaPedido linea(Long lineaId) {
        return lineas.stream().filter(l -> lineaId.equals(l.getId())).findFirst()
                .orElseThrow(() -> new PedidoDatosInvalidosException(
                        "La línea " + lineaId + " no pertenece al pedido " + numeroPedido));
    }

    /** Aplica lo recibido a una línea y recalcula el estado del pedido. */
    public void recibir(Long lineaId, BigDecimal cantidad) {
        exigirRecibible();
        linea(lineaId).recibir(cantidad);
        this.estado = lineas.stream().allMatch(LineaPedido::completa)
                ? EstadoPedido.RECIBIDO : EstadoPedido.RECIBIDO_PARCIAL;
    }

    private void asignarDatos(Long proveedorId, Long depositoDestinoId, LocalDate fechaEntregaEstimada,
                              String observacion, List<LineaPedido> lineas) {
        if (proveedorId == null) {
            throw new PedidoDatosInvalidosException("El proveedor es obligatorio");
        }
        if (depositoDestinoId == null) {
            throw new PedidoDatosInvalidosException("El depósito de destino es obligatorio");
        }
        if (fechaEntregaEstimada != null && fechaPedido != null && fechaEntregaEstimada.isBefore(fechaPedido)) {
            throw new PedidoDatosInvalidosException("La fecha de entrega estimada no puede ser anterior a la del pedido");
        }
        String obs = (observacion == null || observacion.isBlank()) ? null : observacion.trim();
        if (obs != null && obs.length() > OBSERVACION_MAX) {
            throw new PedidoDatosInvalidosException(
                    "La observación no puede superar los " + OBSERVACION_MAX + " caracteres");
        }
        List<LineaPedido> nuevas = lineas == null ? List.of() : lineas;
        Set<Long> productos = new HashSet<>();
        for (LineaPedido linea : nuevas) {
            if (!productos.add(linea.getProductoId())) {
                throw new PedidoDatosInvalidosException(
                        "El producto " + linea.getProductoId() + " está repetido en el pedido");
            }
        }

        this.proveedorId = proveedorId;
        this.depositoDestinoId = depositoDestinoId;
        this.fechaEntregaEstimada = fechaEntregaEstimada;
        this.observacion = obs;
        this.lineas = new ArrayList<>(nuevas);
        this.total = this.lineas.stream().map(LineaPedido::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void exigirEstado(String accion, EstadoPedido... permitidos) {
        for (EstadoPedido permitido : permitidos) {
            if (estado == permitido) {
                return;
            }
        }
        throw new PedidoEstadoInvalidoException(
                "No se puede " + accion + " un pedido en estado " + estado + " (pedido " + numeroPedido + ")");
    }
}
