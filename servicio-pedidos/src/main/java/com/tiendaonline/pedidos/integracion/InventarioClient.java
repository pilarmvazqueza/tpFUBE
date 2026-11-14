package com.tiendaonline.pedidos.integracion;
import com.tiendaonline.pedidos.dto.CrearReservasDTO;
import com.tiendaonline.pedidos.dto.ErrorRespuesta;
import com.tiendaonline.pedidos.dto.ItemReservaDTO;
import com.tiendaonline.pedidos.excepciones.ProductoNoEncontradoExcepcion;
import com.tiendaonline.pedidos.excepciones.ServicioNoDisponibleExcepcion;
import com.tiendaonline.pedidos.excepciones.StockInsuficienteExcepcion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class InventarioClient {

    private final RestClient restClient;

    public InventarioClient(RestClient.Builder builder, @Value("${servicios.inventario.url}") String url) {
        this.restClient = builder.baseUrl(url).build();
    }

    public void reservar(Long pedidoId, List<ItemReservaDTO> items) {
        try {
            restClient.post().uri("/api/v1/inventario/reservas")
                    .body(new CrearReservasDTO(pedidoId, items))
                    .retrieve().toBodilessEntity();
        } catch (HttpClientErrorException.Conflict e) {
            ErrorRespuesta error = e.getResponseBodyAs(ErrorRespuesta.class);
            throw new StockInsuficienteExcepcion(error.getMensaje(), error.getDetalles());
        } catch (HttpClientErrorException.NotFound e) {
            throw new ProductoNoEncontradoExcepcion(e.getResponseBodyAs(ErrorRespuesta.class).getMensaje());
        } catch (ResourceAccessException e) {
            throw new ServicioNoDisponibleExcepcion("servicio-inventario");
        }
    }

    public void liberarPorPedido(Long pedidoId) {
        try {
            restClient.patch().uri("/api/v1/inventario/reservas/pedido/{id}/liberacion", pedidoId)
                    .retrieve().toBodilessEntity();
        } catch (ResourceAccessException e) {
            throw new ServicioNoDisponibleExcepcion("servicio-inventario");
        }
    }
}
