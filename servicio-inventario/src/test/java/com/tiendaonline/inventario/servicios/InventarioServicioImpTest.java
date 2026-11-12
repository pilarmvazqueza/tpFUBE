package com.tiendaonline.inventario.servicios;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import com.tiendaonline.inventario.repositorio.ProductoRepositorio;
import org.junit.jupiter.api.Test;
import com.tiendaonline.inventario.clases.*;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.tiendaonline.inventario.excepciones.ProductoNoEncontradoExcepcion;


@ExtendWith(MockitoExtension.class)
public class InventarioServicioImpTest {

    // el repositorio falso
    @Mock ProductoRepositorio repoProductoFalso;

    //el servicio real
    @InjectMocks InventarioServicioImpl servicioReal;

    //primer test: consulta de stock de un producto que SI existe
    @Test
    public void testConsultarStockProductoExistente(){
        //primero preparacion de los datos y config de los mocks

        Producto producto = Producto.builder()
            .id(1234L)
            .nombre("Pepitos")
            .cantidadDisponible(15)
            .build();

            //a veces es un contratiempo que aparezca producto.builder cannot be resolved to a type
            //pero es un error de vscode, no de compilacion. probe el gradle de este servicio y funciona

            when(repoProductoFalso.findById(1234L)).thenReturn(Optional.of(producto));

        //ejecutar el metodo que quiero probar
        Integer stock = servicioReal.consultarStock(1234L);

        //verificacion del resultado
        assertEquals(15, stock);

        //paso el gradle :)
        
    }

    //segundo test: que pasa cuando no encuentra el producto?
    @Test 
    public void testConsultarStockProductoInexistente() {

        //configuracion del repo falso

        when(repoProductoFalso.findById(999L)).thenReturn(Optional.empty());

        //ejecutar y revisar que se lance la excepcion ProductoNoEncontradoExcepcion

        assertThrows(ProductoNoEncontradoExcepcion.class, () -> {
            servicioReal.consultarStock(999L);
        });


    }
    //este también funciona :)
    

}

