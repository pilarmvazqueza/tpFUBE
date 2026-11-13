package com.tiendaonline.pedidos.dto;

import lombok.Data;

@Data
public class ReservaStockDTO {

    public ReservaStockDTO(Long productoId, Integer cantidad) {
        this.productoId = productoId;
        this.cantidad = cantidad;
    }
    private Long productoId;
    private Integer cantidad;
    private Integer pedidoId;




}
