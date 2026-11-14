package com.tiendaonline.pedidos.excepciones;

public class ProductoNoEncontradoExcepcion extends RuntimeException {
    public ProductoNoEncontradoExcepcion(String mensaje) {
        super(mensaje);
    }
}