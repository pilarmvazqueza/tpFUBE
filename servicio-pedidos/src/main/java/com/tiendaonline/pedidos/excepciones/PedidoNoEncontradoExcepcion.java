package com.tiendaonline.pedidos.excepciones;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PedidoNoEncontradoExcepcion extends RuntimeException {
    public PedidoNoEncontradoExcepcion(Long Id) {
        super("Pedido no encontrado con id: " + Id);
    }
}
