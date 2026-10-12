package com.tienda.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "proveedores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE proveedores SET deleted_at = NOW(), updated_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ProveedorEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_empresa", nullable = false, length = 100)
    private String nombreEmpresa;

    @Column(name = "direccion_empresa", length = 250)
    private String direccionEmpresa;

    @Column(name = "telefono_empresa", length = 20)
    private String telefonoEmpresa;

    @Column(name = "nombre_contacto", length = 100)
    private String nombreContacto;

    @Column(name = "telefono_contacto", length = 20)
    private String telefonoContacto;

    @Column(nullable = false)
    private Boolean estado = true;
}
