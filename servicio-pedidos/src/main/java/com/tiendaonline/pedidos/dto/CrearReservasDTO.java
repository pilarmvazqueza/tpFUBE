package com.tiendaonline.pedidos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearReservasDTO {
    private Long pedidoId;
    private List<ItemReservaDTO> items;
}
