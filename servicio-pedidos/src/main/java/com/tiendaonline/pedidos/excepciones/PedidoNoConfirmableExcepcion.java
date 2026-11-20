package com.tiendaonline.pedidos.excepciones;

import com.tiendaonline.pedidos.clases.EstadoPedido;

public class PedidoNoConfirmableExcepcion extends RuntimeException {
    public PedidoNoConfirmableExcepcion(Long id, EstadoPedido estado) {
        super("El pedido " + id + " no se puede confirmar en estado " + estado);
    }
}
