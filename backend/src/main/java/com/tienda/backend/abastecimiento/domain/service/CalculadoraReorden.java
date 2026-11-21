package com.tienda.backend.abastecimiento.domain.service;

import com.tienda.backend.abastecimiento.domain.model.DatosReorden;
import com.tienda.backend.abastecimiento.domain.model.Escenario;
import com.tienda.backend.abastecimiento.domain.model.EstadoSugerencia;
import com.tienda.backend.abastecimiento.domain.model.Estadistica;
import com.tienda.backend.abastecimiento.domain.model.ParametrosReorden;
import com.tienda.backend.abastecimiento.domain.model.ResultadoEscenario;
import com.tienda.backend.abastecimiento.domain.model.SugerenciaCalculada;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Motor de reorden (RF-34). Modelo de revisión continua (s, Q) con demanda y lead time aleatorios:
 * <ul>
 *   <li>EOQ = √(2·D·S / H), con D = demanda anual, S = costo por pedido y H = pct × <b>último</b> costo de compra
 *       (adaptativo: si el proveedor sube el precio, H sube y el lote óptimo baja).</li>
 *   <li>Stock de seguridad SS = z · √(L·σd² + d²·σL²), que combina la variabilidad de la demanda y la del proveedor.</li>
 *   <li>Punto de reorden ROP = d·L + SS.</li>
 *   <li>Si la posición de inventario (stock + en camino) ≤ ROP se pide hasta ROP + EOQ.</li>
 *   <li>Perecederos: no se sugiere más de lo que se alcanza a vender antes de que venza el lote nuevo.</li>
 *   <li>Alza de costos (RF-35): si el último costo del proveedor subió al menos el umbral en la ventana reciente,
 *       el punto de reorden suma d·N días (se compra N días antes, al costo actual, antes de la próxima alza).</li>
 * </ul>
 * Se calcula para tres escenarios (pesimista, estándar, optimista) variando d y L en una desviación estándar.
 * No depende de Spring ni de la base de datos.
 */
public class CalculadoraReorden {

    private static final double DIAS_ANIO = 365;

    public SugerenciaCalculada calcular(DatosReorden datos) {
        ParametrosReorden p = datos.parametros();
        Estadistica demanda = Estadistica.de(datos.demandaDiaria());
        double z = DistribucionNormal.inversa(p.nivelServicio());

        if (datos.diasHistorial() < datos.historialMinimoDias()) {
            return sinCalculo(datos, EstadoSugerencia.DATOS_INSUFICIENTES, demanda, z, datos.diasHistorial() == 0
                    ? "El producto todavía no tiene ventas registradas; se necesitan al menos "
                      + datos.historialMinimoDias() + " días de historial para sugerir."
                    : "Solo hay " + datos.diasHistorial() + " días de historial de ventas; se necesitan al menos "
                      + datos.historialMinimoDias() + " para que la sugerencia sea confiable.");
        }
        if (demanda.media() <= 0) {
            return sinCalculo(datos, EstadoSugerencia.SIN_DEMANDA, demanda, z,
                    "No hubo ventas en los últimos " + demanda.muestras() + " días: no se sugiere comprar.");
        }
        if (datos.proveedorId() == null) {
            return sinCalculo(datos, EstadoSugerencia.SIN_PROVEEDOR, demanda, z,
                    "El producto nunca se pidió a un proveedor. Asigne uno en los parámetros de reorden.");
        }
        if (datos.costoUnitario() == null || datos.costoUnitario() <= 0) {
            return sinCalculo(datos, EstadoSugerencia.DATOS_INSUFICIENTES, demanda, z,
                    "No hay un costo de compra registrado para el producto: no se puede calcular el EOQ.");
        }

        List<String> notas = new ArrayList<>();
        Estadistica leadTime = Estadistica.de(datos.leadTimes());
        if (leadTime.muestras() == 0) {
            leadTime = new Estadistica(datos.leadTimeDefectoDias(), 0, 0);
            notas.add("El proveedor aún no tiene recepciones registradas: se asumió un lead time de "
                      + n(datos.leadTimeDefectoDias()) + " días sin variabilidad.");
        }

        double costo = datos.costoUnitario();
        double h = p.costoAlmacenPct() * costo;
        int anticipo = datos.alzaEsperada() ? datos.diasAnticipoAlza() : 0;

        ResultadoEscenario pesimista = escenario(Escenario.PESIMISTA, demanda, leadTime, p, h, z, anticipo, datos);
        ResultadoEscenario estandar = escenario(Escenario.ESTANDAR, demanda, leadTime, p, h, z, anticipo, datos);
        ResultadoEscenario optimista = escenario(Escenario.OPTIMISTA, demanda, leadTime, p, h, z, anticipo, datos);

        double posicion = datos.stockActual() + datos.enTransito();
        boolean reordenar = posicion <= estandar.puntoReorden();
        EstadoSugerencia estado = reordenar ? EstadoSugerencia.REORDENAR : EstadoSugerencia.SIN_NECESIDAD;

        StringBuilder motivo = new StringBuilder();
        motivo.append("Posición de inventario ").append(n(posicion)).append(" (stock ").append(n(datos.stockActual()));
        if (datos.enTransito() > 0) {
            motivo.append(" + ").append(n(datos.enTransito())).append(" en camino");
        }
        motivo.append(") ").append(reordenar ? "≤" : ">").append(" punto de reorden ").append(n(estandar.puntoReorden()))
                .append(" = demanda media ").append(n(demanda.media())).append("/día × lead time ")
                .append(n(leadTime.media())).append(" días + stock de seguridad ").append(n(estandar.stockSeguridad()))
                .append(" (nivel de servicio ").append(n(p.nivelServicio() * 100)).append(" %)");
        if (anticipo > 0) {
            motivo.append(" + ").append(n(demanda.media() * anticipo)).append(" de compra anticipada");
        }
        motivo.append(". ");
        if (anticipo > 0) {
            motivo.append("El costo de este proveedor subió ").append(pct(datos.variacionCosto()))
                    .append(" recientemente: se adelanta la compra ").append(anticipo)
                    .append(" días para comprar al costo actual antes de una nueva alza. ");
        } else if (datos.variacionCosto() != null && datos.umbralAlza() > 0
                   && datos.variacionCosto() <= -datos.umbralAlza()) {
            motivo.append("El costo de este proveedor bajó ").append(pct(-datos.variacionCosto()))
                    .append(" recientemente. ");
        }
        if (reordenar) {
            motivo.append("Se sugiere pedir ").append(n(estandar.cantidad())).append(" unidades (EOQ ")
                    .append(n(estandar.eoq())).append(" con el último costo de compra ").append(n2(costo)).append("). ");
        } else {
            double dias = (posicion - estandar.puntoReorden()) / demanda.media();
            motivo.append("No hace falta pedir todavía; al ritmo actual se llega al punto de reorden en unos ")
                    .append(n(dias)).append(" días. ");
        }
        if (estandar.limitadaPorVencimiento()) {
            motivo.append("La cantidad se limitó a lo que se alcanza a vender en la vida útil media del lote (")
                    .append(n(datos.vidaUtilDias())).append(" días). ");
        }
        notas.forEach(nota -> motivo.append(nota).append(' '));

        return new SugerenciaCalculada(datos.productoId(), datos.proveedorId(), estado, datos.diasHistorial(), demanda,
                leadTime, costo, p.costoPedido(), h, p.nivelServicio(), z, datos.stockActual(), datos.enTransito(),
                datos.vidaUtilDias(), pesimista, estandar, optimista, motivo.toString().trim(), datos.variacionCosto(),
                anticipo);
    }

