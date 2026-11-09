package com.tienda.backend.credito.application.port.out;

import com.tienda.backend.credito.application.model.AbonoResumen;
import com.tienda.backend.credito.application.model.ClienteCredito;
import com.tienda.backend.credito.application.model.CuentaRef;
import com.tienda.backend.credito.application.model.CuentaResumen;
import com.tienda.backend.credito.application.model.DeudorResumen;
import com.tienda.backend.credito.application.model.MetodoPagoAbono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CreditoRepositoryPort {
    /** Lee el cliente bloqueándolo: serializa ventas y abonos del mismo cliente. */
    Optional<ClienteCredito> bloquearCliente(Long clienteId);

    Optional<ClienteCredito> buscarCliente(Long clienteId);

    BigDecimal deudaPendiente(Long clienteId);

    void insertarCuenta(Long clienteId, Long ventaId, BigDecimal monto, String usuario);

    Optional<CuentaRef> bloquearCuentaDeVenta(Long ventaId);

    boolean cuentaTieneAbonos(Long cuentaId);

    void actualizarCuenta(Long cuentaId, BigDecimal saldo, String estado, String usuario);

    /** Cuentas PENDIENTE del cliente, la más antigua primero, bloqueadas. */
    List<CuentaRef> cuentasPendientes(Long clienteId);

    List<CuentaResumen> cuentasDeCliente(Long clienteId);

    List<DeudorResumen> deudores();

    Optional<MetodoPagoAbono> buscarMetodoPago(Long id);

    /** Turno ABIERTO del usuario, bloqueado en modo compartido; vacío si no tiene. */
    Optional<Long> turnoAbiertoDeUsuario(Long usuarioId);

    String siguienteNumeroAbono();

    Long insertarAbono(String numero, Long clienteId, Long turnoId, Long usuarioId, Long metodoPagoId, BigDecimal monto,
                       String referencia);

    void insertarAplicacion(Long abonoId, Long cuentaId, BigDecimal monto);

    Optional<AbonoResumen> buscarAbono(Long abonoId);

    List<AbonoResumen> abonosDeCliente(Long clienteId);
}
