package com.tienda.backend.proveedor.application.service;

import com.tienda.backend.proveedor.application.model.ProveedorFiltro;
import com.tienda.backend.proveedor.application.port.in.CambiarEstadoProveedorUseCase;
import com.tienda.backend.proveedor.application.port.in.CrearProveedorUseCase;
import com.tienda.backend.proveedor.application.port.in.EditarProveedorUseCase;
import com.tienda.backend.proveedor.application.port.in.EliminarProveedorUseCase;
import com.tienda.backend.proveedor.application.port.in.ListarProveedorUseCase;
import com.tienda.backend.proveedor.application.port.in.ObtenerProveedorUseCase;
import com.tienda.backend.proveedor.application.port.out.PedidoConsultaPort;
import com.tienda.backend.proveedor.application.port.out.ProveedorRepositoryPort;
import com.tienda.backend.proveedor.domain.exception.ProveedorConPedidosException;
import com.tienda.backend.proveedor.domain.exception.ProveedorNombreDuplicadoException;
import com.tienda.backend.proveedor.domain.exception.ProveedorNotFoundException;
import com.tienda.backend.proveedor.domain.model.Proveedor;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProveedorService implements CrearProveedorUseCase, EditarProveedorUseCase, ListarProveedorUseCase,
        ObtenerProveedorUseCase, EliminarProveedorUseCase, CambiarEstadoProveedorUseCase {

    private final ProveedorRepositoryPort proveedorRepositoryPort;
    private final PedidoConsultaPort pedidoConsultaPort;

    @Override
    @Transactional
    public Proveedor create(Proveedor proveedor) {
        if (proveedorRepositoryPort.existsByNombreEmpresa(proveedor.getNombreEmpresa())) {
            throw new ProveedorNombreDuplicadoException(proveedor.getNombreEmpresa());
        }
        return proveedorRepositoryPort.save(proveedor);
    }

    @Override
    @Transactional
    public Proveedor editar(Long id, Proveedor datos) {
        Proveedor existente = buscar(id);
        if (proveedorRepositoryPort.existsByNombreEmpresaAndIdNot(datos.getNombreEmpresa(), id)) {
            throw new ProveedorNombreDuplicadoException(datos.getNombreEmpresa());
        }
        existente.editar(datos.getNombreEmpresa(), datos.getNombreContacto(), datos.getTelefono(), datos.getEmail());
        return proveedorRepositoryPort.save(existente);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Proveedor> listar(ProveedorFiltro filtro, int page, int size) {
        return proveedorRepositoryPort.findAll(filtro, PageResult.safePage(page), PageResult.safeSize(size));
    }

    @Override
    @Transactional(readOnly = true)
    public Proveedor obtener(Long id) {
        return buscar(id);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        buscar(id);
        if (pedidoConsultaPort.existenPedidosDelProveedor(id)) {
            throw new ProveedorConPedidosException();
        }
        proveedorRepositoryPort.deleteById(id);
    }

    @Override
    @Transactional
    public Proveedor activar(Long id) {
        Proveedor proveedor = buscar(id);
        proveedor.activar();
        return proveedorRepositoryPort.save(proveedor);
    }

    @Override
    @Transactional
    public Proveedor desactivar(Long id) {
        Proveedor proveedor = buscar(id);
        proveedor.desactivar();
        return proveedorRepositoryPort.save(proveedor);
    }

    private Proveedor buscar(Long id) {
        return proveedorRepositoryPort.findById(id).orElseThrow(() -> new ProveedorNotFoundException(id));
    }
}
