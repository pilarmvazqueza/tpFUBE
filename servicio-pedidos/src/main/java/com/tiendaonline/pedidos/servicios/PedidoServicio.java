package com.tiendaonline.pedidos.servicios;

import com.tiendaonline.pedidos.dto.PedidoDTO;

import java.util.List;

public interface PedidoServicio {

    PedidoDTO buscarPorId(Long id);
    PedidoDTO crearPedido(PedidoDTO pedidoDTO);
    List<PedidoDTO> listarPorCliente(Long clienteId);
    PedidoDTO cancelar(Long id);

}
