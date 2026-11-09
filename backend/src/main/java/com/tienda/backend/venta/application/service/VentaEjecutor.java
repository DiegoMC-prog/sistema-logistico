package com.tienda.backend.venta.application.service;

import com.tienda.backend.venta.application.model.AsignacionStock;
import com.tienda.backend.venta.application.model.ClienteVenta;
import com.tienda.backend.venta.application.model.ItemStock;
import com.tienda.backend.venta.application.model.ItemVentaComando;
import com.tienda.backend.venta.application.model.MetodoPagoRef;
import com.tienda.backend.venta.application.model.PagoVentaComando;
import com.tienda.backend.venta.application.model.PresentacionVenta;
import com.tienda.backend.venta.application.model.RegistrarVentaComando;
import com.tienda.backend.venta.application.model.ReposicionStock;
import com.tienda.backend.venta.application.model.TurnoVenta;
import com.tienda.backend.venta.application.port.out.CatalogoVentaPort;
import com.tienda.backend.venta.application.port.out.ClienteVentaPort;
import com.tienda.backend.venta.application.port.out.CreditoVentaPort;
import com.tienda.backend.venta.application.port.out.MetodoPagoVentaPort;
import com.tienda.backend.venta.application.port.out.NumeroVentaGeneradorPort;
import com.tienda.backend.venta.application.port.out.PrecioVentaPort;
import com.tienda.backend.venta.application.port.out.StockVentaPort;
import com.tienda.backend.venta.application.port.out.TurnoVentaPort;
import com.tienda.backend.venta.application.port.out.VentaDevolucionConsultaPort;
import com.tienda.backend.venta.application.port.out.VentaRepositoryPort;
import com.tienda.backend.venta.domain.exception.VentaConDevolucionesException;
import com.tienda.backend.venta.domain.exception.VentaDeCambioNoAnulableException;
import com.tienda.backend.venta.domain.exception.ClienteInactivoException;
import com.tienda.backend.venta.domain.exception.IdempotenciaConflictoException;
import com.tienda.backend.venta.domain.exception.MetodoPagoInactivoException;
import com.tienda.backend.venta.domain.exception.PagoInsuficienteException;
import com.tienda.backend.venta.domain.exception.ProductoInactivoVentaException;
import com.tienda.backend.venta.domain.exception.SinTurnoAbiertoException;
import com.tienda.backend.venta.domain.exception.TurnoCerradoNoAnulableException;
import com.tienda.backend.venta.domain.exception.VentaDatosInvalidosException;
import com.tienda.backend.venta.domain.exception.VentaNotFoundException;
import com.tienda.backend.venta.domain.exception.VueltoSinEfectivoException;
import com.tienda.backend.venta.domain.model.LineaVenta;
import com.tienda.backend.venta.domain.model.LoteVendido;
import com.tienda.backend.venta.domain.model.PagoVenta;
import com.tienda.backend.venta.domain.model.Venta;
import com.tienda.backend.cliente.domain.exception.ClienteNotFoundException;
import com.tienda.backend.metodopago.domain.exception.MetodoPagoNotFoundException;
import com.tienda.backend.metodopago.domain.model.MetodoPago;
import com.tienda.backend.presentacion.domain.exception.PresentacionNotFoundException;
import com.tienda.backend.shared.application.port.out.UsuarioActualPort;
import com.tienda.backend.shared.domain.exception.AccesoDenegadoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Único lugar donde se crea o anula una venta. Cada operación es UNA transacción: si cualquier paso falla (stock,
 * precio, pagos...) no queda nada guardado, ni la venta ni el movimiento de stock. El orden de bloqueos es siempre
 * el mismo (turno, luego stock, luego venta) para no provocar interbloqueos. La nota de venta con nombres la arma
 * {@link VentaService} después del commit.
 */
@Service
@Transactional
@RequiredArgsConstructor
class VentaEjecutor {

    /** Permisos (deben coincidir con los de la tabla permisos). */
    private static final String PERMISO_PRECIO_MODIFICAR = "VENTA_PRECIO_MODIFICAR";
    private static final String PERMISO_PRECIO_BAJO_COSTO = "VENTA_PRECIO_BAJO_COSTO";
    private static final int MAX_ITEMS = 100;
    private static final int MAX_PAGOS = 10;
    private static final int ESCALA_CANTIDAD = 4;

