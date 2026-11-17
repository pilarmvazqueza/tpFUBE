package com.tiendaonline.pedidos.integracion;

import com.tiendaonline.pedidos.dto.ClienteDTO;
import com.tiendaonline.pedidos.excepciones.ClienteNoEncontradoExcepcion;
import com.tiendaonline.pedidos.excepciones.ServicioNoDisponibleExcepcion;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class ClientesClientTest {

    private static final int PUERTO_MOCK = 1081;

    private static ClientAndServer mockServer;
    private ClientesClient clientesClient;

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
        clientesClient = new ClientesClient(RestClient.builder(), "http://localhost:" + PUERTO_MOCK);
    }

    @Test
    void testConsultarPorIdExistenteDevuelveElCliente() {
        mockServer.when(request().withMethod("GET").withPath("/api/v1/clientes/1"))
                .respond(response().withStatusCode(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"nombre\":\"Juan Perez\",\"email\":\"juan@mail.com\"}"));

        ClienteDTO resultadoObtenido = clientesClient.consultarPorId(1L);

        assertEquals(1L, resultadoObtenido.getId());
    }

    @Test
    void testConsultarPorIdInexistenteLanzaClienteNoEncontradoException(){
        mockServer.when(request().withMethod("GET").withPath("/api/v1/clientes/1"))
                .respond(response().withStatusCode(404));

        assertThrows(ClienteNoEncontradoExcepcion.class,
                () -> clientesClient.consultarPorId(1L));
    }

    @Test
    void testConsultarPorIdConServicioCaidoLanzaServicioNoDisponibleExcepcion() {
        ClientesClient clienteSinServicio = new ClientesClient(RestClient.builder(), "http://localhost:1");

        assertThrows(ServicioNoDisponibleExcepcion.class,
                () -> clienteSinServicio.consultarPorId(1L));
    }
}
