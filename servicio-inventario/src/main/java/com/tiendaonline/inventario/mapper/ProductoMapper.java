package com.tiendaonline.inventario.mapper;

import com.tiendaonline.clientes.dto.ProductoDTO;
import com.tiendaonline.clientes.clases.Producto;

public class ProductoMapper {
    public static ProductoDTO toDTO(Producto producto) {

        return ProductoDTO.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .cantidadDisponible(producto.getCantidadDisponible())
                .build();

    }

    public static Producto toEntity(ProductoDTO productoDTO) {
        return Producto.builder()
                .id(productoDTO.getId())
                .nombre(productoDTO.getNombre())
                .cantidadDisponible(productoDTO.getCantidadDisponible())
                .build();
    }
}
