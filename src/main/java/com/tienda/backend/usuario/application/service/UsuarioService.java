package com.tienda.backend.usuario.application.service;

import com.tienda.backend.rol.domain.exception.RolNotFoundException;
import com.tienda.backend.rol.domain.model.Rol;
import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.shared.application.port.out.EnviadorCorreoPort;
import com.tienda.backend.usuario.application.mail.CorreoPasswordTemporal;
import com.tienda.backend.usuario.application.model.CrearUsuarioComando;
import com.tienda.backend.usuario.application.model.EditarUsuarioComando;
import com.tienda.backend.usuario.application.model.UsuarioDetalle;
import com.tienda.backend.usuario.application.model.UsuarioFiltro;
import com.tienda.backend.usuario.application.port.in.AsignarRolesUsuarioUseCase;
import com.tienda.backend.usuario.application.port.in.CambiarEstadoUsuarioUseCase;
import com.tienda.backend.usuario.application.port.in.CrearUsuarioUseCase;
import com.tienda.backend.usuario.application.port.in.EditarUsuarioUseCase;
import com.tienda.backend.usuario.application.port.in.EliminarUsuarioUseCase;
import com.tienda.backend.usuario.application.port.in.ListarUsuarioUseCase;
import com.tienda.backend.usuario.application.port.in.ObtenerUsuarioUseCase;
import com.tienda.backend.usuario.application.port.in.RestablecerPasswordUsuarioUseCase;
import com.tienda.backend.usuario.application.port.out.PasswordGeneratorPort;
import com.tienda.backend.usuario.application.port.out.PasswordHasherPort;
import com.tienda.backend.usuario.application.port.out.RolConsultaPort;
import com.tienda.backend.usuario.application.port.out.UsuarioRepositoryPort;
import com.tienda.backend.usuario.domain.exception.UltimoAdministradorException;
import com.tienda.backend.usuario.domain.exception.UsuarioEmailDuplicadoException;
import com.tienda.backend.usuario.domain.exception.UsuarioNotFoundException;
import com.tienda.backend.usuario.domain.exception.UsuarioUsernameDuplicadoException;
import com.tienda.backend.usuario.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService implements CrearUsuarioUseCase, EditarUsuarioUseCase, ListarUsuarioUseCase,
        ObtenerUsuarioUseCase, EliminarUsuarioUseCase, CambiarEstadoUsuarioUseCase,
        RestablecerPasswordUsuarioUseCase, AsignarRolesUsuarioUseCase {

    /** Mientras no exista login no hay usuario autenticado que registrar. */
    private static final String ASIGNADO_POR_SISTEMA = "SISTEMA";

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final RolConsultaPort rolConsultaPort;
    private final PasswordHasherPort passwordHasherPort;
    private final PasswordGeneratorPort passwordGeneratorPort;
    private final EnviadorCorreoPort enviadorCorreoPort;

    /**
     * La contraseña no la elige quien crea el usuario: se genera, se guarda hasheada y se envía por correo.
     * Si el correo falla, la transacción se revierte y el usuario no se crea.
     */
    @Override
    @Transactional
    public UsuarioDetalle create(CrearUsuarioComando comando) {
        String passwordTemporal = passwordGeneratorPort.generar();
        Usuario usuario = new Usuario(comando.username(), comando.nombre(), comando.email(),
                passwordHasherPort.encode(passwordTemporal));

        if (usuarioRepositoryPort.existsByUsername(usuario.getUsername())) {
            throw new UsuarioUsernameDuplicadoException(usuario.getUsername());
        }
        if (usuarioRepositoryPort.existsByEmail(usuario.getEmail())) {
            throw new UsuarioEmailDuplicadoException(usuario.getEmail());
        }

        Usuario guardado = usuarioRepositoryPort.save(usuario);
        enviadorCorreoPort.enviar(CorreoPasswordTemporal.crear(
                guardado.getEmail(), guardado.getNombre(), guardado.getUsername(), passwordTemporal));

        return new UsuarioDetalle(guardado, List.of());
    }

    @Override
    @Transactional
    public UsuarioDetalle editar(Long id, EditarUsuarioComando comando) {
        Usuario usuario = buscar(id);
        usuario.editar(comando.nombre(), comando.email());

        if (usuarioRepositoryPort.existsByEmailAndIdNot(usuario.getEmail(), id)) {
            throw new UsuarioEmailDuplicadoException(usuario.getEmail());
        }

        return detalle(usuarioRepositoryPort.save(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<UsuarioDetalle> listar(UsuarioFiltro filtro, int page, int size) {
        PageResult<Usuario> pagina = usuarioRepositoryPort.findAll(
                filtro, PageResult.safePage(page), PageResult.safeSize(size));

        List<Long> usuarioIds = pagina.content().stream().map(Usuario::getId).toList();
        if (usuarioIds.isEmpty()) {
            return pagina.map(u -> new UsuarioDetalle(u, List.of()));
        }

        // Dos consultas para toda la página (roles por usuario y los roles en sí), sin una por usuario.
        Map<Long, Set<Long>> rolIdsPorUsuario = usuarioRepositoryPort.findRolIdsByUsuarioIds(usuarioIds);
        Set<Long> todosLosRolIds = rolIdsPorUsuario.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        Map<Long, Rol> rolesPorId = rolConsultaPort.obtenerPorIds(todosLosRolIds).stream()
                .collect(Collectors.toMap(Rol::getId, Function.identity()));

        return pagina.map(u -> new UsuarioDetalle(u,
                rolIdsPorUsuario.getOrDefault(u.getId(), Set.of()).stream()
                        .map(rolesPorId::get)
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparing(Rol::getId))
                        .toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDetalle obtener(Long id) {
        return detalle(buscar(id));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = buscar(id);
        protegerUltimoAdministrador(usuario);
        usuarioRepositoryPort.deleteById(id);
    }

    @Override
    @Transactional
    public UsuarioDetalle activar(Long id) {
        Usuario usuario = buscar(id);
        usuario.activar();
        return detalle(usuarioRepositoryPort.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioDetalle desactivar(Long id) {
        Usuario usuario = buscar(id);
        protegerUltimoAdministrador(usuario);
        usuario.desactivar();
        return detalle(usuarioRepositoryPort.save(usuario));
    }

    /** Si el correo falla, la transacción se revierte y la contraseña anterior sigue vigente. */
    @Override
    @Transactional
    public void restablecerPassword(Long id) {
        Usuario usuario = buscar(id);

        String passwordTemporal = passwordGeneratorPort.generar();
        usuario.asignarPasswordTemporal(passwordHasherPort.encode(passwordTemporal));
        usuarioRepositoryPort.save(usuario);

        enviadorCorreoPort.enviar(CorreoPasswordTemporal.crear(
                usuario.getEmail(), usuario.getNombre(), usuario.getUsername(), passwordTemporal));
    }

    @Override
    @Transactional
    public UsuarioDetalle asignarRoles(Long usuarioId, Set<Long> rolIds) {
        Usuario usuario = buscar(usuarioId);

        Set<Long> solicitados = rolIds == null ? Set.of() : rolIds;
        List<Rol> roles = solicitados.isEmpty() ? List.of() : rolConsultaPort.obtenerPorIds(solicitados);
        if (roles.size() != solicitados.size()) {
            Set<Long> faltantes = new HashSet<>(solicitados);
            roles.forEach(r -> faltantes.remove(r.getId()));
            throw new RolNotFoundException(faltantes);
        }

        // Quitar el rol ADMIN al último administrador activo dejaría el sistema sin administradores.
        boolean conservaAdmin = roles.stream().anyMatch(Rol::esDeSistema);
        if (!conservaAdmin && usuario.estaActivo() && usuarioRepositoryPort.tieneRolAdministrador(usuarioId)) {
            protegerUltimoAdministrador(usuario);
        }

        usuarioRepositoryPort.reemplazarRoles(usuarioId, solicitados, ASIGNADO_POR_SISTEMA);
        return new UsuarioDetalle(usuario, roles.stream().sorted(Comparator.comparing(Rol::getId)).toList());
    }

    private Usuario buscar(Long id) {
        return usuarioRepositoryPort.findById(id).orElseThrow(() -> new UsuarioNotFoundException(id));
    }

    private UsuarioDetalle detalle(Usuario usuario) {
        Set<Long> rolIds = usuarioRepositoryPort.findRolIdsByUsuarioId(usuario.getId());
        List<Rol> roles = rolIds.isEmpty() ? List.of() : rolConsultaPort.obtenerPorIds(rolIds);
        return new UsuarioDetalle(usuario, roles.stream().sorted(Comparator.comparing(Rol::getId)).toList());
    }

    private void protegerUltimoAdministrador(Usuario usuario) {
        if (usuario.estaActivo()
                && usuarioRepositoryPort.tieneRolAdministrador(usuario.getId())
                && usuarioRepositoryPort.contarAdministradoresActivos() <= 1) {
            throw new UltimoAdministradorException();
        }
    }
}
