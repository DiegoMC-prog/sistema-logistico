package com.tienda.backend.inventario.application.service;

import com.tienda.backend.inventario.application.model.AsignacionFefo;
import com.tienda.backend.inventario.application.model.ItemFefoComando;
import com.tienda.backend.inventario.application.model.LoteDisponible;
import com.tienda.backend.inventario.application.model.SalidaFefoComando;
import com.tienda.backend.inventario.domain.exception.StockInsuficienteProductoException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import com.tienda.backend.deposito.domain.exception.DepositoNotFoundException;
import com.tienda.backend.deposito.domain.model.Deposito;
import com.tienda.backend.inventario.application.model.AjusteComando;
import com.tienda.backend.inventario.application.model.ClaveStock;
import com.tienda.backend.inventario.application.model.EntradaComando;
import com.tienda.backend.inventario.application.model.ItemEntradaComando;
import com.tienda.backend.inventario.application.model.ItemMovimientoComando;
import com.tienda.backend.inventario.application.model.NuevoLoteComando;
import com.tienda.backend.inventario.application.model.SalidaComando;
import com.tienda.backend.inventario.application.model.TrasladoComando;
import com.tienda.backend.inventario.application.port.out.DepositoConsultaPort;
import com.tienda.backend.inventario.application.port.out.InventarioRepositoryPort;
import com.tienda.backend.inventario.application.port.out.LoteConsultaPort;
import com.tienda.backend.inventario.application.port.out.LoteGestionPort;
import com.tienda.backend.inventario.application.port.out.MovimientoRepositoryPort;
import com.tienda.backend.inventario.application.port.out.PresentacionConsultaPort;
import com.tienda.backend.inventario.application.port.out.ProductoConsultaPort;
import com.tienda.backend.inventario.domain.exception.DepositoInactivoException;
import com.tienda.backend.inventario.domain.exception.LoteNoPerteneceAProductoException;
import com.tienda.backend.inventario.domain.exception.MovimientoInvalidoException;
import com.tienda.backend.inventario.domain.exception.PresentacionNoPerteneceAProductoException;
import com.tienda.backend.inventario.domain.exception.ProductoInactivoException;
import com.tienda.backend.inventario.domain.model.Inventario;
import com.tienda.backend.inventario.domain.model.Movimiento;
import com.tienda.backend.inventario.domain.model.MovimientoDetalle;
import com.tienda.backend.inventario.domain.model.TipoMovimiento;
import com.tienda.backend.lote.application.model.CrearLoteComando;
import com.tienda.backend.lote.domain.exception.LoteNotFoundException;
import com.tienda.backend.lote.domain.model.Lote;
import com.tienda.backend.presentacion.domain.exception.PresentacionNotFoundException;
import com.tienda.backend.presentacion.domain.model.Presentacion;
import com.tienda.backend.producto.domain.exception.ProductoNotFoundException;
import com.tienda.backend.producto.domain.model.Producto;
import com.tienda.backend.shared.application.port.out.UsuarioActualPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Único lugar donde cambia el stock. Cada operación es UNA transacción con tres fases:
 * <ol>
 *   <li><b>Validar</b>, sin bloquear: depósitos, lotes, productos y conversión a unidad base (solo lectura).</li>
 *   <li><b>Bloquear</b> las filas de stock a tocar ({@code SELECT ... FOR UPDATE}) en un orden global fijo. Quien llegue
 *       mientras tanto espera su turno en vez de fallar, y al despertar lee el stock ya actualizado.</li>
 *   <li><b>Aplicar</b> los cambios sobre las filas bloqueadas y registrar el movimiento.</li>
 * </ol>
 * Los bloqueos se liberan al confirmar o revertir. Por eso aquí no se arma la respuesta (consultas de nombres): eso
 * lo hace {@link InventarioService} después del commit, para que el bloqueo dure lo mínimo.
 */
@Service
@Transactional
@RequiredArgsConstructor
class MovimientoInventarioEjecutor {

    private static final int ESCALA_CANTIDAD = 4;

    private final InventarioRepositoryPort inventarioRepositoryPort;
    private final MovimientoRepositoryPort movimientoRepositoryPort;
    private final DepositoConsultaPort depositoConsultaPort;
    private final ProductoConsultaPort productoConsultaPort;
    private final LoteConsultaPort loteConsultaPort;
    private final LoteGestionPort loteGestionPort;
    private final PresentacionConsultaPort presentacionConsultaPort;
    private final UsuarioActualPort usuarioActualPort;

