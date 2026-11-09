package com.tienda.backend.devolucion.application.service;

import com.tienda.backend.devolucion.application.model.ItemDevueltoComando;
import com.tienda.backend.devolucion.application.model.ItemNuevoComando;
import com.tienda.backend.devolucion.application.model.MetodoPagoRef;
import com.tienda.backend.devolucion.application.model.PagoCambioComando;
import com.tienda.backend.devolucion.application.model.RegistrarCambioComando;
import com.tienda.backend.devolucion.application.model.RegistrarDevolucionComando;
import com.tienda.backend.devolucion.application.model.ResultadoVentaCambio;
import com.tienda.backend.devolucion.application.model.TurnoRef;
import com.tienda.backend.devolucion.application.model.VentaOriginal;
import com.tienda.backend.devolucion.application.port.out.DevolucionRepositoryPort;
import com.tienda.backend.devolucion.application.port.out.MetodoPagoDevolucionPort;
import com.tienda.backend.devolucion.application.port.out.NumeroDevolucionGeneradorPort;
import com.tienda.backend.devolucion.application.port.out.StockDevolucionPort;
import com.tienda.backend.devolucion.application.port.out.TurnoDevolucionPort;
import com.tienda.backend.devolucion.application.port.out.VentaDevolucionPort;
import com.tienda.backend.devolucion.domain.exception.DevolucionDatosInvalidosException;
import com.tienda.backend.devolucion.domain.exception.IdempotenciaConflictoException;
import com.tienda.backend.devolucion.domain.exception.MetodoReembolsoInvalidoException;
import com.tienda.backend.devolucion.domain.exception.SaldoEfectivoInsuficienteException;
import com.tienda.backend.devolucion.domain.exception.SinTurnoAbiertoException;
import com.tienda.backend.devolucion.domain.exception.VentaFueraDePlazoException;
import com.tienda.backend.devolucion.domain.exception.VentaNoDevolvibleException;
import com.tienda.backend.devolucion.domain.model.Devolucion;
import com.tienda.backend.metodopago.domain.exception.MetodoPagoNotFoundException;
import com.tienda.backend.metodopago.domain.model.MetodoPago;
import com.tienda.backend.shared.application.port.out.UsuarioActualPort;
import com.tienda.backend.shared.domain.exception.AccesoDenegadoException;
import com.tienda.backend.venta.domain.exception.MetodoPagoInactivoException;
import com.tienda.backend.venta.domain.exception.VentaNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Único lugar donde se registra una devolución o un cambio. Cada operación es UNA transacción: si cualquier paso falla
 * (cantidades, caja, stock, la venta nueva del cambio...) no queda nada guardado. Orden de bloqueos: venta, turno
 * (para escribir), y luego el stock. La respuesta con nombres la arma {@link DevolucionService} después del commit.
 */
@Service
@Transactional
@RequiredArgsConstructor
class DevolucionEjecutor {

    /** Permiso (debe coincidir con el de la tabla permisos): un cambio también crea una venta. */
    private static final String PERMISO_VENTA_CREAR = "VENTA_CREAR";
    private static final int MAX_ITEMS = 100;
    private static final int MAX_PAGOS = 10;

    private final DevolucionRepositoryPort devolucionRepositoryPort;
    private final VentaDevolucionPort ventaDevolucionPort;
    private final TurnoDevolucionPort turnoDevolucionPort;
    private final MetodoPagoDevolucionPort metodoPagoDevolucionPort;
    private final StockDevolucionPort stockDevolucionPort;
    private final NumeroDevolucionGeneradorPort numeroDevolucionGeneradorPort;
    private final UsuarioActualPort usuarioActualPort;

    /** Plazo en días desde la venta; 0 = sin límite. */
    @Value("${app.devoluciones.dias-maximos:30}")
    private int diasMaximos;

    record Registrada(Devolucion devolucion, boolean creada) {
    }

    /** Lo que tienen en común una devolución simple y un cambio. */
    private record Operacion(Long ventaId, String motivo, List<ItemDevueltoComando> items, String key,
                             boolean cambio, boolean reembolsar, Long metodoPagoId,
                             List<ItemNuevoComando> nuevos, List<PagoCambioComando> pagos) {
    }

    Registrada registrarDevolucion(RegistrarDevolucionComando cmd) {
        return ejecutar(new Operacion(cmd.ventaId(), cmd.motivo(), cmd.items(), cmd.idempotencyKey(), false,
                cmd.reembolsar(), cmd.metodoPagoId(), List.of(), List.of()));
    }

    Registrada registrarCambio(RegistrarCambioComando cmd) {
        return ejecutar(new Operacion(cmd.ventaId(), cmd.motivo(), cmd.devueltos(), cmd.idempotencyKey(), true,
                false, null, cmd.nuevos(), cmd.pagos()));
    }

