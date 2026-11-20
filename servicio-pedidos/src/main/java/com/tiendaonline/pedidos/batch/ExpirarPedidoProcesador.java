package com.tiendaonline.pedidos.batch;

import com.tiendaonline.pedidos.clases.EstadoPedido;
import com.tiendaonline.pedidos.clases.Pedido;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ExpirarPedidoProcesador implements ItemProcessor<Pedido, Pedido> {

    @Override
    public Pedido process(Pedido pedido) {
        pedido.setEstado(EstadoPedido.EXPIRADO);
        pedido.setFechaActualizacion(LocalDateTime.now());
        return pedido;
    }
}
