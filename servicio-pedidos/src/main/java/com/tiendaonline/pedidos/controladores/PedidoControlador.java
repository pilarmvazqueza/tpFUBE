package com.tiendaonline.pedidos.controladores;

import com.tiendaonline.pedidos.dto.PedidoDTO;
import com.tiendaonline.pedidos.servicios.PedidoServicio;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController()
@RequestMapping("/api/v1/pedidos")
public class PedidoControlador {

    private final PedidoServicio servicio;

    public PedidoControlador(PedidoServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public ResponseEntity<List<PedidoDTO>> listarPorCliente(@RequestParam Long clienteId) {
        log.info("GET /api/v1/pedidos?clienteId={}", clienteId);
        return new ResponseEntity<>(servicio.listarPorCliente(clienteId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoDTO> buscarPorId(@PathVariable Long id) {
        log.info("GET /api/v1/pedidos/{}", id);
        return new ResponseEntity<>(servicio.buscarPorId(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<PedidoDTO> crear(@Valid @RequestBody PedidoDTO dto) {
        log.info("POST /api/v1/pedidos");
        return new ResponseEntity<>(servicio.crearPedido(dto), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/confirmacion")
    public ResponseEntity<PedidoDTO> confirmar(@PathVariable Long id) {
        log.info("PATCH /api/v1/pedidos/{}/confirmacion", id);
        return new ResponseEntity<>(servicio.confirmar(id), HttpStatus.OK);
    }

    @PatchMapping("/{id}/cancelacion")
    public ResponseEntity<PedidoDTO> cancelar(@PathVariable Long id) {
        log.info("PATCH /api/v1/pedidos/{}/cancelacion", id);
        return new ResponseEntity<>(servicio.cancelar(id), HttpStatus.OK);
    }
}
