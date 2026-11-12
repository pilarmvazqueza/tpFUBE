package com.tiendaonline.clientes.controladores;

import com.tiendaonline.clientes.clases.Cliente;
import com.tiendaonline.clientes.dto.ClienteDTO;
import com.tiendaonline.clientes.excepciones.ClienteNoEncontradoException;
import com.tiendaonline.clientes.servicios.ClienteServicio;
import com.tiendaonline.clientes.servicios.FeatureFlagService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(ClienteControlador.class)
public class ClienteControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteServicio servicio;

    @MockBean
    private FeatureFlagService featureFlagService;

    @Test
    void testBuscarPorIdExistenteDevuelve200() throws Exception {
        ClienteDTO clienteDTO = ClienteDTO.builder()
                .id(1L)
                .nombre("Juan Perez")
                .email("juan@mail.com")
                .fechaAlta(LocalDateTime.of(2026, 11, 1, 10, 0))
                .build();

        when(servicio.buscarPorId(1L)).thenReturn(clienteDTO);

        mockMvc.perform(get("/api/v1/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan Perez"))
                .andExpect(jsonPath("$.email").value("juan@mail.com"));
    }

    @Test
    void testCrearClienteValidoDevuelve201() throws Exception {
        ClienteDTO respuesta = ClienteDTO.builder()
                .id(1L)
                .nombre("Juan Perez")
                .email("juan@mail.com")
                .fechaAlta(LocalDateTime.of(2026, 11, 1, 10, 0))
                .build();
        when(servicio.crear(any(Cliente.class))).thenReturn(respuesta);

        String cuerpo = """
            {"nombre": "Juan Perez", "email": "juan@mail.com"}
            """;

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testBuscarTodosDevuelve200ConLaLista() throws Exception {
        ClienteDTO c1 = ClienteDTO.builder()
                .id(1L)
                .nombre("Juan Perez")
                .email("juan@mail.com")
                .fechaAlta(LocalDateTime.of(2026, 11, 1, 10, 0))
                .build();

        ClienteDTO c2 = ClienteDTO.builder()
                .id(2L)
                .nombre("Maria Lopez")
                .email("maria@mail.com")
                .fechaAlta(LocalDateTime.of(2026, 11, 1, 10, 0))
                .build();

        List<ClienteDTO> clientes = List.of(c1, c2);

        when(servicio.buscarTodos()).thenReturn(clientes);

        String respuesta = """
            [
                {"id": 1, "nombre": "Juan Perez", "email": "juan@mail.com"},
                {"id": 2, "nombre": "Maria Lopez", "email": "maria@mail.com"}
            ]
            """;
        mockMvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isOk())
                .andExpect(content().json(respuesta))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Juan Perez"))
                .andExpect(jsonPath("$[1].nombre").value("Maria Lopez"));

        verify(servicio).buscarTodos();
        verifyNoMoreInteractions(servicio);
    }

    @Test
    void testActualizarClienteExistenteDevuelve200() throws Exception {
        ClienteDTO clienteActualizado = ClienteDTO.builder()
                .id(1L)
                .nombre("Juan Perez Actualizado")
                .email("juan.actualizado@mail.com")
                .build();

        when(servicio.actualizar(any(Cliente.class))).thenReturn(clienteActualizado);

        String cuerpo = """
                {"nombre": "Juan Perez Actualizado", "email": "juan.actualizado@mail.com"}
                """;

        mockMvc.perform(put("/api/v1/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan Perez Actualizado"))
                .andExpect(jsonPath("$.email").value("juan.actualizado@mail.com"));
    }

    @Test
    void testEliminarClienteExistenteDevuelve204() throws Exception {
        doNothing().when(servicio).eliminar(1L);

        mockMvc.perform(delete("/api/v1/clientes/1"))
                .andExpect(status().isNoContent());

        verify(servicio).eliminar(1L);
        verifyNoMoreInteractions(servicio);
    }

    @Test
    void testBuscarPorIdInexistenteDevuelve404() throws Exception {
        when(servicio.buscarPorId(1L)).thenThrow(new ClienteNoEncontradoException(1L));

        mockMvc.perform(get("/api/v1/clientes/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Cliente no encontrado con id: 1"))
                .andExpect(jsonPath("$.path").value("/api/v1/clientes/1"));
    }

    @Test
    void testActualizarClienteInexistenteDevuelve404() throws Exception {
        when(servicio.actualizar(any(Cliente.class))).thenThrow(new ClienteNoEncontradoException(1L));

        String cuerpo = """
                {"nombre": "Cliente Inexistente", "email": "inexistente@mail.com"}
                """;

        mockMvc.perform(put("/api/v1/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Cliente no encontrado con id: 1"))
                .andExpect(jsonPath("$.path").value("/api/v1/clientes/1"));
    }

    @Test
    void testEliminarClienteInexistenteDevuelve404() throws Exception {
        doThrow(new ClienteNoEncontradoException(1L)).when(servicio).eliminar(1L);

        mockMvc.perform(delete("/api/v1/clientes/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Cliente no encontrado con id: 1"))
                .andExpect(jsonPath("$.path").value("/api/v1/clientes/1"));
    }

    @Test
    void testCrearClienteInvalidoDevuelve400() throws Exception {
        String cuerpo = """
            {"nombre": "", "email": "no-es-un-mail"}
            """;

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Datos inválidos"))
                .andExpect(jsonPath("$.detalles.length()").value(2));

        verifyNoInteractions(servicio);
    }

    @Test
    void testCrearClienteConJsonRotoDevuelve400() throws Exception {
        String cuerpo = """
            {"nombre": "Juan Perez",}
            """;

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Formato de solicitud inválido"))
                .andExpect(jsonPath("$.detalles.length()").value(0));
    }

    @Test
    void testBuscarPorIdConIdNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Tipo de argumento inválido: id"))
                .andExpect(jsonPath("$.detalles.length()").value(0));

        verifyNoInteractions(servicio);
    }

    //Tests del Excepciones

    @Test
    void testCrearClienteConEmailDuplicadoDevuelve409() throws Exception {
        when(servicio.crear(any(Cliente.class))).thenThrow(new DataIntegrityViolationException("Email ya existente"));

        String cuerpo = """
                {"nombre": "Juan Perez", "email": "juan@mail.com"}
                """;

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensaje").value("Ya existe un cliente con ese email"))
                .andExpect(jsonPath("$.path").value("/api/v1/clientes"));
    }

    @Test
    void testRutaSinVersionDevuelve404() throws Exception {
        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/clientes/1"));

        verifyNoInteractions(servicio);
    }

    @Test
    void testMetodoNoPermitidoDevuelve405() throws Exception {
        mockMvc.perform(patch("/api/v1/clientes/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.path").value("/api/v1/clientes/1"));

        verifyNoInteractions(servicio);
    }

    @Test
    void testErrorInesperadoDevuelve500SinExponerDetalle() throws Exception {
        when(servicio.buscarPorId(1L)).thenThrow(new RuntimeException("Error Inesperado"));

        mockMvc.perform(get("/api/v1/clientes/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.mensaje").value("Error Interno del Servidor"))
                .andExpect(content().string(not(containsString("Error Inesperado"))));
    }

    @Test
    void testBuscarPorIdConMaskPiiActivoDevuelveDatosEnmascarados() throws Exception {
        ClienteDTO clienteDTO = ClienteDTO.builder()
                .id(1L)
                .nombre("Juan Perez")
                .email("juan.perez@gmail.com")
                .build();
        when(servicio.buscarPorId(1L)).thenReturn(clienteDTO);
        when(featureFlagService.isEnabled("mask-pii")).thenReturn(true);

        mockMvc.perform(get("/api/v1/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("J*** P***"))
                .andExpect(jsonPath("$.email").value("****@****.com"));

        verify(featureFlagService).isEnabled("mask-pii");
    }

    @Test
    void testBuscarPorIdConMaskPiiInactivoDevuelveDatosSinEnmascarar() throws Exception {
        ClienteDTO clienteDTO = ClienteDTO.builder()
                .id(1L)
                .nombre("Juan Perez")
                .email("juan.perez@gmail.com")
                .build();
        when(servicio.buscarPorId(1L)).thenReturn(clienteDTO);
        when(featureFlagService.isEnabled("mask-pii")).thenReturn(false);

        mockMvc.perform(get("/api/v1/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan Perez"))
                .andExpect(jsonPath("$.email").value("juan.perez@gmail.com"));

        verify(featureFlagService).isEnabled("mask-pii");
    }
}
