package com.tiendaonline.pedidos.integracion;

import com.tiendaonline.pedidos.dto.ItemReservaDTO;
import com.tiendaonline.pedidos.excepciones.ProductoNoEncontradoExcepcion;
import com.tiendaonline.pedidos.excepciones.ServicioNoDisponibleExcepcion;
import com.tiendaonline.pedidos.excepciones.StockInsuficienteExcepcion;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.verify.VerificationTimes;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

public class InventarioClientTest {

    private static final int PUERTO_MOCK = 1082;

    private static ClientAndServer mockServer;
    private InventarioClient inventarioClient;

    @BeforeAll
    static void iniciarMockServer() {
        mockServer = ClientAndServer.startClientAndServer(PUERTO_MOCK);
    }

    @AfterAll
    static void detenerMockServer() {
        mockServer.stop();
    }

    @BeforeEach
    void preparar() {
        mockServer.reset();
        inventarioClient = new InventarioClient(RestClient.builder(), "http://localhost:" + PUERTO_MOCK);
    }

    @Test
    void testReservarSinStockLanzaStockInsuficienteExcepcionConDetalles() {
        mockServer.when(request().withMethod("POST").withPath("/api/v1/inventario/reservas"))
                .respond(response().withStatusCode(409)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":409,\"mensaje\":\"Stock insuficiente para procesar la reserva\","
                                + "\"detalles\":[\"Producto ID 1 sin stock. Requerido: 5, Disponible: 2\"]}"));

        StockInsuficienteExcepcion excepcion = assertThrows(StockInsuficienteExcepcion.class,
                () -> inventarioClient.reservar(10L, List.of(new ItemReservaDTO(1L, 5))));
        assertEquals(1, excepcion.getDetalles().size());
    }

    @Test
    void testReservarConExitoEnviaLaSolicitudCorrecta() {
        mockServer.when(request().withMethod("POST").withPath("/api/v1/inventario/reservas"))
                .respond(response().withStatusCode(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]"));

        List<ItemReservaDTO> items = List.of(new ItemReservaDTO(1L, 5), new ItemReservaDTO(2L, 3));

        assertDoesNotThrow(() -> inventarioClient.reservar(10L, items));
        mockServer.verify(request().withMethod("POST").withPath("/api/v1/inventario/reservas")
                        .withBody(json("{\"pedidoId\":10,\"items\":["
                                + "{\"productoId\":1,\"cantidad\":5},{\"productoId\":2,\"cantidad\":3}]}")),
                VerificationTimes.once());
    }

    @Test
    void testReservarConProductoInexistenteLanzaProductoNoEncontradoExcepcion() {
        mockServer.when(request().withMethod("POST").withPath("/api/v1/inventario/reservas"))
                .respond(response().withStatusCode(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":404,\"mensaje\":\"Producto no encontrado con id: 1\"}"));

        ProductoNoEncontradoExcepcion excepcion = assertThrows(ProductoNoEncontradoExcepcion.class,
                () -> inventarioClient.reservar(10L, List.of(new ItemReservaDTO(1L, 1))));
        assertEquals("Producto no encontrado con id: 1", excepcion.getMessage());
    }

    @Test
    void testLiberarPorPedidoLlamaAlEndpointDeLiberacion() {
        mockServer.when(request().withMethod("PATCH").withPath("/api/v1/inventario/reservas/pedido/10/liberacion"))
                .respond(response().withStatusCode(204));

        assertDoesNotThrow(() -> inventarioClient.liberarPorPedido(10L));
        mockServer.verify(request().withMethod("PATCH").withPath("/api/v1/inventario/reservas/pedido/10/liberacion"),
                VerificationTimes.once());
    }

    @Test
    void testReservarConServicioCaidoLanzaServicioNoDisponibleExcepcion() {
        InventarioClient clienteSinServicio = new InventarioClient(RestClient.builder(), "http://localhost:1");

        ServicioNoDisponibleExcepcion excepcion = assertThrows(ServicioNoDisponibleExcepcion.class,
                () -> clienteSinServicio.reservar(10L, List.of(new ItemReservaDTO(1L, 5))));
        assertEquals("servicio-inventario", excepcion.getMessage());
    }

    @Test
    void testLiberarPorPedidoConServicioCaidoLanzaServicioNoDisponibleExcepcion() {
        InventarioClient clienteSinServicio = new InventarioClient(RestClient.builder(), "http://localhost:1");

        assertThrows(ServicioNoDisponibleExcepcion.class, () -> clienteSinServicio.liberarPorPedido(10L));
    }
}
