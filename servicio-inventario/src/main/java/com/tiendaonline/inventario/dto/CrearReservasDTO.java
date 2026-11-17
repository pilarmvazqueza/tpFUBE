package com.tiendaonline.inventario.dto;
import lombok.*;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 

public class CrearReservasDTO {


    @NotNull(message = "El ID del pedido no puede ser nulo")

    private Long pedidoId;

    @NotEmpty(message = "La lista de ítems no puede estar vacía")
    @Valid
    private List<ItemReservaDTO> items;

}
