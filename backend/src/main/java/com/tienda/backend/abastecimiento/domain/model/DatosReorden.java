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
 * @param variacionCosto      variación del costo del proveedor en la ventana de alzas (0.08 = +8 %); null sin datos
 * @param umbralAlza          variación a partir de la cual se considera que el proveedor está subiendo precios
 * @param diasAnticipoAlza    días de demanda que se adelanta la compra cuando hay alza (RF-35)
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
        ParametrosReorden parametros,
        Double variacionCosto,
        double umbralAlza,
        int diasAnticipoAlza
) {
    /** true si el costo del proveedor subió al menos el umbral: conviene comprar antes de la próxima alza. */
    public boolean alzaEsperada() {
        return variacionCosto != null && umbralAlza > 0 && diasAnticipoAlza > 0 && variacionCosto >= umbralAlza;
    }
}
