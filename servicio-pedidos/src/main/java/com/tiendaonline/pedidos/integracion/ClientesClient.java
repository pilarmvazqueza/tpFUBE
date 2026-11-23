package com.tiendaonline.pedidos.integracion;

import com.tiendaonline.pedidos.dto.ClienteDTO;
import com.tiendaonline.pedidos.excepciones.ClienteNoEncontradoExcepcion;
import com.tiendaonline.pedidos.excepciones.ServicioNoDisponibleExcepcion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import static net.logstash.logback.argument.StructuredArguments.kv;

//creamos clase ClientesClient ya que la necesitamos para hacer la comunicacion entre los microservicios.
@Slf4j
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
            log.warn("Cliente no encontrado en el servicio remoto {} {}",
                    kv("servicio", "servicio-clientes"),
                    kv("clienteId", idCliente));
            throw new ClienteNoEncontradoExcepcion(idCliente);
        } catch (ResourceAccessException e) {
            log.error("Servicio remoto no disponible {} {}",
                    kv("servicio", "servicio-clientes"),
                    kv("clienteId", idCliente));
            throw new ServicioNoDisponibleExcepcion("servicio-clientes");
        }
    }
}
