package com.tiendaonline.pedidos.controladores;

import com.tiendaonline.pedidos.dto.PedidoDTO;
import com.tiendaonline.pedidos.servicios.PedidoServicio;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/v1/pedidos")
public class PedidoControlador {

    private final PedidoServicio servicio;

    public PedidoControlador(PedidoServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoDTO> buscarPorId(@PathVariable Long id) {
        return new ResponseEntity<>(servicio.buscarPorId(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<PedidoDTO> crearPedido(@RequestBody PedidoDTO pedidoDTO){
        return new ResponseEntity<>(servicio.crearPedido(pedidoDTO), HttpStatus.OK);
    }
}
