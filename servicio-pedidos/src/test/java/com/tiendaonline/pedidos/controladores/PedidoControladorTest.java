package com.tiendaonline.pedidos.controladores;

import com.tiendaonline.pedidos.clases.EstadoPedido;
import com.tiendaonline.pedidos.dto.ItemPedidoDTO;
import com.tiendaonline.pedidos.dto.PedidoDTO;
import com.tiendaonline.pedidos.excepciones.ClienteNoEncontradoExcepcion;
import com.tiendaonline.pedidos.excepciones.PedidoNoCancelableExcepcion;
import com.tiendaonline.pedidos.excepciones.PedidoNoConfirmableExcepcion;
import com.tiendaonline.pedidos.excepciones.PedidoNoEncontradoExcepcion;
import com.tiendaonline.pedidos.excepciones.ProductoNoEncontradoExcepcion;
import com.tiendaonline.pedidos.excepciones.ServicioNoDisponibleExcepcion;
import com.tiendaonline.pedidos.excepciones.StockInsuficienteExcepcion;
import com.tiendaonline.pedidos.servicios.PedidoServicio;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoControlador.class)
class PedidoControladorTest {

    private static final String URL = "/api/v1/pedidos";
    private static final String CUERPO_VALIDO = "{\"clienteId\":1,\"items\":["
            + "{\"productoId\":1,\"cantidad\":2,\"precioUnitario\":100.00}]}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoServicio servicio;

    private PedidoDTO armarPedidoCreado() {
        return PedidoDTO.builder()
                .id(10L).clienteId(1L).estado(EstadoPedido.CREADO)
                .montoTotal(new BigDecimal("200.00"))
                .fechaCreacion(LocalDateTime.of(2026, 11, 17, 10, 0))
                .fechaActualizacion(LocalDateTime.of(2026, 11, 17, 10, 0))
                .items(List.of(new ItemPedidoDTO(1L, 1L, 2, new BigDecimal("100.00"))))
                .build();
    }

    @Test
    void testBuscarPorIdDevuelve200ConElPedido() throws Exception {
        when(servicio.buscarPorId(10L)).thenReturn(armarPedidoCreado());

        mockMvc.perform(get(URL + "/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.estado").value("CREADO"))
                .andExpect(jsonPath("$.montoTotal").value(200.00))
                .andExpect(jsonPath("$.items[0].productoId").value(1));
    }

    @Test
    void testBuscarPorIdInexistenteDevuelve404() throws Exception {
        when(servicio.buscarPorId(99L)).thenThrow(new PedidoNoEncontradoExcepcion(99L));

        mockMvc.perform(get(URL + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Pedido no encontrado con id: 99"));
    }

    @Test
    void testBuscarPorIdNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get(URL + "/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Tipo de argumento inválido: id"));
    }

    @Test
    void testCrearDevuelve201ConElPedidoCreado() throws Exception {
        when(servicio.crearPedido(any(PedidoDTO.class))).thenReturn(armarPedidoCreado());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.estado").value("CREADO"))
                .andExpect(jsonPath("$.montoTotal").value(200.00));
    }

    @Test
    void testCrearIgnoraIdEstadoYMontoEnviadosPorElCliente() throws Exception {
        when(servicio.crearPedido(any(PedidoDTO.class))).thenReturn(armarPedidoCreado());
        String cuerpo = "{\"id\":999,\"clienteId\":1,\"estado\":\"CONFIRMADO\",\"montoTotal\":1.00,"
                + "\"items\":[{\"productoId\":1,\"cantidad\":2,\"precioUnitario\":100.00}]}";

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated());

