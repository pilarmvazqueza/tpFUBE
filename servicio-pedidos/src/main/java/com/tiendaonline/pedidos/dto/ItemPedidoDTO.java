package com.tiendaonline.pedidos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPedidoDTO{
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    @NotNull private Long productoId;
    @NotNull @Min(1) private Integer cantidad;
    @NotNull @Positive private BigDecimal precioUnitario;
}
