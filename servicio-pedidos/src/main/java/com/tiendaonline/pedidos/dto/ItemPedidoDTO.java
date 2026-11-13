package com.tiendaonline.pedidos.dto;

import com.tiendaonline.pedidos.clases.Pedido;
import jakarta.persistence.*;

import java.math.BigDecimal;

public class ItemPedidoDTO
{

    private Long id;
    private Pedido pedido;
    private Long productoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;



}
