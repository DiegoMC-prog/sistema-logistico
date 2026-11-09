package com.tienda.backend.reporte.application.service;

import com.tienda.backend.reporte.application.model.Agrupacion;
import com.tienda.backend.reporte.application.model.FilasCaja;
import com.tienda.backend.reporte.application.model.FilasCompras;
import com.tienda.backend.reporte.application.model.FilasInventario;
import com.tienda.backend.reporte.application.model.FilasUtilidad;
import com.tienda.backend.reporte.application.model.FilasVentas;
import com.tienda.backend.reporte.application.model.Panel;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.port.in.ConsultarPanelUseCase;
import com.tienda.backend.reporte.application.port.in.ConsultarReporteCajaUseCase;
import com.tienda.backend.reporte.application.port.in.ConsultarReporteComprasUseCase;
import com.tienda.backend.reporte.application.port.in.ConsultarReporteInventarioUseCase;
import com.tienda.backend.reporte.application.port.in.ConsultarReporteUtilidadUseCase;
import com.tienda.backend.reporte.application.port.in.ConsultarReporteVentasUseCase;
import com.tienda.backend.shared.application.port.out.UsuarioActualPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

/**
 * Arma el panel reutilizando los reportes. Cada bloque exige el mismo permiso que su reporte: así el panel no deja ver
 * nada que el usuario no pueda consultar por su cuenta (p. ej. la utilidad es solo para quien tiene REPORTE_UTILIDAD).
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PanelService implements ConsultarPanelUseCase {
    // Deben coincidir con los de la tabla permisos (los mismos que exige SecurityConfig en cada reporte)
    private static final String REPORTE_VENTAS = "REPORTE_VENTAS";
    private static final String REPORTE_CAJA = "REPORTE_CAJA";
    private static final String REPORTE_INVENTARIO = "REPORTE_INVENTARIO";
    private static final String REPORTE_COMPRAS = "REPORTE_COMPRAS";
    private static final String REPORTE_UTILIDAD = "REPORTE_UTILIDAD";

    static final int CRITICOS = 5;
    static final int ATRASADOS = 3;
    static final int DIAS_POR_VENCER = 7;

    private final ConsultarReporteVentasUseCase ventas;
    private final ConsultarReporteCajaUseCase caja;
    private final ConsultarReporteInventarioUseCase inventario;
    private final ConsultarReporteComprasUseCase compras;
    private final ConsultarReporteUtilidadUseCase utilidad;
    private final UsuarioActualPort usuarioActualPort;

    @Override
    public Panel consultar() {
        LocalDate hoy = LocalDate.now();
        RangoFechas ultimos7 = RangoFechas.de(hoy.minusDays(6), hoy);
        return new Panel(LocalDateTime.now(),
                usuarioActualPort.tienePermiso(REPORTE_VENTAS) ? bloqueVentas(ultimos7) : null,
                usuarioActualPort.tienePermiso(REPORTE_CAJA) ? bloqueCaja(hoy) : null,
                usuarioActualPort.tienePermiso(REPORTE_INVENTARIO) ? bloqueInventario() : null,
                usuarioActualPort.tienePermiso(REPORTE_COMPRAS) ? bloqueCompras() : null,
                usuarioActualPort.tienePermiso(REPORTE_UTILIDAD) ? bloqueUtilidad(ultimos7) : null);
    }

    private Panel.Ventas bloqueVentas(RangoFechas ultimos7) {
        List<FilasVentas.Periodo> dias = ventas.resumen(ultimos7, Agrupacion.DIA).filas();   // 7 filas, la última es hoy
        FilasVentas.Periodo hoy = dias.get(dias.size() - 1);
        FilasVentas.Periodo ayer = dias.get(dias.size() - 2);
        BigDecimal variacion = ayer.total().signum() == 0 ? null
                : hoy.total().subtract(ayer.total()).multiply(BigDecimal.valueOf(100))
                        .divide(ayer.total(), 2, RoundingMode.HALF_UP);
        return new Panel.Ventas(dia(hoy), dia(ayer), variacion,
                dias.stream().map(d -> new Panel.DiaVentas(d.periodo(), d.ventas(), d.total())).toList());
    }

    private static Panel.Dia dia(FilasVentas.Periodo p) {
        return new Panel.Dia(p.ventas(), p.total(), p.ticketPromedio(), p.anuladas());
    }

    private Panel.Caja bloqueCaja(LocalDate hoy) {
        // Un turno puede llevar abierto varios días: se buscan los abiertos del último año
        List<FilasCaja.Cierre> abiertos = caja.cierres(RangoFechas.de(hoy.minusDays(RangoFechas.DIAS_MAXIMO), hoy),
                null, null, "ABIERTO").filas();
        return new Panel.Caja(abiertos.size(), abiertos.stream()
                .map(t -> new Panel.TurnoAbierto(t.turnoId(), t.caja(), t.cajero(), t.fechaApertura(), t.ventas(),
                        t.totalVentas(), t.efectivoEsperado()))
                .toList());
    }

    private Panel.Inventario bloqueInventario() {
        List<FilasInventario.StockBajo> bajos = inventario.stockBajo(null, null).filas();
        List<FilasInventario.LoteVencimiento> lotes = inventario.lotesPorVencer(DIAS_POR_VENCER, true, null).filas();
        long vencidos = lotes.stream().filter(l -> l.diasRestantes() < 0).count();
        return new Panel.Inventario(bajos.size(), bajos.stream().filter(b -> b.stock().signum() == 0).count(),
                bajos.stream().limit(CRITICOS)   // ya vienen de más a menos crítico
                        .map(b -> new Panel.StockCritico(b.productoId(), b.producto(), b.stock(), b.stockMinimo(),
                                b.faltante()))
                        .toList(),
                lotes.size() - vencidos, vencidos, suma(lotes, FilasInventario.LoteVencimiento::valorEnRiesgo));
    }

    private Panel.Compras bloqueCompras() {
        List<FilasCompras.Pendiente> pendientes = compras.pendientes(null, null).filas();
        List<FilasCompras.Pendiente> atrasados = pendientes.stream()
                .filter(p -> p.diasAtraso() != null && p.diasAtraso() > 0).toList();
        return new Panel.Compras(pendientes.size(), atrasados.size(), suma(pendientes, FilasCompras.Pendiente::montoPendiente),
                atrasados.stream()
                        .sorted((a, b) -> Long.compare(b.diasAtraso(), a.diasAtraso()))
                        .limit(ATRASADOS)
                        .map(p -> new Panel.PedidoAtrasado(p.pedidoId(), p.numeroPedido(), p.proveedor(), p.diasAtraso(),
                                p.montoPendiente()))
                        .toList());
    }

    private Panel.Utilidad bloqueUtilidad(RangoFechas ultimos7) {
        List<FilasUtilidad.Periodo> dias = utilidad.porPeriodo(ultimos7, Agrupacion.DIA).filas();
        FilasUtilidad.Periodo hoy = dias.get(dias.size() - 1);
        BigDecimal ingreso7 = suma(dias, FilasUtilidad.Periodo::ingreso);
        BigDecimal utilidad7 = suma(dias, FilasUtilidad.Periodo::utilidad);
        return new Panel.Utilidad(hoy.utilidad(), hoy.margen(), utilidad7,
                ingreso7.signum() == 0 ? null
                        : utilidad7.multiply(BigDecimal.valueOf(100)).divide(ingreso7, 2, RoundingMode.HALF_UP));
    }

    private static <T> BigDecimal suma(List<T> filas, Function<T, BigDecimal> campo) {
        return filas.stream().map(campo).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }
}