    private final VentaRepositoryPort ventaRepositoryPort;
    private final NumeroVentaGeneradorPort numeroVentaGeneradorPort;
    private final TurnoVentaPort turnoVentaPort;
    private final ClienteVentaPort clienteVentaPort;
    private final MetodoPagoVentaPort metodoPagoVentaPort;
    private final CatalogoVentaPort catalogoVentaPort;
    private final PrecioVentaPort precioVentaPort;
    private final StockVentaPort stockVentaPort;
    private final UsuarioActualPort usuarioActualPort;
    private final VentaDevolucionConsultaPort devolucionConsultaPort;
    private final CreditoVentaPort creditoVentaPort;

    /** Resultado interno: la venta y si se creó ahora (false = reintento idempotente). */
    record Registrada(Venta venta, boolean creada, BigDecimal creditoAplicado) {
        Registrada(Venta venta, boolean creada) {
            this(venta, creada, BigDecimal.ZERO);
        }
    }

    Registrada registrar(RegistrarVentaComando cmd) {
        Long usuarioId = usuarioActualPort.id();
        validarComando(cmd);
        String huella = cmd.idempotencyKey() == null ? null : huella(cmd);

        // Reintento: misma clave del mismo usuario = misma venta, siempre que el contenido sea el mismo
        if (cmd.idempotencyKey() != null) {
            Optional<Venta> previa = ventaRepositoryPort.findByUsuarioIdAndIdempotencyKey(usuarioId, cmd.idempotencyKey());
            if (previa.isPresent()) {
                return new Registrada(resolverReintento(previa.get(), huella), false);
            }
        }

        boolean cambiaPrecio = cmd.items().stream().anyMatch(i -> i.precioUnitario() != null);
        if (cambiaPrecio && !usuarioActualPort.tienePermiso(PERMISO_PRECIO_MODIFICAR)) {
            throw new AccesoDenegadoException("No tiene permiso para cambiar el precio de una línea.");
        }

        // 1. Turno del cajero, bloqueado en modo compartido: el cierre de caja espera a las ventas en curso
        TurnoVenta turno = turnoVentaPort.turnoAbiertoDeUsuario(usuarioId).orElseThrow(SinTurnoAbiertoException::new);
        turno = turnoVentaPort.bloquearCompartido(turno.turnoId()).filter(TurnoVenta::abierto)
                .orElseThrow(SinTurnoAbiertoException::new);

        // 2. Cliente, líneas con su precio y métodos de pago (todo validado antes de tocar el stock)
        ClienteVenta cliente = null;
        if (cmd.clienteId() != null) {
            cliente = clienteVentaPort.buscarPorId(cmd.clienteId())
                    .orElseThrow(() -> new ClienteNotFoundException(cmd.clienteId()));
            if (!cliente.activo()) {
                throw new ClienteInactivoException(cliente.nombre());
            }
        }
        Long tipoClienteId = cliente == null ? null : cliente.tipoClienteId();
        List<ArmadorLineas.ItemResuelto> items = resolverItems(cmd.items(), tipoClienteId);
        Map<Long, MetodoPagoRef> metodos = resolverMetodos(cmd.pagos());
        boolean aCredito = metodos.values().stream().anyMatch(m -> MetodoPago.NOMBRE_CREDITO_CLIENTE.equals(m.nombre()));
        if (aCredito && cliente == null) {
            throw new VentaDatosInvalidosException("Una venta a crédito requiere seleccionar un cliente");
        }

        // 3. Insertar la venta primero: con una clave repetida concurrente, el índice único hace esperar y rechaza al segundo
        Venta venta = ventaRepositoryPort.save(Venta.iniciar(numeroVentaGeneradorPort.siguiente(), turno.turnoId(),
                usuarioId, cliente == null ? null : cliente.id(), tipoClienteId, turno.depositoId(),
                cmd.idempotencyKey(), huella));

        // 4. Stock por FEFO (bloquea los lotes candidatos y falla si no alcanza)
        List<ItemStock> itemsStock = items.stream()
                .map(i -> new ItemStock(i.presentacion().productoId(), i.presentacion().presentacionId(), i.cantidad()))
                .toList();
        List<AsignacionStock> asignaciones = stockVentaPort.salir(turno.depositoId(),
                "Venta " + venta.getNumeroVenta(), itemsStock, venta.getId());

        // 5. Líneas, totales y pagos
        List<LineaVenta> lineas = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            int indice = i;
            List<AsignacionStock> deLaLinea = asignaciones.stream().filter(a -> a.itemIndice() == indice).toList();
            exigirPrecioNoBajoCosto(items.get(i), deLaLinea);
            lineas.addAll(ArmadorLineas.armar(items.get(i), deLaLinea));
        }
        BigDecimal subtotal = lineas.stream().map(LineaVenta::bruto).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal descuento = lineas.stream().map(LineaVenta::descuento).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = subtotal.subtract(descuento);

