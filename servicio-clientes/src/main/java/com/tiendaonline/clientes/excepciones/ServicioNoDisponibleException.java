package com.tiendaonline.clientes.excepciones;

public class ServicioNoDisponibleException extends RuntimeException {
    public ServicioNoDisponibleException(String servicio) {
        super("El servicio no esta disponible: " + servicio);
    }
}
