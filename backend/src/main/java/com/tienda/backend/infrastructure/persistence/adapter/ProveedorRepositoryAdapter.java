package com.tienda.backend.infrastructure.persistence.adapter;

import com.tienda.backend.domain.model.Proveedor;
import com.tienda.backend.domain.repository.ProveedorRepository;
import com.tienda.backend.infrastructure.persistence.entity.ProveedorEntity;
import com.tienda.backend.infrastructure.persistence.mapper.ProveedorEntityMapper;
import com.tienda.backend.infrastructure.persistence.repository.ProveedorJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProveedorRepositoryAdapter implements ProveedorRepository {

    private final ProveedorJpaRepository proveedorJpaRepository;
    private final ProveedorEntityMapper mapper;

    public ProveedorRepositoryAdapter(ProveedorJpaRepository proveedorJpaRepository, ProveedorEntityMapper mapper) {
        this.proveedorJpaRepository = proveedorJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Proveedor guardar(Proveedor proveedor) {
        ProveedorEntity entity = mapper.toEntity(proveedor);
        ProveedorEntity guardado = proveedorJpaRepository.save(entity);
        return mapper.toDomain(guardado);
    }

    @Override
    public Optional<Proveedor> buscarPorId(Long id) {
        return proveedorJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Proveedor> listarTodos(Pageable pageable) {
        return proveedorJpaRepository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Proveedor> buscarPorNombre(String nombreEmpresa, Pageable pageable) {
        return proveedorJpaRepository.findByNombreEmpresaContainingIgnoreCase(nombreEmpresa, pageable).map(mapper::toDomain);
    }

    @Override
    public boolean existePorNombreEmpresa(String nombreEmpresa) {
        return proveedorJpaRepository.existsByNombreEmpresaIgnoreCase(nombreEmpresa);
    }

    @Override
    public void eliminar(Long id) {
        proveedorJpaRepository.deleteById(id);
    }

    @Override
    public long contarTotal() {
        return proveedorJpaRepository.count();
    }

    @Override
    public long contarActivos() {
        return proveedorJpaRepository.countByEstado(true);
    }

    @Override
    public long contarInactivos() {
        return proveedorJpaRepository.countByEstado(false);
    }
}
