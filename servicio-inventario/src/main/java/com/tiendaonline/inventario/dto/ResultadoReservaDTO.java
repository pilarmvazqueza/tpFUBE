package com.tiendaonline.inventario.dto;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ResultadoReservaDTO {

    private List<ReservaDTO> reservas;
    private List<ProductoStockDTO> disponibles;
    private List<ProductoStockDTO> noDisponibles;

}
