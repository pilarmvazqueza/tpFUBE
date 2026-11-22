package com.tiendaonline.clientes.excepciones;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServicioNoDisponibleException extends RuntimeException {
    public ServicioNoDisponibleException(String servicio) {

        super("El servicio no esta disponible: " + servicio);
    }
}
