package com.tienda.backend.proveedor.application.port.out;

import com.tienda.backend.proveedor.application.model.ProveedorFiltro;
import com.tienda.backend.proveedor.domain.model.Proveedor;
import com.tienda.backend.shared.application.model.PageResult;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProveedorRepositoryPort {
    Proveedor save(Proveedor proveedor);
    PageResult<Proveedor> findAll(ProveedorFiltro filtro, int page, int size);
    Optional<Proveedor> findById(Long id);
    List<Proveedor> findAllById(Collection<Long> ids);
    void deleteById(Long id);
    boolean existsByNombreEmpresa(String nombreEmpresa);
    boolean existsByNombreEmpresaAndIdNot(String nombreEmpresa, Long id);
}
