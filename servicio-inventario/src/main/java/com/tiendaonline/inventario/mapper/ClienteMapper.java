package com.tiendaonline.inventario.mapper;

import com.tiendaonline.clientes.dto.ClienteDTO;
import com.tiendaonline.clientes.clases.Cliente;

public class ClienteMapper {

    public static ClienteDTO toDTO(Cliente cliente) {

        return ClienteDTO.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .email(cliente.getEmail())
                .telefono(cliente.getTelefono())
                .direccion(cliente.getDireccion())
                .fechaAlta(cliente.getFechaAlta())
                .build();

    }

    public static Cliente toEntity(ClienteDTO dto) {
        return Cliente.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .email(dto.getEmail())
                .telefono(dto.getTelefono())
                .direccion(dto.getDireccion())
                .build();
    }
}
