package com.tienda.backend.credito.infrastructure.adapter.in.rest;

import com.tienda.backend.credito.application.model.AbonoResumen;
import com.tienda.backend.credito.application.model.EstadoCuentaCliente;
import com.tienda.backend.credito.application.port.in.ConsultarCreditoUseCase;
import com.tienda.backend.shared.infrastructure.pdf.DocumentoPdf;
import com.tienda.backend.shared.infrastructure.pdf.PlantillaPdf;
import com.tienda.backend.shared.infrastructure.pdf.RespuestaPdf;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static com.tienda.backend.shared.infrastructure.pdf.DocumentoPdf.dinero;
import static com.tienda.backend.shared.infrastructure.pdf.DocumentoPdf.fecha;

/** Comprobante de abono y estado de cuenta en PDF (RF-29). */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CreditoPdfController {

    private final ConsultarCreditoUseCase consultarCreditoUseCase;
    private final PlantillaPdf plantilla;

    @GetMapping("/abonos/{id}/pdf")
    public ResponseEntity<byte[]> comprobante(@PathVariable Long id) {
        AbonoResumen a = consultarCreditoUseCase.abono(id);

        DocumentoPdf pdf = plantilla.nuevo("Comprobante de abono", a.numeroAbono())
                .dato("Fecha", fecha(a.fecha()))
                .dato("Cliente", a.clienteNombre())
                .dato("Cobrado por", a.usuario())
                .dato("Turno de caja", "#" + a.turnoId())
                .dato("Método de pago", a.metodoPago());
        if (a.referencia() != null) {
            pdf.dato("Referencia", a.referencia());
        }
        pdf.seccion("Aplicado a")
                .tabla(List.of("Venta", "Monto"), new float[]{70, 30},
                        a.aplicaciones().stream().map(x -> List.of(x.numeroVenta(), dinero(x.monto()))).toList(), 1)
                .totales(List.of(new DocumentoPdf.Total("MONTO ABONADO", dinero(a.monto()), true)))
                .firmas("Firma del cliente", "Firma del cajero");

        return RespuestaPdf.descarga(pdf.generar(), "Abono-" + a.numeroAbono());
    }

    @GetMapping("/clientes/{id}/estado-cuenta/pdf")
    public ResponseEntity<byte[]> estadoDeCuenta(@PathVariable Long id) {
        EstadoCuentaCliente e = consultarCreditoUseCase.estadoCuenta(id);

        byte[] contenido = plantilla.nuevo("Estado de cuenta", null)
                .dato("Cliente", e.clienteNombre())
                .dato("Emitido el", fecha(LocalDateTime.now()))
                .dato("Crédito", e.creditoHabilitado() ? "Habilitado" : "No habilitado")
                .seccion("Ventas a crédito")
                .tabla(List.of("Venta", "Fecha", "Estado", "Original", "Saldo"), new float[]{22, 26, 18, 17, 17},
                        e.cuentas().stream().map(c -> List.of(c.numeroVenta(), fecha(c.fecha()),
                                estado(c.estado()), dinero(c.montoOriginal()), dinero(c.saldo()))).toList(), 3)
                .seccion("Abonos")
                .tabla(List.of("Número", "Fecha", "Método", "Monto"), new float[]{24, 28, 28, 20},
                        e.abonos().stream().map(a -> List.of(a.numeroAbono(), fecha(a.fecha()), a.metodoPago(),
                                dinero(a.monto()))).toList(), 3)
                .totales(List.of(new DocumentoPdf.Total("Límite de crédito", dinero(e.limiteCredito())),
                        new DocumentoPdf.Total("Crédito disponible", dinero(e.disponible())),
                        new DocumentoPdf.Total("DEUDA TOTAL", dinero(e.deudaTotal()), true)))
                .generar();

        return RespuestaPdf.descarga(contenido, "EstadoCuenta-" + e.clienteNombre());
    }

    private static String estado(String estado) {
        return switch (estado) {
            case "PENDIENTE" -> "Pendiente";
            case "PAGADA" -> "Pagada";
            case "ANULADA" -> "Anulada";
            default -> estado;
        };
    }
}
