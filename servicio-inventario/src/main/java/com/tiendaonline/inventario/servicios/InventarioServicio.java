package com.tiendaonline.inventario.servicios;

import com.tiendaonline.clientes.dto.ProductoDTO;

public interface InventarioServicio {

    Integer consultarStock (Long Id);
}
