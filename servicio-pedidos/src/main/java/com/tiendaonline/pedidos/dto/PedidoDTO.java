package com.tiendaonline.pedidos.dto;

import com.tiendaonline.pedidos.clases.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {

    private Long id;
    private Long clienteId;
    private EstadoPedido estado;
    private BigDecimal montoTotal;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
