package com.tienda.backend.application.service.impl;

import com.tienda.backend.application.dto.request.DepositoRequest;
import com.tienda.backend.application.dto.response.DepositoResponse;
import com.tienda.backend.application.mapper.DepositoDtoMapper;
import com.tienda.backend.application.service.DepositoService;
import com.tienda.backend.domain.exception.RecursoDuplicadoException;
import com.tienda.backend.domain.exception.RecursoNoEncontradoException;
import com.tienda.backend.domain.model.Deposito;
import com.tienda.backend.domain.repository.DepositoRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

@Service
@Transactional
public class DepositoServiceImpl implements DepositoService {
    private final DepositoRepository depositoRepository;
    private final DepositoDtoMapper depositoDtoMapper;

    public DepositoServiceImpl(DepositoRepository depositoRepository, DepositoDtoMapper depositoDtoMapper) {
        this.depositoRepository = depositoRepository;
        this.depositoDtoMapper = depositoDtoMapper;
    }

    @Override
    public DepositoResponse crear(DepositoRequest request) {
        if (depositoRepository.existePorNombre(request.nombre())){
            throw new RecursoDuplicadoException("El nombre del deposito ya existe");
        }

        Deposito deposito = depositoDtoMapper.toDomain(request);
        Deposito depositoGuardado = depositoRepository.guardar(deposito);

        return depositoDtoMapper.toResponse(depositoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public DepositoResponse obtenerPorId(Long id) {
        Deposito deposito = depositoRepository.buscarPorId(id).orElseThrow(
                () -> new RecursoNoEncontradoException("El deposito con el id: " + id + " no existe"));

        return depositoDtoMapper.toResponse(deposito);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DepositoResponse> listarTodo(Pageable pageable) {
        return depositoRepository.listarTodas(pageable)
                .map(depositoDtoMapper::toResponse);
    }

    @Override
    public DepositoResponse actualizar(Long id, DepositoRequest request) {
        Deposito deposito = depositoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El deposito con " + id + " no existe"));

        if (!deposito.getNombre().equalsIgnoreCase(request.nombre()) && depositoRepository.existePorNombre(request.nombre())) {
            throw new RecursoDuplicadoException("El nombre del deposito ya existe");
        }

        deposito.actualizarDatos(request.nombre(), request.descripcion(), request.activo());
        Deposito depositoActualizado = depositoRepository.guardar(deposito);

        return depositoDtoMapper.toResponse(depositoActualizado);
    }

    @Override
    public void eliminar(Long id) {
        if (!depositoRepository.existePorId(id)) {
            throw new RecursoNoEncontradoException("El deposito no existe");
        }

        depositoRepository.eliminar(id);
    }
}
