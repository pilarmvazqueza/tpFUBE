package com.tiendaonline.pedidos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemReservaDTO {
    private Long productoId;
    private Integer cantidad;
}
