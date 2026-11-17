package com.tiendaonline.pedidos.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClienteDTO {

    private Long id;
    private String nombre;
    private String email;
}
