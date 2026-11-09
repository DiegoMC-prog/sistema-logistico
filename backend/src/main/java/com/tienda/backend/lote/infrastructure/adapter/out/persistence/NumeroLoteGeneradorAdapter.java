package com.tienda.backend.lote.infrastructure.adapter.out.persistence;

import com.tienda.backend.lote.application.port.out.GeneradorNumeroLotePort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Genera {@code LOT-yyyyMMdd-NNNNNN} usando una secuencia de la BD, segura ante peticiones simultáneas. */
@Component
@RequiredArgsConstructor
public class NumeroLoteGeneradorAdapter implements GeneradorNumeroLotePort {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public String siguiente() {
        Long secuencia = jdbcTemplate.queryForObject("SELECT nextval('seq_numero_lote')", Long.class);
        return String.format("LOT-%s-%06d", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE), secuencia);
    }
}
