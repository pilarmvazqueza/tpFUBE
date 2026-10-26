package com.tiendaonline.pedidos.servicios;

import com.tiendaonline.clientes.dto.ProductoDTO;

public interface InventarioServicio {

    Integer consultarStock (Long Id);
}
