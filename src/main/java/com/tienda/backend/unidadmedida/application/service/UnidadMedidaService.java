package com.tienda.backend.unidadmedida.application.service;

import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.unidadmedida.application.model.UnidadMedidaFiltro;
import com.tienda.backend.unidadmedida.application.port.in.CrearUnidadMedidaUseCase;
import com.tienda.backend.unidadmedida.application.port.in.EditarUnidadMedidaUseCase;
import com.tienda.backend.unidadmedida.application.port.in.EliminarUnidadMedidaUseCase;
import com.tienda.backend.unidadmedida.application.port.in.ListarUnidadMedidaUseCase;
import com.tienda.backend.unidadmedida.application.port.in.ObtenerUnidadMedidaUseCase;
import com.tienda.backend.unidadmedida.application.port.out.UnidadMedidaRepositoryPort;
import com.tienda.backend.unidadmedida.domain.exception.UnidadMedidaAbreviaturaDuplicadaException;
import com.tienda.backend.unidadmedida.domain.exception.UnidadMedidaNombreDuplicadoException;
import com.tienda.backend.unidadmedida.domain.exception.UnidadMedidaNotFoundException;
import com.tienda.backend.unidadmedida.domain.model.UnidadMedida;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UnidadMedidaService implements CrearUnidadMedidaUseCase, EditarUnidadMedidaUseCase,
        ListarUnidadMedidaUseCase, ObtenerUnidadMedidaUseCase, EliminarUnidadMedidaUseCase {

    private final UnidadMedidaRepositoryPort unidadMedidaRepositoryPort;

    @Override
    @Transactional
    public UnidadMedida create(UnidadMedida unidadMedida) {
        if (unidadMedidaRepositoryPort.existsByNombre(unidadMedida.getNombre())) {
            throw new UnidadMedidaNombreDuplicadoException(unidadMedida.getNombre());
        }
        if (unidadMedidaRepositoryPort.existsByAbreviatura(unidadMedida.getAbreviatura())) {
            throw new UnidadMedidaAbreviaturaDuplicadaException(unidadMedida.getAbreviatura());
        }
        return unidadMedidaRepositoryPort.save(unidadMedida);
    }

    @Override
    @Transactional
    public UnidadMedida editar(Long id, UnidadMedida datos) {
        UnidadMedida existente = buscar(id);

        if (unidadMedidaRepositoryPort.existsByNombreAndIdNot(datos.getNombre(), id)) {
            throw new UnidadMedidaNombreDuplicadoException(datos.getNombre());
        }
        if (unidadMedidaRepositoryPort.existsByAbreviaturaAndIdNot(datos.getAbreviatura(), id)) {
            throw new UnidadMedidaAbreviaturaDuplicadaException(datos.getAbreviatura());
        }

        existente.editar(datos.getNombre(), datos.getAbreviatura());
        return unidadMedidaRepositoryPort.save(existente);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<UnidadMedida> listar(UnidadMedidaFiltro filtro, int page, int size) {
        return unidadMedidaRepositoryPort.findAll(filtro, PageResult.safePage(page), PageResult.safeSize(size));
    }

    @Override
    @Transactional(readOnly = true)
    public UnidadMedida obtener(Long id) {
        return buscar(id);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        buscar(id);
        unidadMedidaRepositoryPort.deleteById(id);
    }

    private UnidadMedida buscar(Long id) {
        return unidadMedidaRepositoryPort.findById(id)
                .orElseThrow(() -> new UnidadMedidaNotFoundException(id));
    }
}
