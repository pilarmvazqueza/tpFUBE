package com.tiendaonline.pedidos.excepciones;

import com.tiendaonline.pedidos.clases.EstadoPedido;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PedidoNoConfirmableExcepcion extends RuntimeException {
    public PedidoNoConfirmableExcepcion(Long id, EstadoPedido estado) {
        super("El pedido " + id + " no se puede confirmar en estado " + estado);
    }
}
