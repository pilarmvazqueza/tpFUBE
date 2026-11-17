package com.tiendaonline.pedidos.servicios;

import com.tiendaonline.pedidos.dto.PedidoDTO;

public interface PedidoServicio {

    PedidoDTO buscarPorId(Long id);
    PedidoDTO crearPedido(PedidoDTO pedidoDTO);

}
