package com.tiendaonline.inventario.dto;

import com.tiendaonline.inventario.clases.EstadoReserva;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaDTO {

    private Long id;
    private Long productoId;
    private Long pedidoId;
    private Integer cantidad;
    private EstadoReserva estado;
}
