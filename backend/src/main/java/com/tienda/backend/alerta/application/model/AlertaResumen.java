package com.tienda.backend.alerta.application.model;

import com.tienda.backend.alerta.domain.model.TipoAlerta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AlertaResumen(Long id, TipoAlerta tipo, Long productoId, String producto, Long loteId,
                            String numeroLote, LocalDate fechaReferencia, BigDecimal cantidad, String mensaje,
                            LocalDateTime fecha, boolean leida, String leidaPor, LocalDateTime leidaAt,
                            boolean resuelta) {
}
