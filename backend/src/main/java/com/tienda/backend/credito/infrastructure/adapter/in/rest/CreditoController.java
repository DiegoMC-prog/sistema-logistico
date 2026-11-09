package com.tienda.backend.credito.infrastructure.adapter.in.rest;

import com.tienda.backend.credito.application.model.AbonoResumen;
import com.tienda.backend.credito.application.model.DeudorResumen;
import com.tienda.backend.credito.application.model.EstadoCuentaCliente;
import com.tienda.backend.credito.application.model.RegistrarAbonoComando;
import com.tienda.backend.credito.application.port.in.ConsultarCreditoUseCase;
import com.tienda.backend.credito.application.port.in.RegistrarAbonoUseCase;
import com.tienda.backend.credito.infrastructure.adapter.in.rest.dto.AbonoRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Todo exige el permiso CREDITO_GESTIONAR (ver SecurityConfig). */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CreditoController {

    private final ConsultarCreditoUseCase consultarCreditoUseCase;
    private final RegistrarAbonoUseCase registrarAbonoUseCase;

    /** Clientes con deuda pendiente, de mayor a menor. */
    @GetMapping("/creditos/deudores")
    public ResponseEntity<List<DeudorResumen>> deudores() {
        return ResponseEntity.ok(consultarCreditoUseCase.deudores());
    }

    @GetMapping("/clientes/{id}/estado-cuenta")
    public ResponseEntity<EstadoCuentaCliente> estadoCuenta(@PathVariable Long id) {
        return ResponseEntity.ok(consultarCreditoUseCase.estadoCuenta(id));
    }

    @PostMapping("/clientes/{id}/abonos")
    public ResponseEntity<AbonoResumen> abonar(@PathVariable Long id, @Valid @RequestBody AbonoRequest request) {
        AbonoResumen abono = registrarAbonoUseCase.registrar(
                new RegistrarAbonoComando(id, request.metodoPagoId(), request.monto(), request.referencia()));
        return ResponseEntity.status(HttpStatus.CREATED).body(abono);
    }

    /** Comprobante del abono. */
    @GetMapping("/abonos/{id}")
    public ResponseEntity<AbonoResumen> abono(@PathVariable Long id) {
        return ResponseEntity.ok(consultarCreditoUseCase.abono(id));
    }
}
