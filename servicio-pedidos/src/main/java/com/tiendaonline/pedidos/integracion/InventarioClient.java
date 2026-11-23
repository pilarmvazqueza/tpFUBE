package com.tiendaonline.pedidos.integracion;
import com.tiendaonline.pedidos.dto.CrearReservasDTO;
import com.tiendaonline.pedidos.dto.ErrorRespuesta;
import com.tiendaonline.pedidos.dto.ItemReservaDTO;
import com.tiendaonline.pedidos.excepciones.ProductoNoEncontradoExcepcion;
import com.tiendaonline.pedidos.excepciones.ServicioNoDisponibleExcepcion;
import com.tiendaonline.pedidos.excepciones.StockInsuficienteExcepcion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
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
            log.warn("Reserva rechazada por stock insuficiente {} {}",
                    kv("servicio", "servicio-inventario"),
                    kv("pedidoId", pedidoId));
            ErrorRespuesta error = e.getResponseBodyAs(ErrorRespuesta.class);
            throw new StockInsuficienteExcepcion(error.getMensaje(), error.getDetalles());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Reserva rechazada por producto inexistente {} {}",
                    kv("servicio", "servicio-inventario"),
                    kv("pedidoId", pedidoId));
            throw new ProductoNoEncontradoExcepcion(e.getResponseBodyAs(ErrorRespuesta.class).getMensaje());
        } catch (ResourceAccessException e) {
            log.error("Servicio remoto no disponible al reservar {} {}",
                    kv("servicio", "servicio-inventario"),
                    kv("pedidoId", pedidoId));
            throw new ServicioNoDisponibleExcepcion("servicio-inventario");
        }
    }

    public void liberarPorPedido(Long pedidoId) {
        try {
            restClient.patch().uri("/api/v1/inventario/reservas/pedido/{id}/liberacion", pedidoId)
                    .retrieve().toBodilessEntity();
        } catch (ResourceAccessException e) {
            log.error("Servicio remoto no disponible al liberar {} {}",
                    kv("servicio", "servicio-inventario"),
                    kv("pedidoId", pedidoId));
            throw new ServicioNoDisponibleExcepcion("servicio-inventario");
        }
    }
}
