package com.tiendaonline.clientes.mapper;

import com.tiendaonline.clientes.clases.Cliente;
import com.tiendaonline.clientes.dto.ClienteDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClienteMapperTest {

    @Test
    void testToDTOCopiaTodosLosCampos(){
        LocalDateTime fechaAlta = LocalDateTime.of(2026,10,27,10,30);
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombre("Juan Perez")
                .email("juanperez@mail.com")
                .fechaAlta(fechaAlta)
                .build();

        ClienteDTO clienteDTO = ClienteMapper.toDTO(cliente);

        assertEquals(cliente.getId(), clienteDTO.getId());
        assertEquals(cliente.getNombre(), clienteDTO.getNombre());
        assertEquals(cliente.getEmail(), clienteDTO.getEmail());
        assertEquals(cliente.getFechaAlta(), clienteDTO.getFechaAlta());
    }

    @Test
    void testToEntityCopiaTodosLosCampos(){
        LocalDateTime fechaAlta = LocalDateTime.of(2026,10,27,10,30);
        ClienteDTO clienteDTO = ClienteDTO.builder()
                .id(1L)
                .nombre("Juan Perez")
                .email("juanperez@mail.com")
                .fechaAlta(fechaAlta)
                .build();

        Cliente cliente = ClienteMapper.toEntity(clienteDTO);

        assertEquals(clienteDTO.getId(), cliente.getId());
        assertEquals(clienteDTO.getNombre(), cliente.getNombre());
        assertEquals(clienteDTO.getEmail(), cliente.getEmail());
        assertEquals(clienteDTO.getFechaAlta(), cliente.getFechaAlta());
    }

}
