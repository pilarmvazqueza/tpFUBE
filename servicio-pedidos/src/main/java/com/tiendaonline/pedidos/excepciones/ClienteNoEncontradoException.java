package com.tiendaonline.pedidos.excepciones;

public class ClienteNoEncontradoException extends RuntimeException {
    public ClienteNoEncontradoException(Long id){
        super("Cliente no encontrado con id: " + id);
    }
}
