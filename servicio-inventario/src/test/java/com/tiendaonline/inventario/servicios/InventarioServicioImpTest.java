package com.tiendaonline.inventario.servicios;

import com.tiendaonline.inventario.clases.EstadoReserva;
import com.tiendaonline.inventario.clases.Producto;
import com.tiendaonline.inventario.clases.Reserva;
import com.tiendaonline.inventario.dto.CrearReservasDTO;
import com.tiendaonline.inventario.dto.ItemReservaDTO;
import com.tiendaonline.inventario.dto.ReservaDTO;
import com.tiendaonline.inventario.excepciones.ProductoNoEncontradoExcepcion;
import com.tiendaonline.inventario.excepciones.StockInsuficienteExcepcion;
import com.tiendaonline.inventario.repositorio.ProductoRepositorio;
import com.tiendaonline.inventario.repositorio.ReservaRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventarioServicioImpTest {

    @Mock
    private ProductoRepositorio repoProducto;

    @Mock
    private ReservaRepositorio repoReserva;

    @InjectMocks
    private InventarioServicioImpl servicio;

    //test de consulta de stock

    @Test
    void consultarStock_conExito() {
        Producto productoMock = Producto.builder().id(1L).cantidadDisponible(10).build();
        when(repoProducto.findById(1L)).thenReturn(Optional.of(productoMock));

        Integer stock = servicio.consultarStock(1L);

        assertEquals(10, stock);
        verify(repoProducto, times(1)).findById(1L);
    }

    @Test
    void consultarStock_productoNoEncontrado_lanzaExcepcion() {
        when(repoProducto.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductoNoEncontradoExcepcion.class, () -> {
            servicio.consultarStock(1L);
        });
    }

    //tests para reservar

    //en este no tenemos stock suficiente

    @Test
    void reservar_conStockInsuficiente_lanzaExcepcionYNoGuardaNada() {
        //arrange (hay disponibles 10 y queremos 15)
        CrearReservasDTO solicitud = new CrearReservasDTO(100L, List.of(
                new ItemReservaDTO(1L, 15)
        ));
        Producto productoMock = Producto.builder().id(1L).cantidadDisponible(10).build();
        when(repoProducto.findById(1L)).thenReturn(Optional.of(productoMock));

        //act y assert
        StockInsuficienteExcepcion excepcion = assertThrows(StockInsuficienteExcepcion.class, () -> {
            servicio.reservar(solicitud);
        });

        assertFalse(excepcion.getDetalles().isEmpty());
        //assertTrue(excepcion.getDetalles().get(0).contains("no tiene stock suficiente"));
        assertTrue(excepcion.getDetalles().get(0).contains("Producto ID 1"));
        
        
        assertEquals(10, productoMock.getCantidadDisponible()); //el stock deberia permanecer con el mismo valor siempre
        verify(repoReserva, never()).save(any()); //bajo ningun contexto se deberia haber guardado una reserva
    }

    @Test
    void reservar_productoRepetidoEnSolicitud_SumaCantidadesCorrectamente() {
        //arrange (tenemos 10 y nos pidieron 6+5=11 entonces como no hay suficiente deberia fallar)
        CrearReservasDTO solicitud = new CrearReservasDTO(100L, List.of(
                new ItemReservaDTO(1L, 6),
                new ItemReservaDTO(1L, 5)
        ));
        Producto productoMock = Producto.builder().id(1L).cantidadDisponible(10).build();
        when(repoProducto.findById(1L)).thenReturn(Optional.of(productoMock));

        //act y assert que deberia fallar porque no hay stock suficiente
        assertThrows(StockInsuficienteExcepcion.class, () -> {
            servicio.reservar(solicitud);
        });

        verify(repoReserva, never()).save(any());
    }

    //tests para liberacion de reservas

    @Test
    void liberarPorPedido_restauraStockYCambiaEstado() {
        //arrange
        Long pedidoId = 100L;
        Producto productoMock = Producto.builder().id(1L).cantidadDisponible(5).build();
        Reserva reservaMock = Reserva.builder()
                .id(10L).producto(productoMock).pedidoId(pedidoId).cantidad(2).estado(EstadoReserva.ACTIVA).build();

        when(repoReserva.findByPedidoIdAndEstado(pedidoId, EstadoReserva.ACTIVA))
                .thenReturn(List.of(reservaMock));

        //act
        servicio.liberarPorPedido(pedidoId);

        //assert
        assertEquals(EstadoReserva.LIBERADA, reservaMock.getEstado());
        assertEquals(7, productoMock.getCantidadDisponible()); //es 7 porque tenemos 5 originales y 2 liberados
    }

    //test de a todo o nada, productos con stock y productos sin stock suficiente. que pasara?
    @Test
    void reservar_multiplesProductosUnoSinStock_lanzaExcepcionYNoModificaNada() {
        //arrange
        Long pedidoId = 100L;
        //prod1 con suficiente stock y prod2 con stock insuf
        CrearReservasDTO solicitud = new CrearReservasDTO(pedidoId, List.of(
                new ItemReservaDTO(1L, 2), // anda
                new ItemReservaDTO(2L, 20)  // debe fallar
        ));

        Producto prod1 = Producto.builder().id(1L).cantidadDisponible(10).build();
        Producto prod2 = Producto.builder().id(2L).cantidadDisponible(5).build();

        when(repoProducto.findById(1L)).thenReturn(Optional.of(prod1));
        when(repoProducto.findById(2L)).thenReturn(Optional.of(prod2));

        //act y assert
        StockInsuficienteExcepcion excepcion = assertThrows(StockInsuficienteExcepcion.class, () -> {
            servicio.reservar(solicitud);
        });

        //ver si aparece el error
        assertFalse(excepcion.getDetalles().isEmpty());
        assertTrue(excepcion.getDetalles().stream().anyMatch(d -> d.contains("Producto ID 2")));

        //verificamos el todo o nada, como no hay suficiente stock del prod 2 entonces el prod1 no se deberia haber tocado
        assertEquals(10, prod1.getCantidadDisponible()); 
        assertEquals(5, prod2.getCantidadDisponible());
        
        //que no se guarde ninguna reserva en la bdd!!!!
        verify(repoReserva, never()).save(any());
    }

    //test de reserva exitosa con argumentcaptor para 
    @Test
    void reservar_conStockSuficiente_creaReservasYDescuentaStock() {
        //arrange
        Long pedidoId = 100L;
        CrearReservasDTO solicitud = new CrearReservasDTO(pedidoId, List.of(
                new ItemReservaDTO(1L, 2)
        ));

        Producto productoMock = Producto.builder().id(1L).cantidadDisponible(10).build();
        when(repoProducto.findById(1L)).thenReturn(Optional.of(productoMock));

        Reserva reservaGuardada = Reserva.builder()
                .id(10L).producto(productoMock).pedidoId(pedidoId).cantidad(2).estado(EstadoReserva.ACTIVA).build();
        when(repoReserva.save(any(Reserva.class))).thenReturn(reservaGuardada);

        //act
        List<ReservaDTO> resultado = servicio.reservar(solicitud);

        //assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(8, productoMock.getCantidadDisponible()); // Verifica stock en memoria

        //usar argumentcaptor para ver exactamente que objeto fue mandado a guardar a la bdd
        org.mockito.ArgumentCaptor<Reserva> captor = org.mockito.ArgumentCaptor.forClass(Reserva.class);
        verify(repoReserva, times(1)).save(captor.capture());
        
        Reserva reservaCapturada = captor.getValue();
        assertEquals(pedidoId, reservaCapturada.getPedidoId());
        assertEquals(2, reservaCapturada.getCantidad());
        assertEquals(EstadoReserva.ACTIVA, reservaCapturada.getEstado());
    }

    //que pasa si llamo dos veces para liberar por pedido. se busca que no modifique el stock
    @Test
    void liberarPorPedido_llamadoDosVeces_esIdempotenteYNoModificaStockDeNuevo() {
        //arranque
        Long pedidoId = 100L;
        Producto productoMock = Producto.builder().id(1L).cantidadDisponible(5).build();
        Reserva reservaMock = Reserva.builder()
                .id(10L).producto(productoMock).pedidoId(pedidoId).cantidad(2).estado(EstadoReserva.ACTIVA).build();

        //en la prim llamada se devuelve la reserva activa
        //seg llamada: simula que fue liberada y por eso devuelve lista vacia
        when(repoReserva.findByPedidoIdAndEstado(pedidoId, EstadoReserva.ACTIVA))
                .thenReturn(List.of(reservaMock))
                .thenReturn(List.of());

        //act de 1era ejecucion: libera la reserva
        servicio.liberarPorPedido(pedidoId);
        assertEquals(7, productoMock.getCantidadDisponible()); // 5 + 2

        //act de segunda ejec: simulando intento del bacth o del servicio de pedidos
        servicio.liberarPorPedido(pedidoId);

        //assert: verificar que el stock no vuelva a sumarse y que quede en 7
        assertEquals(7, productoMock.getCantidadDisponible());
        assertEquals(EstadoReserva.LIBERADA, reservaMock.getEstado());
        
        //verificar que el repo de reservas se haya consultado dos veces
        verify(repoReserva, times(2)).findByPedidoIdAndEstado(pedidoId, EstadoReserva.ACTIVA);
    }

    //tenemos algun producto repetido en la solicitud y hay stock suficiente
    @Test
    void reservar_productoRepetidoQueSiAlcanza_sumaCantidadesYReservaCorrectamente() {
        //arrange hay 10 disponibles, pedimos una vez 4 y otra vez 5, deberia alcanzar
        Long pedidoId = 100L;
        CrearReservasDTO solicitud = new CrearReservasDTO(pedidoId, List.of(
                new ItemReservaDTO(1L, 4),
                new ItemReservaDTO(1L, 5)
        ));

        Producto productoMock = Producto.builder().id(1L).cantidadDisponible(10).build();
        when(repoProducto.findById(1L)).thenReturn(Optional.of(productoMock));

        Reserva reservaGuardada = Reserva.builder()
                .id(10L).producto(productoMock).pedidoId(pedidoId).cantidad(9).estado(EstadoReserva.ACTIVA).build();
        when(repoReserva.save(any(Reserva.class))).thenReturn(reservaGuardada);

        //act
        List<ReservaDTO> resultado = servicio.reservar(solicitud);

        //assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size()); //agrupando en una sola
        assertEquals(1, productoMock.getCantidadDisponible()); //10-9=1 deberia quedar 1 en memoria

        //argumentcaptor par ver que se haya guardado 9
        org.mockito.ArgumentCaptor<Reserva> captor = org.mockito.ArgumentCaptor.forClass(Reserva.class);
        verify(repoReserva, times(1)).save(captor.capture());
        
        Reserva reservaCapturada = captor.getValue();
        assertEquals(9, reservaCapturada.getCantidad()); // Valida que sumó 4 + 5 = 9
    }

    //me piden un producto que no existe
    @Test
    void reservar_productoInexistente_lanzaExcepcion() {
        CrearReservasDTO solicitud = new CrearReservasDTO(100L, List.of(
                new ItemReservaDTO(99L, 2)
        ));
        when(repoProducto.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductoNoEncontradoExcepcion.class, () -> {
            servicio.reservar(solicitud);
        });
        verify(repoReserva, never()).save(any());
    }

    //caso extremo: me piden justo el stock que tengo
    @Test
    void reservar_pedirExactamenteElStockDisponible_dejaStockEnCero() {
        Long pedidoId = 100L;
        CrearReservasDTO solicitud = new CrearReservasDTO(pedidoId, List.of(
                new ItemReservaDTO(1L, 10)
        ));

        Producto productoMock = Producto.builder().id(1L).cantidadDisponible(10).build();
        when(repoProducto.findById(1L)).thenReturn(Optional.of(productoMock));

        Reserva reservaGuardada = Reserva.builder()
                .id(10L).producto(productoMock).pedidoId(pedidoId).cantidad(10).estado(EstadoReserva.ACTIVA).build();
        when(repoReserva.save(any(Reserva.class))).thenReturn(reservaGuardada);

        List<ReservaDTO> resultado = servicio.reservar(solicitud);

        assertNotNull(resultado);
        assertEquals(0, productoMock.getCantidadDisponible()); // Queda en cero exacto
        verify(repoReserva, times(1)).save(any(Reserva.class));
    }

    //todos los productos que me piden para reservar no tienen stock
    @Test
    void reservar_todosLosProductosSinStock_listaTodosLosErroresEnDetalles() {
        Long pedidoId = 100L;
        CrearReservasDTO solicitud = new CrearReservasDTO(pedidoId, List.of(
                new ItemReservaDTO(1L, 50),
                new ItemReservaDTO(2L, 50)
        ));

        Producto prod1 = Producto.builder().id(1L).cantidadDisponible(5).build();
        Producto prod2 = Producto.builder().id(2L).cantidadDisponible(5).build();

        when(repoProducto.findById(1L)).thenReturn(Optional.of(prod1));
        when(repoProducto.findById(2L)).thenReturn(Optional.of(prod2));

        StockInsuficienteExcepcion excepcion = assertThrows(StockInsuficienteExcepcion.class, () -> {
            servicio.reservar(solicitud);
        });

        assertEquals(2, excepcion.getDetalles().size());
        assertTrue(excepcion.getDetalles().stream().anyMatch(d -> d.contains("Producto ID 1")));
        assertTrue(excepcion.getDetalles().stream().anyMatch(d -> d.contains("Producto ID 2")));
        verify(repoReserva, never()).save(any());
    }


    //liberar por pedido con multiples reservas
    @Test
    void liberarPorPedido_conMultiplesReservas_restauraStockDeCadaUna() {
        Long pedidoId = 100L;
        Producto prod1 = Producto.builder().id(1L).cantidadDisponible(5).build();
        Producto prod2 = Producto.builder().id(2L).cantidadDisponible(3).build();

        Reserva res1 = Reserva.builder().id(10L).producto(prod1).pedidoId(pedidoId).cantidad(4).estado(EstadoReserva.ACTIVA).build();
        Reserva res2 = Reserva.builder().id(11L).producto(prod2).pedidoId(pedidoId).cantidad(2).estado(EstadoReserva.ACTIVA).build();

        when(repoReserva.findByPedidoIdAndEstado(pedidoId, EstadoReserva.ACTIVA))
                .thenReturn(List.of(res1, res2));

        servicio.liberarPorPedido(pedidoId);

        assertEquals(EstadoReserva.LIBERADA, res1.getEstado());
        assertEquals(EstadoReserva.LIBERADA, res2.getEstado());
        assertEquals(9, prod1.getCantidadDisponible()); // 5 + 4
        assertEquals(5, prod2.getCantidadDisponible()); // 3 + 2
    }
}

