package com.tiendaonline.clientes.controladores;

import com.tiendaonline.clientes.dto.ClienteDTO;
import com.tiendaonline.clientes.mapper.ClienteMapper;
import com.tiendaonline.clientes.servicios.ClienteServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/api/v1/clientes")
public class ClienteControlador {

    private final ClienteServicio servicio;

    public ClienteControlador(ClienteServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<ClienteDTO> crear(@RequestBody @Valid ClienteDTO clienteDTO) {
        return new ResponseEntity<>(servicio.crear(ClienteMapper.toEntity(clienteDTO)), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> buscarPorId(@PathVariable Long id) {
        return new ResponseEntity<>(servicio.buscarPorId(id), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> buscarTodos() {
        return new ResponseEntity<>(servicio.buscarTodos(), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> actualizar(@PathVariable Long id, @RequestBody @Valid ClienteDTO clienteDTO) {
        clienteDTO.setId(id);
        return new ResponseEntity<>(servicio.actualizar(ClienteMapper.toEntity(clienteDTO)), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
