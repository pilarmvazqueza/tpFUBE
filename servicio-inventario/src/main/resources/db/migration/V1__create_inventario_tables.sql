CREATE TABLE productos (
    id                   BIGSERIAL PRIMARY KEY,
    nombre               VARCHAR(255) NOT NULL,
    cantidad_disponible  INTEGER NOT NULL DEFAULT 0 CHECK (cantidad_disponible >= 0)
);

CREATE TABLE reservas (
    id               BIGSERIAL PRIMARY KEY,
    producto_id      BIGINT NOT NULL REFERENCES productos(id),
    pedido_id        BIGINT NOT NULL,
    cantidad         INTEGER NOT NULL CHECK (cantidad > 0),
    estado           VARCHAR(20) NOT NULL CHECK (estado IN ('ACTIVA', 'LIBERADA')),
    fecha_creacion   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_reservas_pedido_id ON reservas(pedido_id);
CREATE INDEX idx_reservas_producto_id ON reservas(producto_id);
