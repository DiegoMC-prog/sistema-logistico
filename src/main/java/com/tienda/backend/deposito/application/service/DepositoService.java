package com.tienda.backend.deposito.application.service;

import com.tienda.backend.deposito.application.model.DepositoFiltro;
import com.tienda.backend.deposito.application.port.in.CambiarEstadoDepositoUseCase;
import com.tienda.backend.deposito.application.port.in.CrearDepositoUseCase;
import com.tienda.backend.deposito.application.port.in.EditarDepositoUseCase;
import com.tienda.backend.deposito.application.port.in.EliminarDepositoUseCase;
import com.tienda.backend.deposito.application.port.in.ListarDepositoUseCase;
import com.tienda.backend.deposito.application.port.in.ObtenerDepositoUseCase;
import com.tienda.backend.deposito.application.port.out.DepositoRepositoryPort;
import com.tienda.backend.deposito.domain.exception.DepositoNombreDuplicadoException;
import com.tienda.backend.deposito.domain.exception.DepositoNotFoundException;
import com.tienda.backend.deposito.domain.model.Deposito;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepositoService implements CrearDepositoUseCase, EditarDepositoUseCase, ListarDepositoUseCase,
        ObtenerDepositoUseCase, EliminarDepositoUseCase, CambiarEstadoDepositoUseCase {

    private final DepositoRepositoryPort depositoRepositoryPort;

    @Override
    @Transactional
    public Deposito create(Deposito deposito) {
        if (depositoRepositoryPort.existsByNombre(deposito.getNombre())) {
            throw new DepositoNombreDuplicadoException(deposito.getNombre());
        }
        return depositoRepositoryPort.save(deposito);
    }

    @Override
    @Transactional
    public Deposito editar(Long id, Deposito datos) {
        Deposito existente = buscar(id);

        if (depositoRepositoryPort.existsByNombreAndIdNot(datos.getNombre(), id)) {
            throw new DepositoNombreDuplicadoException(datos.getNombre());
        }

        existente.editar(datos.getNombre(), datos.getDescripcion());
        return depositoRepositoryPort.save(existente);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Deposito> listar(DepositoFiltro filtro, int page, int size) {
        return depositoRepositoryPort.findAll(filtro, PageResult.safePage(page), PageResult.safeSize(size));
    }

    @Override
    @Transactional(readOnly = true)
    public Deposito obtener(Long id) {
        return buscar(id);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        buscar(id);
        depositoRepositoryPort.deleteById(id);
    }

    @Override
    @Transactional
    public Deposito activar(Long id) {
        Deposito deposito = buscar(id);
        deposito.activar();
        return depositoRepositoryPort.save(deposito);
    }

    @Override
    @Transactional
    public Deposito desactivar(Long id) {
        Deposito deposito = buscar(id);
        deposito.desactivar();
        return depositoRepositoryPort.save(deposito);
    }

    private Deposito buscar(Long id) {
        return depositoRepositoryPort.findById(id)
                .orElseThrow(() -> new DepositoNotFoundException(id));
    }
}
