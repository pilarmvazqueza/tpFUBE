package com.tiendaonline.pedidos.controladores;

import com.tiendaonline.pedidos.dto.PedidoDTO;
import com.tiendaonline.pedidos.servicios.PedidoServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/api/v1/pedidos")
public class PedidoControlador {

    private final PedidoServicio servicio;

    public PedidoControlador(PedidoServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public ResponseEntity<List<PedidoDTO>> listarPorCliente(@RequestParam Long clienteId) {
        return new ResponseEntity<>(servicio.listarPorCliente(clienteId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoDTO> buscarPorId(@PathVariable Long id) {
        return new ResponseEntity<>(servicio.buscarPorId(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<PedidoDTO> crear(@Valid @RequestBody PedidoDTO dto) {
        return new ResponseEntity<>(servicio.crearPedido(dto), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/confirmacion")
    public ResponseEntity<PedidoDTO> confirmar(@PathVariable Long id) {
        return new ResponseEntity<>(servicio.confirmar(id), HttpStatus.OK);
    }

    @PatchMapping("/{id}/cancelacion")
    public ResponseEntity<PedidoDTO> cancelar(@PathVariable Long id) {
        return new ResponseEntity<>(servicio.cancelar(id), HttpStatus.OK);
    }
}
