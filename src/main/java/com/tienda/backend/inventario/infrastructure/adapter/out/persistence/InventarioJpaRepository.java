package com.tienda.backend.inventario.infrastructure.adapter.out.persistence;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface InventarioJpaRepository
        extends JpaRepository<InventarioEntity, Long>, JpaSpecificationExecutor<InventarioEntity> {

    @Override
    @EntityGraph(attributePaths = {"deposito", "lote", "lote.producto"})
    Page<InventarioEntity> findAll(Specification<InventarioEntity> spec, Pageable pageable);

    Optional<InventarioEntity> findByDepositoIdAndLoteId(Long depositoId, Long loteId);

    boolean existsByLoteIdAndStockActualGreaterThan(Long loteId, BigDecimal minimo);

    boolean existsByDepositoIdAndStockActualGreaterThan(Long depositoId, BigDecimal minimo);

    /**
     * Lee la fila con {@code SELECT ... FOR UPDATE}: nadie más puede modificarla hasta que termine la transacción,
     * y quien la pida mientras tanto espera. Al despertar lee el valor ya confirmado por la transacción anterior.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InventarioEntity i where i.deposito.id = :depositoId and i.lote.id = :loteId")
    Optional<InventarioEntity> findParaActualizar(@Param("depositoId") Long depositoId, @Param("loteId") Long loteId);

    /**
     * Crea la fila con stock cero si no existe. {@code FOR UPDATE} no puede bloquear una fila inexistente, y dos ingresos
     * simultáneos intentarían insertarla a la vez; con {@code ON CONFLICT DO NOTHING} el segundo espera al primero y sigue.
     * El destino del conflicto es el índice único parcial {@code uk_inventarios_deposito_lote}.
     */
    @Modifying
    @Query(value = "INSERT INTO inventarios (deposito_id, lote_id, stock_actual, version, created_by, updated_by, "
            + "created_at, updated_at) VALUES (:depositoId, :loteId, 0, 0, :usuario, :usuario, :ahora, :ahora) "
            + "ON CONFLICT (deposito_id, lote_id) WHERE deleted_at IS NULL DO NOTHING", nativeQuery = true)
    int crearSiNoExiste(@Param("depositoId") Long depositoId, @Param("loteId") Long loteId,
                        @Param("usuario") String usuario, @Param("ahora") LocalDateTime ahora);

    /** Limita (solo dentro de la transacción actual) cuánto espera un bloqueo antes de fallar, para no esperar indefinidamente. */
    @Query(value = "select set_config('lock_timeout', :tiempo, true)", nativeQuery = true)
    String configurarTiempoEsperaBloqueo(@Param("tiempo") String tiempo);

    /**
     * Candidatos FEFO como columnas sueltas (id de lote, id de producto, vencimiento, costo, descuento), NO como entidades:
     * si se cargaran las filas de stock aquí, quedarían en el contexto de persistencia con el stock y la versión de ANTES
     * de bloquearlas, y la lectura con bloqueo posterior devolvería esos valores viejos en vez de los vigentes.
     */
    @Query("""
            select l.id, p.id, l.fechaVencimiento, l.costoUnitarioCompra, l.porcentajeDescuentoVencimiento
            from InventarioEntity i
              join i.lote l
              join l.producto p
            where i.deposito.id = :depositoId
              and p.id in :productoIds
              and i.stockActual > 0
              and (l.fechaVencimiento is null or l.fechaVencimiento >= :hoy)
            order by case when l.fechaVencimiento is null then 1 else 0 end, l.fechaVencimiento asc, l.id asc
            """)
    List<Object[]> findDisponiblesFefo(@Param("depositoId") Long depositoId,
                                       @Param("productoIds") Collection<Long> productoIds,
                                       @Param("hoy") LocalDate hoy);
}
