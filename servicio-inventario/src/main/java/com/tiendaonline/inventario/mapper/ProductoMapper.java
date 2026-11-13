package com.tiendaonline.inventario.mapper;

import com.tiendaonline.inventario.clases.Producto;
import com.tiendaonline.inventario.dto.ProductoDTO;
import com.tiendaonline.inventario.dto.ProductoStockDTO;

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

    public static ProductoStockDTO toProductoStockDTO(Producto producto, Integer cantidadSolicitada){
        return ProductoStockDTO.builder()
            .productoId(producto.getId())
            .nombre(producto.getNombre())
            .cantidadSolicitada(cantidadSolicitada)
            .cantidadDisponible(producto.getCantidadDisponible())
            .build();
    }
}
