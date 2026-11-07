package com.tienda.backend.producto.infrastructure.adapter.out.persistence;

import com.tienda.backend.producto.application.port.out.ProductoRepositoryPort;
import com.tienda.backend.producto.domain.model.Producto;
import com.tienda.backend.producto.application.model.ProductoDetalle;
import com.tienda.backend.producto.application.model.ProductoFiltro;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductoRepositoryAdapter implements ProductoRepositoryPort {
    private final ProductoJpaRepository productoJpaRepository;
    private final ProductoPersistenceMapper mapper;

    @Override
    public Producto save(Producto producto) {
        ProductoEntity guardado = productoJpaRepository.save(mapper.toEntity(producto));
        return mapper.toDomain(guardado);
    }

    @Override
    public PageResult<ProductoDetalle> findAll(ProductoFiltro filtro, int page, int size) {
        Page<ProductoEntity> result = productoJpaRepository.findAll(ProductoSpecifications.desde(filtro), PageRequest.of(page, size, Sort.by("id")));
        return new PageResult<>(
                result.getContent().stream()
                        .map(e -> new ProductoDetalle(mapper.toDomain(e), e.getCategoria().getNombre()))
                        .toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public Optional<Producto> findById(Long id) {
        return productoJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        productoJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByNombre(String nombre) {
        return productoJpaRepository.existsByNombre(nombre);
    }

    @Override
    public boolean existsByNombreAndIdNot(String nombre, Long id) {
        return productoJpaRepository.existsByNombreAndIdNot(nombre, id);
    }
}
