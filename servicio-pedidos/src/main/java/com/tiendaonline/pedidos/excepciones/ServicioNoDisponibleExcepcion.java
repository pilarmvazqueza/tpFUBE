package com.tiendaonline.pedidos.excepciones;

public class ServicioNoDisponibleExcepcion extends RuntimeException {
    public ServicioNoDisponibleExcepcion(String message) {
        super(message);
    }
}
