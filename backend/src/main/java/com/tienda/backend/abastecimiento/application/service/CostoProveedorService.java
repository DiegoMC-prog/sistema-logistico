package com.tienda.backend.abastecimiento.application.service;

import com.tienda.backend.abastecimiento.application.model.CostoFiltro;
import com.tienda.backend.abastecimiento.application.model.HistoricoCosto;
import com.tienda.backend.abastecimiento.application.model.VariacionCostoResumen;
import com.tienda.backend.abastecimiento.application.model.VariacionProveedor;
import com.tienda.backend.abastecimiento.application.port.in.ConsultarCostosUseCase;
import com.tienda.backend.abastecimiento.application.port.out.CostoRepositoryPort;
import com.tienda.backend.abastecimiento.domain.exception.ParametrosReordenInvalidosException;
import com.tienda.backend.abastecimiento.domain.model.PuntoCosto;
import com.tienda.backend.abastecimiento.domain.service.VariacionCosto;
import com.tienda.backend.shared.application.model.PageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Histórico de costos por proveedor y su variación a 30 y 90 días (RF-35). Se calcula a partir de los pedidos
 * confirmados; no hay una tabla aparte que mantener.
 */
@Service
public class CostoProveedorService implements ConsultarCostosUseCase {

    /** Cuánto hacia atrás se mira el histórico: dos años alcanzan para cualquier ventana razonable. */
    static final int DIAS_HISTORICO = 730;

    private final CostoRepositoryPort repositorio;
    private final int ventanaAlzaDias;
    private final double umbralAlza;

