package com.tiendaonline.inventario.servicios;

import com.tiendaonline.clientes.dto.PedidoDTO;

public interface PedidoServicio {
    PedidoDTO buscarPorId (Long Id);
}
