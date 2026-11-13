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

    private final repositorio;
    public PedidoServicioImpl(PedidoRepositorio repositorio) {
        this.repositorio = repositorio;
    }


    //Consultar un pedido mediante su identificador.
    @Override
    public PedidoDTO buscarPorId(Long id) {
        return repositorio.findById(id)
                .map(PedidoMapper::toDTO)
                .orElseThrow(() -> new PedidoNoEncontradoExcepcion(id));
    }

    //unica operacion transaccional para que se reserve todo (todos los productos) o nada (ningun prod.).
    @Override
    public PedidoDTO crearPedido(){
        //TRYYYYYY
        //○ verificar que exista el cliente (utilizando el servicio-clientes)
        //○ reservar el stock de cada ítem contra servicio-inventario
        //○ calcular el monto total.

        //CATCHHHHH
        //○ Si el cliente no existe o no hay stock suficiente, el pedido no se crea y se
        //devuelve el error correspondiente.
        try {

        }   catch (PedidoNoCreadoExcepcion excepcion) {}
    }
}
