package com.tiendaonline.pedidos.mapper;

import com.tiendaonline.pedidos.clases.ItemPedido;
import com.tiendaonline.pedidos.clases.Pedido;
import com.tiendaonline.pedidos.dto.ItemPedidoDTO;
import com.tiendaonline.pedidos.dto.PedidoDTO;

public class PedidoMapper {

    public static PedidoDTO toDTO(Pedido pedido) {
        return PedidoDTO.builder()
                .id(pedido.getId())
                .clienteId(pedido.getClienteId())
                .estado(pedido.getEstado())
                .montoTotal(pedido.getMontoTotal())
                .fechaCreacion(pedido.getFechaCreacion())
                .fechaActualizacion(pedido.getFechaActualizacion())
                .items(pedido.getListaItems().stream()
                        .map(PedidoMapper::toDTO)
                        .toList())
                .build();
    }

    public static Pedido toEntity(PedidoDTO pedidoDTO) {
        Pedido pedido = Pedido.builder()
                .id(pedidoDTO.getId())
                .clienteId(pedidoDTO.getClienteId())
                .estado(pedidoDTO.getEstado())
                .montoTotal(pedidoDTO.getMontoTotal())
                .fechaCreacion(pedidoDTO.getFechaCreacion())
                .fechaActualizacion(pedidoDTO.getFechaActualizacion())
                .build();

        pedidoDTO.getItems().forEach(itemDTO -> {
            ItemPedido item = toEntity(itemDTO);
            item.setPedido(pedido);
            pedido.getListaItems().add(item);
        });

        return pedido;
    }

    public static ItemPedidoDTO toDTO(ItemPedido item) {
        return ItemPedidoDTO.builder()
                .id(item.getId())
                .productoId(item.getProductoId())
                .cantidad(item.getCantidad())
                .precioUnitario(item.getPrecioUnitario())
                .build();
    }

    public static ItemPedido toEntity(ItemPedidoDTO itemPedidoDTO) {
        return ItemPedido.builder()
                .id(itemPedidoDTO.getId())
                .productoId(itemPedidoDTO.getProductoId())
                .cantidad(itemPedidoDTO.getCantidad())
                .precioUnitario(itemPedidoDTO.getPrecioUnitario())
                .build();
    }
}
