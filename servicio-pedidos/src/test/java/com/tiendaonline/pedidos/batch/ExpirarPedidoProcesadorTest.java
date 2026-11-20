package com.tiendaonline.pedidos.batch;

import com.tiendaonline.pedidos.clases.EstadoPedido;
import com.tiendaonline.pedidos.clases.Pedido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

class ExpirarPedidoProcesadorTest {

    private final ExpirarPedidoProcesador procesador = new ExpirarPedidoProcesador();

    private Pedido armarPedidoVencido() {
        LocalDateTime hace3Dias = LocalDateTime.now().minusDays(3);
        return Pedido.builder().id(7L).clienteId(1L).estado(EstadoPedido.CREADO)
                .montoTotal(new BigDecimal("150.00"))
                .fechaCreacion(hace3Dias).fechaActualizacion(hace3Dias).build();
    }

    @Test
    void testProcesarPasaElPedidoAExpirado() {
        Pedido pedido = armarPedidoVencido();

        Pedido resultado = procesador.process(pedido);

        assertEquals(EstadoPedido.EXPIRADO, resultado.getEstado());
    }

    @Test
    void testProcesarActualizaLaFechaDeActualizacionYConservaLaDeCreacion() {
        Pedido pedido = armarPedidoVencido();
        LocalDateTime creacion = pedido.getFechaCreacion();
        LocalDateTime antes = LocalDateTime.now();

        Pedido resultado = procesador.process(pedido);

        assertFalse(resultado.getFechaActualizacion().isBefore(antes));
        assertEquals(creacion, resultado.getFechaCreacion());
    }

    @Test
    void testProcesarDevuelveElMismoPedidoSinAlterarSusDemasDatos() {
        Pedido pedido = armarPedidoVencido();

        Pedido resultado = procesador.process(pedido);

        assertSame(pedido, resultado);
        assertEquals(7L, resultado.getId());
        assertEquals(1L, resultado.getClienteId());
        assertEquals(new BigDecimal("150.00"), resultado.getMontoTotal());
    }
}
