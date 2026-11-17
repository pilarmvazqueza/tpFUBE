package com.tiendaonline.inventario.controladores;

import com.tiendaonline.inventario.clases.EstadoReserva;
import com.tiendaonline.inventario.dto.CrearReservasDTO;
import com.tiendaonline.inventario.dto.ReservaDTO;
import com.tiendaonline.inventario.excepciones.ProductoNoEncontradoExcepcion;
import com.tiendaonline.inventario.excepciones.StockInsuficienteExcepcion;
import com.tiendaonline.inventario.servicios.InventarioServicio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventarioControlador.class)
public class InventarioControladorTest {

    private static final String URL_RESERVAS = "/api/v1/inventario/reservas";
    private static final String CUERPO_VALIDO =
            "{\"pedidoId\":10,\"items\":[{\"productoId\":1,\"cantidad\":5}]}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventarioServicio servicio;

    @Test
    public void testConsultarStockDevuelve200ConLaCantidad() throws Exception {
        when(servicio.consultarStock(1L)).thenReturn(15);

        mockMvc.perform(get("/api/v1/inventario/productos/1/cantidad-disponible"))
                .andExpect(status().isOk())
                .andExpect(content().string("15"));
    }

    @Test
    public void testConsultarStockDeProductoInexistenteDevuelve404() throws Exception {
        when(servicio.consultarStock(99L)).thenThrow(new ProductoNoEncontradoExcepcion(99L));

        mockMvc.perform(get("/api/v1/inventario/productos/99/cantidad-disponible"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Producto no encontrado con id: 99"));
    }

    @Test
    public void testConsultarStockConIdNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/inventario/productos/abc/cantidad-disponible"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Tipo de argumento inválido: id"));
    }

    @Test
    public void testReservarDevuelve201ConLasReservas() throws Exception {
        ReservaDTO reserva = ReservaDTO.builder().id(1L).productoId(1L).pedidoId(10L)
                .cantidad(5).estado(EstadoReserva.ACTIVA).build();
        when(servicio.reservar(any(CrearReservasDTO.class))).thenReturn(List.of(reserva));

        mockMvc.perform(post(URL_RESERVAS)
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].estado").value("ACTIVA"));
    }

    @Test
    public void testReservarSinStockDevuelve409ConDetalles() throws Exception {
        when(servicio.reservar(any(CrearReservasDTO.class))).thenThrow(
                new StockInsuficienteExcepcion(List.of("Producto ID 1 sin stock")));

        mockMvc.perform(post(URL_RESERVAS)
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Stock insuficiente para procesar la reserva"))
                .andExpect(jsonPath("$.detalles[0]").value(containsString("sin stock")));
    }

    @Test
    public void testReservarConProductoInexistenteDevuelve404() throws Exception {
        when(servicio.reservar(any(CrearReservasDTO.class)))
                .thenThrow(new ProductoNoEncontradoExcepcion(1L));

        mockMvc.perform(post(URL_RESERVAS)
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Producto no encontrado con id: 1"));
    }

    @Test
    public void testReservarConItemsVaciosDevuelve400() throws Exception {
        String cuerpo = "{\"pedidoId\":10,\"items\":[]}";

        mockMvc.perform(post(URL_RESERVAS)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0]")
                        .value(containsString("La lista de ítems no puede estar vacía")));
    }

    @Test
    public void testReservarConCantidadCeroDevuelve400() throws Exception {
        String cuerpo = "{\"pedidoId\":10,\"items\":[{\"productoId\":1,\"cantidad\":0}]}";

        mockMvc.perform(post(URL_RESERVAS)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0]")
                        .value(containsString("La cantidad debe ser mayor a cero")));
    }

    @Test
    public void testReservarSinPedidoIdDevuelve400() throws Exception {
        String cuerpo = "{\"items\":[{\"productoId\":1,\"cantidad\":5}]}";

        mockMvc.perform(post(URL_RESERVAS)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0]")
                        .value(containsString("El ID del pedido no puede ser nulo")));
    }

    @Test
    public void testReservarSinProductoIdEnUnItemDevuelve400() throws Exception {
        String cuerpo = "{\"pedidoId\":10,\"items\":[{\"cantidad\":5}]}";

        mockMvc.perform(post(URL_RESERVAS)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0]")
                        .value(containsString("El ID del producto no puede ser nulo")));
    }

    @Test
    public void testReservarConJsonIlegibleDevuelve400() throws Exception {
        String cuerpo = "{pedidoId: 10, items: [";

        mockMvc.perform(post(URL_RESERVAS)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Formato de solicitud inválido"));
    }

    @Test
    public void testLiberarPorPedidoDevuelve204YLlamaAlServicio() throws Exception {
        mockMvc.perform(patch(URL_RESERVAS + "/pedido/10/liberacion"))
                .andExpect(status().isNoContent());

        verify(servicio).liberarPorPedido(10L);
    }

    @Test
    public void testLiberarPorPedidoConIdNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(patch(URL_RESERVAS + "/pedido/abc/liberacion"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Tipo de argumento inválido: pedidoId"));
    }
}
