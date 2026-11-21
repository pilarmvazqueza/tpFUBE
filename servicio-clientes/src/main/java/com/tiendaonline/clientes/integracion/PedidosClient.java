package com.tiendaonline.clientes.integracion;

import com.tiendaonline.clientes.dto.PedidoDTO;
import com.tiendaonline.clientes.excepciones.ServicioNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class PedidosClient {

    private final RestClient restClient;

    public PedidosClient(RestClient.Builder builder, @Value("${servicios.pedidos.url}") String url) {
        this.restClient = builder.baseUrl(url).build();
    }

    public List<PedidoDTO> listarPorCliente(Long clienteId) {
        try {
            return restClient.get()
                    .uri("/api/v1/pedidos?clienteId={clienteId}", clienteId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<PedidoDTO>>() {});
        } catch (ResourceAccessException | HttpServerErrorException e) {
            throw new ServicioNoDisponibleException("servicio-pedidos");
        }
    }
}
