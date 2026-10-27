package com.tiendaonline.inventario.servicios;

import com.tiendaonline.inventario.clases.Producto;
import com.tiendaonline.inventario.excepciones.ProductoNoEncontradoExcepcion;
import com.tiendaonline.inventario.repositorio.ProductoRepositorio;
import org.springframework.stereotype.Service;

@Service
public class InventarioServicioImpl implements InventarioServicio {

    private final ProductoRepositorio repoProducto;

    public InventarioServicioImpl(ProductoRepositorio repoProducto) {
        this.repoProducto = repoProducto;
    }

    @Override
    public Integer consultarStock(Long id) {
        Producto producto = repoProducto.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoExcepcion(id));
        return producto.getCantidadDisponible();
    }
}
