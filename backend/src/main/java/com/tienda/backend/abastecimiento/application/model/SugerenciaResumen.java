package com.tienda.backend.abastecimiento.application.model;

import com.tienda.backend.abastecimiento.domain.model.AccionSugerencia;
import com.tienda.backend.abastecimiento.domain.model.EstadoSugerencia;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Sugerencia con los datos de entrada del cálculo, los tres escenarios y el motivo. Cantidades en unidad base. */
public record SugerenciaResumen(
        Long id,
        Long calculoId,
        Long productoId,
        String producto,
        String categoria,
        boolean perecedero,
        Long proveedorId,
        String proveedor,
        EstadoSugerencia estado,
        AccionSugerencia accion,
        Long pedidoId,
        String numeroPedido,
        String accionPor,
        LocalDateTime accionAt,
        int diasHistorial,
        BigDecimal demandaDiaria,
        BigDecimal desviacionDemanda,
        BigDecimal leadTimeDias,
        BigDecimal desviacionLeadTime,
        int muestrasLeadTime,
        BigDecimal costoUnitario,
        BigDecimal costoPedido,
        BigDecimal costoAlmacenAnual,
        BigDecimal nivelServicio,
        BigDecimal factorZ,
        BigDecimal stockActual,
        BigDecimal enTransito,
        BigDecimal vidaUtilDias,
        EscenarioResumen pesimista,
        EscenarioResumen estandar,
        EscenarioResumen optimista,
        String motivo,
        /** Variación reciente del costo del proveedor (fracción); null si no hay compras suficientes. */
        BigDecimal variacionCosto,
        /** Días que se adelantó la compra por alza de costo (0 = no se adelantó). */
        int diasAnticipo
) {
}
