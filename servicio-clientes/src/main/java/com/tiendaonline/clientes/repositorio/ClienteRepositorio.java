package com.tiendaonline.clientes.repositorio;

import com.tiendaonline.clientes.clases.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {

}
