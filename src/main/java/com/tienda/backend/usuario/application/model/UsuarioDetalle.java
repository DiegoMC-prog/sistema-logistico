package com.tienda.backend.usuario.application.model;

import com.tienda.backend.rol.domain.model.Rol;
import com.tienda.backend.usuario.domain.model.Usuario;

import java.util.List;

/** Modelo de lectura: el usuario junto con sus roles. */
public record UsuarioDetalle(Usuario usuario, List<Rol> roles) {
}
