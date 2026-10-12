package com.tienda.backend.application.service.impl;

import com.tienda.backend.application.dto.request.DepositoRequest;
import com.tienda.backend.application.dto.response.DepositoResponse;
import com.tienda.backend.application.dto.response.DepositoStatsResponse;
import com.tienda.backend.application.dto.response.PageResponse;
import com.tienda.backend.application.mapper.DepositoDtoMapper;
import com.tienda.backend.application.service.DepositoService;
import com.tienda.backend.domain.exception.RecursoNoEncontrado;
import com.tienda.backend.domain.model.Deposito;
import com.tienda.backend.domain.repository.DepositoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DepositoServiceImpl implements DepositoService {

    private final DepositoRepository depositoRepository;
    private final DepositoDtoMapper mapper;

    public DepositoServiceImpl(DepositoRepository depositoRepository, DepositoDtoMapper mapper) {
        this.depositoRepository = depositoRepository;
        this.mapper = mapper;
    }

    @Override
    public DepositoResponse crear(DepositoRequest request) {
        if (depositoRepository.existePorNombre(request.nombre().trim())) {
            throw new IllegalArgumentException(
                    "Ya existe un depósito con el nombre '" + request.nombre().trim() + "'.");
        }

        Deposito deposito = mapper.toDomain(request);
        Deposito guardado = depositoRepository.guardar(deposito);

        return mapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public DepositoResponse obtenerPorId(Long id) {
        return depositoRepository.buscarPorId(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el depósito con ID " + id + "."));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DepositoResponse> listar(int page, int size, String nombre) {
        if (page < 0) {
            throw new IllegalArgumentException("El parámetro 'page' debe ser mayor o igual a 0.");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("El parámetro 'size' debe estar entre 1 y 100.");
        }

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("nombre").ascending().and(Sort.by("id").ascending()));

        Page<Deposito> resultado = (nombre == null || nombre.isBlank())
                ? depositoRepository.listarTodos(pageable)
                : depositoRepository.buscarPorNombre(nombre.trim(), pageable);

        return PageResponse.from(resultado.map(mapper::toResponse));
    }

    @Override
    public DepositoResponse actualizar(Long id, DepositoRequest request) {
        Deposito deposito = depositoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el depósito con ID " + id + "."));

        if (!deposito.getNombre().equalsIgnoreCase(request.nombre().trim())
                && depositoRepository.existePorNombre(request.nombre().trim())) {
            throw new IllegalArgumentException(
                    "Ya existe otro depósito con el nombre '" + request.nombre().trim() + "'.");
        }

        deposito.actualizar(request.nombre(), request.descripcion());
        Deposito actualizado = depositoRepository.guardar(deposito);

        return mapper.toResponse(actualizado);
    }

    @Override
    public DepositoResponse cambiarEstado(Long id, boolean estado) {
        Deposito deposito = depositoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el depósito con ID " + id + "."));

        deposito.cambiarEstado(estado);
        Deposito actualizado = depositoRepository.guardar(deposito);

        return mapper.toResponse(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        if (depositoRepository.buscarPorId(id).isEmpty()) {
            throw new RecursoNoEncontrado("No se puede eliminar: no existe un depósito con ID " + id + ".");
        }

        depositoRepository.eliminar(id);
    }

    @Override
    @Transactional(readOnly = true)
    public DepositoStatsResponse obtenerEstadisticas() {
        long totalDepositos = depositoRepository.contarTotal();
        long activos = depositoRepository.contarActivos();
        long inactivos = depositoRepository.contarInactivos();

        return new DepositoStatsResponse(totalDepositos, activos, inactivos);
    }
}
