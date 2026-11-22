package com.tiendaonline.clientes.servicios;

import com.tiendaonline.clientes.dto.ClienteDTO;
import com.tiendaonline.clientes.dto.PedidoDTO;
import com.tiendaonline.clientes.excepciones.ClienteNoEncontradoException;
import com.tiendaonline.clientes.integracion.PedidosClient;
import com.tiendaonline.clientes.mapper.ClienteMapper;
import com.tiendaonline.clientes.clases.Cliente;
import com.tiendaonline.clientes.repositorio.ClienteRepositorio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
public class ClienteServicioImpl implements ClienteServicio {

    private final ClienteRepositorio repositorio;
    private final PedidosClient pedidosClient;

    public ClienteServicioImpl(ClienteRepositorio repositorio, PedidosClient pedidosClient) {
        this.repositorio = repositorio;
        this.pedidosClient = pedidosClient;
    }

    @Override
    public ClienteDTO crear(Cliente cliente) {
        cliente.setFechaAlta(LocalDateTime.now());
        ClienteDTO clienteCreado = ClienteMapper.toDTO(repositorio.save(cliente));

        log.info("Cliente creado {}", kv("clienteId", clienteCreado.getId()));

        return clienteCreado;
    }

    @Override
    public ClienteDTO buscarPorId(Long id) {
        log.debug("Buscando cliente {}", kv("clienteId", id));

        return repositorio.findById(id)
                .map(ClienteMapper::toDTO)
                .orElseThrow(() -> {
                    log.warn("Cliente no encontrado {}", kv("clienteId", id));
                    return new ClienteNoEncontradoException(id);
                });
    }

    @Override
    public List<ClienteDTO> buscarTodos() {
        List<ClienteDTO> clientes = repositorio.findAll().stream()
                .map(ClienteMapper::toDTO)
                .toList();
        log.info("Lista de clientes {}", kv("cantidad", clientes.size()));

        return clientes;
    }

    @Override
    public ClienteDTO actualizar(Cliente cliente) {
        Cliente clienteViejo = repositorio.findById(cliente.getId())
                .orElseThrow(() -> {
                   log.warn("Intento de actualizar cliente intexistente {}",
                           kv("clienteId", cliente.getId()));
                   return new ClienteNoEncontradoException(cliente.getId());
                });

        cliente.setFechaAlta(clienteViejo.getFechaAlta());
        ClienteDTO clienteActualizado = ClienteMapper.toDTO(repositorio.save(cliente));

        log.info("Cliente actualizado {}", kv("clienteId", clienteActualizado.getId()));

        return clienteActualizado;
    }

    @Override
    public void eliminar(Long id) {
        if (!repositorio.existsById(id)) {
            log.warn("Intento de eliminar cliente inexistente{}", kv("clienteId", id));
            throw new ClienteNoEncontradoException(id);
        }
        repositorio.deleteById(id);
        log.info("Cliente eliminado {}", kv("clienteId", id));
    }

    @Override
    public List<PedidoDTO> listarPedidos(Long id) {
        buscarPorId(id);
        List<PedidoDTO> pedidos = pedidosClient.listarPorCliente(id);

        log.info("Lista de pedidos del cliente {} {}",
                kv("clienteId", id),
                kv("cantidad", pedidos.size()));
        //pedidosClient.listarPorCliente(id);
        return pedidos;
    }
}
