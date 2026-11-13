package com.tiendaonline.inventario.servicios;

import com.tiendaonline.inventario.clases.Reserva;
import com.tiendaonline.inventario.dto.ReservaDTO;

public interface InventarioServicio {

    Integer consultarStock(Long id);
}
