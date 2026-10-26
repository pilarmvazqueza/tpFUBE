package com.tiendaonline.clientes.excepciones;

public class PedidoNoEncontradoExcepcion extends RuntimeException {
    public PedidoNoEncontradoExcepcion(Long Id) {
        super("Pedido no encontrado con id: " + Id);
    }
}
