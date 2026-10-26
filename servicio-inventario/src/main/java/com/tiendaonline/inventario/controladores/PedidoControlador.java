package com.tiendaonline.inventario.controladores;

import com.tiendaonline.clientes.dto.PedidoDTO;
import com.tiendaonline.clientes.servicios.PedidoServicio;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/pedidos")
public class PedidoControlador {

    private final PedidoServicio servicio;

    public PedidoControlador(PedidoServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/{id}")
    public PedidoDTO buscarPorId(@PathVariable Long id) {
        return servicio.buscarPorId(id);
    }


}