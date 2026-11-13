package com.tiendaonline.pedidos.clases;

import com.tiendaonline.pedidos.dto.ClienteDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

//creamos clase ClientesClient ya que la necesitamos para hacer la comunicacion entre los microservicios.
@Component
public class ClientesClient {
   private final RestClient restClient;

   public ClientesClient( RestClient.Builder builder, @Value("${servicios.clientes.url") String url) {

       this.restClient = builder.baseUrl(url).build();
   }

   public ClienteDTO consultarClientePorId(Integer idCliente) {
       return restClient.get().uri(("/api/v1/clientes/{id}"), idCliente).retrieve().body(ClienteDTO.class);
   }





}
