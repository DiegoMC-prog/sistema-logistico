package com.tienda.backend.credito.application.service;

import com.tienda.backend.cliente.domain.exception.ClienteNotFoundException;
import com.tienda.backend.credito.application.model.AbonoResumen;
import com.tienda.backend.credito.application.model.ClienteCredito;
import com.tienda.backend.credito.application.model.CuentaRef;
import com.tienda.backend.credito.application.model.DeudorResumen;
import com.tienda.backend.credito.application.model.EstadoCuentaCliente;
import com.tienda.backend.credito.application.model.MetodoPagoAbono;
import com.tienda.backend.credito.application.model.RegistrarAbonoComando;
import com.tienda.backend.credito.application.port.in.ConsultarCreditoUseCase;
import com.tienda.backend.credito.application.port.in.GestionarCuentaPorCobrarUseCase;
import com.tienda.backend.credito.application.port.in.RegistrarAbonoUseCase;
import com.tienda.backend.credito.application.port.out.CreditoRepositoryPort;
import com.tienda.backend.credito.domain.exception.AbonoExcedeDeudaException;
import com.tienda.backend.credito.domain.exception.AbonoNotFoundException;
import com.tienda.backend.credito.domain.exception.CreditoDatosInvalidosException;
import com.tienda.backend.credito.domain.exception.CreditoNoHabilitadoException;
import com.tienda.backend.credito.domain.exception.CuentaConAbonosException;
import com.tienda.backend.credito.domain.exception.LimiteCreditoExcedidoException;
import com.tienda.backend.credito.domain.exception.SinTurnoParaAbonoException;
import com.tienda.backend.metodopago.domain.exception.MetodoPagoNotFoundException;
import com.tienda.backend.metodopago.domain.model.MetodoPago;
import com.tienda.backend.shared.application.port.out.UsuarioActualPort;
import com.tienda.backend.venta.domain.exception.MetodoPagoInactivoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Cuentas por cobrar (ventas a crédito) y abonos. Todo corre en la transacción de quien lo llama. */
@Service
@RequiredArgsConstructor
public class CreditoService implements GestionarCuentaPorCobrarUseCase, RegistrarAbonoUseCase, ConsultarCreditoUseCase {

    private static final String PENDIENTE = "PENDIENTE";
    private static final String PAGADA = "PAGADA";
    private static final String ANULADA = "ANULADA";

    private final CreditoRepositoryPort repo;
    private final UsuarioActualPort usuarioActualPort;

    // ---------- desde ventas y devoluciones ----------

    @Override
    @Transactional
    public void crearParaVenta(Long clienteId, Long ventaId, BigDecimal monto) {
        ClienteCredito cliente = repo.bloquearCliente(clienteId).orElseThrow(() -> new ClienteNotFoundException(clienteId));
        if (!cliente.creditoHabilitado()) {
            throw new CreditoNoHabilitadoException(cliente.nombre());
        }
        BigDecimal disponible = cliente.limiteCredito().subtract(repo.deudaPendiente(clienteId));
        if (monto.compareTo(disponible) > 0) {
            throw new LimiteCreditoExcedidoException(cliente.nombre(), disponible.max(BigDecimal.ZERO));
        }
        repo.insertarCuenta(clienteId, ventaId, monto.setScale(2, RoundingMode.HALF_UP), usuarioActualPort.nombre());
    }

    @Override
    @Transactional
    public void anularDeVenta(Long ventaId) {
        CuentaRef cuenta = repo.bloquearCuentaDeVenta(ventaId).orElse(null);
        if (cuenta == null || ANULADA.equals(cuenta.estado())) {
            return;
        }
        if (repo.cuentaTieneAbonos(cuenta.id())) {
            throw new CuentaConAbonosException();
        }
        repo.actualizarCuenta(cuenta.id(), cuenta.saldo(), ANULADA, usuarioActualPort.nombre());
    }

