package com.tienda.backend.venta.infrastructure.adapter.in.rest;

import com.tienda.backend.shared.infrastructure.pdf.DocumentoPdf;
import com.tienda.backend.shared.infrastructure.pdf.PlantillaPdf;
import com.tienda.backend.shared.infrastructure.pdf.RespuestaPdf;
import com.tienda.backend.venta.application.port.in.ConsultarVentaUseCase;
import com.tienda.backend.venta.infrastructure.adapter.in.rest.dto.VentaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.tienda.backend.shared.infrastructure.pdf.DocumentoPdf.cantidad;
import static com.tienda.backend.shared.infrastructure.pdf.DocumentoPdf.dinero;
import static com.tienda.backend.shared.infrastructure.pdf.DocumentoPdf.fecha;

/** Nota de venta en PDF. Usa la misma consulta que la nota en pantalla, con sus mismas restricciones de acceso. */
@RestController
@RequestMapping("/api/v1/ventas")
@RequiredArgsConstructor
public class VentaPdfController {

    private final ConsultarVentaUseCase consultarVentaUseCase;
    private final PlantillaPdf plantilla;

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> notaDeVenta(@PathVariable Long id) {
        VentaResponse v = VentaResponse.from(consultarVentaUseCase.obtener(id));

        DocumentoPdf pdf = plantilla.nuevo("Nota de venta", v.numeroVenta())
                .dato("Fecha", fecha(v.fechaVenta()))
                .dato("Turno de caja", "#" + v.turnoId())
                .dato("Cliente", v.cliente() == null ? "Sin cliente" : v.cliente().nombre());
        if (v.anulacion() != null) {
            pdf.aviso("VENTA ANULADA el " + fecha(v.anulacion().fecha()) + ": " + v.anulacion().motivo());
        }

        List<List<String>> lineas = v.lineas().stream().map(l -> List.of(
                l.factorConversion().compareTo(BigDecimal.ONE) == 0 ? l.producto()
                        : l.producto() + " (x" + cantidad(l.factorConversion()) + ")",
                cantidad(l.cantidad()), dinero(l.precioUnitario()), dinero(l.descuento()), dinero(l.subtotal())))
                .toList();
        pdf.tabla(List.of("Producto", "Cantidad", "Precio", "Descuento", "Subtotal"),
                new float[]{44, 13, 14, 14, 15}, lineas, 1);

        List<DocumentoPdf.Total> totales = new ArrayList<>();
        totales.add(new DocumentoPdf.Total("Subtotal", dinero(v.subtotal())));
        if (v.descuentoTotal().signum() > 0) {
            totales.add(new DocumentoPdf.Total("Descuento por vencimiento", "-" + dinero(v.descuentoTotal())));
        }
        totales.add(new DocumentoPdf.Total("TOTAL", dinero(v.total()), true));
        pdf.totales(totales);

        pdf.seccion("Pagos").tabla(List.of("Método", "Referencia", "Monto"), new float[]{40, 40, 20},
                v.pagos().stream().map(p -> List.of(
                        "CREDITO".equals(p.metodoPago()) ? "Crédito (fiado)" : p.metodoPago(),
                        p.referencia() == null ? "-" : p.referencia(), dinero(p.monto()))).toList(), 2);
        if (v.efectivoRecibido() != null) {
            pdf.totales(List.of(new DocumentoPdf.Total("Efectivo recibido", dinero(v.efectivoRecibido())),
                    new DocumentoPdf.Total("Vuelto", dinero(v.vuelto()))));
        }

        byte[] contenido = pdf.nota("Gracias por su compra. Este documento no es una factura fiscal.").generar();
        return RespuestaPdf.descarga(contenido, "NotaVenta-" + v.numeroVenta());
    }
}
