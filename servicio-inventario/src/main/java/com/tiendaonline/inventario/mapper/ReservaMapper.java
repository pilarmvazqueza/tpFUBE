package com.tiendaonline.inventario.mapper;

import com.tiendaonline.inventario.clases.Producto;
import com.tiendaonline.inventario.clases.Reserva;
import com.tiendaonline.inventario.dto.ReservaDTO;

public class ReservaMapper {

    public static ReservaDTO toDTO(Reserva reserva) {
        return ReservaDTO.builder()
                .id(reserva.getId())
                .productoId(reserva.getProducto().getId())
                .pedidoId(reserva.getPedidoId())
                .cantidad(reserva.getCantidad())
                .estado(reserva.getEstado())
                .build();
    }

    public static Reserva toEntity(ReservaDTO reservaDTO) {
        return Reserva.builder()
                .id(reservaDTO.getId())
                .producto(Producto.builder().id(reservaDTO.getProductoId()).build())
                .pedidoId(reservaDTO.getPedidoId())
                .cantidad(reservaDTO.getCantidad())
                .estado(reservaDTO.getEstado())
                .build();
    }
}