    @Override
    @Transactional
    public BigDecimal descontarPorDevolucion(Long ventaId, BigDecimal monto) {
        CuentaRef cuenta = repo.bloquearCuentaDeVenta(ventaId).orElse(null);
        if (cuenta == null || !PENDIENTE.equals(cuenta.estado()) || monto.signum() <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        BigDecimal aplicado = cuenta.saldo().min(monto).setScale(2, RoundingMode.HALF_UP);
        BigDecimal saldo = cuenta.saldo().subtract(aplicado);
        repo.actualizarCuenta(cuenta.id(), saldo, saldo.signum() == 0 ? PAGADA : PENDIENTE, usuarioActualPort.nombre());
        return aplicado;
    }

    // ---------- abonos ----------

    @Override
    @Transactional
    public AbonoResumen registrar(RegistrarAbonoComando cmd) {
        if (cmd.monto() == null || cmd.monto().signum() <= 0 || cmd.monto().stripTrailingZeros().scale() > 2) {
            throw new CreditoDatosInvalidosException("El monto del abono debe ser mayor a cero y admite hasta 2 decimales");
        }
        if (cmd.metodoPagoId() == null) {
            throw new CreditoDatosInvalidosException("El método de pago del abono es obligatorio");
        }
        if (cmd.referencia() != null && cmd.referencia().length() > 100) {
            throw new CreditoDatosInvalidosException("La referencia no puede superar los 100 caracteres");
        }
        Long usuarioId = usuarioActualPort.id();
        Long turnoId = repo.turnoAbiertoDeUsuario(usuarioId).orElseThrow(SinTurnoParaAbonoException::new);

        MetodoPagoAbono metodo = repo.buscarMetodoPago(cmd.metodoPagoId())
                .orElseThrow(() -> new MetodoPagoNotFoundException(cmd.metodoPagoId()));
        if (!metodo.activo()) {
            throw new MetodoPagoInactivoException(metodo.nombre());
        }
        if (MetodoPago.NOMBRE_CREDITO.equals(metodo.nombre()) || MetodoPago.NOMBRE_CREDITO_CLIENTE.equals(metodo.nombre())) {
            throw new CreditoDatosInvalidosException("Un abono no se puede pagar con crédito");
        }

        ClienteCredito cliente = repo.bloquearCliente(cmd.clienteId())
                .orElseThrow(() -> new ClienteNotFoundException(cmd.clienteId()));
        List<CuentaRef> cuentas = repo.cuentasPendientes(cliente.id());
        BigDecimal deuda = cuentas.stream().map(CuentaRef::saldo).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (cmd.monto().compareTo(deuda) > 0) {
            throw new AbonoExcedeDeudaException(deuda);
        }

        BigDecimal monto = cmd.monto().setScale(2, RoundingMode.HALF_UP);
        String usuario = usuarioActualPort.nombre();
        Long abonoId = repo.insertarAbono(repo.siguienteNumeroAbono(), cliente.id(), turnoId, usuarioId, metodo.id(),
                monto, cmd.referencia() == null || cmd.referencia().isBlank() ? null : cmd.referencia().trim());

        // Deudas más antiguas primero
        BigDecimal restante = monto;
        for (CuentaRef cuenta : cuentas) {
            if (restante.signum() == 0) {
                break;
            }
            BigDecimal aplicado = cuenta.saldo().min(restante);
            BigDecimal saldo = cuenta.saldo().subtract(aplicado);
            repo.insertarAplicacion(abonoId, cuenta.id(), aplicado);
            repo.actualizarCuenta(cuenta.id(), saldo, saldo.signum() == 0 ? PAGADA : PENDIENTE, usuario);
            restante = restante.subtract(aplicado);
        }
        return repo.buscarAbono(abonoId).orElseThrow(() -> new AbonoNotFoundException(abonoId));
    }

    // ---------- consultas ----------

    @Override
    @Transactional(readOnly = true)
    public EstadoCuentaCliente estadoCuenta(Long clienteId) {
        ClienteCredito cliente = repo.buscarCliente(clienteId).orElseThrow(() -> new ClienteNotFoundException(clienteId));
        BigDecimal deuda = repo.deudaPendiente(clienteId);
        return new EstadoCuentaCliente(cliente.id(), cliente.nombre(), cliente.creditoHabilitado(), cliente.limiteCredito(),
                deuda, cliente.limiteCredito().subtract(deuda).max(BigDecimal.ZERO), repo.cuentasDeCliente(clienteId),
                repo.abonosDeCliente(clienteId));
    }

    @Override
    @Transactional(readOnly = true)
    public AbonoResumen abono(Long abonoId) {
        return repo.buscarAbono(abonoId).orElseThrow(() -> new AbonoNotFoundException(abonoId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeudorResumen> deudores() {
        return repo.deudores();
    }
}
