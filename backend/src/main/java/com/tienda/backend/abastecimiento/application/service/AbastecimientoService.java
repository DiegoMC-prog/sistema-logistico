package com.tienda.backend.abastecimiento.application.service;

import com.tienda.backend.abastecimiento.application.model.CalculoResumen;
import com.tienda.backend.abastecimiento.application.model.CompraReciente;
import com.tienda.backend.abastecimiento.application.model.ConfigReorden;
import com.tienda.backend.abastecimiento.application.model.CrearPedidosComando;
import com.tienda.backend.abastecimiento.application.model.GuardarParametrosComando;
import com.tienda.backend.abastecimiento.application.model.LineaCompra;
import com.tienda.backend.abastecimiento.application.model.OrigenCalculo;
import com.tienda.backend.abastecimiento.application.model.ParametrosProducto;
import com.tienda.backend.abastecimiento.application.model.PedidoGenerado;
import com.tienda.backend.abastecimiento.application.model.ProductoReorden;
import com.tienda.backend.abastecimiento.application.model.SugerenciaFiltro;
import com.tienda.backend.abastecimiento.application.model.SugerenciaParaPedido;
import com.tienda.backend.abastecimiento.application.model.SugerenciaResumen;
import com.tienda.backend.abastecimiento.application.port.in.ConsultarSugerenciasUseCase;
import com.tienda.backend.abastecimiento.application.port.in.GestionarSugerenciasUseCase;
import com.tienda.backend.abastecimiento.application.port.in.ParametrosReordenUseCase;
import com.tienda.backend.abastecimiento.application.port.in.RecalcularSugerenciasUseCase;
import com.tienda.backend.abastecimiento.application.port.out.AbastecimientoRepositoryPort;
import com.tienda.backend.abastecimiento.application.port.out.PedidoCompraPort;
import com.tienda.backend.abastecimiento.domain.exception.ParametrosReordenInvalidosException;
import com.tienda.backend.abastecimiento.domain.exception.SugerenciaNoConvertibleException;
import com.tienda.backend.abastecimiento.domain.exception.SugerenciaNotFoundException;
import com.tienda.backend.abastecimiento.domain.model.AccionSugerencia;
import com.tienda.backend.abastecimiento.domain.model.DatosReorden;
import com.tienda.backend.abastecimiento.domain.model.Escenario;
import com.tienda.backend.abastecimiento.domain.model.ParametrosReorden;
import com.tienda.backend.abastecimiento.domain.model.SugerenciaCalculada;
import com.tienda.backend.abastecimiento.domain.service.CalculadoraReorden;
import com.tienda.backend.producto.domain.exception.ProductoNotFoundException;
import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.shared.application.port.out.UsuarioActualPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AbastecimientoService implements RecalcularSugerenciasUseCase, ConsultarSugerenciasUseCase,
        GestionarSugerenciasUseCase, ParametrosReordenUseCase {

    /** Lead times y vida útil se miden sobre el último año: el comportamiento reciente del proveedor es el que vale. */
    private static final int DIAS_HISTORIA_PROVEEDOR = 365;

    private final AbastecimientoRepositoryPort repositorio;
    private final PedidoCompraPort pedidoCompraPort;
    private final UsuarioActualPort usuarioActualPort;
    private final ConfigReorden config;
    private final CalculadoraReorden calculadora = new CalculadoraReorden();

    public AbastecimientoService(AbastecimientoRepositoryPort repositorio, PedidoCompraPort pedidoCompraPort,
                                 UsuarioActualPort usuarioActualPort,
                                 @Value("${app.reorden.costo-pedido:50}") double costoPedido,
                                 @Value("${app.reorden.costo-almacen-pct:0.25}") double costoAlmacenPct,
                                 @Value("${app.reorden.nivel-servicio:0.95}") double nivelServicio,
                                 @Value("${app.reorden.dias-ventana:90}") int diasVentana,
                                 @Value("${app.reorden.historial-minimo-dias:90}") int historialMinimoDias,
                                 @Value("${app.reorden.lead-time-defecto-dias:7}") double leadTimeDefectoDias) {
        this.repositorio = repositorio;
        this.pedidoCompraPort = pedidoCompraPort;
        this.usuarioActualPort = usuarioActualPort;
        // Valida los globales al arrancar: una configuración inválida debe fallar pronto, no en la tarea nocturna
        new ParametrosReorden(costoPedido, costoAlmacenPct, nivelServicio);
        this.config = new ConfigReorden(costoPedido, costoAlmacenPct, nivelServicio, Math.max(diasVentana, 7),
                Math.max(historialMinimoDias, 0), Math.max(leadTimeDefectoDias, 0));
    }

    // ------------------------------------------------------------------ recálculo

    @Override
    @Transactional
    public CalculoResumen recalcular(OrigenCalculo origen) {
        LocalDate hoy = LocalDate.now();
        LocalDate haceUnAnio = hoy.minusDays(DIAS_HISTORIA_PROVEEDOR);

        List<ProductoReorden> productos = repositorio.productosEvaluables();
        Map<Long, Map<LocalDate, Double>> ventas = repositorio.ventasDiarias(hoy.minusDays(config.diasVentana()), hoy);
        Map<Long, LocalDate> primeraVenta = repositorio.primeraVenta();
        Map<Long, List<CompraReciente>> compras = repositorio.comprasRecientes().stream()
                .collect(Collectors.groupingBy(CompraReciente::productoId));
        Map<Long, Double> costoLote = repositorio.costoUltimoLote();
        Map<Long, List<Double>> leadTimes = repositorio.leadTimesPorProveedor(haceUnAnio);
        Map<Long, Double> stock = repositorio.stockVendible(hoy);
        Map<Long, Double> transito = repositorio.enTransito();
        Map<Long, Double> vidaUtil = repositorio.vidaUtilMedia(haceUnAnio);

        List<SugerenciaCalculada> resultados = new ArrayList<>(productos.size());
        for (ProductoReorden producto : productos) {
            Long id = producto.productoId();
            LocalDate primera = primeraVenta.get(id);
            int diasHistorial = primera == null ? 0 : (int) ChronoUnit.DAYS.between(primera, hoy);

            // Ventana: los últimos N días completos (sin hoy); si el producto es más nuevo, solo los días que existe
            int largo = Math.max(1, Math.min(config.diasVentana(), diasHistorial));
            double[] serie = new double[largo];
            Map<LocalDate, Double> porDia = ventas.getOrDefault(id, Map.of());
            for (int i = 0; i < largo; i++) {
                serie[i] = porDia.getOrDefault(hoy.minusDays(largo - i), 0.0);
            }

            List<CompraReciente> comprasProducto = compras.getOrDefault(id, List.of());
            Long proveedorId = producto.proveedorPreferidoId() != null ? producto.proveedorPreferidoId()
                    : comprasProducto.stream().max(Comparator.comparing(CompraReciente::fecha))
                            .map(CompraReciente::proveedorId).orElse(null);
            Double costo = costoAdaptativo(comprasProducto, proveedorId, costoLote.get(id));

            ParametrosReorden parametros = new ParametrosReorden(
                    valor(producto.costoPedido(), config.costoPedido()),
                    valor(producto.costoAlmacenPct(), config.costoAlmacenPct()),
                    valor(producto.nivelServicio(), config.nivelServicio()));

            DatosReorden datos = new DatosReorden(id, producto.perecedero(), diasHistorial, config.historialMinimoDias(),
                    serie, proveedorId == null ? List.of() : leadTimes.getOrDefault(proveedorId, List.of()),
                    config.leadTimeDefectoDias(), proveedorId, costo, stock.getOrDefault(id, 0.0),
                    transito.getOrDefault(id, 0.0), producto.perecedero() ? vidaUtil.get(id) : null, parametros);
            resultados.add(calculadora.calcular(datos));
        }

        Long calculoId = repositorio.guardarCalculo(origen, usuarioActualPort.nombre(), config.diasVentana(),
                config.historialMinimoDias(), resultados);
        return repositorio.calculo(calculoId).orElseThrow();
    }

    /** Último costo pactado con el proveedor elegido; si nunca se le compró, el último de cualquiera; si no, el del lote. */
    private static Double costoAdaptativo(List<CompraReciente> compras, Long proveedorId, Double costoLote) {
        Comparator<CompraReciente> porFecha = Comparator.comparing(CompraReciente::fecha);
        return compras.stream().filter(c -> c.proveedorId().equals(proveedorId)).max(porFecha)
                .or(() -> compras.stream().max(porFecha))
                .map(CompraReciente::costoUnitario)
                .orElse(costoLote);
    }

    private static double valor(Double propio, double global) {
        return propio != null ? propio : global;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeCalculo() {
        return repositorio.existeCalculo();
    }

    // ------------------------------------------------------------------ consultas

    @Override
    @Transactional(readOnly = true)
    public Optional<CalculoResumen> ultimoCalculo() {
        return repositorio.ultimoCalculo();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<SugerenciaResumen> listar(SugerenciaFiltro filtro, int page, int size) {
        int p = PageResult.safePage(page);
        int s = PageResult.safeSize(size);
        return repositorio.ultimoCalculo()
                .map(c -> repositorio.listar(c.id(), filtro, p, s))
                .orElseGet(() -> new PageResult<>(List.of(), p, s, 0, 0));
    }

    @Override
    @Transactional(readOnly = true)
    public SugerenciaResumen obtener(Long id) {
        return repositorio.sugerencia(id).orElseThrow(() -> new SugerenciaNotFoundException(id));
    }

    // ------------------------------------------------------------------ acciones

    @Override
    @Transactional
    public List<PedidoGenerado> crearPedidos(CrearPedidosComando comando) {
        if (comando.depositoDestinoId() == null) {
            throw new ParametrosReordenInvalidosException("Elija el depósito donde se recibirá la mercadería");
        }
        if (comando.items() == null || comando.items().isEmpty()) {
            throw new ParametrosReordenInvalidosException("Elija al menos una sugerencia");
        }
        Escenario escenario = comando.escenario() == null ? Escenario.ESTANDAR : comando.escenario();

        Map<Long, BigDecimal> cantidadPropia = new LinkedHashMap<>();
        for (CrearPedidosComando.Item item : comando.items()) {
            if (item.sugerenciaId() == null) {
                throw new ParametrosReordenInvalidosException("Falta el id de una sugerencia");
            }
            cantidadPropia.put(item.sugerenciaId(), item.cantidad());
        }

        List<Long> ids = new ArrayList<>(cantidadPropia.keySet());
        Map<Long, SugerenciaParaPedido> bloqueadas = repositorio.bloquearParaPedido(ids).stream()
                .collect(Collectors.toMap(SugerenciaParaPedido::id, Function.identity()));

        // Proveedor -> sugerencias, en el orden recibido
        Map<Long, List<SugerenciaParaPedido>> porProveedor = new LinkedHashMap<>();
        Map<Long, BigDecimal> cantidades = new LinkedHashMap<>();
        for (Long id : ids) {
            SugerenciaParaPedido s = bloqueadas.get(id);
            if (s == null) {
                throw new SugerenciaNotFoundException(id);
            }
            if (s.accion() != AccionSugerencia.PENDIENTE) {
                throw new SugerenciaNoConvertibleException("La sugerencia de " + s.producto() + " ya fue "
                        + (s.accion() == AccionSugerencia.PEDIDO_CREADO ? "convertida en pedido" : "descartada") + ".");
            }
            if (s.proveedorId() == null) {
                throw new SugerenciaNoConvertibleException("La sugerencia de " + s.producto()
                        + " no tiene proveedor: asígnele uno en los parámetros de reorden y recalcule.");
            }
            BigDecimal cantidad = cantidadPropia.get(id) != null ? cantidadPropia.get(id) : switch (escenario) {
                case PESIMISTA -> s.cantidadPesimista();
                case ESTANDAR -> s.cantidadEstandar();
                case OPTIMISTA -> s.cantidadOptimista();
            };
            if (cantidad == null || cantidad.signum() <= 0) {
                throw new ParametrosReordenInvalidosException("La cantidad sugerida de " + s.producto()
                        + " en el escenario " + escenario.name().toLowerCase() + " es 0: indique una cantidad.");
            }
            cantidades.put(id, cantidad.setScale(0, RoundingMode.CEILING));
            porProveedor.computeIfAbsent(s.proveedorId(), k -> new ArrayList<>()).add(s);
        }

        String observacion = comando.observacion() != null && !comando.observacion().isBlank()
                ? comando.observacion().trim()
                : "Generado desde las sugerencias de reorden (escenario " + escenario.name().toLowerCase() + ").";
        String usuario = usuarioActualPort.nombre();

        List<PedidoGenerado> pedidos = new ArrayList<>();
        for (Map.Entry<Long, List<SugerenciaParaPedido>> grupo : porProveedor.entrySet()) {
            List<LineaCompra> lineas = grupo.getValue().stream()
                    .map(s -> new LineaCompra(s.productoId(), cantidades.get(s.id()), s.costoUnitario()))
                    .toList();
            PedidoGenerado pedido = pedidoCompraPort.crearBorrador(grupo.getKey(), comando.depositoDestinoId(),
                    observacion, lineas);
            repositorio.marcarPedidoCreado(grupo.getValue().stream().map(SugerenciaParaPedido::id).toList(),
                    pedido.pedidoId(), usuario);
            pedidos.add(pedido);
        }
        return pedidos;
    }

    @Override
    @Transactional
    public SugerenciaResumen descartar(Long id) {
        SugerenciaResumen actual = obtener(id);
        if (!repositorio.descartar(id, usuarioActualPort.nombre())) {
            throw new SugerenciaNoConvertibleException("La sugerencia de " + actual.producto()
                    + " ya no está pendiente.");
        }
        return obtener(id);
    }

    // ------------------------------------------------------------------ parámetros

    @Override
    @Transactional(readOnly = true)
    public ParametrosProducto obtenerParametros(Long productoId) {
        return repositorio.parametros(productoId, config.costoPedido(), config.costoAlmacenPct(), config.nivelServicio())
                .orElseThrow(() -> new ProductoNotFoundException(productoId));
    }

    @Override
    @Transactional
    public ParametrosProducto guardarParametros(Long productoId, GuardarParametrosComando comando) {
        obtenerParametros(productoId);
        // Construir los efectivos valida rangos con las mismas reglas que usa el cálculo
        new ParametrosReorden(
                comando.costoPedido() == null ? config.costoPedido() : comando.costoPedido().doubleValue(),
                comando.costoAlmacenPct() == null ? config.costoAlmacenPct() : comando.costoAlmacenPct().doubleValue(),
                comando.nivelServicio() == null ? config.nivelServicio() : comando.nivelServicio().doubleValue());
        if (comando.proveedorId() != null && !repositorio.proveedorActivo(comando.proveedorId())) {
            throw new ParametrosReordenInvalidosException("El proveedor elegido no existe o está inactivo");
        }
        repositorio.guardarParametros(productoId, comando, usuarioActualPort.nombre());
        return obtenerParametros(productoId);
    }
}
