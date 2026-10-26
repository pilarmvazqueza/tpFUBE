package com.tiendaonline.clientes.servicios;


import com.tiendaonline.clientes.dto.ProductoDTO;
import com.tiendaonline.clientes.excepciones.ProductoNoEncontradoExcepcion;
import com.tiendaonline.clientes.mapper.ProductoMapper;
import com.tiendaonline.clientes.clases.Producto;
import com.tiendaonline.clientes.repositorio.ProductoRepositorio;
import org.springframework.stereotype.Service;

@Service
public class InventarioServicioImpl implements InventarioServicio {

    private final ProductoRepositorio repoProducto;

    public InventarioServicioImpl(ProductoRepositorio repoProducto) {
        this.repoProducto = repoProducto;
    }

    @Override
    public Integer consultarStock(Long Id) {
        Producto producto = repoProducto.findById(Id)
                //.map(ProductoMapper::toDTO)
                .orElseThrow(() -> new ProductoNoEncontradoExcepcion(Id));

        //aca devolvemos el stock del producto
        return producto.getCantidadDisponible();
    }

}
