package com.tienda.backend.cliente.application.port.out;

import com.tienda.backend.cliente.application.model.ClienteDetalle;
import com.tienda.backend.cliente.application.model.ClienteFiltro;
import com.tienda.backend.cliente.domain.model.Cliente;
import com.tienda.backend.shared.application.model.PageResult;

import java.util.Optional;

public interface ClienteRepositoryPort {
    Cliente save(Cliente cliente);
    PageResult<ClienteDetalle> findAll(ClienteFiltro filtro, int page, int size);
    Optional<Cliente> findById(Long id);
    void deleteById(Long id);
    boolean existsByDocumento(String documento);
    boolean existsByDocumentoAndIdNot(String documento, Long id);
}
