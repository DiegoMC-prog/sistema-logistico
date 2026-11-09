package com.tienda.backend.metodopago.application.service;

import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.metodopago.application.model.MetodoPagoFiltro;
import com.tienda.backend.metodopago.application.port.in.CambiarEstadoMetodoPagoUseCase;
import com.tienda.backend.metodopago.application.port.in.CrearMetodoPagoUseCase;
import com.tienda.backend.metodopago.application.port.in.EditarMetodoPagoUseCase;
import com.tienda.backend.metodopago.application.port.in.EliminarMetodoPagoUseCase;
import com.tienda.backend.metodopago.application.port.in.ListarMetodoPagoUseCase;
import com.tienda.backend.metodopago.application.port.in.ObtenerMetodoPagoUseCase;
import com.tienda.backend.metodopago.application.port.out.MetodoPagoRepositoryPort;
import com.tienda.backend.metodopago.application.port.out.UsoMetodoPagoConsultaPort;
import com.tienda.backend.metodopago.domain.exception.MetodoPagoDeSistemaException;
import com.tienda.backend.metodopago.domain.exception.MetodoPagoEnUsoException;
import com.tienda.backend.metodopago.domain.exception.MetodoPagoNombreDuplicadoException;
import com.tienda.backend.metodopago.domain.exception.MetodoPagoNotFoundException;
import com.tienda.backend.metodopago.domain.model.MetodoPago;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MetodoPagoService implements CrearMetodoPagoUseCase, EditarMetodoPagoUseCase,
        ListarMetodoPagoUseCase, ObtenerMetodoPagoUseCase, EliminarMetodoPagoUseCase,
        CambiarEstadoMetodoPagoUseCase {

    private final MetodoPagoRepositoryPort metodoPagoRepositoryPort;
    private final UsoMetodoPagoConsultaPort usoMetodoPagoConsultaPort;

    @Override
    @Transactional
    public MetodoPago create(MetodoPago metodoPago) {
        if (metodoPagoRepositoryPort.existsByNombre(metodoPago.getNombre())) {
            throw new MetodoPagoNombreDuplicadoException(metodoPago.getNombre());
        }
        return metodoPagoRepositoryPort.save(metodoPago);
    }

    @Override
    @Transactional
    public MetodoPago editar(Long id, MetodoPago datos) {
        MetodoPago existente = buscar(id);
        exigirNoDeSistema(existente);
        if (metodoPagoRepositoryPort.existsByNombreAndIdNot(datos.getNombre(), id)) {
            throw new MetodoPagoNombreDuplicadoException(datos.getNombre());
        }

        existente.editarNombre(datos.getNombre());
        return metodoPagoRepositoryPort.save(existente);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<MetodoPago> listar(MetodoPagoFiltro filtro, int page, int size) {
        return metodoPagoRepositoryPort.findAll(filtro, PageResult.safePage(page), PageResult.safeSize(size));
    }

    @Override
    @Transactional(readOnly = true)
    public MetodoPago obtener(Long id) {
        return buscar(id);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        MetodoPago tipo = buscar(id);
        exigirNoDeSistema(tipo);
        if (usoMetodoPagoConsultaPort.estaEnUso(id)) {
            throw new MetodoPagoEnUsoException();
        }
        metodoPagoRepositoryPort.deleteById(id);
    }

    @Override
    @Transactional
    public MetodoPago activar(Long id) {
        MetodoPago tipo = buscar(id);
        tipo.activar();
        return metodoPagoRepositoryPort.save(tipo);
    }

    @Override
    @Transactional
    public MetodoPago desactivar(Long id) {
        MetodoPago tipo = buscar(id);
        exigirNoDeSistema(tipo);
        tipo.desactivar();
        return metodoPagoRepositoryPort.save(tipo);
    }

    private MetodoPago buscar(Long id) {
        return metodoPagoRepositoryPort.findById(id).orElseThrow(() -> new MetodoPagoNotFoundException(id));
    }

    private void exigirNoDeSistema(MetodoPago tipo) {
        if (tipo.esDeSistema()) {
            throw new MetodoPagoDeSistemaException(tipo.getNombre());
        }
    }
}
