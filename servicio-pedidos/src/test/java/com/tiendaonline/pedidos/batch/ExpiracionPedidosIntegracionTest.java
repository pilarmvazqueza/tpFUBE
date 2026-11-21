package com.tiendaonline.pedidos.batch;

import com.tiendaonline.pedidos.clases.EstadoPedido;
import com.tiendaonline.pedidos.clases.Pedido;
import com.tiendaonline.pedidos.dto.ResumenExpiracionDTO;
import com.tiendaonline.pedidos.repositorio.PedidoRepositorio;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest(properties = "servicios.inventario.url=http://localhost:1082")
class ExpiracionPedidosIntegracionTest {

    private static final int PUERTO_MOCK = 1082;
    private static final String RUTA_LIBERACION = "/api/v1/inventario/reservas/pedido/%d/liberacion";
    private static final String RUTA_CUALQUIER_LIBERACION = "/api/v1/inventario/reservas/pedido/.*/liberacion";

    private static ClientAndServer mockServer;

    @Autowired
    private EjecutorExpiracion ejecutor;

    @Autowired
    private PedidoRepositorio repositorio;

    private final List<Long> idsCreados = new ArrayList<>();

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
    }

    @AfterEach
    void limpiar() {
        repositorio.deleteAllById(idsCreados);
        idsCreados.clear();
    }

    private Pedido crearPedido(EstadoPedido estado, LocalDateTime fechaCreacion) {
        Pedido pedido = Pedido.builder().clienteId(1L).estado(estado)
                .montoTotal(new BigDecimal("100.00"))
                .fechaCreacion(fechaCreacion).fechaActualizacion(fechaCreacion).build();
        Pedido guardado = repositorio.save(pedido);
        idsCreados.add(guardado.getId());
        return guardado;
    }

    private void simularLiberacionExitosa() {
        mockServer.when(request().withMethod("PATCH").withPath(RUTA_CUALQUIER_LIBERACION))
                .respond(response().withStatusCode(204));
    }

    private void verificarLiberaciones(Long pedidoId, VerificationTimes veces) {
        mockServer.verify(request().withMethod("PATCH").withPath(String.format(RUTA_LIBERACION, pedidoId)), veces);
    }

    private EstadoPedido estadoActual(Long id) {
        return repositorio.findById(id).orElseThrow().getEstado();
    }

    @Test
    void testExpiraSoloLosPedidosCreadosConMasDe24Horas() {
        simularLiberacionExitosa();
        Pedido vencido = crearPedido(EstadoPedido.CREADO, LocalDateTime.now().minusHours(25));
        Pedido reciente = crearPedido(EstadoPedido.CREADO, LocalDateTime.now().minusHours(2));
        Pedido confirmadoViejo = crearPedido(EstadoPedido.CONFIRMADO, LocalDateTime.now().minusDays(3));
        Pedido canceladoViejo = crearPedido(EstadoPedido.CANCELADO, LocalDateTime.now().minusDays(3));

        ResumenExpiracionDTO resumen = ejecutor.ejecutar();

        assertEquals("COMPLETED", resumen.getEstado());
        assertEquals(1, resumen.getLeidos());
        assertEquals(1, resumen.getExpirados());
        assertEquals(EstadoPedido.EXPIRADO, estadoActual(vencido.getId()));
        assertEquals(EstadoPedido.CREADO, estadoActual(reciente.getId()));
        assertEquals(EstadoPedido.CONFIRMADO, estadoActual(confirmadoViejo.getId()));
        assertEquals(EstadoPedido.CANCELADO, estadoActual(canceladoViejo.getId()));
    }

    @Test
    void testLiberaLaReservaSoloDelPedidoExpirado() {
        simularLiberacionExitosa();
        Pedido vencido = crearPedido(EstadoPedido.CREADO, LocalDateTime.now().minusHours(30));
        Pedido reciente = crearPedido(EstadoPedido.CREADO, LocalDateTime.now().minusHours(1));
        Pedido confirmadoViejo = crearPedido(EstadoPedido.CONFIRMADO, LocalDateTime.now().minusDays(3));

        ejecutor.ejecutar();

        verificarLiberaciones(vencido.getId(), VerificationTimes.exactly(1));
        verificarLiberaciones(reciente.getId(), VerificationTimes.never());
        verificarLiberaciones(confirmadoViejo.getId(), VerificationTimes.never());
    }

    @Test
    void testActualizaLaFechaDeActualizacionDelPedidoExpirado() {
        simularLiberacionExitosa();
        Pedido vencido = crearPedido(EstadoPedido.CREADO, LocalDateTime.now().minusHours(48));
        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        ejecutor.ejecutar();

        Pedido actualizado = repositorio.findById(vencido.getId()).orElseThrow();
        assertTrue(actualizado.getFechaActualizacion().isAfter(antes));
    }

    @Test
    void testUnaSegundaCorridaNoVuelveAExpirarNiALiberarElMismoPedido() {
        simularLiberacionExitosa();
        Pedido vencido = crearPedido(EstadoPedido.CREADO, LocalDateTime.now().minusHours(26));

        ejecutor.ejecutar();
        ResumenExpiracionDTO segunda = ejecutor.ejecutar();

        assertEquals("COMPLETED", segunda.getEstado());
        verificarLiberaciones(vencido.getId(), VerificationTimes.exactly(1));
    }

    @Test
    void testSiInventarioFallaLaCorridaTerminaFailedYElPedidoSigueCreado() {
        mockServer.when(request().withMethod("PATCH").withPath(RUTA_CUALQUIER_LIBERACION))
                .respond(response().withStatusCode(500));
        Pedido vencido = crearPedido(EstadoPedido.CREADO, LocalDateTime.now().minusHours(27));

        ResumenExpiracionDTO resumen = ejecutor.ejecutar();

        assertEquals("FAILED", resumen.getEstado());
        assertEquals(EstadoPedido.CREADO, estadoActual(vencido.getId()));
    }
}
