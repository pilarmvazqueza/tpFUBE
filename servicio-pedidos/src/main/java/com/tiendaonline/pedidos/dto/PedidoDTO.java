package com.tiendaonline.pedidos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tiendaonline.pedidos.clases.EstadoPedido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotNull private Long clienteId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private EstadoPedido estado;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal montoTotal;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime fechaCreacion;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime fechaActualizacion;

    @Valid
    @NotEmpty
    @Builder.Default
    private List<ItemPedidoDTO> items = new ArrayList<>();
}
