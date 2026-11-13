package com.tiendaonline.inventario.dto;
import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ProductoStockDTO {

    private Long productoId;
    private String nombre;
    private Integer cantidadSolicitada;
    private Integer cantidadDisponible;

}
