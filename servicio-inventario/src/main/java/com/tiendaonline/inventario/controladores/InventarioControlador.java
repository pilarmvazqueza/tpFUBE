package com.tiendaonline.inventario.controladores;

import com.tiendaonline.inventario.dto.ReservaDTO;
import com.tiendaonline.inventario.mapper.ReservaMapper;
import com.tiendaonline.inventario.servicios.InventarioServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/v1/inventario")
public class InventarioControlador {

    private final InventarioServicio servicio;

    public InventarioControlador(InventarioServicio servicio) {
        this.servicio = servicio;
    }

}
