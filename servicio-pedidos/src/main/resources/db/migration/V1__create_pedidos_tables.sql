CREATE TABLE pedidos (
    id                    BIGSERIAL PRIMARY KEY,
    cliente_id            BIGINT NOT NULL,
    estado                VARCHAR(20) NOT NULL CHECK (estado IN ('CREADO', 'CONFIRMADO', 'CANCELADO', 'EXPIRADO')),
    monto_total           NUMERIC(12, 2) NOT NULL DEFAULT 0,
    fecha_creacion        TIMESTAMP NOT NULL DEFAULT now(),
    fecha_actualizacion   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE items_pedido (
    id                BIGSERIAL PRIMARY KEY,
    pedido_id         BIGINT NOT NULL REFERENCES pedidos(id),
    producto_id       BIGINT NOT NULL,
    cantidad          INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario   NUMERIC(12, 2) NOT NULL
);

CREATE INDEX idx_pedidos_cliente_id ON pedidos(cliente_id);
CREATE INDEX idx_items_pedido_pedido_id ON items_pedido(pedido_id);
