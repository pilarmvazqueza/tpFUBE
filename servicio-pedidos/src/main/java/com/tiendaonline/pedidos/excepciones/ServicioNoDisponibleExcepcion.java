package com.tiendaonline.pedidos.excepciones;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServicioNoDisponibleExcepcion extends RuntimeException {
    public ServicioNoDisponibleExcepcion(String message) {
        super(message);
    }
}
