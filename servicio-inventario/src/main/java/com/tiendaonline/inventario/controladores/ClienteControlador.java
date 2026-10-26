package com.tiendaonline.inventario.controladores;

import com.tiendaonline.clientes.dto.ClienteDTO;
import com.tiendaonline.clientes.mapper.ClienteMapper;
import com.tiendaonline.clientes.servicios.ClienteServicio;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/api/clientes")
public class ClienteControlador {

    private final ClienteServicio servicio;

    public ClienteControlador(ClienteServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ClienteDTO crear(@RequestBody @Valid ClienteDTO clienteDTO) {
        return servicio.crear(ClienteMapper.toEntity(clienteDTO));
    }

    @GetMapping("/{id}")
    public ClienteDTO buscarPorId(@PathVariable Long id) {
        return servicio.buscarPorId(id);
    }

    @GetMapping
    public List<ClienteDTO> buscarTodos() {
        return servicio.buscarTodos();
    }

    @PutMapping("/{id}")
    public ClienteDTO actualizar(@PathVariable Long id, @RequestBody @Valid ClienteDTO clienteDTO) {
        clienteDTO.setId(id);
        return servicio.actualizar(ClienteMapper.toEntity(clienteDTO));
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
