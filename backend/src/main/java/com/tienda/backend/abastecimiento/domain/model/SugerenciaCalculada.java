package com.tienda.backend.abastecimiento.domain.model;

/**
 * Lo que la calculadora concluye para un producto.
 *
 * @param costoAlmacenAnual H: costo de mantener una unidad durante un año
 * @param factorZ           cuantil de la normal estándar para el nivel de servicio
 * @param motivo            explicación en lenguaje llano de la sugerencia (se muestra al encargado de compras)
 * @param variacionCosto    variación reciente del costo del proveedor (null sin datos suficientes)
 * @param diasAnticipo      días de demanda sumados al punto de reorden por alza de costo (0 = no se adelantó)
 */
public record SugerenciaCalculada(
        Long productoId,
        Long proveedorId,
        EstadoSugerencia estado,
        int diasHistorial,
        Estadistica demanda,
        Estadistica leadTime,
        double costoUnitario,
        double costoPedido,
        double costoAlmacenAnual,
        double nivelServicio,
        double factorZ,
        double stockActual,
        double enTransito,
        Double vidaUtilDias,
        ResultadoEscenario pesimista,
        ResultadoEscenario estandar,
        ResultadoEscenario optimista,
        String motivo,
        Double variacionCosto,
        int diasAnticipo
) {
    public double posicionInventario() {
        return stockActual + enTransito;
    }
}