    /** Un lote ya resuelto y la cantidad convertida a unidad base. */
    private record LineaResuelta(Lote lote, BigDecimal cantidadBase) {
    }

    Movimiento ejecutarEntrada(EntradaComando comando) {
        // 1. Validar
        Long usuarioId = usuarioActualPort.id();
        Deposito destino = depositoActivo(comando.depositoId());
        List<ItemEntradaComando> items = itemsNoVacios(comando.items());
        verificarSinRepetidos(items.stream().map(ItemEntradaComando::loteId).filter(Objects::nonNull).toList());

        List<LineaResuelta> lineas = new ArrayList<>();
        for (ItemEntradaComando item : items) {
            Lote lote = resolverLoteDeEntrada(item);
            lineas.add(new LineaResuelta(lote, aUnidadBase(lote.getProductoId(), item.presentacionId(), item.cantidad())));
        }

        // 2. Bloquear
        Map<ClaveStock, Inventario> bloqueados = inventarioRepositoryPort.bloquear(
                lineas.stream().map(l -> new ClaveStock(destino.getId(), l.lote().getId())).toList());

        // 3. Aplicar
        List<MovimientoDetalle> detalles = new ArrayList<>();
        for (LineaResuelta linea : lineas) {
            Inventario inventario = bloqueados.get(new ClaveStock(destino.getId(), linea.lote().getId()));
            inventario.aumentar(linea.cantidadBase());
            inventarioRepositoryPort.save(inventario);
            detalles.add(new MovimientoDetalle(linea.lote().getProductoId(), linea.lote().getId(), linea.cantidadBase()));
        }

        return movimientoRepositoryPort.save(Movimiento.nuevo(TipoMovimiento.ENTRADA, null, destino.getId(), usuarioId,
                comando.observacion(), comando.referenciaTipo(), comando.referenciaId(), detalles));
    }

    Movimiento ejecutarSalida(SalidaComando comando) {
        // 1. Validar
        Long usuarioId = usuarioActualPort.id();
        Deposito origen = depositoExistente(comando.depositoId());
        exigirTexto(comando.motivo(), "El motivo de la salida es obligatorio");
        List<ItemMovimientoComando> items = itemsNoVacios(comando.items());
        verificarSinRepetidos(items.stream().map(ItemMovimientoComando::loteId).toList());
        List<LineaResuelta> lineas = resolverLineas(items);

        // 2. Bloquear
        Map<ClaveStock, Inventario> bloqueados = inventarioRepositoryPort.bloquear(
                lineas.stream().map(l -> new ClaveStock(origen.getId(), l.lote().getId())).toList());

        // 3. Aplicar: con el stock ya bloqueado y actualizado, aquí se decide si alcanza
        List<MovimientoDetalle> detalles = new ArrayList<>();
        for (LineaResuelta linea : lineas) {
            Inventario inventario = bloqueados.get(new ClaveStock(origen.getId(), linea.lote().getId()));
            inventario.disminuir(linea.cantidadBase());
            inventarioRepositoryPort.save(inventario);
            detalles.add(new MovimientoDetalle(linea.lote().getProductoId(), linea.lote().getId(), linea.cantidadBase()));
        }

        return movimientoRepositoryPort.save(Movimiento.nuevo(TipoMovimiento.SALIDA, origen.getId(), null, usuarioId,
                comando.motivo(), comando.referenciaTipo(), comando.referenciaId(), detalles));
    }

    /** Resultado de una salida FEFO: el movimiento registrado y de qué lotes salió cada línea. */
    record FefoEjecutado(Movimiento movimiento, List<AsignacionFefo> asignaciones) {
    }

