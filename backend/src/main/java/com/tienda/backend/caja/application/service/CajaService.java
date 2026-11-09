package com.tienda.backend.caja.application.service;

import com.tienda.backend.caja.application.model.CajaFiltro;
import com.tienda.backend.caja.application.model.DepositoRef;
import com.tienda.backend.caja.application.port.in.CambiarEstadoCajaUseCase;
import com.tienda.backend.caja.application.port.in.CrearCajaUseCase;
import com.tienda.backend.caja.application.port.in.EditarCajaUseCase;
import com.tienda.backend.caja.application.port.in.EliminarCajaUseCase;
import com.tienda.backend.caja.application.port.in.ListarCajaUseCase;
import com.tienda.backend.caja.application.port.in.ObtenerCajaUseCase;
import com.tienda.backend.caja.application.port.out.CajaDepositoConsultaPort;
import com.tienda.backend.caja.application.port.out.CajaRepositoryPort;
import com.tienda.backend.caja.application.port.out.TurnoRepositoryPort;
import com.tienda.backend.caja.domain.exception.CajaConTurnoAbiertoException;
import com.tienda.backend.caja.domain.exception.CajaConTurnosException;
import com.tienda.backend.caja.domain.exception.CajaDepositoInactivoException;
import com.tienda.backend.caja.domain.exception.CajaNombreDuplicadoException;
import com.tienda.backend.caja.domain.exception.CajaNotFoundException;
import com.tienda.backend.caja.domain.model.Caja;
import com.tienda.backend.deposito.domain.exception.DepositoNotFoundException;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CajaService implements CrearCajaUseCase, EditarCajaUseCase, ListarCajaUseCase, ObtenerCajaUseCase,
        EliminarCajaUseCase, CambiarEstadoCajaUseCase {

    private final CajaRepositoryPort cajaRepositoryPort;
    private final TurnoRepositoryPort turnoRepositoryPort;
    private final CajaDepositoConsultaPort depositoConsultaPort;

    @Override
    @Transactional
    public Caja create(Caja caja) {
        exigirDepositoActivo(caja.getDepositoId());
        if (cajaRepositoryPort.existsByNombre(caja.getNombre())) {
            throw new CajaNombreDuplicadoException(caja.getNombre());
        }
        return cajaRepositoryPort.save(caja);
    }

    @Override
    @Transactional
    public Caja editar(Long id, Caja datos) {
        Caja existente = buscar(id);
        boolean cambiaDeposito = !existente.getDepositoId().equals(datos.getDepositoId());
        if (cambiaDeposito) {
            // el stock de las ventas del turno abierto sale del depósito de la caja: no puede cambiar a mitad de turno
            if (turnoRepositoryPort.existsAbiertoByCajaId(id)) {
                throw new CajaConTurnoAbiertoException();
            }
            exigirDepositoActivo(datos.getDepositoId());
        }
        if (cajaRepositoryPort.existsByNombreAndIdNot(datos.getNombre(), id)) {
            throw new CajaNombreDuplicadoException(datos.getNombre());
        }

        existente.editar(datos.getNombre(), datos.getDepositoId());
        return cajaRepositoryPort.save(existente);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Caja> listar(CajaFiltro filtro, int page, int size) {
        return cajaRepositoryPort.findAll(filtro, PageResult.safePage(page), PageResult.safeSize(size));
    }

    @Override
    @Transactional(readOnly = true)
    public Caja obtener(Long id) {
        return buscar(id);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        buscar(id);
        if (turnoRepositoryPort.existsAbiertoByCajaId(id)) {
            throw new CajaConTurnoAbiertoException();
        }
        if (turnoRepositoryPort.existsByCajaId(id)) {
            throw new CajaConTurnosException();
        }
        cajaRepositoryPort.deleteById(id);
    }

    @Override
    @Transactional
    public Caja activar(Long id) {
        Caja caja = buscar(id);
        exigirDepositoActivo(caja.getDepositoId());
        caja.activar();
        return cajaRepositoryPort.save(caja);
    }

    @Override
    @Transactional
    public Caja desactivar(Long id) {
        Caja caja = buscar(id);
        if (turnoRepositoryPort.existsAbiertoByCajaId(id)) {
            throw new CajaConTurnoAbiertoException();
        }
        caja.desactivar();
        return cajaRepositoryPort.save(caja);
    }

    private Caja buscar(Long id) {
        return cajaRepositoryPort.findById(id).orElseThrow(() -> new CajaNotFoundException(id));
    }

    private void exigirDepositoActivo(Long depositoId) {
        DepositoRef deposito = depositoConsultaPort.buscarPorId(depositoId)
                .orElseThrow(() -> new DepositoNotFoundException(depositoId));
        if (!deposito.activo()) {
            throw new CajaDepositoInactivoException(deposito.nombre());
        }
    }
}