        ArgumentCaptor<PedidoDTO> captor = ArgumentCaptor.forClass(PedidoDTO.class);
        verify(servicio).crearPedido(captor.capture());
        assertNull(captor.getValue().getId());
        assertNull(captor.getValue().getEstado());
        assertNull(captor.getValue().getMontoTotal());
        assertEquals(1L, captor.getValue().getClienteId());
    }

    @Test
    void testCrearSinClienteIdDevuelve400() throws Exception {
        String cuerpo = "{\"items\":[{\"productoId\":1,\"cantidad\":2,\"precioUnitario\":100.00}]}";

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Datos inválidos"))
                .andExpect(jsonPath("$.detalles[0]").value(containsString("clienteId")));
    }

    @Test
    void testCrearSinItemsDevuelve400() throws Exception {
        String cuerpo = "{\"clienteId\":1,\"items\":[]}";

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0]").value(containsString("items")));
    }

    @Test
    void testCrearConCantidadCeroDevuelve400() throws Exception {
        String cuerpo = "{\"clienteId\":1,\"items\":[{\"productoId\":1,\"cantidad\":0,\"precioUnitario\":100.00}]}";

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0]").value(containsString("items[0].cantidad")));
    }

    @Test
    void testCrearConPrecioNegativoDevuelve400() throws Exception {
        String cuerpo = "{\"clienteId\":1,\"items\":[{\"productoId\":1,\"cantidad\":2,\"precioUnitario\":-5}]}";

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0]").value(containsString("items[0].precioUnitario")));
    }

    @Test
    void testCrearSinProductoIdEnUnItemDevuelve400() throws Exception {
        String cuerpo = "{\"clienteId\":1,\"items\":[{\"cantidad\":2,\"precioUnitario\":100.00}]}";

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0]").value(containsString("items[0].productoId")));
    }

    @Test
    void testCrearConJsonIlegibleDevuelve400() throws Exception {
        String cuerpo = "{clienteId: 1, items: [";

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Formato de solicitud inválido"));
    }

    @Test
    void testCrearConClienteInexistenteDevuelve404() throws Exception {
        when(servicio.crearPedido(any(PedidoDTO.class))).thenThrow(new ClienteNoEncontradoExcepcion(1L));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Cliente no encontrado con id: 1"));
    }

    @Test
    void testCrearConProductoInexistenteDevuelve404() throws Exception {
        when(servicio.crearPedido(any(PedidoDTO.class)))
                .thenThrow(new ProductoNoEncontradoExcepcion("Producto no encontrado con id: 1"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Producto no encontrado con id: 1"));
    }

    @Test
    void testCrearSinStockDevuelve409ConDetalles() throws Exception {
        when(servicio.crearPedido(any(PedidoDTO.class))).thenThrow(
                new StockInsuficienteExcepcion("Stock insuficiente para procesar la reserva",
                        List.of("Producto ID 1 sin stock")));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Stock insuficiente para procesar la reserva"))
                .andExpect(jsonPath("$.detalles[0]").value("Producto ID 1 sin stock"));
    }

    @Test
    void testCrearConServicioCaidoDevuelve503() throws Exception {
        when(servicio.crearPedido(any(PedidoDTO.class)))
                .thenThrow(new ServicioNoDisponibleExcepcion("servicio-inventario"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.mensaje").value("servicio-inventario"));
    }

    @Test
    void testListarPorClienteDevuelve200ConLosPedidos() throws Exception {
        when(servicio.listarPorCliente(1L)).thenReturn(List.of(armarPedidoCreado()));

        mockMvc.perform(get(URL).param("clienteId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].clienteId").value(1));
    }

    @Test
    void testListarPorClienteSinPedidosDevuelve200ConListaVacia() throws Exception {
        when(servicio.listarPorCliente(7L)).thenReturn(List.of());

        mockMvc.perform(get(URL).param("clienteId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testListarPorClienteSinParametroDevuelve400() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testListarPorClienteConIdNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get(URL).param("clienteId", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Tipo de argumento inválido: clienteId"));
    }

    @Test
    void testCancelarDevuelve200ConElPedidoCancelado() throws Exception {
        PedidoDTO cancelado = armarPedidoCreado();
        cancelado.setEstado(EstadoPedido.CANCELADO);
        when(servicio.cancelar(10L)).thenReturn(cancelado);

        mockMvc.perform(patch(URL + "/10/cancelacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.estado").value("CANCELADO"));
    }

    @Test
    void testCancelarPedidoInexistenteDevuelve404() throws Exception {
        when(servicio.cancelar(99L)).thenThrow(new PedidoNoEncontradoExcepcion(99L));

        mockMvc.perform(patch(URL + "/99/cancelacion"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Pedido no encontrado con id: 99"));
    }

    @Test
    void testCancelarPedidoNoCancelableDevuelve409() throws Exception {
        when(servicio.cancelar(10L)).thenThrow(new PedidoNoCancelableExcepcion(10L, EstadoPedido.EXPIRADO));

        mockMvc.perform(patch(URL + "/10/cancelacion"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El pedido 10 no se puede cancelar en estado EXPIRADO"));
    }

    @Test
    void testCancelarConInventarioCaidoDevuelve503() throws Exception {
        when(servicio.cancelar(10L)).thenThrow(new ServicioNoDisponibleExcepcion("servicio-inventario"));

        mockMvc.perform(patch(URL + "/10/cancelacion"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void testCancelarConIdNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(patch(URL + "/abc/cancelacion"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Tipo de argumento inválido: id"));
    }

    @Test
    void testConfirmarDevuelve200ConElPedidoConfirmado() throws Exception {
        PedidoDTO confirmado = armarPedidoCreado();
        confirmado.setEstado(EstadoPedido.CONFIRMADO);
        when(servicio.confirmar(10L)).thenReturn(confirmado);

        mockMvc.perform(patch(URL + "/10/confirmacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"));
    }

    @Test
    void testConfirmarPedidoInexistenteDevuelve404() throws Exception {
        when(servicio.confirmar(99L)).thenThrow(new PedidoNoEncontradoExcepcion(99L));

        mockMvc.perform(patch(URL + "/99/confirmacion"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Pedido no encontrado con id: 99"));
    }

    @Test
    void testConfirmarPedidoNoConfirmableDevuelve409() throws Exception {
        when(servicio.confirmar(10L)).thenThrow(new PedidoNoConfirmableExcepcion(10L, EstadoPedido.CANCELADO));

        mockMvc.perform(patch(URL + "/10/confirmacion"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El pedido 10 no se puede confirmar en estado CANCELADO"));
    }

    @Test
    void testConfirmarConIdNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(patch(URL + "/abc/confirmacion"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Tipo de argumento inválido: id"));
    }
}
