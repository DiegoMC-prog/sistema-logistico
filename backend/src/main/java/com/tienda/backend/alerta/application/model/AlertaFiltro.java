package com.tienda.backend.alerta.application.model;

import com.tienda.backend.alerta.domain.model.TipoAlerta;

/**
 * @param leida            null = todas; true/false = solo leídas o no leídas
 * @param tipo             null = todos los tipos
 * @param incluirResueltas false = solo las alertas cuya condición sigue vigente
 */
public record AlertaFiltro(Boolean leida, TipoAlerta tipo, boolean incluirResueltas) {
}
