package com.tiendaonline.inventario.controladores;

import com.tiendaonline.inventario.servicios.InventarioServicio;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/v1/inventario")
public class InventarioControlador {

    private final InventarioServicio servicio;

    public InventarioControlador(InventarioServicio servicio) {
        this.servicio = servicio;
    }
}