    public CostoProveedorService(CostoRepositoryPort repositorio,
                                 @Value("${app.reorden.alza-ventana-dias:90}") int ventanaAlzaDias,
                                 @Value("${app.reorden.alza-umbral:0.05}") double umbralAlza) {
        this.repositorio = repositorio;
        this.ventanaAlzaDias = Math.max(ventanaAlzaDias, 1);
        this.umbralAlza = umbralAlza;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<VariacionCostoResumen> variaciones(CostoFiltro filtro, int page, int size) {
        int p = PageResult.safePage(page);
        int s = PageResult.safeSize(size);
        LocalDate hoy = LocalDate.now();

        List<VariacionCostoResumen> todas = new ArrayList<>();
        for (List<HistoricoCosto> grupo : porPar(repositorio.historial(filtro, hoy.minusDays(DIAS_HISTORICO))).values()) {
            todas.add(resumen(grupo, hoy));
        }
        // Primero las mayores alzas a 90 días; los que no tienen variación, al final
        todas.sort(Comparator.comparing(VariacionCostoResumen::variacion90,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(VariacionCostoResumen::producto)
                .thenComparing(VariacionCostoResumen::proveedor));

        long total = todas.size();
        int desde = (int) Math.min((long) p * s, total);
        int hasta = (int) Math.min((long) desde + s, total);
        int paginas = (int) ((total + s - 1) / s);
        return new PageResult<>(todas.subList(desde, hasta), p, s, total, paginas);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoricoCosto> historial(Long productoId, Long proveedorId) {
        if (productoId == null) {
            throw new ParametrosReordenInvalidosException("Indique el producto");
        }
        List<HistoricoCosto> conVariacion = new ArrayList<>();
        CostoFiltro filtro = new CostoFiltro(proveedorId, productoId, null);
        for (List<HistoricoCosto> grupo : porPar(repositorio.historial(filtro, LocalDate.now().minusDays(DIAS_HISTORICO))).values()) {
            HistoricoCosto anterior = null;
            for (HistoricoCosto h : grupo) {
                conVariacion.add(h.conVariacion(anterior == null ? null : fraccion(variacion(anterior, h))));
                anterior = h;
            }
        }
        conVariacion.sort(Comparator.comparing(HistoricoCosto::fecha).thenComparing(HistoricoCosto::detalleId).reversed());
        return conVariacion;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariacionProveedor> resumenProveedores() {
        LocalDate hoy = LocalDate.now();
        Map<Long, List<VariacionCostoResumen>> porProveedor = new LinkedHashMap<>();
        for (List<HistoricoCosto> grupo : porPar(repositorio.historial(new CostoFiltro(null, null, null),
                hoy.minusDays(DIAS_HISTORICO))).values()) {
            VariacionCostoResumen r = resumen(grupo, hoy);
            porProveedor.computeIfAbsent(r.proveedorId(), k -> new ArrayList<>()).add(r);
        }

        List<VariacionProveedor> out = new ArrayList<>();
        for (List<VariacionCostoResumen> productos : porProveedor.values()) {
            VariacionCostoResumen primero = productos.getFirst();
            List<BigDecimal> v30 = productos.stream().map(VariacionCostoResumen::variacion30).filter(Objects::nonNull).toList();
            List<BigDecimal> v90 = productos.stream().map(VariacionCostoResumen::variacion90).filter(Objects::nonNull).toList();
            int conAlza = (int) productos.stream().filter(VariacionCostoResumen::alza).count();
            out.add(new VariacionProveedor(primero.proveedorId(), primero.proveedor(), productos.size(), conAlza,
                    media(v30), media(v90), v90.stream().max(Comparator.naturalOrder()).orElse(null)));
        }
        out.sort(Comparator.comparing(VariacionProveedor::variacionMedia90, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(VariacionProveedor::proveedor));
        return out;
    }

    // ------------------------------------------------------------------ cálculo

    private VariacionCostoResumen resumen(List<HistoricoCosto> grupo, LocalDate hoy) {
        HistoricoCosto primero = grupo.getFirst();
        HistoricoCosto ultimo = grupo.getLast();
        List<PuntoCosto> puntos = puntos(grupo);
        Double alza = VariacionCosto.calcular(puntos, hoy, ventanaAlzaDias);
        BigDecimal minimo = grupo.stream().map(HistoricoCosto::costoUnitario).min(Comparator.naturalOrder()).orElseThrow();
        BigDecimal maximo = grupo.stream().map(HistoricoCosto::costoUnitario).max(Comparator.naturalOrder()).orElseThrow();
        return new VariacionCostoResumen(ultimo.proveedorId(), ultimo.proveedor(), ultimo.productoId(), ultimo.producto(),
                ultimo.categoria(), grupo.size(), primero.fecha(), ultimo.fecha(), ultimo.costoUnitario(), minimo, maximo,
                fraccion(VariacionCosto.calcular(puntos, hoy, 30)), fraccion(VariacionCosto.calcular(puntos, hoy, 90)),
                alza != null && umbralAlza > 0 && alza >= umbralAlza);
    }

    /** Agrupa por par proveedor-producto conservando el orden (ya viene por fecha ascendente). */
    private static Map<String, List<HistoricoCosto>> porPar(List<HistoricoCosto> historial) {
        Map<String, List<HistoricoCosto>> grupos = new LinkedHashMap<>();
        for (HistoricoCosto h : historial) {
            grupos.computeIfAbsent(h.proveedorId() + ":" + h.productoId(), k -> new ArrayList<>()).add(h);
        }
        return grupos;
    }

    static List<PuntoCosto> puntos(List<HistoricoCosto> grupo) {
        return grupo.stream().map(h -> new PuntoCosto(h.fecha(), h.costoUnitario().doubleValue())).toList();
    }

    private static Double variacion(HistoricoCosto anterior, HistoricoCosto actual) {
        double base = anterior.costoUnitario().doubleValue();
        return base <= 0 ? null : (actual.costoUnitario().doubleValue() - base) / base;
    }

    private static BigDecimal media(List<BigDecimal> valores) {
        if (valores.isEmpty()) {
            return null;
        }
        BigDecimal suma = valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return suma.divide(BigDecimal.valueOf(valores.size()), 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal fraccion(Double x) {
        return x == null || x.isNaN() || x.isInfinite() ? null : BigDecimal.valueOf(x).setScale(4, RoundingMode.HALF_UP);
    }
}
