package com.tienda.backend.pedido.application.port.out;

import com.tienda.backend.deposito.domain.model.Deposito;
import com.tienda.backend.producto.domain.model.Producto;
import com.tienda.backend.proveedor.domain.model.Proveedor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Consultas a otros módulos que el pedido necesita para validar y para armar sus vistas. */
public interface ReferenciasPedidoPort {
    Optional<Proveedor> proveedor(Long id);

    Optional<Deposito> deposito(Long id);

    List<Producto> productos(Collection<Long> ids);

    Map<Long, String> nombresProveedores(Collection<Long> ids);

    Map<Long, String> nombresDepositos(Collection<Long> ids);

    Map<Long, String> nombresProductos(Collection<Long> ids);

    Map<Long, String> nombresUsuarios(Collection<Long> ids);

    Map<Long, String> numerosLote(Collection<Long> ids);
}
