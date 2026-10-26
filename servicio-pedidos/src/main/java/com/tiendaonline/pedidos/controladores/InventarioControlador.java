package com.tiendaonline.pedidos.controladores;

import com.tiendaonline.clientes.servicios.InventarioServicioImpl;
import org.springframework.web.bind.annotation.*;


@RestController()
@RequestMapping("/api/inventario")
public class InventarioControlador {

    private final InventarioServicioImpl servicio;


    public InventarioControlador(InventarioServicioImpl servicio) {
        this.servicio = servicio;
    }


}