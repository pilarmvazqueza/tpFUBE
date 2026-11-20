package com.tiendaonline.pedidos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenExpiracionDTO {
    private String estado;
    private long leidos;
    private long expirados;
    private long chunksRevertidos;
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private long duracionMs;
}
