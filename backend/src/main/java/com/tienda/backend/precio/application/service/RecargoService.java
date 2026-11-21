package com.tienda.backend.precio.application.service;

import com.tienda.backend.precio.application.model.CrearRecargoComando;
import com.tienda.backend.precio.application.model.RecargoFiltro;
import com.tienda.backend.precio.application.model.RecargoResumen;
import com.tienda.backend.precio.application.port.in.GestionarRecargosUseCase;
import com.tienda.backend.precio.application.port.out.RecargoRepositoryPort;
import com.tienda.backend.precio.domain.exception.RecargoConflictoException;
import com.tienda.backend.precio.domain.exception.RecargoDatosInvalidosException;
import com.tienda.backend.precio.domain.exception.RecargoNotFoundException;
import com.tienda.backend.precio.domain.model.AlcanceRecargo;
import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.shared.application.port.out.UsuarioActualPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Recargos por escasez (RF-35). Se suman al precio que resuelve la jerarquía de precios (tipo de cliente, escalón por
 * cantidad) y quedan registrados con quién los activó y quién los desactivó.
 */
@Service
@RequiredArgsConstructor
public class RecargoService implements GestionarRecargosUseCase {

    private static final int MOTIVO_MAXIMO = 500;

    private final RecargoRepositoryPort repositorio;
    private final UsuarioActualPort usuarioActualPort;

    @Override
    @Transactional(readOnly = true)
    public PageResult<RecargoResumen> listar(RecargoFiltro filtro, int page, int size) {
        return repositorio.listar(filtro, PageResult.safePage(page), PageResult.safeSize(size));
    }

    @Override
    @Transactional
    public RecargoResumen crear(CrearRecargoComando c) {
        if (c.alcance() == null) {
            throw new RecargoDatosInvalidosException("Indique si el recargo es para un producto o para una categoría");
        }
        Long destinoId = c.alcance() == AlcanceRecargo.PRODUCTO ? c.productoId() : c.categoriaId();
        if (destinoId == null) {
            throw new RecargoDatosInvalidosException(c.alcance() == AlcanceRecargo.PRODUCTO
                    ? "Elija el producto" : "Elija la categoría");
        }
        BigDecimal porcentaje = c.porcentaje();
        if (porcentaje == null || porcentaje.signum() <= 0 || porcentaje.compareTo(BigDecimal.ONE) > 0) {
            throw new RecargoDatosInvalidosException("El recargo debe ser mayor que 0 % y como mucho 100 %");
        }
        porcentaje = porcentaje.setScale(4, RoundingMode.HALF_UP);
        if (porcentaje.signum() <= 0) {
            throw new RecargoDatosInvalidosException("El recargo es demasiado pequeño");
        }
        String motivo = c.motivo() == null ? "" : c.motivo().trim();
        if (motivo.isEmpty()) {
            throw new RecargoDatosInvalidosException("Indique el motivo del recargo (queda registrado)");
        }
        if (motivo.length() > MOTIVO_MAXIMO) {
            throw new RecargoDatosInvalidosException("El motivo admite hasta " + MOTIVO_MAXIMO + " caracteres");
        }

        String nombre = repositorio.nombreDestino(c.alcance(), destinoId)
                .orElseThrow(() -> new RecargoDatosInvalidosException(c.alcance() == AlcanceRecargo.PRODUCTO
                        ? "El producto no existe" : "La categoría no existe"));
        repositorio.activo(c.alcance(), destinoId).ifPresent(r -> {
            throw new RecargoConflictoException("Ya hay un recargo activo de " + porcentajeTexto(r.porcentaje())
                    + " para " + nombre + ". Desactívelo antes de activar otro.");
        });

        Long id = repositorio.crear(c.alcance(), destinoId, porcentaje, motivo, usuarioActualPort.nombre());
        return repositorio.buscar(id).orElseThrow(() -> new RecargoNotFoundException(id));
    }

    @Override
    @Transactional
    public RecargoResumen desactivar(Long id) {
        RecargoResumen actual = repositorio.buscar(id).orElseThrow(() -> new RecargoNotFoundException(id));
        if (!repositorio.desactivar(id, usuarioActualPort.nombre())) {
            throw new RecargoConflictoException("El recargo para " + actual.destino() + " ya estaba desactivado.");
        }
        return repositorio.buscar(id).orElseThrow(() -> new RecargoNotFoundException(id));
    }

    private static String porcentajeTexto(BigDecimal fraccion) {
        return fraccion.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString() + " %";
    }
}
