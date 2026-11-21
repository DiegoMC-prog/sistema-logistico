package com.tienda.backend.precio.application.port.out;

import com.tienda.backend.precio.application.model.RecargoAplicado;
import com.tienda.backend.precio.application.model.RecargoFiltro;
import com.tienda.backend.precio.application.model.RecargoResumen;
import com.tienda.backend.precio.domain.model.AlcanceRecargo;
import com.tienda.backend.shared.application.model.PageResult;

import java.math.BigDecimal;
import java.util.Optional;

public interface RecargoRepositoryPort {

    PageResult<RecargoResumen> listar(RecargoFiltro filtro, int page, int size);

    Optional<RecargoResumen> buscar(Long id);

    /**
     * @throws com.tienda.backend.precio.domain.exception.RecargoConflictoException si otro usuario activó un recargo
     *                                                                              para el mismo destino al mismo tiempo
     */
    Long crear(AlcanceRecargo alcance, Long destinoId, BigDecimal porcentaje, String motivo, String usuario);

    /** true si estaba activo y quedó desactivado. */
    boolean desactivar(Long id, String usuario);

    /** Recargo activo del producto o de la categoría indicados, si hay. */
    Optional<RecargoResumen> activo(AlcanceRecargo alcance, Long destinoId);

    /** Nombre del producto o de la categoría (no eliminados); vacío si no existe. */
    Optional<String> nombreDestino(AlcanceRecargo alcance, Long destinoId);

    /** Recargo que corresponde a una presentación: el de su producto o, si no hay, el de su categoría. */
    Optional<RecargoAplicado> vigentePorPresentacion(Long presentacionId);
}
