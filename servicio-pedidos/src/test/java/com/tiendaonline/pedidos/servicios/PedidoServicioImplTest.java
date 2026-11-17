package com.tiendaonline.pedidos.servicios;

import com.tiendaonline.pedidos.clases.EstadoPedido;
import com.tiendaonline.pedidos.clases.ItemPedido;
import com.tiendaonline.pedidos.clases.Pedido;
import com.tiendaonline.pedidos.dto.ItemPedidoDTO;
import com.tiendaonline.pedidos.dto.ItemReservaDTO;
import com.tiendaonline.pedidos.dto.PedidoDTO;
import com.tiendaonline.pedidos.excepciones.ClienteNoEncontradoExcepcion;
import com.tiendaonline.pedidos.excepciones.PedidoNoCancelableExcepcion;
import com.tiendaonline.pedidos.excepciones.PedidoNoEncontradoExcepcion;
import com.tiendaonline.pedidos.excepciones.ProductoNoEncontradoExcepcion;
import com.tiendaonline.pedidos.excepciones.ServicioNoDisponibleExcepcion;
import com.tiendaonline.pedidos.excepciones.StockInsuficienteExcepcion;
import com.tiendaonline.pedidos.integracion.ClientesClient;
import com.tiendaonline.pedidos.integracion.InventarioClient;
import com.tiendaonline.pedidos.repositorio.PedidoRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServicioImplTest {

    @Mock
    private PedidoRepositorio repositorio;

    @Mock
    private ClientesClient clientesClient;

    @Mock
    private InventarioClient inventarioClient;

    @InjectMocks
    private PedidoServicioImpl servicio;

    private PedidoDTO armarSolicitud() {
        return PedidoDTO.builder()
                .clienteId(1L)
                .items(List.of(
                        new ItemPedidoDTO(null, 1L, 2, new BigDecimal("100.00")),
                        new ItemPedidoDTO(null, 2L, 1, new BigDecimal("50.50"))))
                .build();
    }

    private Pedido armarPedido(Long id, EstadoPedido estado) {
        LocalDateTime hace2Dias = LocalDateTime.now().minusDays(2);
        return Pedido.builder().id(id).clienteId(1L).estado(estado)
                .montoTotal(new BigDecimal("100.00"))
                .fechaCreacion(hace2Dias).fechaActualizacion(hace2Dias).build();
    }

    private void simularGuardadoConId10() {
        when(repositorio.save(any(Pedido.class))).thenAnswer(invocacion -> {
            Pedido pedido = invocacion.getArgument(0);
            pedido.setId(10L);
            return pedido;
        });
    }

    @Test
    public void testCrearPedidoCalculaElTotalLoDejaCreadoYReservaElStock() {
        simularGuardadoConId10();

        PedidoDTO resultadoObtenido = servicio.crearPedido(armarSolicitud());

        assertEquals(10L, resultadoObtenido.getId());
        assertEquals(EstadoPedido.CREADO, resultadoObtenido.getEstado());
        assertEquals(new BigDecimal("250.50"), resultadoObtenido.getMontoTotal());
        assertEquals(2, resultadoObtenido.getItems().size());
        verify(inventarioClient).reservar(eq(10L), anyList());
    }

    @Test
    public void testCrearPedidoReservaCadaItemConSuProductoYCantidad() {
        simularGuardadoConId10();

        servicio.crearPedido(armarSolicitud());

        ArgumentCaptor<List<ItemReservaDTO>> captor = ArgumentCaptor.forClass(List.class);
        verify(inventarioClient).reservar(eq(10L), captor.capture());
        List<ItemReservaDTO> reservado = captor.getValue();
        assertEquals(2, reservado.size());
        assertEquals(1L, reservado.get(0).getProductoId());
        assertEquals(2, reservado.get(0).getCantidad());
        assertEquals(2L, reservado.get(1).getProductoId());
        assertEquals(1, reservado.get(1).getCantidad());
    }

    @Test
    public void testCrearPedidoGuardaElPedidoConEstadoFechasEItemsVinculados() {
        simularGuardadoConId10();

        servicio.crearPedido(armarSolicitud());

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(repositorio).save(captor.capture());

        Pedido guardado = captor.getValue();
        assertEquals(1L, guardado.getClienteId());
        assertEquals(EstadoPedido.CREADO, guardado.getEstado());
        assertNotNull(guardado.getFechaCreacion());
        assertEquals(guardado.getFechaCreacion(), guardado.getFechaActualizacion());
        assertEquals(2, guardado.getListaItems().size());

        for (ItemPedido item : guardado.getListaItems()) {
            assertEquals(guardado, item.getPedido());
        }
    }

    @Test
    public void testCrearPedidoFijaElEstadoCreadoAunqueElDtoTraigaOtro() {
        PedidoDTO solicitud = armarSolicitud();
        solicitud.setEstado(EstadoPedido.CONFIRMADO);
        simularGuardadoConId10();

        PedidoDTO resultadoObtenido = servicio.crearPedido(solicitud);

        assertEquals(EstadoPedido.CREADO, resultadoObtenido.getEstado());
    }

    @Test
    public void testCrearPedidoConClienteInexistenteNoGuardaNiReserva() {
        when(clientesClient.consultarPorId(1L)).thenThrow(new ClienteNoEncontradoExcepcion(1L));

        assertThrows(ClienteNoEncontradoExcepcion.class, () -> servicio.crearPedido(armarSolicitud()));
        verify(repositorio, never()).save(any());
        verifyNoInteractions(inventarioClient);
    }

    @Test
    public void testCrearPedidoConServicioClientesCaidoLanzaServicioNoDisponible() {
        when(clientesClient.consultarPorId(1L)).thenThrow(new ServicioNoDisponibleExcepcion("servicio-clientes"));

        assertThrows(ServicioNoDisponibleExcepcion.class, () -> servicio.crearPedido(armarSolicitud()));
        verify(repositorio, never()).save(any());
        verifyNoInteractions(inventarioClient);
    }

    @Test
    public void testCrearPedidoSinStockPropagaLaExcepcionConSusDetalles() {
        simularGuardadoConId10();
        doThrow(new StockInsuficienteExcepcion("Stock insuficiente", List.of("Producto ID 1 sin stock")))
                .when(inventarioClient).reservar(anyLong(), anyList());

        StockInsuficienteExcepcion excepcion = assertThrows(StockInsuficienteExcepcion.class,
                () -> servicio.crearPedido(armarSolicitud()));

        assertEquals(List.of("Producto ID 1 sin stock"), excepcion.getDetalles());
    }

    @Test
    public void testCrearPedidoConProductoInexistenteEnInventarioPropagaLaExcepcion() {
        simularGuardadoConId10();
        doThrow(new ProductoNoEncontradoExcepcion("Producto no encontrado con id: 2"))
                .when(inventarioClient).reservar(anyLong(), anyList());

        assertThrows(ProductoNoEncontradoExcepcion.class, () -> servicio.crearPedido(armarSolicitud()));
    }

    @Test
    public void testCrearPedidoConServicioInventarioCaidoLanzaServicioNoDisponible() {
        simularGuardadoConId10();
        doThrow(new ServicioNoDisponibleExcepcion("servicio-inventario"))
                .when(inventarioClient).reservar(anyLong(), anyList());

        assertThrows(ServicioNoDisponibleExcepcion.class, () -> servicio.crearPedido(armarSolicitud()));
    }

    @Test
    public void testBuscarPorIdExistenteDevuelveElPedido() {
        Pedido pedido = Pedido.builder().id(5L).clienteId(1L).estado(EstadoPedido.CREADO)
                .montoTotal(new BigDecimal("100.00"))
                .fechaCreacion(LocalDateTime.now()).fechaActualizacion(LocalDateTime.now())
                .build();
        when(repositorio.findById(5L)).thenReturn(Optional.of(pedido));

        PedidoDTO resultadoObtenido = servicio.buscarPorId(5L);

        assertEquals(5L, resultadoObtenido.getId());
        assertEquals(EstadoPedido.CREADO, resultadoObtenido.getEstado());
    }

    @Test
    public void testBuscarPorIdInexistenteLanzaPedidoNoEncontradoExcepcion() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        PedidoNoEncontradoExcepcion excepcion = assertThrows(PedidoNoEncontradoExcepcion.class,
                () -> servicio.buscarPorId(99L));

        assertEquals("Pedido no encontrado con id: 99", excepcion.getMessage());
    }

    @Test
    void testListarPorClienteDevuelveLosPedidosDelCliente() {
        Pedido pedidoNuevo = Pedido.builder().id(2L).clienteId(1L).estado(EstadoPedido.CREADO)
                .montoTotal(new BigDecimal("50.00"))
                .fechaCreacion(LocalDateTime.now()).fechaActualizacion(LocalDateTime.now()).build();
        Pedido pedidoViejo = Pedido.builder().id(1L).clienteId(1L).estado(EstadoPedido.CANCELADO)
                .montoTotal(new BigDecimal("80.00"))
                .fechaCreacion(LocalDateTime.now().minusDays(1)).fechaActualizacion(LocalDateTime.now()).build();
        when(repositorio.findByClienteIdOrderByFechaCreacionDesc(1L))
                .thenReturn(List.of(pedidoNuevo, pedidoViejo));

        List<PedidoDTO> resultadoObtenido = servicio.listarPorCliente(1L);

        assertEquals(2, resultadoObtenido.size());
        assertEquals(2L, resultadoObtenido.get(0).getId());
        assertEquals(EstadoPedido.CANCELADO, resultadoObtenido.get(1).getEstado());
    }

    @Test
    void testListarPorClienteSinPedidosDevuelveListaVacia() {
        when(repositorio.findByClienteIdOrderByFechaCreacionDesc(7L)).thenReturn(List.of());

        List<PedidoDTO> resultadoObtenido = servicio.listarPorCliente(7L);

        assertEquals(0, resultadoObtenido.size());
        verifyNoInteractions(clientesClient, inventarioClient);
    }

    @Test
    void testCancelarPedidoCreadoLoCancelaYLiberaLaReserva() {
        Pedido pedido = armarPedido(5L, EstadoPedido.CREADO);
        when(repositorio.findById(5L)).thenReturn(Optional.of(pedido));

        PedidoDTO resultadoObtenido = servicio.cancelar(5L);

        assertEquals(EstadoPedido.CANCELADO, resultadoObtenido.getEstado());
        assertTrue(pedido.getFechaActualizacion().isAfter(pedido.getFechaCreacion()));
        verify(inventarioClient).liberarPorPedido(5L);
    }

    @Test
    void testCancelarPedidoConfirmadoLoCancelaYLiberaLaReserva() {
        when(repositorio.findById(5L)).thenReturn(Optional.of(armarPedido(5L, EstadoPedido.CONFIRMADO)));

        PedidoDTO resultadoObtenido = servicio.cancelar(5L);

        assertEquals(EstadoPedido.CANCELADO, resultadoObtenido.getEstado());
        verify(inventarioClient).liberarPorPedido(5L);
    }

    @Test
    void testCancelarPedidoYaCanceladoLanzaPedidoNoCancelableExcepcion() {
        when(repositorio.findById(5L)).thenReturn(Optional.of(armarPedido(5L, EstadoPedido.CANCELADO)));

        PedidoNoCancelableExcepcion excepcion = assertThrows(PedidoNoCancelableExcepcion.class,
                () -> servicio.cancelar(5L));

        assertEquals("El pedido 5 no se puede cancelar en estado CANCELADO", excepcion.getMessage());
        verifyNoInteractions(inventarioClient);
    }

    @Test
    void testCancelarPedidoExpiradoLanzaPedidoNoCancelableExcepcion() {
        when(repositorio.findById(5L)).thenReturn(Optional.of(armarPedido(5L, EstadoPedido.EXPIRADO)));

        assertThrows(PedidoNoCancelableExcepcion.class, () -> servicio.cancelar(5L));
        verifyNoInteractions(inventarioClient);
    }

    @Test
    void testCancelarPedidoInexistenteLanzaPedidoNoEncontradoExcepcion() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PedidoNoEncontradoExcepcion.class, () -> servicio.cancelar(99L));
        verifyNoInteractions(inventarioClient);
    }

    @Test
    void testCancelarConInventarioCaidoPropagaServicioNoDisponible() {
        when(repositorio.findById(5L)).thenReturn(Optional.of(armarPedido(5L, EstadoPedido.CREADO)));
        doThrow(new ServicioNoDisponibleExcepcion("servicio-inventario"))
                .when(inventarioClient).liberarPorPedido(5L);

        assertThrows(ServicioNoDisponibleExcepcion.class, () -> servicio.cancelar(5L));
    }
}
