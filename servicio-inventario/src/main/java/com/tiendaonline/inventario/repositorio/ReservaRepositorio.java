package com.tiendaonline.inventario.repositorio;

import com.tiendaonline.inventario.clases.EstadoReserva;
import com.tiendaonline.inventario.clases.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservaRepositorio extends JpaRepository<Reserva, Long> {

    List<Reserva> findByPedidoIdAndEstado(Long pedidoId, EstadoReserva estado);

}
