package com.tiendaonline.pedidos.excepciones;


public class ClienteNoEncontradoExcepcion extends RuntimeException {
    public ClienteNoEncontradoExcepcion(Long id){
        super("Cliente no encontrado con id: " + id);
    }
}