    /**
     * Salida donde el sistema elige los lotes (FEFO). Se bloquean TODOS los lotes candidatos de los productos antes de
     * repartir, así dos ventas simultáneas nunca consumen el mismo stock, y el reparto usa el stock ya bloqueado.
     * Si dos líneas piden el mismo producto, el reparto lleva un stock de trabajo para no usar el mismo stock dos veces.
     */
    FefoEjecutado ejecutarSalidaFefo(SalidaFefoComando comando) {
        // 1. Validar
        Long usuarioId = usuarioActualPort.id();
        Deposito origen = depositoExistente(comando.depositoId());
        exigirTexto(comando.motivo(), "El motivo de la salida es obligatorio");
        List<ItemFefoComando> items = itemsNoVacios(comando.items());

        Map<Long, Producto> productos = new HashMap<>();
        List<BigDecimal> cantidadesBase = new ArrayList<>();
        for (ItemFefoComando item : items) {
            if (item.productoId() == null) {
                throw new MovimientoInvalidoException("El producto es obligatorio en cada línea");
            }
            Producto producto = productoConsultaPort.obtenerPorId(item.productoId())
                    .orElseThrow(() -> new ProductoNotFoundException(item.productoId()));
            if (!producto.estaActivo()) {
                throw new ProductoInactivoException(item.productoId());
            }
            productos.put(producto.getId(), producto);
            cantidadesBase.add(aUnidadBase(item.productoId(), item.presentacionId(), item.cantidad()));
        }

        // 2. Candidatos (lectura sin bloqueo) y bloqueo de todos ellos en el orden global
        List<LoteDisponible> candidatos = inventarioRepositoryPort.findLotesDisponiblesFefo(
                origen.getId(), productos.keySet(), LocalDate.now());
        Map<ClaveStock, Inventario> bloqueados = candidatos.isEmpty() ? Map.of() : inventarioRepositoryPort.bloquear(
                candidatos.stream().map(c -> new ClaveStock(origen.getId(), c.loteId())).toList());

        // 3. Repartir con el stock ya bloqueado y vigente
        Map<Long, BigDecimal> restante = new HashMap<>();
        Map<Long, List<LoteDisponible>> lotesPorProducto = new HashMap<>();
        Map<Long, Long> productoDeLote = new HashMap<>();
        for (LoteDisponible c : candidatos) {
            restante.put(c.loteId(), bloqueados.get(new ClaveStock(origen.getId(), c.loteId())).getStockActual());
            lotesPorProducto.computeIfAbsent(c.productoId(), k -> new ArrayList<>()).add(c);
            productoDeLote.put(c.loteId(), c.productoId());
        }

        List<AsignacionFefo> asignaciones = new ArrayList<>();
        Map<Long, BigDecimal> consumido = new LinkedHashMap<>();
        for (int i = 0; i < items.size(); i++) {
            Long productoId = items.get(i).productoId();
            BigDecimal pedido = cantidadesBase.get(i);
            BigDecimal falta = pedido;
            for (LoteDisponible lote : lotesPorProducto.getOrDefault(productoId, List.of())) {
                if (falta.signum() <= 0) {
                    break;
                }
                BigDecimal disponible = restante.get(lote.loteId());
                if (disponible.signum() <= 0) {
                    continue;
                }
                BigDecimal tomar = disponible.min(falta);
                restante.put(lote.loteId(), disponible.subtract(tomar));
                consumido.merge(lote.loteId(), tomar, BigDecimal::add);
                asignaciones.add(new AsignacionFefo(i, lote.loteId(), tomar, lote.costoUnitario(),
                        lote.porcentajeDescuentoVencimiento()));
                falta = falta.subtract(tomar);
            }
            if (falta.signum() > 0) {
                throw new StockInsuficienteProductoException(productos.get(productoId).getNombre(),
                        pedido.subtract(falta), pedido);
            }
        }

        // 4. Aplicar
        List<MovimientoDetalle> detalles = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> e : consumido.entrySet()) {
            Inventario inventario = bloqueados.get(new ClaveStock(origen.getId(), e.getKey()));
            inventario.disminuir(e.getValue());
            inventarioRepositoryPort.save(inventario);
            detalles.add(new MovimientoDetalle(productoDeLote.get(e.getKey()), e.getKey(), e.getValue()));
        }

