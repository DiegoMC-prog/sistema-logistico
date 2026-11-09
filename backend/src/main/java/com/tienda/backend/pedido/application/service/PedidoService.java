package com.tienda.backend.pedido.application.service;

import com.tienda.backend.deposito.domain.exception.DepositoNotFoundException;
import com.tienda.backend.deposito.domain.model.Deposito;
import com.tienda.backend.pedido.application.model.PedidoComando;
import com.tienda.backend.pedido.application.model.PedidoDetallado;
import com.tienda.backend.pedido.application.model.PedidoFiltro;
import com.tienda.backend.pedido.application.model.RecepcionDetallada;
import com.tienda.backend.pedido.application.port.in.ConsultarPedidoUseCase;
import com.tienda.backend.pedido.application.port.in.GestionarPedidoUseCase;
import com.tienda.backend.pedido.application.port.out.NumeroPedidoGeneradorPort;
import com.tienda.backend.pedido.application.port.out.PedidoRepositoryPort;
import com.tienda.backend.pedido.application.port.out.ReferenciasPedidoPort;
import com.tienda.backend.pedido.domain.exception.PedidoDatosInvalidosException;
import com.tienda.backend.pedido.domain.exception.PedidoEstadoInvalidoException;
import com.tienda.backend.pedido.domain.exception.PedidoNotFoundException;
import com.tienda.backend.pedido.domain.model.LineaPedido;
import com.tienda.backend.pedido.domain.model.PedidoProveedor;
import com.tienda.backend.producto.domain.exception.ProductoNotFoundException;
import com.tienda.backend.producto.domain.model.Producto;
import com.tienda.backend.proveedor.domain.exception.ProveedorInactivoException;
import com.tienda.backend.proveedor.domain.exception.ProveedorNotFoundException;
import com.tienda.backend.proveedor.domain.model.Proveedor;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoService implements GestionarPedidoUseCase, ConsultarPedidoUseCase {

    private final PedidoRepositoryPort pedidoRepositoryPort;
    private final ReferenciasPedidoPort referencias;
    private final NumeroPedidoGeneradorPort numeroGenerador;
    private final PedidoVistaFactory vistaFactory;

    @Override
    @Transactional
    public PedidoDetallado crear(PedidoComando comando) {
        List<LineaPedido> lineas = validarYArmarLineas(comando);
        PedidoProveedor pedido = PedidoProveedor.nuevo(numeroGenerador.siguiente(), comando.proveedorId(),
                comando.depositoDestinoId(), comando.fechaEntregaEstimada(), comando.observacion(), lineas);
        return vistaFactory.construir(pedidoRepositoryPort.save(pedido));
    }

    @Override
    @Transactional
    public PedidoDetallado editar(Long id, PedidoComando comando) {
        PedidoProveedor pedido = buscar(id);
        if (!pedido.esBorrador()) {
            throw new PedidoEstadoInvalidoException("Solo se puede editar un pedido en BORRADOR (pedido "
                    + pedido.getNumeroPedido() + " está " + pedido.getEstado() + ")");
        }
        List<LineaPedido> lineas = validarYArmarLineas(comando);
        pedido.editar(comando.proveedorId(), comando.depositoDestinoId(), comando.fechaEntregaEstimada(),
                comando.observacion(), lineas);
        return vistaFactory.construir(pedidoRepositoryPort.save(pedido));
    }

    @Override
    @Transactional
    public PedidoDetallado enviar(Long id) {
        PedidoProveedor pedido = buscar(id);
        pedido.enviar();
        // Entre el borrador y el envío el proveedor o el depósito pudieron desactivarse
        exigirProveedorActivo(pedido.getProveedorId());
        exigirDepositoActivo(pedido.getDepositoDestinoId());
        return vistaFactory.construir(pedidoRepositoryPort.save(pedido));
    }

    @Override
    @Transactional
    public PedidoDetallado cancelar(Long id) {
        PedidoProveedor pedido = buscar(id);
        pedido.cancelar();
        return vistaFactory.construir(pedidoRepositoryPort.save(pedido));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        PedidoProveedor pedido = buscar(id);
        if (!pedido.esBorrador()) {
            throw new PedidoEstadoInvalidoException("Solo se puede eliminar un pedido en BORRADOR (pedido "
                    + pedido.getNumeroPedido() + " está " + pedido.getEstado() + "). Puede cancelarlo.");
        }
        pedidoRepositoryPort.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<PedidoDetallado> listar(PedidoFiltro filtro, int page, int size) {
        PageResult<PedidoProveedor> pagina = pedidoRepositoryPort.findAll(filtro, PageResult.safePage(page),
                PageResult.safeSize(size));
        List<PedidoDetallado> vistas = vistaFactory.construirCabeceras(pagina.content());
        return new PageResult<>(vistas, pagina.page(), pagina.size(), pagina.totalElements(), pagina.totalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoDetallado obtener(Long id) {
        return vistaFactory.construir(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecepcionDetallada> listarRecepciones(Long pedidoId) {
        PedidoProveedor pedido = buscar(pedidoId);
        return vistaFactory.construir(pedido, pedidoRepositoryPort.findRecepcionesByPedidoId(pedidoId));
    }

    private PedidoProveedor buscar(Long id) {
        return pedidoRepositoryPort.findById(id).orElseThrow(() -> new PedidoNotFoundException(id));
    }

    /** Valida proveedor, depósito y productos (que existan y estén activos) y arma las líneas del pedido. */
    private List<LineaPedido> validarYArmarLineas(PedidoComando comando) {
        if (comando.proveedorId() == null) {
            throw new PedidoDatosInvalidosException("El proveedor es obligatorio");
        }
        if (comando.depositoDestinoId() == null) {
            throw new PedidoDatosInvalidosException("El depósito de destino es obligatorio");
        }
        exigirProveedorActivo(comando.proveedorId());
        exigirDepositoActivo(comando.depositoDestinoId());

        List<PedidoComando.Item> items = comando.items() == null ? List.of() : comando.items();
        Set<Long> productoIds = items.stream().map(PedidoComando.Item::productoId)
                .filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Producto> productos = referencias.productos(productoIds).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));
        for (Long productoId : productoIds) {
            Producto producto = productos.get(productoId);
            if (producto == null) {
                throw new ProductoNotFoundException(productoId);
            }
            if (!producto.estaActivo()) {
                throw new PedidoDatosInvalidosException("El producto " + producto.getNombre() + " está inactivo");
            }
        }
        return items.stream()
                .map(i -> new LineaPedido(i.productoId(), i.cantidad(), i.costoUnitario()))
                .toList();
    }

    private void exigirProveedorActivo(Long proveedorId) {
        Proveedor proveedor = referencias.proveedor(proveedorId)
                .orElseThrow(() -> new ProveedorNotFoundException(proveedorId));
        if (!proveedor.estaActivo()) {
            throw new ProveedorInactivoException(proveedorId);
        }
    }

    private void exigirDepositoActivo(Long depositoId) {
        Deposito deposito = referencias.deposito(depositoId)
                .orElseThrow(() -> new DepositoNotFoundException(depositoId));
        if (!deposito.estaActivo()) {
            throw new PedidoDatosInvalidosException("El depósito " + deposito.getNombre() + " está inactivo");
        }
    }
}
