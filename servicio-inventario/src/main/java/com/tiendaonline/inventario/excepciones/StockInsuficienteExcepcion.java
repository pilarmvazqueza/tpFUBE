package com.tiendaonline.inventario.excepciones;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Getter
@ResponseStatus(HttpStatus.CONFLICT)
public class StockInsuficienteExcepcion extends RuntimeException {
    
    private final List<String> detalles;

    public StockInsuficienteExcepcion(List<String> detalles) {
        super("Stock insuficiente para procesar la reserva");
        this.detalles = detalles;
    }
}
