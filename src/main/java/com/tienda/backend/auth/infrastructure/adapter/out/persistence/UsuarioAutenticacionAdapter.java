package com.tienda.backend.auth.infrastructure.adapter.out.persistence;

import com.tienda.backend.auth.application.model.CredencialesUsuario;
import com.tienda.backend.auth.application.model.DatosAcceso;
import com.tienda.backend.auth.application.port.out.UsuarioAutenticacionPort;
import com.tienda.backend.permiso.application.port.out.PermisoRepositoryPort;
import com.tienda.backend.permiso.domain.model.Permiso;
import com.tienda.backend.rol.application.port.out.RolRepositoryPort;
import com.tienda.backend.rol.domain.model.Rol;
import com.tienda.backend.usuario.application.port.out.UsuarioRepositoryPort;
import com.tienda.backend.usuario.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Lee usuarios, roles y permisos a través de los puertos de sus módulos, sin tocar sus entidades JPA. */
@Component
@RequiredArgsConstructor
public class UsuarioAutenticacionAdapter implements UsuarioAutenticacionPort {
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final RolRepositoryPort rolRepositoryPort;
    private final PermisoRepositoryPort permisoRepositoryPort;

    @Override
    public Optional<CredencialesUsuario> buscarCredencialesPorUsername(String username) {
        return usuarioRepositoryPort.findByUsername(username).map(this::credenciales);
    }

    @Override
    public Optional<CredencialesUsuario> buscarCredencialesPorId(Long usuarioId) {
        return usuarioRepositoryPort.findById(usuarioId).map(this::credenciales);
    }

    @Override
    public Optional<DatosAcceso> cargarDatosAcceso(Long usuarioId) {
        return usuarioRepositoryPort.findById(usuarioId).map(usuario -> {
            Set<Long> rolIds = usuarioRepositoryPort.findRolIdsByUsuarioId(usuarioId);
            List<Rol> roles = rolIds.isEmpty() ? List.of() : rolRepositoryPort.findAllById(rolIds);

            Set<Long> permisoIds = new HashSet<>();
            roles.forEach(rol -> permisoIds.addAll(rolRepositoryPort.findPermisoIdsByRolId(rol.getId())));
            List<Permiso> permisos = permisoIds.isEmpty() ? List.of() : permisoRepositoryPort.findAllById(permisoIds);

            return new DatosAcceso(
                    usuario.getId(),
                    usuario.getUsername(),
                    usuario.getNombre(),
                    usuario.getEmail(),
                    usuario.estaActivo(),
                    usuario.isPasswordTemporal(),
                    roles.stream().map(Rol::getNombre).sorted().toList(),
                    permisos.stream().map(Permiso::getNombre).sorted(Comparator.naturalOrder()).toList());
        });
    }

    @Override
    public void guardarPassword(Long usuarioId, String passwordHash) {
        usuarioRepositoryPort.findById(usuarioId).ifPresent(usuario -> {
            usuario.cambiarPassword(passwordHash);
            usuarioRepositoryPort.save(usuario);
        });
    }

    @Override
    public List<Long> usuarioIdsPorRol(Long rolId) {
        return usuarioRepositoryPort.findUsuarioIdsByRolId(rolId);
    }

    private CredencialesUsuario credenciales(Usuario usuario) {
        return new CredencialesUsuario(usuario.getId(), usuario.getPasswordHash(), usuario.estaActivo());
    }
}
