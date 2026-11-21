package com.tienda.backend.abastecimiento.domain.model;

import java.util.List;

/**
 * Todo lo que la calculadora necesita saber de un producto. Las cantidades van en unidad base y los tiempos en días.
 *
 * @param diasHistorial       días desde la primera venta del producto
 * @param historialMinimoDias historial exigido antes de sugerir (recomendación del documento: 3 meses)
 * @param demandaDiaria       unidades vendidas por día en la ventana de análisis (días sin venta = 0)
 * @param leadTimes           días entre la emisión y la primera recepción de cada pedido del proveedor
 * @param leadTimeDefectoDias lead time asumido si el proveedor todavía no tiene recepciones
 * @param proveedorId         proveedor a quien se pediría; null si no hay ninguno
 * @param costoUnitario       último costo de compra (adaptativo); null si nunca se compró
 * @param enTransito          pedido y aún no recibido (pedidos en borrador, enviados o recibidos en parte)
 * @param vidaUtilDias        vida útil media de los lotes del producto; null si no es perecedero o no hay datos
 */
public record DatosReorden(
        Long productoId,
        boolean perecedero,
        int diasHistorial,
        int historialMinimoDias,
        double[] demandaDiaria,
        List<Double> leadTimes,
        double leadTimeDefectoDias,
        Long proveedorId,
        Double costoUnitario,
        double stockActual,
        double enTransito,
        Double vidaUtilDias,
        ParametrosReorden parametros
) {
}
