package com.tienda.backend.rol.application.service;

import com.tienda.backend.permiso.domain.exception.PermisoNotFoundException;
import com.tienda.backend.permiso.domain.model.Permiso;
import com.tienda.backend.rol.application.model.RolDetalle;
import com.tienda.backend.rol.application.model.RolFiltro;
import com.tienda.backend.rol.application.port.in.AsignarPermisosRolUseCase;
import com.tienda.backend.rol.application.port.in.CrearRolUseCase;
import com.tienda.backend.rol.application.port.in.EditarRolUseCase;
import com.tienda.backend.rol.application.port.in.EliminarRolUseCase;
import com.tienda.backend.rol.application.port.in.ListarRolUseCase;
import com.tienda.backend.rol.application.port.in.ObtenerRolUseCase;
import com.tienda.backend.rol.application.port.out.PermisoConsultaPort;
import com.tienda.backend.rol.application.port.out.RolRepositoryPort;
import com.tienda.backend.rol.application.port.out.UsuarioConsultaPort;
import com.tienda.backend.rol.domain.exception.RolDeSistemaException;
import com.tienda.backend.rol.domain.exception.RolEnUsoException;
import com.tienda.backend.rol.domain.exception.RolNombreDuplicadoException;
import com.tienda.backend.rol.domain.exception.RolNotFoundException;
import com.tienda.backend.rol.domain.model.Rol;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RolService implements CrearRolUseCase, EditarRolUseCase, ListarRolUseCase, ObtenerRolUseCase,
        EliminarRolUseCase, AsignarPermisosRolUseCase {

    private final RolRepositoryPort rolRepositoryPort;
    private final PermisoConsultaPort permisoConsultaPort;
    private final UsuarioConsultaPort usuarioConsultaPort;

    @Override
    @Transactional
    public Rol create(Rol rol) {
        if (rolRepositoryPort.existsByNombre(rol.getNombre())) {
            throw new RolNombreDuplicadoException(rol.getNombre());
        }
        return rolRepositoryPort.save(rol);
    }

    @Override
    @Transactional
    public Rol editar(Long id, Rol datos) {
        Rol existente = buscar(id);
        if (existente.esDeSistema()) {
            throw new RolDeSistemaException(existente.getNombre());
        }
        if (rolRepositoryPort.existsByNombreAndIdNot(datos.getNombre(), id)) {
            throw new RolNombreDuplicadoException(datos.getNombre());
        }

        existente.editarNombre(datos.getNombre());
        return rolRepositoryPort.save(existente);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Rol> listar(RolFiltro filtro, int page, int size) {
        return rolRepositoryPort.findAll(filtro, PageResult.safePage(page), PageResult.safeSize(size));
    }

    @Override
    @Transactional(readOnly = true)
    public RolDetalle obtener(Long id) {
        Rol rol = buscar(id);
        Set<Long> permisoIds = rolRepositoryPort.findPermisoIdsByRolId(id);
        return new RolDetalle(rol, permisoConsultaPort.obtenerPorIds(permisoIds));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Rol rol = buscar(id);
        if (rol.esDeSistema()) {
            throw new RolDeSistemaException(rol.getNombre());
        }
        if (usuarioConsultaPort.existeUsuarioConRol(id)) {
            throw new RolEnUsoException(rol.getNombre());
        }
        rolRepositoryPort.deleteById(id);
    }

    @Override
    @Transactional
    public RolDetalle asignarPermisos(Long rolId, Set<Long> permisoIds) {
        Rol rol = buscar(rolId);
        if (rol.esDeSistema()) {
            throw new RolDeSistemaException(rol.getNombre());
        }

        Set<Long> solicitados = permisoIds == null ? Set.of() : permisoIds;
        List<Permiso> permisos = permisoConsultaPort.obtenerPorIds(solicitados);

        if (permisos.size() != solicitados.size()) {
            Set<Long> faltantes = new HashSet<>(solicitados);
            permisos.forEach(p -> faltantes.remove(p.getId()));
            throw new PermisoNotFoundException(faltantes);
        }

        rolRepositoryPort.reemplazarPermisos(rolId, solicitados);
        return new RolDetalle(rol, permisos);
    }

    private Rol buscar(Long id) {
        return rolRepositoryPort.findById(id).orElseThrow(() -> new RolNotFoundException(id));
    }
}
