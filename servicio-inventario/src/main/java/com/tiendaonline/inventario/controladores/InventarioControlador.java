package com.tiendaonline.inventario.controladores;

import com.tiendaonline.inventario.dto.CrearReservasDTO;
import com.tiendaonline.inventario.dto.ReservaDTO;
import com.tiendaonline.inventario.servicios.InventarioServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/v1/inventario")
public class InventarioControlador {

    private final InventarioServicio servicio;

    public InventarioControlador(InventarioServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/productos/{id}/cantidad-disponible")
    public ResponseEntity<Integer> consultarStock(@PathVariable Long id) {
        return ResponseEntity.ok(servicio.consultarStock(id));
    }

    @PostMapping("/reservas")
    public ResponseEntity<List<ReservaDTO>> reservar(@Valid @RequestBody CrearReservasDTO solicitud) {
        List<ReservaDTO> reservas = servicio.reservar(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservas); 
    }

    //patch para cambiar el estado de una reserva a liberada y restaurar el stock de los productos asociados
    @PatchMapping("/reservas/pedido/{pedidoId}/liberacion")
    public ResponseEntity<Void> liberarReservaPorPedido(@PathVariable Long pedidoId) {
        servicio.liberarPorPedido(pedidoId);
        return ResponseEntity.noContent().build(); //204 exitosa pero sin cuerpo
    }
}
