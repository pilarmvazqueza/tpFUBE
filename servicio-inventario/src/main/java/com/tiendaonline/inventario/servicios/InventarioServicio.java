package com.tiendaonline.inventario.servicios;

import com.tiendaonline.inventario.dto.*;
import java.util.List;

public interface InventarioServicio {
    Integer consultarStock(Long id);
    List<ReservaDTO> reservar(CrearReservasDTO solicitud);
    void liberarPorPedido(Long pedidoId);
}
