package com.tiendaonline.pedidos.servicios;

import com.tiendaonline.pedidos.clases.EstadoPedido;
import com.tiendaonline.pedidos.clases.Pedido;
import com.tiendaonline.pedidos.dto.ItemReservaDTO;
import com.tiendaonline.pedidos.integracion.ClientesClient;
import com.tiendaonline.pedidos.integracion.InventarioClient;
import com.tiendaonline.pedidos.dto.PedidoDTO;
import com.tiendaonline.pedidos.excepciones.PedidoNoCancelableExcepcion;
import com.tiendaonline.pedidos.excepciones.PedidoNoEncontradoExcepcion;
import com.tiendaonline.pedidos.mapper.PedidoMapper;
import com.tiendaonline.pedidos.repositorio.PedidoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Service
public class PedidoServicioImpl implements PedidoServicio {

    private final PedidoRepositorio repositorio;
    private final ClientesClient clientesClient;
    private final InventarioClient inventarioClient;

    public PedidoServicioImpl(PedidoRepositorio repositorio, ClientesClient clientesClient, InventarioClient inventarioClient) {
        this.repositorio = repositorio;
        this.clientesClient = clientesClient;
        this.inventarioClient = inventarioClient;
    }


    @Override
    @Transactional(readOnly = true)
    public PedidoDTO buscarPorId(Long id) {
        return repositorio.findById(id)
                .map(PedidoMapper::toDTO)
                .orElseThrow(() -> new PedidoNoEncontradoExcepcion(id));
    }

    @Override
    @Transactional
    public PedidoDTO cancelar(Long id) {
        Pedido pedido = repositorio.findById(id)
                .orElseThrow(() -> new PedidoNoEncontradoExcepcion(id));

        if (pedido.getEstado() != EstadoPedido.CREADO && pedido.getEstado() != EstadoPedido.CONFIRMADO) {
            throw new PedidoNoCancelableExcepcion(id, pedido.getEstado());
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setFechaActualizacion(LocalDateTime.now());
        inventarioClient.liberarPorPedido(id);

        return PedidoMapper.toDTO(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDTO> listarPorCliente(Long clienteId) {
        return repositorio.findByClienteIdOrderByFechaCreacionDesc(clienteId)
                .stream()
                .map(PedidoMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public PedidoDTO crearPedido(PedidoDTO dto) {
        clientesClient.consultarPorId(dto.getClienteId());

        BigDecimal total = dto.getItems().stream()
                .map(item -> item.getPrecioUnitario()
                        .multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Pedido pedido = PedidoMapper.toEntity(dto);
        pedido.setEstado(EstadoPedido.CREADO);
        pedido.setMontoTotal(total);
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setFechaActualizacion(pedido.getFechaCreacion());
        Pedido guardado = repositorio.save(pedido);

        List<ItemReservaDTO> reservas = dto
                .getItems()
                .stream()
                .map(item -> new ItemReservaDTO(item.getProductoId(), item.getCantidad())).toList();
        inventarioClient.reservar(guardado.getId(), reservas);

        return PedidoMapper.toDTO(guardado);
    }
}
