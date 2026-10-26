package com.tiendaonline.clientes.servicios;

import com.tiendaonline.clientes.dto.ProductoDTO;

public interface InventarioServicio {

    Integer consultarStock (Long Id);
}