    private Registrada ejecutar(Operacion op) {
        Long usuarioId = usuarioActualPort.id();
        validar(op);
        String huella = op.key() == null ? null : huella(op);

        // Reintento: misma clave del mismo usuario = misma devolución, siempre que el contenido sea el mismo
        if (op.key() != null) {
            Optional<Devolucion> previa = devolucionRepositoryPort.findByUsuarioIdAndIdempotencyKey(usuarioId, op.key());
            if (previa.isPresent()) {
                if (!huella.equals(previa.get().getIdempotencyHash())) {
                    throw new IdempotenciaConflictoException();
                }
                return new Registrada(previa.get(), false);
            }
        }
        if (op.cambio() && !usuarioActualPort.tienePermiso(PERMISO_VENTA_CREAR)) {
            throw new AccesoDenegadoException("Un cambio crea una venta nueva: requiere también permiso para vender.");
        }

        // 1. Venta (bloqueada) y plazo
        VentaOriginal venta = ventaDevolucionPort.bloquearVenta(op.ventaId())
                .orElseThrow(() -> new VentaNotFoundException(op.ventaId()));
        if (venta.anulada()) {
            throw new VentaNoDevolvibleException(venta.numeroVenta());
        }
        if (diasMaximos > 0 && venta.fechaVenta().plusDays(diasMaximos).isBefore(LocalDateTime.now())) {
            throw new VentaFueraDePlazoException(diasMaximos);
        }

        // 2. Turno de quien devuelve, bloqueado para escribir
        TurnoRef turno = turnoDevolucionPort.turnoAbiertoDeUsuario(usuarioId).orElseThrow(SinTurnoAbiertoException::new);
        turno = turnoDevolucionPort.bloquearParaEscritura(turno.turnoId()).filter(TurnoRef::abierto)
                .orElseThrow(SinTurnoAbiertoException::new);

        // 3. Reparto entre lotes y valor de lo devuelto
        CalculadorDevolucion.Calculo calculo = CalculadorDevolucion.calcular(venta, op.items(),
                devolucionRepositoryPort.devueltoPorLinea(venta.id()));
        BigDecimal credito = calculo.monto();

        return op.cambio()
                ? cambio(op, venta, turno, calculo, credito, usuarioId, huella)
                : simple(op, venta, turno, calculo, credito, usuarioId, huella);
    }

    private Registrada simple(Operacion op, VentaOriginal venta, TurnoRef turno, CalculadorDevolucion.Calculo calculo,
                              BigDecimal credito, Long usuarioId, String huella) {
        MetodoPagoRef metodo = null;
        BigDecimal monto = BigDecimal.ZERO.setScale(2);
        if (op.reembolsar() && credito.signum() > 0) {
            if (op.metodoPagoId() == null) {
                throw new DevolucionDatosInvalidosException("Debe indicar el método de pago del reembolso");
            }
            metodo = metodoPagoDevolucionPort.buscarPorId(op.metodoPagoId())
                    .orElseThrow(() -> new MetodoPagoNotFoundException(op.metodoPagoId()));
            if (MetodoPago.NOMBRE_CREDITO.equals(metodo.nombre())) {
                throw new MetodoReembolsoInvalidoException();
            }
            if (!metodo.activo()) {
                throw new MetodoPagoInactivoException(metodo.nombre());
            }
            if (MetodoPago.NOMBRE_EFECTIVO.equals(metodo.nombre())) {
                BigDecimal disponible = turnoDevolucionPort.efectivoDisponible(turno.turnoId());
                if (credito.compareTo(disponible) > 0) {
                    throw new SaldoEfectivoInsuficienteException(disponible);
                }
            }
            monto = credito;
        }

        Devolucion devolucion = devolucionRepositoryPort.save(Devolucion.registrar(
                numeroDevolucionGeneradorPort.siguiente(), venta.id(), usuarioId, turno.turnoId(), op.motivo(), monto,
                metodo == null ? null : metodo.id(), op.key(), huella, calculo.lineas()));
        reponerStock(venta, devolucion, calculo);
        return new Registrada(devolucion, true);
    }

