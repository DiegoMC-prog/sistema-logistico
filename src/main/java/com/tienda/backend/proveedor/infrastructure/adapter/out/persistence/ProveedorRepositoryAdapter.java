package com.tienda.backend.proveedor.infrastructure.adapter.out.persistence;

import com.tienda.backend.proveedor.application.model.ProveedorFiltro;
import com.tienda.backend.proveedor.application.port.out.ProveedorRepositoryPort;
import com.tienda.backend.proveedor.domain.model.Proveedor;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProveedorRepositoryAdapter implements ProveedorRepositoryPort {
    private final ProveedorJpaRepository proveedorJpaRepository;
    private final ProveedorPersistenceMapper mapper;

    @Override
    public Proveedor save(Proveedor proveedor) {
        return mapper.toDomain(proveedorJpaRepository.save(mapper.toEntity(proveedor)));
    }

    @Override
    public PageResult<Proveedor> findAll(ProveedorFiltro filtro, int page, int size) {
        Page<ProveedorEntity> result = proveedorJpaRepository.findAll(
                ProveedorSpecifications.desde(filtro), PageRequest.of(page, size, Sort.by("nombreEmpresa")));
        return new PageResult<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public Optional<Proveedor> findById(Long id) {
        return proveedorJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Proveedor> findAllById(Collection<Long> ids) {
        return proveedorJpaRepository.findAllById(ids).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        proveedorJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByNombreEmpresa(String nombreEmpresa) {
        return proveedorJpaRepository.existsByNombreEmpresa(nombreEmpresa);
    }

    @Override
    public boolean existsByNombreEmpresaAndIdNot(String nombreEmpresa, Long id) {
        return proveedorJpaRepository.existsByNombreEmpresaAndIdNot(nombreEmpresa, id);
    }
}
