package com.tienda.backend.caja.infrastructure.adapter.out.persistence;

import com.tienda.backend.caja.application.model.TotalesMovimientos;
import com.tienda.backend.caja.application.port.out.MovimientoCajaRepositoryPort;
import com.tienda.backend.caja.domain.model.MovimientoCaja;
import com.tienda.backend.caja.domain.model.TipoMovimientoCaja;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MovimientoCajaRepositoryAdapter implements MovimientoCajaRepositoryPort {
    private final MovimientoCajaJpaRepository movimientoJpaRepository;
    private final CajaPersistenceMapper mapper;

    @Override
    public MovimientoCaja save(MovimientoCaja movimiento) {
        return mapper.toDomain(movimientoJpaRepository.save(mapper.toEntity(movimiento)));
    }

    @Override
    public PageResult<MovimientoCaja> findByTurnoId(Long turnoId, int page, int size) {
        Page<MovimientoCajaEntity> result = movimientoJpaRepository.findByTurnoId(turnoId,
                PageRequest.of(page, size, Sort.by(Sort.Order.asc("fecha"), Sort.Order.asc("id"))));
        return new PageResult<>(result.getContent().stream().map(mapper::toDomain).toList(), result.getNumber(),
                result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public TotalesMovimientos totalesPorTurno(Long turnoId) {
        BigDecimal ingresos = BigDecimal.ZERO.setScale(4);
        BigDecimal egresos = BigDecimal.ZERO.setScale(4);
        List<Object[]> filas = movimientoJpaRepository.totalesPorTipo(turnoId);
        for (Object[] fila : filas) {
            BigDecimal suma = (BigDecimal) fila[1];
            if (TipoMovimientoCaja.INGRESO.name().equals(fila[0])) {
                ingresos = suma;
            } else {
                egresos = suma;
            }
        }
        return new TotalesMovimientos(ingresos, egresos);
    }
}
