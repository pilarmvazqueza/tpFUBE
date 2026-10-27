package com.tiendaonline.pedidos.servicios;

import com.tiendaonline.pedidos.dto.PedidoDTO;
import com.tiendaonline.pedidos.excepciones.PedidoNoEncontradoExcepcion;
import com.tiendaonline.pedidos.mapper.PedidoMapper;
import com.tiendaonline.pedidos.repositorio.PedidoRepositorio;
import org.springframework.stereotype.Service;

@Service
public class PedidoServicioImpl implements PedidoServicio {

    private final PedidoRepositorio repositorio;

    public PedidoServicioImpl(PedidoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public PedidoDTO buscarPorId(Long id) {
        return repositorio.findById(id)
                .map(PedidoMapper::toDTO)
                .orElseThrow(() -> new PedidoNoEncontradoExcepcion(id));
    }
}
