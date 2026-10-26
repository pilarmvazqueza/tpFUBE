package com.tiendaonline.inventario.clases;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "estadoPedidos")
public class EstadoPedido {
    String CREADO;
    String CONFIRMADO;
    String CANCELADO;
    String EXPIRADO;
}