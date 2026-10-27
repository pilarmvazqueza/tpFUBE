package com.tiendaonline.clientes.mapper;

import com.tiendaonline.clientes.dto.ClienteDTO;
import com.tiendaonline.clientes.clases.Cliente;

public class ClienteMapper {

    public static ClienteDTO toDTO(Cliente cliente) {

        return ClienteDTO.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .email(cliente.getEmail())
                .fechaAlta(cliente.getFechaAlta())
                .build();

    }

    public static Cliente toEntity(ClienteDTO dto) {
        return Cliente.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .email(dto.getEmail())
                .fechaAlta(dto.getFechaAlta())
                .build();
    }
}
