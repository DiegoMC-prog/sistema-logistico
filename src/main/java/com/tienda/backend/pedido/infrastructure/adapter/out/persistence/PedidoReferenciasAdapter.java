package com.tienda.backend.pedido.infrastructure.adapter.out.persistence;

import com.tienda.backend.deposito.application.port.out.DepositoRepositoryPort;
import com.tienda.backend.deposito.domain.model.Deposito;
import com.tienda.backend.lote.application.port.out.LoteRepositoryPort;
import com.tienda.backend.lote.domain.model.Lote;
import com.tienda.backend.pedido.application.port.out.ReferenciasPedidoPort;
import com.tienda.backend.producto.application.port.out.ProductoRepositoryPort;
import com.tienda.backend.producto.domain.model.Producto;
import com.tienda.backend.proveedor.application.port.out.ProveedorRepositoryPort;
import com.tienda.backend.proveedor.domain.model.Proveedor;
import com.tienda.backend.usuario.application.port.out.UsuarioRepositoryPort;
import com.tienda.backend.usuario.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PedidoReferenciasAdapter implements ReferenciasPedidoPort {
    private final ProveedorRepositoryPort proveedorRepositoryPort;
    private final DepositoRepositoryPort depositoRepositoryPort;
    private final ProductoRepositoryPort productoRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final LoteRepositoryPort loteRepositoryPort;

    @Override
    public Optional<Proveedor> proveedor(Long id) {
        return proveedorRepositoryPort.findById(id);
    }

    @Override
    public Optional<Deposito> deposito(Long id) {
        return depositoRepositoryPort.findById(id);
    }

    @Override
    public List<Producto> productos(Collection<Long> ids) {
        return ids.isEmpty() ? List.of() : productoRepositoryPort.findAllById(ids);
    }

    @Override
    public Map<Long, String> nombresProveedores(Collection<Long> ids) {
        Map<Long, String> nombres = new HashMap<>();
        if (!ids.isEmpty()) {
            proveedorRepositoryPort.findAllById(ids).forEach(p -> nombres.put(p.getId(), p.getNombreEmpresa()));
        }
        return nombres;
    }

    @Override
    public Map<Long, String> nombresDepositos(Collection<Long> ids) {
        Map<Long, String> nombres = new HashMap<>();
        if (!ids.isEmpty()) {
            depositoRepositoryPort.findAllById(ids).forEach(d -> nombres.put(d.getId(), d.getNombre()));
        }
        return nombres;
    }

    @Override
    public Map<Long, String> nombresProductos(Collection<Long> ids) {
        Map<Long, String> nombres = new HashMap<>();
        if (!ids.isEmpty()) {
            productoRepositoryPort.findAllById(ids).forEach(p -> nombres.put(p.getId(), p.getNombre()));
        }
        return nombres;
    }

    @Override
    public Map<Long, String> nombresUsuarios(Collection<Long> ids) {
        Map<Long, String> nombres = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Usuario u : usuarioRepositoryPort.findAllById(ids)) {
                nombres.put(u.getId(), u.getNombre());
            }
        }
        return nombres;
    }

    @Override
    public Map<Long, String> numerosLote(Collection<Long> ids) {
        Map<Long, String> numeros = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Lote l : loteRepositoryPort.findAllById(ids)) {
                numeros.put(l.getId(), l.getNumeroLote());
            }
        }
        return numeros;
    }
}