    private ResultadoEscenario escenario(Escenario e, Estadistica demanda, Estadistica leadTime, ParametrosReorden p,
                                         double h, double z, int diasAnticipo, DatosReorden datos) {
        double d = e.ajustar(demanda.media(), demanda.desviacion());
        double l = e.ajustar(leadTime.media(), leadTime.desviacion());

        double eoq = d <= 0 || p.costoPedido() <= 0 ? 0 : Math.sqrt(2 * d * DIAS_ANIO * p.costoPedido() / h);
        double ss = z * Math.sqrt(l * demanda.desviacion() * demanda.desviacion()
                                  + d * d * leadTime.desviacion() * leadTime.desviacion());
        ss = Math.max(ss, 0);
        // Compra anticipada por alza de costo: el pedido sale cuando aún quedan diasAnticipo días de demanda extra
        double rop = d * l + ss + d * diasAnticipo;

        double posicion = datos.stockActual() + datos.enTransito();
        double cantidad = posicion <= rop ? rop + eoq - posicion : 0;

        boolean limitada = false;
        if (cantidad > 0 && datos.perecedero() && datos.vidaUtilDias() != null && datos.vidaUtilDias() > 0) {
            // El stock actual sale primero (FEFO): lo que quede de él al llegar el pedido le resta ventas al lote nuevo
            double sobrante = Math.max(datos.stockActual() - d * l, 0);
            double tope = Math.max(d * datos.vidaUtilDias() - sobrante, 0);
            if (cantidad > tope) {
                cantidad = tope;
                limitada = true;
            }
        }
        // Las unidades base son indivisibles
        cantidad = Math.ceil(cantidad - 1e-9);
        return new ResultadoEscenario(e, d, l, eoq, ss, rop, Math.max(cantidad, 0), limitada);
    }

    private SugerenciaCalculada sinCalculo(DatosReorden datos, EstadoSugerencia estado, Estadistica demanda, double z,
                                           String motivo) {
        ParametrosReorden p = datos.parametros();
        double costo = datos.costoUnitario() == null ? 0 : datos.costoUnitario();
        return new SugerenciaCalculada(datos.productoId(), datos.proveedorId(), estado, datos.diasHistorial(), demanda,
                Estadistica.de(datos.leadTimes()), costo, p.costoPedido(), p.costoAlmacenPct() * costo,
                p.nivelServicio(), z, datos.stockActual(), datos.enTransito(), datos.vidaUtilDias(),
                ResultadoEscenario.vacio(Escenario.PESIMISTA), ResultadoEscenario.vacio(Escenario.ESTANDAR),
                ResultadoEscenario.vacio(Escenario.OPTIMISTA), motivo, datos.variacionCosto(), 0);
    }

    /** Número con hasta un decimal para el texto del motivo. */
    private static String n(double x) {
        return x == Math.rint(x) ? String.valueOf((long) x) : String.format(Locale.ROOT, "%.1f", x);
    }

    private static String pct(double fraccion) {
        return String.format(Locale.ROOT, "%.1f %%", fraccion * 100);
    }

    private static String n2(double x) {
        return String.format(Locale.ROOT, "%.2f", x);
    }
}
