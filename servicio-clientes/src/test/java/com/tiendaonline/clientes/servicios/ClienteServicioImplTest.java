package com.tiendaonline.clientes.servicios;

import com.tiendaonline.clientes.clases.Cliente;
import com.tiendaonline.clientes.dto.ClienteDTO;
import com.tiendaonline.clientes.dto.PedidoDTO;
import com.tiendaonline.clientes.excepciones.ClienteNoEncontradoException;
import com.tiendaonline.clientes.excepciones.ServicioNoDisponibleException;
import com.tiendaonline.clientes.integracion.PedidosClient;
import com.tiendaonline.clientes.repositorio.ClienteRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ClienteServicioImplTest {

    @Mock
    private ClienteRepositorio repositorio;

    @Mock
    private PedidosClient pedidosClient;

    @InjectMocks
    private ClienteServicioImpl servicio;

    @Test
    void testCrearAsignaFechaAltaYGuardaElCliente(){
        Cliente cliente = Cliente.builder()
                .nombre("Juan Perez")
                .email("juan@mail.com")
                .build();

        Cliente clienteGuardado = Cliente.builder()
                .id(1L)
                .nombre("Juan Perez")
                .email("juan@mail.com")
                .fechaAlta(LocalDateTime.of(2026,11,1,10,0))
                .build();

        when(repositorio.save(any(Cliente.class))).thenReturn(clienteGuardado);

        ClienteDTO resultado = servicio.crear(cliente);

        assertEquals(clienteGuardado.getId(), resultado.getId());
        assertEquals(cliente.getNombre(), resultado.getNombre());
        assertEquals(cliente.getEmail(), resultado.getEmail());
        assertNotNull(cliente.getFechaAlta());
        assertEquals(clienteGuardado.getFechaAlta(), resultado.getFechaAlta());

        verify(repositorio).save(cliente);
    }

    @Test
    void testBuscarPorIdExistenteDevuelveClienteDTO(){
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombre("Juan Perez")
                .email("juan@mail.com")
                .fechaAlta(LocalDateTime.of(2026,11,1,10,0))
                .build();

        when(repositorio.findById(1L)).thenReturn(Optional.of(cliente));

        ClienteDTO clienteDTOEcontrado = servicio.buscarPorId(1L);

        assertEquals(cliente.getId(), clienteDTOEcontrado.getId());
        assertEquals(cliente.getNombre(), clienteDTOEcontrado.getNombre());
        assertEquals(cliente.getEmail(), clienteDTOEcontrado.getEmail());
        assertEquals(cliente.getFechaAlta(), clienteDTOEcontrado.getFechaAlta());
        verify(repositorio).findById(1L);
    }

    @Test
    void testBuscarTodosDevuelveListaDeClientesDTO(){
        Cliente c1 = Cliente.builder()
                .id(1L)
                .nombre("Juan Perez")
                .email("juan@mail.com")
                .fechaAlta(LocalDateTime.of(2026,11,1,10,0))
                .build();

        Cliente c2 = Cliente.builder()
                .id(2L)
                .nombre("Maria Garcia")
                .email("maria@mail.com")
                .fechaAlta(LocalDateTime.of(2026,11,1,10,0))
                .build();

        List<Cliente> clientes = Arrays.asList(c1,c2);

        when(repositorio.findAll()).thenReturn(clientes);

        List<ClienteDTO> resultado = servicio.buscarTodos();

        assertEquals(2, resultado.size());

        assertEquals(c1.getId(), resultado.get(0).getId());
        assertEquals(c1.getNombre(), resultado.get(0).getNombre());
        assertEquals(c1.getEmail(), resultado.get(0).getEmail());
        assertEquals(c1.getFechaAlta(), resultado.get(0).getFechaAlta());

        assertEquals(c2.getId(), resultado.get(1).getId());
        assertEquals(c2.getNombre(), resultado.get(1).getNombre());
        assertEquals(c2.getEmail(), resultado.get(1).getEmail());
        assertEquals(c2.getFechaAlta(), resultado.get(1).getFechaAlta());

        verify(repositorio).findAll();
    }

    @Test
    void testActualizarClienteConservaLaFechaAltaDelCliente() {
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombre("Juan Perez")
                .email("juan@mail.com")
                .fechaAlta(LocalDateTime.of(2026, 11, 1, 10, 0))
                .build();

        Cliente clienteActualizado = Cliente.builder()
                .id(1L)
                .nombre("Juan Perez Actualizado")
                .email("juan.actualizado@mail.com")
                .build();

        when(repositorio.findById(1L)).thenReturn(Optional.of(cliente));

        when(repositorio.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClienteDTO resultado = servicio.actualizar(clienteActualizado);
        assertEquals(clienteActualizado.getNombre(), resultado.getNombre());
        assertEquals(clienteActualizado.getEmail(), resultado.getEmail());
        assertEquals(cliente.getFechaAlta(), resultado.getFechaAlta());

        verify(repositorio).findById(1L);
        verify(repositorio).save(any(Cliente.class));
    }

    @Test
    void testEliminarExistenteBorra(){
        when(repositorio.existsById(1L)).thenReturn(true);
        servicio.eliminar(1L);
        verify(repositorio).deleteById(1L);
    }

    //Test de Excepciones

    @Test
    void testBuscarPorIdNoExistenteLanzaExcepcion(){
        when(repositorio.findById(1L)).thenReturn(Optional.empty());

        ClienteNoEncontradoException excepcion = assertThrows(
                ClienteNoEncontradoException.class,
                () -> servicio.buscarPorId(1L)
        );

        assertEquals("Cliente no encontrado con id: 1", excepcion.getMessage());
        verify(repositorio).findById(1L);
    }

    @Test
    void testActualizarInexistenteLanzaExcepcionYNoGuarda() {
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombre("Juan Perez")
                .email("juan@mail.com")
                .fechaAlta(LocalDateTime.of(2026, 11, 1, 10, 0))
                .build();

        when(repositorio.findById(1L)).thenReturn(Optional.empty());

        ClienteNoEncontradoException excepcion = assertThrows(
                ClienteNoEncontradoException.class,
                () -> servicio.actualizar(cliente)
        );

        assertEquals("Cliente no encontrado con id: 1", excepcion.getMessage());
        verify(repositorio, never()).save(any(Cliente.class));
    }

    @Test
    void testEliminarInexistenteLanzaExcepcionYNoBorra(){
        when(repositorio.existsById(1L)).thenReturn(false);

        ClienteNoEncontradoException excepcion = assertThrows(
                ClienteNoEncontradoException.class,
                () -> servicio.eliminar(1L)
        );
        assertEquals("Cliente no encontrado con id: 1", excepcion.getMessage());
        verify(repositorio, never()).deleteById(1L);
    }

    @Test
    void testListarPedidosDeClienteExistenteDevuelveLosPedidosDeServicioPedidos(){
        Cliente cliente = Cliente.builder().id(1L).nombre("Juan Perez").email("juan@mail.com").build();
        List<PedidoDTO> pedidos = List.of(PedidoDTO.builder().id(10L).clienteId(1L).estado("CREADO").build());
        when(repositorio.findById(1L)).thenReturn(Optional.of(cliente));
        when(pedidosClient.listarPorCliente(1L)).thenReturn(pedidos);

        List<PedidoDTO> resultado = servicio.listarPedidos(1L);

        assertEquals(pedidos, resultado);
        verify(pedidosClient).listarPorCliente(1L);
    }

    @Test
    void testListarPedidosDeClienteSinPedidosDevuelveListaVacia(){
        Cliente cliente = Cliente.builder().id(1L).nombre("Juan Perez").email("juan@mail.com").build();
        when(repositorio.findById(1L)).thenReturn(Optional.of(cliente));
        when(pedidosClient.listarPorCliente(1L)).thenReturn(List.of());

        List<PedidoDTO> resultado = servicio.listarPedidos(1L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void testListarPedidosDeClienteInexistenteLanzaExcepcionYNoConsultaAPedidos(){
        when(repositorio.findById(1L)).thenReturn(Optional.empty());

        ClienteNoEncontradoException excepcion = assertThrows(
                ClienteNoEncontradoException.class,
                () -> servicio.listarPedidos(1L)
        );

        assertEquals("Cliente no encontrado con id: 1", excepcion.getMessage());
        verifyNoInteractions(pedidosClient);
    }

    @Test
    void testListarPedidosPropagaElErrorSiServicioPedidosNoEstaDisponible(){
        Cliente cliente = Cliente.builder().id(1L).nombre("Juan Perez").email("juan@mail.com").build();
        when(repositorio.findById(1L)).thenReturn(Optional.of(cliente));
        when(pedidosClient.listarPorCliente(1L)).thenThrow(new ServicioNoDisponibleException("servicio-pedidos"));

        assertThrows(ServicioNoDisponibleException.class, () -> servicio.listarPedidos(1L));
    }
}
