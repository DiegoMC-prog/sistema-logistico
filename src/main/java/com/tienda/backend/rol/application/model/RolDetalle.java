package com.tienda.backend.rol.application.model;

import com.tienda.backend.permiso.domain.model.Permiso;
import com.tienda.backend.rol.domain.model.Rol;

import java.util.List;

/** Modelo de lectura: el rol junto con sus permisos. */
public record RolDetalle(Rol rol, List<Permiso> permisos) {
}