        // Crédito de una devolución (solo en un cambio): cubre parte del total y el resto se cobra con los pagos
        BigDecimal credito = cmd.creditoDisponible().min(total).setScale(2, java.math.RoundingMode.HALF_UP);
        List<PagoVenta> pagos = new ArrayList<>();
        if (credito.signum() > 0) {
            MetodoPagoRef metodoCredito = metodoPagoVentaPort.buscarPorNombre(MetodoPago.NOMBRE_CREDITO)
                    .orElseThrow(() -> new IllegalStateException(
                            "No existe el método de pago de sistema " + MetodoPago.NOMBRE_CREDITO));
            pagos.add(new PagoVenta(null, metodoCredito.id(), credito, null));
        }
        BigDecimal[] vuelto = new BigDecimal[1];
        pagos.addAll(resolverPagos(cmd.pagos(), metodos, total.subtract(credito), vuelto));

        venta.completar(lineas, pagos, subtotal, descuento, total, vuelto[0]);
        Venta guardada = ventaRepositoryPort.save(venta);

        // La parte pagada con CREDITO queda como deuda del cliente (misma transacción: si falla, no queda la venta)
        BigDecimal deuda = pagos.stream()
                .filter(p -> metodos.containsKey(p.metodoPagoId())
                        && MetodoPago.NOMBRE_CREDITO_CLIENTE.equals(metodos.get(p.metodoPagoId()).nombre()))
                .map(PagoVenta::monto).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (deuda.signum() > 0) {
            creditoVentaPort.crearCuenta(cliente.id(), guardada.getId(), deuda);
        }
        return new Registrada(guardada, true, credito);
    }

    Venta anular(Long ventaId, String motivo) {
        Venta venta = ventaRepositoryPort.findByIdParaActualizar(ventaId)
                .orElseThrow(() -> new VentaNotFoundException(ventaId));
        venta.anular(usuarioActualPort.nombre(), motivo);   // valida estado y motivo antes de tocar nada

        // Anular repondría todo el stock: con devoluciones ya hechas se duplicaría. Y la venta de un cambio depende de su devolución
        if (devolucionConsultaPort.tieneDevoluciones(ventaId)) {
            throw new VentaConDevolucionesException(venta.getNumeroVenta());
        }
        if (devolucionConsultaPort.esVentaDeCambio(ventaId)) {
            throw new VentaDeCambioNoAnulableException(venta.getNumeroVenta());
        }

        // Solo con el turno abierto: después la caja ya se cuadró y corresponde una devolución
        TurnoVenta turno = turnoVentaPort.bloquearCompartido(venta.getTurnoId()).orElse(null);
        if (turno == null || !turno.abierto()) {
            throw new TurnoCerradoNoAnulableException(venta.getNumeroVenta());
        }

        // Cada lote vuelve a su sitio; un lote repetido en varias líneas se suma
        Map<Long, ReposicionStock> repos = new LinkedHashMap<>();
        for (LineaVenta linea : venta.getLineas()) {
            for (LoteVendido lote : linea.lotes()) {
                repos.merge(lote.loteId(), new ReposicionStock(linea.productoId(), lote.loteId(), lote.cantidadBase()),
                        (a, b) -> new ReposicionStock(a.productoId(), a.loteId(), a.cantidadBase().add(b.cantidadBase())));
            }
        }
        stockVentaPort.reponer(venta.getDepositoId(), "Anulación de la venta " + venta.getNumeroVenta(),
                List.copyOf(repos.values()), venta.getId());
        creditoVentaPort.anularCuenta(venta.getId());   // falla si ya tiene abonos: se revierte toda la anulación
        return ventaRepositoryPort.save(venta);
    }

    // ---------- validaciones y resolución ----------

    private void validarComando(RegistrarVentaComando cmd) {
        if (cmd.items().isEmpty()) {
            throw new VentaDatosInvalidosException("La venta debe tener al menos una línea");
        }
        if (cmd.items().size() > MAX_ITEMS) {
            throw new VentaDatosInvalidosException("Una venta admite hasta " + MAX_ITEMS + " líneas");
        }
        if (cmd.pagos().size() > MAX_PAGOS) {
            throw new VentaDatosInvalidosException("Una venta admite hasta " + MAX_PAGOS + " pagos");
        }
        String clave = cmd.idempotencyKey();
        if (clave != null && !clave.matches("^[A-Za-z0-9._-]{8,64}$")) {
            throw new VentaDatosInvalidosException(
                    "La clave de idempotencia debe tener entre 8 y 64 caracteres (letras, números, punto, guion y guion bajo)");
        }
        for (ItemVentaComando item : cmd.items()) {
            if (item.presentacionId() == null) {
                throw new VentaDatosInvalidosException("La presentación es obligatoria en cada línea");
            }
            exigirCantidad(item.cantidad());
            if (item.precioUnitario() != null) {
                if (item.precioUnitario().signum() < 0 || item.precioUnitario().stripTrailingZeros().scale() > ESCALA_CANTIDAD) {
                    throw new VentaDatosInvalidosException("El precio manual no puede ser negativo y admite hasta 4 decimales");
                }
                if (item.motivoPrecio() == null || item.motivoPrecio().isBlank()) {
                    throw new VentaDatosInvalidosException("Un precio manual requiere indicar el motivo");
                }
                if (item.motivoPrecio().trim().length() > 150) {
                    throw new VentaDatosInvalidosException("El motivo del precio no puede superar los 150 caracteres");
                }
            }
        }
        for (PagoVentaComando pago : cmd.pagos()) {
            if (pago.metodoPagoId() == null) {
                throw new VentaDatosInvalidosException("El método de pago es obligatorio en cada pago");
            }
            if (pago.monto() == null || pago.monto().signum() <= 0 || pago.monto().stripTrailingZeros().scale() > 2) {
                throw new VentaDatosInvalidosException("El monto de cada pago debe ser mayor a cero y admite hasta 2 decimales");
            }
            if (pago.referencia() != null && pago.referencia().length() > 100) {
                throw new VentaDatosInvalidosException("La referencia del pago no puede superar los 100 caracteres");
            }
        }
    }

    private void exigirCantidad(BigDecimal cantidad) {
        if (cantidad == null || cantidad.signum() <= 0) {
            throw new VentaDatosInvalidosException("La cantidad de cada línea debe ser mayor a cero");
        }
        if (cantidad.stripTrailingZeros().scale() > ESCALA_CANTIDAD) {
            throw new VentaDatosInvalidosException("Las cantidades admiten hasta " + ESCALA_CANTIDAD + " decimales");
        }
    }

    private List<ArmadorLineas.ItemResuelto> resolverItems(List<ItemVentaComando> items, Long tipoClienteId) {
        List<ArmadorLineas.ItemResuelto> resueltos = new ArrayList<>();
        for (ItemVentaComando item : items) {
            PresentacionVenta presentacion = catalogoVentaPort.buscarPresentacion(item.presentacionId())
                    .orElseThrow(() -> new PresentacionNotFoundException(item.presentacionId()));
            if (!presentacion.productoActivo()) {
                throw new ProductoInactivoVentaException(presentacion.productoNombre());
            }
            boolean manual = item.precioUnitario() != null;
            BigDecimal precio = manual ? item.precioUnitario()
                    : precioVentaPort.precioAplicable(item.presentacionId(), item.cantidad(), tipoClienteId);
            resueltos.add(new ArmadorLineas.ItemResuelto(presentacion, item.cantidad(), precio, manual,
                    manual ? item.motivoPrecio().trim() : null));
        }
        return resueltos;
    }

    private Map<Long, MetodoPagoRef> resolverMetodos(List<PagoVentaComando> pagos) {
        Set<Long> ids = new HashSet<>();
        for (PagoVentaComando pago : pagos) {
            if (!ids.add(pago.metodoPagoId())) {
                throw new VentaDatosInvalidosException(
                        "El método de pago " + pago.metodoPagoId() + " aparece más de una vez; sume sus montos en un solo pago");
            }
        }
        Map<Long, MetodoPagoRef> metodos = ids.isEmpty() ? Map.of() : metodoPagoVentaPort.buscarPorIds(ids);
        for (Long id : ids) {
            MetodoPagoRef metodo = metodos.get(id);
            if (metodo == null) {
                throw new MetodoPagoNotFoundException(id);
            }
            if (MetodoPago.NOMBRE_CREDITO.equals(metodo.nombre())) {
                throw new VentaDatosInvalidosException("El crédito de una devolución no se puede usar como método de pago");
            }
            if (!metodo.activo()) {
                throw new MetodoPagoInactivoException(metodo.nombre());
            }
        }
        return metodos;
    }

    /** Un precio manual por debajo del costo de algún lote usado exige además el permiso de supervisor. */
    private void exigirPrecioNoBajoCosto(ArmadorLineas.ItemResuelto item, List<AsignacionStock> asignaciones) {
        if (!item.manual() || usuarioActualPort.tienePermiso(PERMISO_PRECIO_BAJO_COSTO)) {
            return;
        }
        BigDecimal factor = item.presentacion().factorConversion();
        for (AsignacionStock a : asignaciones) {
            if (item.precio().compareTo(a.costoUnitario().multiply(factor)) < 0) {
                throw new AccesoDenegadoException("El precio manual de " + item.presentacion().productoNombre()
                        + " está por debajo de su costo; se requiere autorización de un supervisor.");
            }
        }
    }

    /**
     * Pagos que cubren el total. Si pagan de más, el sobrante es el vuelto y solo se admite sobre un pago en efectivo
     * mayor al sobrante; el efectivo se guarda neto, para que el efectivo esperado de caja sea el real.
     */
    private List<PagoVenta> resolverPagos(List<PagoVentaComando> pagos, Map<Long, MetodoPagoRef> metodos,
                                          BigDecimal total, BigDecimal[] vueltoSalida) {
        BigDecimal cero = BigDecimal.ZERO.setScale(2);
        vueltoSalida[0] = cero;
        if (total.signum() == 0) {
            if (!pagos.isEmpty()) {
                throw new VentaDatosInvalidosException("La venta no tiene importe a cobrar: no debe indicar pagos");
            }
            return List.of();
        }
        if (pagos.isEmpty()) {
            throw new VentaDatosInvalidosException("Debe indicar los pagos de la venta");
        }

        BigDecimal suma = pagos.stream().map(PagoVentaComando::monto).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (suma.compareTo(total) < 0) {
            throw new PagoInsuficienteException(total.subtract(suma));
        }
        BigDecimal sobrante = suma.subtract(total);

        List<PagoVenta> resultado = new ArrayList<>();
        for (PagoVentaComando pago : pagos) {
            BigDecimal monto = pago.monto();
            boolean efectivo = MetodoPago.NOMBRE_EFECTIVO.equals(metodos.get(pago.metodoPagoId()).nombre());
            if (sobrante.signum() > 0 && efectivo) {
                if (monto.compareTo(sobrante) <= 0) {
                    throw new VueltoSinEfectivoException();
                }
                monto = monto.subtract(sobrante);
                vueltoSalida[0] = sobrante;
                sobrante = BigDecimal.ZERO;
            }
            resultado.add(new PagoVenta(null, pago.metodoPagoId(), monto, pago.referencia()));
        }
        if (sobrante.signum() > 0) {
            throw new VueltoSinEfectivoException();
        }
        return resultado;
    }

    // ---------- idempotencia ----------

    private Venta resolverReintento(Venta previa, String huella) {
        if (!huella.equals(previa.getIdempotencyHash())) {
            throw new IdempotenciaConflictoException();
        }
        return previa;
    }

    /** Huella SHA-256 del contenido pedido: detecta si alguien reutiliza una clave con otra venta. */
    private String huella(RegistrarVentaComando cmd) {
        StringBuilder sb = new StringBuilder("c=").append(cmd.clienteId());
        for (ItemVentaComando i : cmd.items()) {
            sb.append("|i=").append(i.presentacionId()).append(':').append(numero(i.cantidad())).append(':')
                    .append(i.precioUnitario() == null ? "" : numero(i.precioUnitario())).append(':')
                    .append(i.motivoPrecio() == null ? "" : i.motivoPrecio().trim());
        }
        for (PagoVentaComando p : cmd.pagos()) {
            sb.append("|p=").append(p.metodoPagoId()).append(':').append(numero(p.monto())).append(':')
                    .append(p.referencia() == null ? "" : p.referencia());
        }
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    private static String numero(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString();
    }
}
