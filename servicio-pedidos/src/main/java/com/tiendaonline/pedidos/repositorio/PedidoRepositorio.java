package com.tiendaonline.pedidos.repositorio;

import org.springframework.stereotype.Repository;
import com.tiendaonline.clientes.clases.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface PedidoRepositorio extends JpaRepository<Pedido, Long> {

}