    private Registrada cambio(Operacion op, VentaOriginal venta, TurnoRef turno, CalculadorDevolucion.Calculo calculo,
                              BigDecimal credito, Long usuarioId, String huella) {
        MetodoPagoRef metodoCredito = metodoPagoDevolucionPort.buscarPorNombre(MetodoPago.NOMBRE_CREDITO)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el método de pago de sistema " + MetodoPago.NOMBRE_CREDITO));

        Devolucion devolucion = devolucionRepositoryPort.save(Devolucion.registrar(
                numeroDevolucionGeneradorPort.siguiente(), venta.id(), usuarioId, turno.turnoId(), op.motivo(), credito,
                credito.signum() > 0 ? metodoCredito.id() : null, op.key(), huella, calculo.lineas()));

        // Lo devuelto vuelve al stock primero: la venta nueva ya puede usarlo
        reponerStock(venta, devolucion, calculo);

        // La venta nueva cubre su total con el crédito y cobra la diferencia con los pagos
        ResultadoVentaCambio nueva = ventaDevolucionPort.registrarVentaDeCambio(venta.clienteId(), op.nuevos(), op.pagos(), credito);

        // Si el crédito supera lo nuevo, la diferencia se entrega en efectivo y sale de la caja del turno
        BigDecimal sobrante = credito.subtract(nueva.creditoAplicado());
        if (sobrante.signum() > 0) {
            turnoDevolucionPort.registrarEgreso(turno.turnoId(), sobrante, "Saldo a favor del cambio "
                    + devolucion.getNumeroDevolucion() + " (venta " + venta.numeroVenta() + ")");
        }

        devolucion.vincularVentaCambio(nueva.venta().id());
        return new Registrada(devolucionRepositoryPort.save(devolucion), true);
    }

    private void reponerStock(VentaOriginal venta, Devolucion devolucion, CalculadorDevolucion.Calculo calculo) {
        if (!calculo.reposiciones().isEmpty()) {
            stockDevolucionPort.reponer(venta.depositoId(), "Devolución " + devolucion.getNumeroDevolucion()
                    + " de la venta " + venta.numeroVenta(), calculo.reposiciones(), devolucion.getId());
        }
    }

    // ---------- validaciones ----------

    private void validar(Operacion op) {
        if (op.motivo() == null || op.motivo().isBlank()) {
            throw new DevolucionDatosInvalidosException("El motivo es obligatorio");
        }
        if (op.motivo().trim().length() > 255) {
            throw new DevolucionDatosInvalidosException("El motivo no puede superar los 255 caracteres");
        }
        if (op.items().isEmpty()) {
            throw new DevolucionDatosInvalidosException("Debe indicar al menos una línea a devolver");
        }
        if (op.items().size() > MAX_ITEMS || op.nuevos().size() > MAX_ITEMS) {
            throw new DevolucionDatosInvalidosException("Se admiten hasta " + MAX_ITEMS + " líneas");
        }
        if (op.pagos().size() > MAX_PAGOS) {
            throw new DevolucionDatosInvalidosException("Se admiten hasta " + MAX_PAGOS + " pagos");
        }
        if (op.key() != null && !op.key().matches("^[A-Za-z0-9._-]{8,64}$")) {
            throw new DevolucionDatosInvalidosException(
                    "La clave de idempotencia debe tener entre 8 y 64 caracteres (letras, números, punto, guion y guion bajo)");
        }
        if (op.cambio() && op.nuevos().isEmpty()) {
            throw new DevolucionDatosInvalidosException("Un cambio debe indicar al menos un producto nuevo");
        }
        Set<Long> vistos = new HashSet<>();
        for (ItemDevueltoComando item : op.items()) {
            if (item.ventaDetalleId() == null) {
                throw new DevolucionDatosInvalidosException("La línea de la venta es obligatoria");
            }
            if (!vistos.add(item.ventaDetalleId())) {
                throw new DevolucionDatosInvalidosException(
                        "La línea " + item.ventaDetalleId() + " aparece más de una vez; sume sus cantidades");
            }
            if (item.cantidad() == null || item.cantidad().signum() <= 0) {
                throw new DevolucionDatosInvalidosException("La cantidad devuelta debe ser mayor a cero");
            }
            if (item.cantidad().stripTrailingZeros().scale() > 4) {
                throw new DevolucionDatosInvalidosException("Las cantidades admiten hasta 4 decimales");
            }
        }
    }

    /** Huella SHA-256 del contenido pedido: detecta si alguien reutiliza una clave con otra operación. */
    private String huella(Operacion op) {
        StringBuilder sb = new StringBuilder(op.cambio() ? "cambio" : "devolucion").append("|v=").append(op.ventaId())
                .append("|m=").append(op.motivo().trim()).append("|r=").append(op.reembolsar())
                .append("|mp=").append(op.metodoPagoId());
        for (ItemDevueltoComando i : op.items()) {
            sb.append("|d=").append(i.ventaDetalleId()).append(':').append(numero(i.cantidad())).append(':').append(i.reingresaStock());
        }
        for (ItemNuevoComando n : op.nuevos()) {
            sb.append("|n=").append(n.presentacionId()).append(':').append(numero(n.cantidad())).append(':')
                    .append(n.precioUnitario() == null ? "" : numero(n.precioUnitario())).append(':')
                    .append(n.motivoPrecio() == null ? "" : n.motivoPrecio().trim());
        }
        for (PagoCambioComando p : op.pagos()) {
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
        return valor == null ? "" : valor.stripTrailingZeros().toPlainString();
    }
}
