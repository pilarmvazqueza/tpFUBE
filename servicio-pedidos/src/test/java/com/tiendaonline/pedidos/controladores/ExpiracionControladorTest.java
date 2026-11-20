package com.tiendaonline.pedidos.controladores;

import com.tiendaonline.pedidos.batch.EjecutorExpiracion;
import com.tiendaonline.pedidos.dto.ResumenExpiracionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpiracionControlador.class)
class ExpiracionControladorTest {

    private static final String URL = "/api/v1/pedidos/expiraciones";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EjecutorExpiracion ejecutor;

    private ResumenExpiracionDTO armarResumen(String estado) {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 19, 2, 0, 0);
        return new ResumenExpiracionDTO(estado, 5, 4, 1, inicio, inicio.plusSeconds(2), 2000);
    }

    @Test
    void testExpirarDevuelve201ConElResumenDeLaCorrida() throws Exception {
        when(ejecutor.ejecutar()).thenReturn(armarResumen("COMPLETED"));

        mockMvc.perform(post(URL))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("COMPLETED"))
                .andExpect(jsonPath("$.leidos").value(5))
                .andExpect(jsonPath("$.expirados").value(4))
                .andExpect(jsonPath("$.chunksRevertidos").value(1))
                .andExpect(jsonPath("$.duracionMs").value(2000));

        verify(ejecutor).ejecutar();
    }

    @Test
    void testExpirarConCorridaFallidaDevuelve201ConElEstadoFailed() throws Exception {
        when(ejecutor.ejecutar()).thenReturn(armarResumen("FAILED"));

        mockMvc.perform(post(URL))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("FAILED"));
    }

    @Test
    void testExpirarSiNoSePudoLanzarElJobDevuelve500() throws Exception {
        when(ejecutor.ejecutar()).thenThrow(new IllegalStateException("No se pudo lanzar el job"));

        mockMvc.perform(post(URL))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void testExpirarConMetodoNoPermitidoDevuelve405() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isMethodNotAllowed());
    }
}
