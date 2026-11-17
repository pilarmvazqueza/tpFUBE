package com.tiendaonline.pedidos.integracion;

import com.tiendaonline.pedidos.dto.ClienteDTO;
import com.tiendaonline.pedidos.excepciones.ClienteNoEncontradoExcepcion;
import com.tiendaonline.pedidos.excepciones.ServicioNoDisponibleExcepcion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

//creamos clase ClientesClient ya que la necesitamos para hacer la comunicacion entre los microservicios.
@Component
public class ClientesClient {
   private final RestClient restClient;

   public ClientesClient( RestClient.Builder builder,
                          @Value("${servicios.clientes.url}") String url) {
       this.restClient = builder.baseUrl(url).build();
   }

    public ClienteDTO consultarPorId(Long idCliente) {
        try {
            return restClient.get().uri("/api/v1/clientes/{id}", idCliente)
                    .retrieve().body(ClienteDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ClienteNoEncontradoExcepcion(idCliente);
        } catch (ResourceAccessException e) {
            throw new ServicioNoDisponibleExcepcion("servicio-clientes");
        }
    }
}
