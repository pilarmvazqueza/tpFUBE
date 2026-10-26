package com.tiendaonline.inventario.excepciones;

public class ProductoNoEncontradoExcepcion extends RuntimeException {
    public ProductoNoEncontradoExcepcion(Long Id) {
        super("Producto no encontrado con id: " + Id);
    }
}
