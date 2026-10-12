CREATE TABLE proveedores(
    id BIGSERIAL PRIMARY KEY,
    nombre_empresa VARCHAR(100) NOT NULL,
    direccion_empresa VARCHAR(250),
    telefono_empresa VARCHAR(20),
    nombre_contacto VARCHAR(100),
    telefono_contacto VARCHAR(20),
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);