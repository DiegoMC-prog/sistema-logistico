package com.tienda.backend.abastecimiento.application.port.out;

import com.tienda.backend.abastecimiento.application.model.CalculoResumen;
import com.tienda.backend.abastecimiento.application.model.CompraReciente;
import com.tienda.backend.abastecimiento.application.model.GuardarParametrosComando;
import com.tienda.backend.abastecimiento.application.model.OrigenCalculo;
import com.tienda.backend.abastecimiento.application.model.ParametrosProducto;
import com.tienda.backend.abastecimiento.application.model.ProductoReorden;
import com.tienda.backend.abastecimiento.application.model.SugerenciaFiltro;
import com.tienda.backend.abastecimiento.application.model.SugerenciaParaPedido;
import com.tienda.backend.abastecimiento.application.model.SugerenciaResumen;
import com.tienda.backend.abastecimiento.domain.model.SugerenciaCalculada;
import com.tienda.backend.shared.application.model.PageResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Lecturas por lote (una consulta por concepto, no por producto) y persistencia de cálculos y sugerencias. */
public interface AbastecimientoRepositoryPort {

    // ---- datos de entrada
    List<ProductoReorden> productosEvaluables();

    /** productoId -> (día -> unidades base vendidas) desde {@code desde} (inclusive) hasta {@code hasta} (exclusive). */
    Map<Long, Map<LocalDate, Double>> ventasDiarias(LocalDate desde, LocalDate hasta);

    /** productoId -> fecha de su primera venta completada. */
    Map<Long, LocalDate> primeraVenta();

    /** Último pedido no cancelado de cada par producto-proveedor. */
    List<CompraReciente> comprasRecientes();

    /** productoId -> costo de compra del lote más reciente (respaldo si nunca hubo un pedido). */
    Map<Long, Double> costoUltimoLote();

    /** proveedorId -> días entre emisión y primera recepción de cada pedido recibido desde {@code desde}. */
    Map<Long, List<Double>> leadTimesPorProveedor(LocalDate desde);

    /** productoId -> stock en lotes no vencidos a {@code hoy}. */
    Map<Long, Double> stockVendible(LocalDate hoy);

    /** productoId -> cantidad pedida y no recibida (pedidos BORRADOR, ENVIADO o RECIBIDO_PARCIAL). */
    Map<Long, Double> enTransito();

    /** productoId -> vida útil media (días entre el ingreso y el vencimiento) de sus lotes creados desde {@code desde}. */
    Map<Long, Double> vidaUtilMedia(LocalDate desde);

    // ---- cálculos y sugerencias
    Long guardarCalculo(OrigenCalculo origen, String usuario, int diasVentana, int historialMinimoDias,
                        List<SugerenciaCalculada> sugerencias);

    boolean existeCalculo();

    Optional<CalculoResumen> ultimoCalculo();

    Optional<CalculoResumen> calculo(Long id);

    PageResult<SugerenciaResumen> listar(Long calculoId, SugerenciaFiltro filtro, int page, int size);

    Optional<SugerenciaResumen> sugerencia(Long id);

    /** Bloquea las sugerencias (FOR UPDATE) para convertirlas sin carreras. */
    List<SugerenciaParaPedido> bloquearParaPedido(List<Long> ids);

    void marcarPedidoCreado(List<Long> sugerenciaIds, Long pedidoId, String usuario);

    /** true si pasó de PENDIENTE a DESCARTADA. */
    boolean descartar(Long id, String usuario);

    // ---- parámetros por producto
    /** Vacío si el producto no existe; los efectivos combinan los propios con los globales recibidos. */
    Optional<ParametrosProducto> parametros(Long productoId, double costoPedido, double costoAlmacenPct,
                                           double nivelServicio);

    void guardarParametros(Long productoId, GuardarParametrosComando comando, String usuario);

    boolean proveedorActivo(Long proveedorId);
}
