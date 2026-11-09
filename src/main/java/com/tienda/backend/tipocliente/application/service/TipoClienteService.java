package com.tienda.backend.tipocliente.application.service;

import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.tipocliente.application.model.TipoClienteFiltro;
import com.tienda.backend.tipocliente.application.port.in.CambiarEstadoTipoClienteUseCase;
import com.tienda.backend.tipocliente.application.port.in.CrearTipoClienteUseCase;
import com.tienda.backend.tipocliente.application.port.in.EditarTipoClienteUseCase;
import com.tienda.backend.tipocliente.application.port.in.EliminarTipoClienteUseCase;
import com.tienda.backend.tipocliente.application.port.in.ListarTipoClienteUseCase;
import com.tienda.backend.tipocliente.application.port.in.ObtenerTipoClienteUseCase;
import com.tienda.backend.tipocliente.application.port.out.TipoClienteRepositoryPort;
import com.tienda.backend.tipocliente.application.port.out.UsoTipoClienteConsultaPort;
import com.tienda.backend.tipocliente.domain.exception.TipoClienteDeSistemaException;
import com.tienda.backend.tipocliente.domain.exception.TipoClienteEnUsoException;
import com.tienda.backend.tipocliente.domain.exception.TipoClienteNombreDuplicadoException;
import com.tienda.backend.tipocliente.domain.exception.TipoClienteNotFoundException;
import com.tienda.backend.tipocliente.domain.model.TipoCliente;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TipoClienteService implements CrearTipoClienteUseCase, EditarTipoClienteUseCase,
        ListarTipoClienteUseCase, ObtenerTipoClienteUseCase, EliminarTipoClienteUseCase,
        CambiarEstadoTipoClienteUseCase {

    private final TipoClienteRepositoryPort tipoClienteRepositoryPort;
    private final UsoTipoClienteConsultaPort usoTipoClienteConsultaPort;

    @Override
    @Transactional
    public TipoCliente create(TipoCliente tipoCliente) {
        if (tipoClienteRepositoryPort.existsByNombre(tipoCliente.getNombre())) {
            throw new TipoClienteNombreDuplicadoException(tipoCliente.getNombre());
        }
        return tipoClienteRepositoryPort.save(tipoCliente);
    }

    @Override
    @Transactional
    public TipoCliente editar(Long id, TipoCliente datos) {
        TipoCliente existente = buscar(id);
        exigirNoDeSistema(existente);
        if (tipoClienteRepositoryPort.existsByNombreAndIdNot(datos.getNombre(), id)) {
            throw new TipoClienteNombreDuplicadoException(datos.getNombre());
        }

        existente.editarNombre(datos.getNombre());
        return tipoClienteRepositoryPort.save(existente);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<TipoCliente> listar(TipoClienteFiltro filtro, int page, int size) {
        return tipoClienteRepositoryPort.findAll(filtro, PageResult.safePage(page), PageResult.safeSize(size));
    }

    @Override
    @Transactional(readOnly = true)
    public TipoCliente obtener(Long id) {
        return buscar(id);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        TipoCliente tipo = buscar(id);
        exigirNoDeSistema(tipo);
        if (usoTipoClienteConsultaPort.estaEnUso(id)) {
            throw new TipoClienteEnUsoException();
        }
        tipoClienteRepositoryPort.deleteById(id);
    }

    @Override
    @Transactional
    public TipoCliente activar(Long id) {
        TipoCliente tipo = buscar(id);
        tipo.activar();
        return tipoClienteRepositoryPort.save(tipo);
    }

    @Override
    @Transactional
    public TipoCliente desactivar(Long id) {
        TipoCliente tipo = buscar(id);
        exigirNoDeSistema(tipo);
        tipo.desactivar();
        return tipoClienteRepositoryPort.save(tipo);
    }

    private TipoCliente buscar(Long id) {
        return tipoClienteRepositoryPort.findById(id).orElseThrow(() -> new TipoClienteNotFoundException(id));
    }

    private void exigirNoDeSistema(TipoCliente tipo) {
        if (tipo.esDeSistema()) {
            throw new TipoClienteDeSistemaException(tipo.getNombre());
        }
    }
}