        Movimiento movimiento = movimientoRepositoryPort.save(Movimiento.nuevo(TipoMovimiento.SALIDA, origen.getId(),
                null, usuarioId, comando.motivo(), comando.referenciaTipo(), comando.referenciaId(), detalles));
        return new FefoEjecutado(movimiento, asignaciones);
    }

    Movimiento ejecutarTraslado(TrasladoComando comando) {
        // 1. Validar
        Long usuarioId = usuarioActualPort.id();
        if (comando.depositoOrigenId() != null && comando.depositoOrigenId().equals(comando.depositoDestinoId())) {
            throw new MovimientoInvalidoException("El depósito de origen y el de destino deben ser distintos");
        }
        Deposito origen = depositoExistente(comando.depositoOrigenId());
        Deposito destino = depositoActivo(comando.depositoDestinoId());
        List<ItemMovimientoComando> items = itemsNoVacios(comando.items());
        verificarSinRepetidos(items.stream().map(ItemMovimientoComando::loteId).toList());
        List<LineaResuelta> lineas = resolverLineas(items);

        // 2. Bloquear origen y destino de todas las líneas juntos: el orden global evita deadlocks con traslados cruzados
        List<ClaveStock> claves = new ArrayList<>();
        for (LineaResuelta linea : lineas) {
            claves.add(new ClaveStock(origen.getId(), linea.lote().getId()));
            claves.add(new ClaveStock(destino.getId(), linea.lote().getId()));
        }
        Map<ClaveStock, Inventario> bloqueados = inventarioRepositoryPort.bloquear(claves);

        // 3. Aplicar
        List<MovimientoDetalle> detalles = new ArrayList<>();
        for (LineaResuelta linea : lineas) {
            Inventario enOrigen = bloqueados.get(new ClaveStock(origen.getId(), linea.lote().getId()));
            Inventario enDestino = bloqueados.get(new ClaveStock(destino.getId(), linea.lote().getId()));
            enOrigen.disminuir(linea.cantidadBase());
            enDestino.aumentar(linea.cantidadBase());
            inventarioRepositoryPort.save(enOrigen);
            inventarioRepositoryPort.save(enDestino);
            detalles.add(new MovimientoDetalle(linea.lote().getProductoId(), linea.lote().getId(), linea.cantidadBase()));
        }

        return movimientoRepositoryPort.save(Movimiento.nuevo(TipoMovimiento.TRASLADO, origen.getId(), destino.getId(),
                usuarioId, comando.observacion(), null, null, detalles));
    }

    Movimiento ejecutarAjuste(AjusteComando comando) {
        // 1. Validar
        Long usuarioId = usuarioActualPort.id();
        Deposito deposito = depositoExistente(comando.depositoId());
        exigirTexto(comando.motivo(), "El motivo del ajuste es obligatorio");
        BigDecimal stockReal = comando.stockReal();
        if (stockReal == null || stockReal.signum() < 0) {
            throw new MovimientoInvalidoException("El stock real es obligatorio y no puede ser negativo");
        }
        validarEscala(stockReal);
        Lote lote = loteExistente(comando.loteId());

        // 2. Bloquear ANTES de leer el stock actual: la diferencia se calcula sobre el valor vigente, no uno viejo
        Inventario inventario = inventarioRepositoryPort
                .bloquear(List.of(new ClaveStock(deposito.getId(), lote.getId())))
                .get(new ClaveStock(deposito.getId(), lote.getId()));

        // 3. Aplicar
        BigDecimal diferencia = stockReal.subtract(inventario.getStockActual());
        if (diferencia.signum() == 0) {
            throw new MovimientoInvalidoException("El stock real coincide con el stock actual: no hay nada que ajustar");
        }

        BigDecimal cantidad = diferencia.abs();
        TipoMovimiento tipo;
        Long origenId = null;
        Long destinoId = null;
        if (diferencia.signum() > 0) {
            inventario.aumentar(cantidad);
            tipo = TipoMovimiento.AJUSTE_POSITIVO;
            destinoId = deposito.getId();
        } else {
            inventario.disminuir(cantidad);
            tipo = TipoMovimiento.AJUSTE_NEGATIVO;
            origenId = deposito.getId();
        }
        inventarioRepositoryPort.save(inventario);

        return movimientoRepositoryPort.save(Movimiento.nuevo(tipo, origenId, destinoId, usuarioId, comando.motivo(),
                null, null, List.of(new MovimientoDetalle(lote.getProductoId(), lote.getId(), cantidad))));
    }

    /** Resuelve el lote de cada línea (debe existir) y convierte su cantidad a unidad base. */
    private List<LineaResuelta> resolverLineas(List<ItemMovimientoComando> items) {
        List<LineaResuelta> lineas = new ArrayList<>();
        for (ItemMovimientoComando item : items) {
            Lote lote = loteExistente(item.loteId());
            lineas.add(new LineaResuelta(lote, aUnidadBase(lote.getProductoId(), item.presentacionId(), item.cantidad())));
        }
        return lineas;
    }

    private Lote resolverLoteDeEntrada(ItemEntradaComando item) {
        if (item.loteId() != null) {
            Lote lote = loteExistente(item.loteId());
            if (item.productoId() != null && !item.productoId().equals(lote.getProductoId())) {
                throw new LoteNoPerteneceAProductoException(lote.getId(), item.productoId());
            }
            productoActivo(lote.getProductoId());
            return lote;
        }

        NuevoLoteComando nuevo = item.lote();
        if (nuevo == null) {
            throw new MovimientoInvalidoException(
                    "Cada línea debe indicar un lote existente (loteId) o los datos de un lote nuevo (lote)");
        }
        if (item.productoId() == null) {
            throw new MovimientoInvalidoException("Para crear un lote nuevo debe indicar el productoId");
        }
        productoActivo(item.productoId());
        return loteGestionPort.crearLote(new CrearLoteComando(item.productoId(), nuevo.numeroLote(),
                nuevo.fechaVencimiento(), nuevo.costoUnitarioCompra(), nuevo.porcentajeDescuentoVencimiento()));
    }

    /** Convierte a unidad base: sin presentación la cantidad ya es base; con presentación se multiplica por su factor. */
    private BigDecimal aUnidadBase(Long productoId, Long presentacionId, BigDecimal cantidad) {
        if (cantidad == null || cantidad.signum() <= 0) {
            throw new MovimientoInvalidoException("La cantidad debe ser mayor que cero");
        }
        validarEscala(cantidad);
        if (presentacionId == null) {
            return cantidad;
        }
        Presentacion presentacion = presentacionConsultaPort.obtenerPorId(presentacionId)
                .orElseThrow(() -> new PresentacionNotFoundException(presentacionId));
        if (!presentacion.getProductoId().equals(productoId)) {
            throw new PresentacionNoPerteneceAProductoException(presentacionId, productoId);
        }
        return cantidad.multiply(presentacion.getFactorConversion()).setScale(ESCALA_CANTIDAD, RoundingMode.HALF_UP);
    }

    private void validarEscala(BigDecimal cantidad) {
        if (cantidad.stripTrailingZeros().scale() > ESCALA_CANTIDAD) {
            throw new MovimientoInvalidoException("Las cantidades admiten hasta " + ESCALA_CANTIDAD + " decimales");
        }
    }

    private Deposito depositoExistente(Long depositoId) {
        if (depositoId == null) {
            throw new MovimientoInvalidoException("El depósito es obligatorio");
        }
        return depositoConsultaPort.obtenerPorId(depositoId).orElseThrow(() -> new DepositoNotFoundException(depositoId));
    }

    private Deposito depositoActivo(Long depositoId) {
        Deposito deposito = depositoExistente(depositoId);
        if (!deposito.estaActivo()) {
            throw new DepositoInactivoException(deposito.getNombre());
        }
        return deposito;
    }

    private Lote loteExistente(Long loteId) {
        if (loteId == null) {
            throw new MovimientoInvalidoException("El lote es obligatorio");
        }
        return loteConsultaPort.obtenerPorId(loteId).orElseThrow(() -> new LoteNotFoundException(loteId));
    }

    private void productoActivo(Long productoId) {
        Producto producto = productoConsultaPort.obtenerPorId(productoId)
                .orElseThrow(() -> new ProductoNotFoundException(productoId));
        if (!producto.estaActivo()) {
            throw new ProductoInactivoException(productoId);
        }
    }

    private <T> List<T> itemsNoVacios(List<T> items) {
        if (items == null || items.isEmpty()) {
            throw new MovimientoInvalidoException("Debe indicar al menos una línea");
        }
        return items;
    }

    private void verificarSinRepetidos(List<Long> loteIds) {
        Set<Long> vistos = new HashSet<>();
        for (Long id : loteIds) {
            if (!vistos.add(id)) {
                throw new MovimientoInvalidoException(
                        "El lote " + id + " aparece más de una vez; sume sus cantidades en una sola línea");
            }
        }
    }

    private void exigirTexto(String texto, String mensaje) {
        if (texto == null || texto.isBlank()) {
            throw new MovimientoInvalidoException(mensaje);
        }
    }
}
