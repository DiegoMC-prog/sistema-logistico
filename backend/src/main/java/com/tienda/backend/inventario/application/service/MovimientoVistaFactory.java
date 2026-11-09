package com.tienda.backend.inventario.application.service;

import com.tienda.backend.deposito.domain.model.Deposito;
import com.tienda.backend.inventario.application.model.MovimientoDetallado;
import com.tienda.backend.inventario.application.model.MovimientoDetallado.LineaDetallada;
import com.tienda.backend.inventario.application.port.out.DepositoConsultaPort;
import com.tienda.backend.inventario.application.port.out.LoteConsultaPort;
import com.tienda.backend.inventario.application.port.out.ProductoConsultaPort;
import com.tienda.backend.inventario.application.port.out.UsuarioConsultaPort;
import com.tienda.backend.inventario.domain.model.Movimiento;
import com.tienda.backend.inventario.domain.model.MovimientoDetalle;
import com.tienda.backend.lote.domain.model.Lote;
import com.tienda.backend.producto.domain.model.Producto;
import com.tienda.backend.usuario.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Agrega a los movimientos los nombres de depósitos, usuarios, productos y lotes. Para una lista completa hace una
 * consulta por tipo de dato (no una por movimiento).
 */
@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MovimientoVistaFactory {
    private static final String ELIMINADO = "(eliminado)";

    private final DepositoConsultaPort depositoConsultaPort;
    private final UsuarioConsultaPort usuarioConsultaPort;
    private final ProductoConsultaPort productoConsultaPort;
    private final LoteConsultaPort loteConsultaPort;

    public MovimientoDetallado construir(Movimiento movimiento) {
        return construir(List.of(movimiento)).get(0);
    }

    public List<MovimientoDetallado> construir(List<Movimiento> movimientos) {
        if (movimientos.isEmpty()) {
            return List.of();
        }

        Set<Long> depositoIds = new HashSet<>();
        Set<Long> usuarioIds = new HashSet<>();
        Set<Long> productoIds = new HashSet<>();
        Set<Long> loteIds = new HashSet<>();
        for (Movimiento m : movimientos) {
            if (m.getDepositoOrigenId() != null) depositoIds.add(m.getDepositoOrigenId());
            if (m.getDepositoDestinoId() != null) depositoIds.add(m.getDepositoDestinoId());
            usuarioIds.add(m.getUsuarioId());
            for (MovimientoDetalle d : m.getDetalles()) {
                productoIds.add(d.productoId());
                loteIds.add(d.loteId());
            }
        }

        Map<Long, String> depositos = depositoConsultaPort.obtenerPorIds(depositoIds).stream()
                .collect(Collectors.toMap(Deposito::getId, Deposito::getNombre));
        Map<Long, String> usuarios = usuarioConsultaPort.obtenerPorIds(usuarioIds).stream()
                .collect(Collectors.toMap(Usuario::getId, Usuario::getUsername));
        Map<Long, String> productos = productoConsultaPort.obtenerPorIds(productoIds).stream()
                .collect(Collectors.toMap(Producto::getId, Producto::getNombre));
        Map<Long, String> lotes = loteConsultaPort.obtenerPorIds(loteIds).stream()
                .collect(Collectors.toMap(Lote::getId, Lote::getNumeroLote));

        return movimientos.stream()
                .map(m -> new MovimientoDetallado(
                        m,
                        nombre(depositos, m.getDepositoOrigenId()),
                        nombre(depositos, m.getDepositoDestinoId()),
                        nombre(usuarios, m.getUsuarioId()),
                        m.getDetalles().stream()
                                .map(d -> new LineaDetallada(
                                        d.productoId(), nombre(productos, d.productoId()),
                                        d.loteId(), nombre(lotes, d.loteId()),
                                        d.cantidad()))
                                .toList()))
                .toList();
    }

    private static String nombre(Map<Long, String> nombres, Long id) {
        return id == null ? null : nombres.getOrDefault(id, ELIMINADO);
    }
}
