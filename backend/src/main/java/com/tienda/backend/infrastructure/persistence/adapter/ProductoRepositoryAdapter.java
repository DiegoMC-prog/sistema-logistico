package com.tienda.backend.infrastructure.persistence.adapter;

import com.tienda.backend.domain.model.Producto;
import com.tienda.backend.domain.repository.ProductoRepository;
import com.tienda.backend.infrastructure.persistence.entity.ProductoEntity;
import com.tienda.backend.infrastructure.persistence.mapper.ProductoEntityMapper;
import com.tienda.backend.infrastructure.persistence.repository.ProductoJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProductoRepositoryAdapter implements ProductoRepository {

    private final ProductoJpaRepository productoJpaRepository;
    private final ProductoEntityMapper mapper;

    public ProductoRepositoryAdapter(ProductoJpaRepository productoJpaRepository, ProductoEntityMapper mapper) {
        this.productoJpaRepository = productoJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Producto guardar(Producto producto) {
        ProductoEntity entity = mapper.toEntity(producto);
        ProductoEntity guardado = productoJpaRepository.save(entity);
        return mapper.toDomain(guardado);
    }

    @Override
    public Optional<Producto> buscarPorId(Long id) {
        return productoJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Producto> listarTodos(Pageable pageable) {
        return productoJpaRepository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Producto> buscarPorFiltros(String busqueda, Long categoriaId, Boolean estado, Pageable pageable) {
        return productoJpaRepository.buscarPorFiltros(busqueda, categoriaId, estado, pageable).map(mapper::toDomain);
    }

    @Override
    public boolean existePorCodigoBarras(String codigoBarras) {
        return productoJpaRepository.existsByCodigoBarras(codigoBarras);
    }

    @Override
    public boolean existePorCodigoBarrasYIdDistinto(String codigoBarras, Long id) {
        return productoJpaRepository.existsByCodigoBarrasAndIdNot(codigoBarras, id);
    }

    @Override
    public boolean existePorCategoriaId(Long categoriaId) {
        return productoJpaRepository.existsByCategoriaId(categoriaId);
    }

    @Override
    public void eliminar(Long id) {
        productoJpaRepository.deleteById(id);
    }

    @Override
    public long contarTotal() {
        return productoJpaRepository.count();
    }

    @Override
    public long contarActivos() {
        return productoJpaRepository.countByEstado(true);
    }

    @Override
    public long contarInactivos() {
        return productoJpaRepository.countByEstado(false);
    }
}
