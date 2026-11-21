package com.tiendaonline.clientes.integracion;

import com.tiendaonline.clientes.dto.PedidoDTO;
import com.tiendaonline.clientes.excepciones.ServicioNoDisponibleException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class PedidosClientTest {

    private static final int PUERTO_MOCK = 1083;

    private static ClientAndServer mockServer;
    private PedidosClient pedidosClient;

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
        pedidosClient = new PedidosClient(RestClient.builder(), "http://localhost:" + PUERTO_MOCK);
    }

    @Test
    void testListarPorClienteDevuelveLosPedidosDelCliente() {
        mockServer.when(request().withMethod("GET").withPath("/api/v1/pedidos")
                        .withQueryStringParameter("clienteId", "5"))
                .respond(response().withStatusCode(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":10,\"clienteId\":5,\"estado\":\"CREADO\",\"montoTotal\":200.00,"
                                + "\"fechaCreacion\":\"2026-11-17T10:00:00\",\"fechaActualizacion\":\"2026-11-17T10:00:00\","
                                + "\"items\":[{\"id\":1,\"productoId\":3,\"cantidad\":2,\"precioUnitario\":100.00}]}]"));

        List<PedidoDTO> pedidos = pedidosClient.listarPorCliente(5L);

        assertEquals(1, pedidos.size());
        assertEquals(10L, pedidos.get(0).getId());
        assertEquals("CREADO", pedidos.get(0).getEstado());
        assertEquals(new BigDecimal("200.00"), pedidos.get(0).getMontoTotal());
        assertEquals(1, pedidos.get(0).getItems().size());
        assertEquals(3L, pedidos.get(0).getItems().get(0).getProductoId());
    }

    @Test
    void testListarPorClienteSinPedidosDevuelveListaVacia() {
        mockServer.when(request().withMethod("GET").withPath("/api/v1/pedidos")
                        .withQueryStringParameter("clienteId", "5"))
                .respond(response().withStatusCode(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]"));

        List<PedidoDTO> pedidos = pedidosClient.listarPorCliente(5L);

        assertTrue(pedidos.isEmpty());
    }

    @Test
    void testListarPorClienteConErrorDelServidorLanzaServicioNoDisponibleException() {
        mockServer.when(request().withMethod("GET").withPath("/api/v1/pedidos"))
                .respond(response().withStatusCode(500));

        assertThrows(ServicioNoDisponibleException.class, () -> pedidosClient.listarPorCliente(5L));
    }

    @Test
    void testListarPorClienteConServicioCaidoLanzaServicioNoDisponibleException() {
        PedidosClient clienteSinServidor = new PedidosClient(RestClient.builder(), "http://localhost:1");

        assertThrows(ServicioNoDisponibleException.class, () -> clienteSinServidor.listarPorCliente(5L));
    }
}
