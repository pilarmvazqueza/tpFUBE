package com.tiendaonline.inventario.repositorio;

import com.tiendaonline.inventario.clases.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservaRepositorio extends JpaRepository<Reserva, Long> {

}
