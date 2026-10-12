package com.tienda.backend.application.service.impl;

import com.tienda.backend.application.dto.request.ProveedorRequest;
import com.tienda.backend.application.dto.response.PageResponse;
import com.tienda.backend.application.dto.response.ProveedorResponse;
import com.tienda.backend.application.dto.response.ProveedorStatsResponse;
import com.tienda.backend.application.mapper.ProveedorDtoMapper;
import com.tienda.backend.application.service.ProveedorService;
import com.tienda.backend.domain.exception.RecursoNoEncontrado;
import com.tienda.backend.domain.model.Proveedor;
import com.tienda.backend.domain.repository.ProveedorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final ProveedorDtoMapper mapper;

    public ProveedorServiceImpl(ProveedorRepository proveedorRepository, ProveedorDtoMapper mapper) {
        this.proveedorRepository = proveedorRepository;
        this.mapper = mapper;
    }

    @Override
    public ProveedorResponse crear(ProveedorRequest request) {
        if (proveedorRepository.existePorNombreEmpresa(request.nombreEmpresa().trim())) {
            throw new IllegalArgumentException(
                    "Ya existe un proveedor con el nombre de empresa '" + request.nombreEmpresa().trim() + "'.");
        }

        Proveedor proveedor = mapper.toDomain(request);
        Proveedor guardado = proveedorRepository.guardar(proveedor);

        return mapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorResponse obtenerPorId(Long id) {
        return proveedorRepository.buscarPorId(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el proveedor con ID " + id + "."));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProveedorResponse> listar(int page, int size, String nombreEmpresa) {
        if (page < 0) {
            throw new IllegalArgumentException("El parámetro 'page' debe ser mayor o igual a 0.");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("El parámetro 'size' debe estar entre 1 y 100.");
        }

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("nombreEmpresa").ascending().and(Sort.by("id").ascending()));

        Page<Proveedor> resultado = (nombreEmpresa == null || nombreEmpresa.isBlank())
                ? proveedorRepository.listarTodos(pageable)
                : proveedorRepository.buscarPorNombre(nombreEmpresa.trim(), pageable);

        return PageResponse.from(resultado.map(mapper::toResponse));
    }

    @Override
    public ProveedorResponse actualizar(Long id, ProveedorRequest request) {
        Proveedor proveedor = proveedorRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el proveedor con ID " + id + "."));

        if (!proveedor.getNombreEmpresa().equalsIgnoreCase(request.nombreEmpresa().trim())
                && proveedorRepository.existePorNombreEmpresa(request.nombreEmpresa().trim())) {
            throw new IllegalArgumentException(
                    "Ya existe otro proveedor con el nombre de empresa '" + request.nombreEmpresa().trim() + "'.");
        }

        proveedor.actualizar(
                request.nombreEmpresa(),
                request.direccionEmpresa(),
                request.telefonoEmpresa(),
                request.nombreContacto(),
                request.telefonoContacto()
        );

        Proveedor actualizado = proveedorRepository.guardar(proveedor);
        return mapper.toResponse(actualizado);
    }

    @Override
    public ProveedorResponse cambiarEstado(Long id, boolean estado) {
        Proveedor proveedor = proveedorRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el proveedor con ID " + id + "."));

        proveedor.cambiarEstado(estado);
        Proveedor actualizado = proveedorRepository.guardar(proveedor);
        return mapper.toResponse(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        if (proveedorRepository.buscarPorId(id).isEmpty()) {
            throw new RecursoNoEncontrado("No se puede eliminar: no existe un proveedor con ID " + id + ".");
        }

        proveedorRepository.eliminar(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorStatsResponse obtenerEstadisticas() {
        long totalProveedores = proveedorRepository.contarTotal();
        long activos = proveedorRepository.contarActivos();
        long inactivos = proveedorRepository.contarInactivos();

        return new ProveedorStatsResponse(totalProveedores, activos, inactivos);
    }
}
