package com.tienda.backend.inventario.infrastructure.adapter.out.consulta;

import com.tienda.backend.inventario.application.port.out.PresentacionConsultaPort;
import com.tienda.backend.presentacion.application.model.PresentacionDetalle;
import com.tienda.backend.presentacion.application.port.out.PresentacionRepositoryPort;
import com.tienda.backend.presentacion.domain.model.Presentacion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InventarioPresentacionConsultaAdapter implements PresentacionConsultaPort {
    private final PresentacionRepositoryPort presentacionRepositoryPort;

    @Override
    public Optional<Presentacion> obtenerPorId(Long presentacionId) {
        return presentacionRepositoryPort.findById(presentacionId);
    }

    @Override
    public Map<Long, String> abreviaturaUnidadBasePorProducto(Collection<Long> productoIds) {
        if (productoIds.isEmpty()) {
            return Map.of();
        }
        return presentacionRepositoryPort.findBasesDetalleByProductoIds(productoIds).stream()
                .collect(Collectors.toMap(
                        (PresentacionDetalle d) -> d.presentacion().getProductoId(),
                        PresentacionDetalle::unidadAbreviatura,
                        (primera, segunda) -> primera));
    }
}
