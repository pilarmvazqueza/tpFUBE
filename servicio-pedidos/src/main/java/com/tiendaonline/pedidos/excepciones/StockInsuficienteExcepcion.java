package com.tiendaonline.pedidos.excepciones;

import lombok.Getter;

import java.util.List;

@Getter
public class StockInsuficienteExcepcion extends RuntimeException {
    private final List<String> detalles;
    public StockInsuficienteExcepcion(String mensaje, List<String> detalles) {
        super(mensaje);
        this.detalles = detalles;
    }
}
