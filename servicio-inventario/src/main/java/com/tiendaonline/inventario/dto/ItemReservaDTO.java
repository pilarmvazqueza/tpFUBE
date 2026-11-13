package com.tiendaonline.inventario.dto;
import lombok.*;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data 
@Builder 
@NoArgsConstructor
@AllArgsConstructor

public class ItemReservaDTO {

    @NotNull(message = "El ID del producto no puede ser nulo")
    private long productoId;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 1, message = "La cantidad debe ser mayor a cero")
    private Integer cantidad; 

}
