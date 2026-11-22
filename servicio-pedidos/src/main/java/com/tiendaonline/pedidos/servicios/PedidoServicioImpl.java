package com.tiendaonline.pedidos.servicios;

import com.tiendaonline.pedidos.clases.EstadoPedido;
import com.tiendaonline.pedidos.clases.Pedido;
import com.tiendaonline.pedidos.dto.ItemReservaDTO;
import com.tiendaonline.pedidos.integracion.ClientesClient;
import com.tiendaonline.pedidos.integracion.InventarioClient;
import com.tiendaonline.pedidos.dto.PedidoDTO;
import com.tiendaonline.pedidos.excepciones.PedidoNoCancelableExcepcion;
import com.tiendaonline.pedidos.excepciones.PedidoNoConfirmableExcepcion;
import com.tiendaonline.pedidos.excepciones.PedidoNoEncontradoExcepcion;
import com.tiendaonline.pedidos.mapper.PedidoMapper;
import com.tiendaonline.pedidos.repositorio.PedidoRepositorio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
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
        log.debug("Buscando pedido {}", kv("pedidoId", id));

        return repositorio.findById(id)
                .map(PedidoMapper::toDTO)
                .orElseThrow(() -> {
                    log.warn("Pedido no encontrado {}", kv("pedidoId", id));
                    return new PedidoNoEncontradoExcepcion(id);
                });
    }

    @Override
    @Transactional
    public PedidoDTO cancelar(Long id) {
        log.debug("Iniciando cancelación de pedido {}", kv("pedidoId", id));

        Pedido pedido = repositorio.findById(id)
                .orElseThrow(() -> {
                    log.warn("Pedido no encontrado al intentar cancelar{}", kv("pedidoId", id));
                    return new PedidoNoEncontradoExcepcion(id);
                });

        if (pedido.getEstado() != EstadoPedido.CREADO
                && pedido.getEstado() != EstadoPedido.CONFIRMADO) {
            log.warn("Pedido no cancelable {} {}",
                    kv("pedidoId", id),
                    kv("estado", pedido.getEstado()));

            throw new PedidoNoCancelableExcepcion(id, pedido.getEstado());
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setFechaActualizacion(LocalDateTime.now());

        log.info("Liberando reservas del pedido {}", kv("pedidoId", id));
        inventarioClient.liberarPorPedido(id);

        log.info("Pedido cancelado correctamente {}", kv("pedidoId", id));

        return PedidoMapper.toDTO(pedido);
    }

    @Override
    @Transactional
    public PedidoDTO confirmar(Long id) {
        log.debug("Iniciando confirmación de pedido {}", kv("pedidoId", id));

        Pedido pedido = repositorio.findById(id)
                .orElseThrow(() -> {
                    log.warn("Pedido no encontrado al intentar confirmar{}",
                            kv("pedidoId", id));
                    return new PedidoNoEncontradoExcepcion(id);
                });

        if (pedido.getEstado() != EstadoPedido.CREADO) {
            log.warn("Pedido no confirmable",
                    kv("pedidoId", id),
                    kv("estado", pedido.getEstado()));

            throw new PedidoNoConfirmableExcepcion(id, pedido.getEstado());
        }

        pedido.setEstado(EstadoPedido.CONFIRMADO);
        pedido.setFechaActualizacion(LocalDateTime.now());

        log.info("Pedido confirmado correctamente {}", kv("pedidoId", id));

        return PedidoMapper.toDTO(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoDTO> listarPorCliente(Long clienteId) {
        log.debug("Buscando pedidos del cliente {}", kv("clienteId", clienteId));

        List<PedidoDTO> pedidos = repositorio
                                .findByClienteIdOrderByFechaCreacionDesc(clienteId)
                                .stream()
                                .map(PedidoMapper::toDTO)
                                .toList();

        log.info("Listado de pedidos {} {}",
                kv("clienteId", clienteId),
                kv("cantidad", pedidos.size()));

        return pedidos;
    }

    @Override
    @Transactional
    public PedidoDTO crearPedido(PedidoDTO dto) {
        log.info("Iniciando creación de pedido {}", kv("clienteId", dto.getClienteId()));

        //verificamos que el cliente exista
        log.debug("Consultando cliente en microservicio Clientes {}",
                kv("clienteId", dto.getClienteId()));

        clientesClient.consultarPorId(dto.getClienteId());

        //calcular monto total
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

        //preparar las reservas de stock
        List<ItemReservaDTO> reservas = dto
                .getItems()
                .stream()
                .map(item -> new ItemReservaDTO(item.getProductoId(), item.getCantidad()))
                .toList();

        //solicitar la reserva al microservicio Inventario
        log.info("Solicitando reserva de stock en microservicio Inventario {} {}",
                kv("pedidoId", guardado.getId()),
                kv("cantidadProductos", reservas.size()));

        inventarioClient.reservar(guardado.getId(), reservas);

        log.info("Pedido creado correctamente {} {}",
                kv("pedidoId", guardado.getId()),
                kv("clienteId", guardado.getClienteId()));

        return PedidoMapper.toDTO(guardado);
    }
}
