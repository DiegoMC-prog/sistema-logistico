package com.tienda.backend.cliente.infrastructure.adapter.out.persistence;

import com.tienda.backend.cliente.application.model.ClienteDetalle;
import com.tienda.backend.cliente.application.model.ClienteFiltro;
import com.tienda.backend.cliente.application.port.out.ClienteRepositoryPort;
import com.tienda.backend.cliente.domain.model.Cliente;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ClienteRepositoryAdapter implements ClienteRepositoryPort {
    private final ClienteJpaRepository clienteJpaRepository;
    private final ClientePersistenceMapper mapper;

    @Override
    public Cliente save(Cliente cliente) {
        return mapper.toDomain(clienteJpaRepository.save(mapper.toEntity(cliente)));
    }

    @Override
    public PageResult<ClienteDetalle> findAll(ClienteFiltro filtro, int page, int size) {
        Page<ClienteEntity> result = clienteJpaRepository.findAll(
                ClienteSpecifications.desde(filtro), PageRequest.of(page, size, Sort.by("id")));
        return new PageResult<>(
                result.getContent().stream()
                        .map(e -> new ClienteDetalle(mapper.toDomain(e), e.getTipoCliente().getNombre()))
                        .toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public Optional<Cliente> findById(Long id) {
        return clienteJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        clienteJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByDocumento(String documento) {
        return clienteJpaRepository.existsByDocumento(documento);
    }

    @Override
    public boolean existsByDocumentoAndIdNot(String documento, Long id) {
        return clienteJpaRepository.existsByDocumentoAndIdNot(documento, id);
    }
}
