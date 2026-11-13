package com.tiendaonline.inventario.excepciones;

import lombok.Getter;
import java.util.List;

@Getter
public class StockInsuficienteExcepcion extends RuntimeException {
    
    private final List<String> detalles;

    public StockInsuficienteExcepcion(List<String> detalles) {
        super("Stock insuficiente para procesar la reserva");
        this.detalles = detalles;
    }
}
