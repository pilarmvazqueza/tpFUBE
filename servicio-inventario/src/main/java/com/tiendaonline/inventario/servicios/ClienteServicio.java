package com.tiendaonline.inventario.servicios;

import com.tiendaonline.clientes.dto.ClienteDTO;
import com.tiendaonline.clientes.clases.Cliente;

import java.util.List;

public interface ClienteServicio {

    ClienteDTO crear(Cliente cliente);
    ClienteDTO buscarPorId(Long id);
    List<ClienteDTO> buscarTodos();
    ClienteDTO actualizar(Cliente cliente);
    void eliminar(Long id);
}
