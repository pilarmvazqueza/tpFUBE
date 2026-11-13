package com.tiendaonline.pedidos.servicios;

import com.tiendaonline.pedidos.clases.ClientesClient;
import com.tiendaonline.pedidos.clases.InventarioClient;
import com.tiendaonline.pedidos.clases.ItemPedido;
import com.tiendaonline.pedidos.dto.ClienteDTO;
import com.tiendaonline.pedidos.dto.PedidoDTO;
import com.tiendaonline.pedidos.dto.ReservaStockDTO;
import com.tiendaonline.pedidos.excepciones.PedidoNoCreadoExcepcion;
import com.tiendaonline.pedidos.excepciones.PedidoNoEncontradoExcepcion;
import com.tiendaonline.pedidos.mapper.PedidoMapper;
import com.tiendaonline.pedidos.repositorio.PedidoRepositorio;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class PedidoServicioImpl implements PedidoServicio {


    private final PedidoRepositorio repositorio;
    private final ClientesClient clientesClient;
    //private final InvatarioClient invatarioClient


//FALTA AGREGAR INVENTARIO CLIENT!!!!!!!!!!!!!!!! ACA
    public PedidoServicioImpl(PedidoRepositorio repositorio, ClientesClient clientesClient) {
        this.repositorio = repositorio;
        this.clientesClient = clientesClient;
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
    public PedidoDTO crearPedido(PedidoDTO pedidoDTO){
       List<ReservaStockDTO> reservasRealizadas = new ArrayList<>();
       InventarioClient inventarioClient = new InventarioClient();
       PedidoRepositorio pedidoRepositorio;


        //TRYYYYYY
        //○ verificar que exista el cliente (utilizando el servicio-clientes)
        //○ reservar el stock de cada ítem contra servicio-inventario
        //○ calcular el monto total.

        //CATCHHHHH
        //○ Si el cliente no existe o no hay stock suficiente, el pedido no se crea y se
        //devuelve el error correspondiente.
        try {
            ClienteDTO clienteDTO = clientesClient.consultarClientePorId(pedidoDTO.getClienteId());
            if (clienteDTO == null){
                throw new RuntimeException("Cliente no encontrado");
            }

            for(ItemPedido item : pedidoDTO.getListaItemDTO()) {
                ReservaStockDTO reservaStockDTO = new ReservaStockDTO(
                        item.getProductoId(),
                        item.getCantidad()
                );

                inventarioClient.reservarStock(reservaStockDTO);
                reservasRealizadas.add(reservaStockDTO);
            }

            return pedidoRepositorio.save(pedidoDTO);

        }   catch (PedidoNoCreadoExcepcion excepcion) {
            for (ReservaStockDTO reservaStockRealizadas : reservasRealizadas){
                try{
                    inventarioClient.liberarStock(reservaStockRealizadas);
                } catch (Exception errorLiberacion){
                    System.err.println("No se puedo liberar el stock del producto: " + reservaStockRealizadas.getProductoId());
                }
            }

        }
        //ACOMODARRR!!
      //  throw new RuntimeException("No se puedo registrar el pedido" +  excepcion.getMessage(), excepcion);


    //}
}
