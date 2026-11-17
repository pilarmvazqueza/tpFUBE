package com.tiendaonline.pedidos.repositorio;

import com.tiendaonline.pedidos.clases.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepositorio extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteIdOrderByFechaCreacionDesc(Long clienteId);

}
