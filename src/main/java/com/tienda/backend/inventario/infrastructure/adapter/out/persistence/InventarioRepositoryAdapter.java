package com.tienda.backend.inventario.infrastructure.adapter.out.persistence;

import com.tienda.backend.inventario.application.model.LoteDisponible;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import com.tienda.backend.inventario.application.model.ClaveStock;
import com.tienda.backend.inventario.application.model.StockDetalle;
import com.tienda.backend.inventario.application.model.StockFiltro;
import com.tienda.backend.inventario.application.port.out.InventarioRepositoryPort;
import com.tienda.backend.inventario.domain.model.Inventario;
import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.shared.application.port.out.UsuarioActualPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InventarioRepositoryAdapter implements InventarioRepositoryPort {
    /** Máximo que espera un bloqueo antes de fallar con 409 "ocupado". */
    private static final String ESPERA_MAXIMA_BLOQUEO = "5s";

    private final InventarioJpaRepository inventarioJpaRepository;
    private final InventarioPersistenceMapper mapper;
    private final UsuarioActualPort usuarioActualPort;

    @Override
    public Optional<Inventario> findByDepositoIdAndLoteId(Long depositoId, Long loteId) {
        return inventarioJpaRepository.findByDepositoIdAndLoteId(depositoId, loteId).map(mapper::toDomain);
    }

    @Override
    public Map<ClaveStock, Inventario> bloquear(Collection<ClaveStock> claves) {
        inventarioJpaRepository.configurarTiempoEsperaBloqueo(ESPERA_MAXIMA_BLOQUEO);

        String usuario = usuarioActualPort.nombre();
        LocalDateTime ahora = LocalDateTime.now();
        Map<ClaveStock, Inventario> bloqueados = new LinkedHashMap<>();

        // Orden global fijo: evita deadlocks entre operaciones que tocan las mismas filas en distinto orden.
        claves.stream()
                .distinct()
                .sorted(Comparator.comparing(ClaveStock::loteId).thenComparing(ClaveStock::depositoId))
                .forEach(clave -> {
                    inventarioJpaRepository.crearSiNoExiste(clave.depositoId(), clave.loteId(), usuario, ahora);
                    InventarioEntity fila = inventarioJpaRepository
                            .findParaActualizar(clave.depositoId(), clave.loteId())
                            .orElseThrow(() -> new IllegalStateException(
                                    "No se pudo bloquear el stock del lote " + clave.loteId()
                                            + " en el depósito " + clave.depositoId()));
                    bloqueados.put(clave, mapper.toDomain(fila));
                });
        return bloqueados;
    }

    @Override
    public Inventario save(Inventario inventario) {
        return mapper.toDomain(inventarioJpaRepository.save(mapper.toEntity(inventario)));
    }

    @Override
    public PageResult<StockDetalle> findAll(StockFiltro filtro, int page, int size) {
        // Vista FEFO: primero lo que vence antes (los lotes sin vencimiento al final) y luego por id.
        Sort orden = Sort.by(Sort.Order.asc("lote.fechaVencimiento"), Sort.Order.asc("id"));
        Page<InventarioEntity> result = inventarioJpaRepository.findAll(
                InventarioSpecifications.desde(filtro), PageRequest.of(page, size, orden));
        return new PageResult<>(
                result.getContent().stream().map(mapper::toStockDetalle).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public boolean existeStockPorLote(Long loteId) {
        return inventarioJpaRepository.existsByLoteIdAndStockActualGreaterThan(loteId, BigDecimal.ZERO);
    }

    @Override
    public boolean existeStockPorDeposito(Long depositoId) {
        return inventarioJpaRepository.existsByDepositoIdAndStockActualGreaterThan(depositoId, BigDecimal.ZERO);
    }

    @Override
    public List<LoteDisponible> findLotesDisponiblesFefo(Long depositoId, Collection<Long> productoIds, LocalDate hoy) {
        if (productoIds.isEmpty()) {
            return List.of();
        }
        return inventarioJpaRepository.findDisponiblesFefo(depositoId, productoIds, hoy).stream()
                .map(f -> new LoteDisponible((Long) f[0], (Long) f[1], (LocalDate) f[2], (java.math.BigDecimal) f[3],
                        (java.math.BigDecimal) f[4]))
                .toList();
    }
}
