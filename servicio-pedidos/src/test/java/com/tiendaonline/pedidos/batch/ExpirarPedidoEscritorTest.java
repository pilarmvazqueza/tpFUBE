package com.tiendaonline.pedidos.batch;

import com.tiendaonline.pedidos.clases.EstadoPedido;
import com.tiendaonline.pedidos.clases.Pedido;
import com.tiendaonline.pedidos.excepciones.ServicioNoDisponibleExcepcion;
import com.tiendaonline.pedidos.integracion.InventarioClient;
import com.tiendaonline.pedidos.repositorio.PedidoRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ExpirarPedidoEscritorTest {

    @Mock
    private PedidoRepositorio repositorio;

    @Mock
    private InventarioClient inventarioClient;

    @InjectMocks
    private ExpirarPedidoEscritor escritor;

    private Pedido armarPedidoExpirado(Long id) {
        LocalDateTime ahora = LocalDateTime.now();
        return Pedido.builder().id(id).clienteId(1L).estado(EstadoPedido.EXPIRADO)
                .montoTotal(new BigDecimal("100.00"))
                .fechaCreacion(ahora.minusDays(2)).fechaActualizacion(ahora).build();
    }

    @Test
    void testEscribirGuardaElChunkYLiberaLaReservaDeCadaPedido() {
        List<Pedido> pedidos = List.of(armarPedidoExpirado(1L), armarPedidoExpirado(2L));

        escritor.write(new Chunk<>(pedidos));

        verify(repositorio).saveAll(pedidos);
        verify(inventarioClient).liberarPorPedido(1L);
        verify(inventarioClient).liberarPorPedido(2L);
    }

    @Test
    void testEscribirGuardaLosPedidosAntesDeLiberarLasReservas() {
        List<Pedido> pedidos = List.of(armarPedidoExpirado(1L));

        escritor.write(new Chunk<>(pedidos));

        InOrder orden = inOrder(repositorio, inventarioClient);
        orden.verify(repositorio).saveAll(pedidos);
        orden.verify(inventarioClient).liberarPorPedido(1L);
    }

    @Test
    void testEscribirPropagaElErrorSiInventarioFallaParaQueSeRevierteElChunk() {
        List<Pedido> pedidos = List.of(armarPedidoExpirado(1L), armarPedidoExpirado(2L), armarPedidoExpirado(3L));
        doNothing().when(inventarioClient).liberarPorPedido(1L);
        doThrow(new ServicioNoDisponibleExcepcion("servicio-inventario"))
                .when(inventarioClient).liberarPorPedido(2L);

        assertThrows(ServicioNoDisponibleExcepcion.class, () -> escritor.write(new Chunk<>(pedidos)));

        verify(inventarioClient).liberarPorPedido(1L);
        verify(inventarioClient, never()).liberarPorPedido(3L);
    }

    @Test
    void testEscribirChunkVacioNoLiberaNingunaReserva() {
        escritor.write(new Chunk<>(List.of()));

        verifyNoInteractions(inventarioClient);
    }
}
