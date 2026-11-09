-- Enlaces de un solo uso para restablecer la contrasena ("olvide mi contrasena").
-- Solo se guarda el hash (SHA-256) del token: el valor en claro viaja unicamente en el enlace del correo.
CREATE TABLE tokens_restablecimiento(
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    token_hash VARCHAR(64) NOT NULL,
    expira_at TIMESTAMP NOT NULL,
    -- se llena al usar el enlace o al pedir uno nuevo (que invalida los anteriores)
    usado_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip VARCHAR(45)
);

CREATE UNIQUE INDEX uk_tokens_restablecimiento_hash ON tokens_restablecimiento(token_hash);
-- limite de solicitudes por cuenta: cuantos enlaces se pidieron en la ultima hora
CREATE INDEX idx_tokens_restablecimiento_usuario_fecha ON tokens_restablecimiento(usuario_id, created_at);
