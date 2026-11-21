package com.tiendaonline.clientes.integracion;

import com.tiendaonline.clientes.clases.Cliente;
import com.tiendaonline.clientes.repositorio.ClienteRepositorio;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "servicios.pedidos.url=http://localhost:1083")
@AutoConfigureMockMvc
class ClientePedidosIntegracionTest {

    private static final int PUERTO_MOCK = 1083;

    private static ClientAndServer mockServer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepositorio repositorio;

    private Long clienteId;

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
        Cliente cliente = Cliente.builder()
                .nombre("Juan Perez").email("juan.integracion@mail.com")
                .fechaAlta(LocalDateTime.now()).build();
        clienteId = repositorio.save(cliente).getId();
    }

    @AfterEach
    void limpiar() {
        repositorio.deleteById(clienteId);
    }

    @Test
    void testPedidosDeUnClienteExistenteDevuelveLosPedidosDeServicioPedidos() throws Exception {
        mockServer.when(request().withMethod("GET").withPath("/api/v1/pedidos")
                        .withQueryStringParameter("clienteId", String.valueOf(clienteId)))
                .respond(response().withStatusCode(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":10,\"clienteId\":" + clienteId + ",\"estado\":\"CREADO\",\"montoTotal\":200.00,"
                                + "\"fechaCreacion\":\"2026-11-17T10:00:00\",\"fechaActualizacion\":\"2026-11-17T10:00:00\","
                                + "\"items\":[{\"id\":1,\"productoId\":3,\"cantidad\":2,\"precioUnitario\":100.00}]}]"));

        mockMvc.perform(get("/api/v1/clientes/" + clienteId + "/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].estado").value("CREADO"))
                .andExpect(jsonPath("$[0].items[0].cantidad").value(2));
    }

    @Test
    void testPedidosDeUnClienteInexistenteDevuelve404SinConsultarAServicioPedidos() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/999999/pedidos"))
                .andExpect(status().isNotFound());

        mockServer.verify(request().withPath("/api/v1/pedidos"), VerificationTimes.never());
    }

    @Test
    void testPedidosConServicioPedidosFallandoDevuelve503() throws Exception {
        mockServer.when(request().withMethod("GET").withPath("/api/v1/pedidos"))
                .respond(response().withStatusCode(500));

        mockMvc.perform(get("/api/v1/clientes/" + clienteId + "/pedidos"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }
}
