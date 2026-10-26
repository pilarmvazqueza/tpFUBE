package com.tiendaonline.clientes.repositorio;

import com.tiendaonline.clientes.clases.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ReservaRepositorio extends JpaRepository<Reserva, Long> {

}