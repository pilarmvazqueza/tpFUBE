package com.tiendaonline.pedidos.excepciones;

public class ReservaNoEncontradaExcepcion extends RuntimeException {
    public ReservaNoEncontradaExcepcion(Long Id) {
        super("Reserva no encontrada con id: " + Id);
    }
}