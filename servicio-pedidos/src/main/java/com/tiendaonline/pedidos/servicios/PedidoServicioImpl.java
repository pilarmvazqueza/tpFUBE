package com.tiendaonline.pedidos.servicios;


import com.tiendaonline.clientes.dto.PedidoDTO;
import com.tiendaonline.clientes.excepciones.PedidoNoEncontradoExcepcion;
import com.tiendaonline.clientes.mapper.ProductoMapper;

import com.tiendaonline.clientes.clases.Producto;
import com.tiendaonline.clientes.repositorio.ProductoRepositorio;
import org.springframework.stereotype.Service;

@Service
public class PedidoServicioImpl implements PedidoServicio {

    private final ProductoRepositorio repoPedido;

    public PedidoServicioImpl(ProductoRepositorio repoPedido) {
        this.repoPedido = repoPedido;
    }

    @Override
    public PedidoDTO buscarPorId (Long Id) {
        return repoPedido.findById(Id)
                .map(ProductoMapper::toDTO)
                .orElseThrow(() -> new PedidoNoEncontradoExcepcion(Id));

    }

}