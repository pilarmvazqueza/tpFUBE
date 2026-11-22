package com.tiendaonline.pedidos.excepciones;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ClienteNoEncontradoExcepcion extends RuntimeException {
    public ClienteNoEncontradoExcepcion(Long id){
        super("Cliente no encontrado con id: " + id);
    }
}
