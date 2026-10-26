package com.tiendaonline.pedidos.servicios;

import com.tiendaonline.clientes.dto.ClienteDTO;
import com.tiendaonline.clientes.excepciones.ClienteNoEncontradoException;
import com.tiendaonline.clientes.mapper.ClienteMapper;
import com.tiendaonline.clientes.clases.Cliente;
import com.tiendaonline.clientes.repositorio.ClienteRepositorio;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClienteServicioImpl implements ClienteServicio {

    private final ClienteRepositorio repositorio;

    public ClienteServicioImpl(ClienteRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public ClienteDTO crear(Cliente cliente) {
        cliente.setFechaAlta(LocalDateTime.now());
        return ClienteMapper.toDTO(repositorio.save(cliente));
    }

    @Override
    public ClienteDTO buscarPorId(Long id) {
        return repositorio.findById(id)
                .map(ClienteMapper::toDTO)
                .orElseThrow(() -> new ClienteNoEncontradoException(id));
    }

    @Override
    public List<ClienteDTO> buscarTodos() {
        return repositorio.findAll().stream()
                .map(ClienteMapper::toDTO)
                .toList();
    }

    @Override
    public ClienteDTO actualizar(Cliente cliente) {
        Cliente clienteViejo = repositorio.findById(cliente.getId())
                .orElseThrow(() -> new ClienteNoEncontradoException(cliente.getId()));
        cliente.setFechaAlta(clienteViejo.getFechaAlta());
        return ClienteMapper.toDTO(repositorio.save(cliente));
    }

    @Override
    public void eliminar(Long id) {
        repositorio.deleteById(id);
    }
}
