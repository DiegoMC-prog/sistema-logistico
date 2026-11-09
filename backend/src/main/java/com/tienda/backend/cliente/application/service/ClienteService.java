package com.tienda.backend.cliente.application.service;

import com.tienda.backend.cliente.application.model.ClienteDetalle;
import com.tienda.backend.cliente.application.model.ClienteFiltro;
import com.tienda.backend.cliente.application.model.TipoClienteRef;
import com.tienda.backend.cliente.application.port.in.CambiarEstadoClienteUseCase;
import com.tienda.backend.cliente.application.port.in.CrearClienteUseCase;
import com.tienda.backend.cliente.application.port.in.EditarClienteUseCase;
import com.tienda.backend.cliente.application.port.in.EliminarClienteUseCase;
import com.tienda.backend.cliente.application.port.in.ListarClienteUseCase;
import com.tienda.backend.cliente.application.port.in.ObtenerClienteUseCase;
import com.tienda.backend.cliente.application.port.out.ClienteRepositoryPort;
import com.tienda.backend.cliente.application.port.out.TipoClienteConsultaPort;
import com.tienda.backend.cliente.application.port.out.VentaConsultaPort;
import com.tienda.backend.cliente.domain.exception.ClienteConVentasException;
import com.tienda.backend.cliente.domain.exception.ClienteDocumentoDuplicadoException;
import com.tienda.backend.cliente.domain.exception.ClienteNotFoundException;
import com.tienda.backend.cliente.domain.exception.TipoClienteInactivoException;
import com.tienda.backend.cliente.domain.model.Cliente;
import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.tipocliente.domain.exception.TipoClienteNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteService implements CrearClienteUseCase, EditarClienteUseCase, ListarClienteUseCase,
        ObtenerClienteUseCase, EliminarClienteUseCase, CambiarEstadoClienteUseCase {

    private final ClienteRepositoryPort clienteRepositoryPort;
    private final TipoClienteConsultaPort tipoClienteConsultaPort;
    private final VentaConsultaPort ventaConsultaPort;

    @Override
    @Transactional
    public ClienteDetalle create(Cliente cliente) {
        TipoClienteRef tipo = resolverTipo(cliente.getTipoClienteId());
        cliente.asignarTipo(tipo.id());
        if (cliente.getDocumento() != null && clienteRepositoryPort.existsByDocumento(cliente.getDocumento())) {
            throw new ClienteDocumentoDuplicadoException(cliente.getDocumento());
        }
        return new ClienteDetalle(clienteRepositoryPort.save(cliente), tipo.nombre());
    }

    @Override
    @Transactional
    public ClienteDetalle editar(Long id, Cliente datos) {
        Cliente existente = buscar(id);
        TipoClienteRef tipo = resolverTipoParaEdicion(datos.getTipoClienteId(), existente);
        if (datos.getDocumento() != null
                && clienteRepositoryPort.existsByDocumentoAndIdNot(datos.getDocumento(), id)) {
            throw new ClienteDocumentoDuplicadoException(datos.getDocumento());
        }

        existente.editar(tipo.id(), datos.getDocumento(), datos.getNombre(), datos.getTelefono(), datos.getEmail(),
                datos.isCreditoHabilitado(), datos.getLimiteCredito());
        return new ClienteDetalle(clienteRepositoryPort.save(existente), tipo.nombre());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClienteDetalle> listar(ClienteFiltro filtro, int page, int size) {
        return clienteRepositoryPort.findAll(filtro, PageResult.safePage(page), PageResult.safeSize(size));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteDetalle obtener(Long id) {
        return detalle(buscar(id));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        buscar(id);
        if (ventaConsultaPort.clienteTieneVentas(id)) {
            throw new ClienteConVentasException();
        }
        clienteRepositoryPort.deleteById(id);
    }

    @Override
    @Transactional
    public ClienteDetalle activar(Long id) {
        Cliente cliente = buscar(id);
        cliente.activar();
        return detalle(clienteRepositoryPort.save(cliente));
    }

    @Override
    @Transactional
    public ClienteDetalle desactivar(Long id) {
        Cliente cliente = buscar(id);
        cliente.desactivar();
        return detalle(clienteRepositoryPort.save(cliente));
    }

    private Cliente buscar(Long id) {
        return clienteRepositoryPort.findById(id).orElseThrow(() -> new ClienteNotFoundException(id));
    }

    private ClienteDetalle detalle(Cliente cliente) {
        TipoClienteRef tipo = tipoClienteConsultaPort.buscarPorId(cliente.getTipoClienteId())
                .orElseThrow(() -> new TipoClienteNotFoundException(cliente.getTipoClienteId()));
        return new ClienteDetalle(cliente, tipo.nombre());
    }

    /** Sin tipo en la petición se usa GENERAL; con tipo, debe existir y estar activo. */
    private TipoClienteRef resolverTipo(Long tipoClienteId) {
        if (tipoClienteId == null) {
            return tipoClienteConsultaPort.obtenerGeneral();
        }
        TipoClienteRef tipo = tipoClienteConsultaPort.buscarPorId(tipoClienteId)
                .orElseThrow(() -> new TipoClienteNotFoundException(tipoClienteId));
        if (!tipo.activo()) {
            throw new TipoClienteInactivoException(tipo.nombre());
        }
        return tipo;
    }

    /** Al editar, conservar el tipo actual (aunque esté inactivo) es válido; solo se exige activo si cambia. */
    private TipoClienteRef resolverTipoParaEdicion(Long tipoClienteId, Cliente existente) {
        if (tipoClienteId != null && tipoClienteId.equals(existente.getTipoClienteId())) {
            return tipoClienteConsultaPort.buscarPorId(tipoClienteId)
                    .orElseThrow(() -> new TipoClienteNotFoundException(tipoClienteId));
        }
        return resolverTipo(tipoClienteId);
    }
}
