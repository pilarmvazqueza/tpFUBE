// package com.tiendaonline.inventario.servicios;

// import com.tiendaonline.inventario.excepciones.ProductoNoEncontradoExcepcion;

// import org.springframework.stereotype.Service;
// import com.tiendaonline.inventario.dto.*;
// import com.tiendaonline.inventario.clases.*;
// import com.tiendaonline.inventario.repositorio.*;
// import com.tiendaonline.inventario.mapper.*;
// import org.springframework.transaction.annotation.Transactional;
// import java.util.ArrayList;
// import java.util.List;




// @Service
// public class InventarioServicioImpl implements InventarioServicio {

//     private final ProductoRepositorio repoProducto;
//     private final ReservaRepositorio repoReserva;

//     public InventarioServicioImpl(ProductoRepositorio repoProducto, ReservaRepositorio repoReserva) {
//         this.repoProducto = repoProducto;
//         this.repoReserva = repoReserva;
//     }

//     @Override
//     public Integer consultarStock(Long id) {
//         Producto producto = repoProducto.findById(id)
//                 .orElseThrow(() -> new ProductoNoEncontradoExcepcion(id));
//         return producto.getCantidadDisponible();
//     }

//     @Override 
//     @Transactional 
//     public ResultadoReservaDTO reservar(CrearReservasDTO solicitud){
//         List <ProductoStockDTO> disponibles = new ArrayList<>();
//         List <ProductoStockDTO> noDisponibles = new ArrayList<>();

//         //consulta y clasificacion de los productos que han sido solicitados
//         for (ItemReservaDTO item : solicitud.getItems()){
//             Producto producto = repoProducto.findById(item.getProductoId())
//                 .orElseThrow(() -> new ProductoNoEncontradoExcepcion(item.getProductoId()));
//             //comparacion de stock, es suficiente para la reserva que se quiere realizar?
//             boolean tieneStock = producto.getCantidadDisponible() >= item.getCantidad();
//             //creamos el DTO. haya o no stock, se debe agregar a la lista que corresponda
//             ProductoStockDTO productoStockDTO = ProductoMapper.toProductoStockDTO(producto, item.getCantidad());
//             //clasificamos el producto para ser incluido en la lista que corresponda
//             if (tieneStock){
//                 disponibles.add(productoStockDTO);
//             } else {
//                 noDisponibles.add(productoStockDTO);
//             }
             

//         }
//         //si la lista de noDisponibles tiene al menos un elemento, no se puede continuar con la reserva de NINGUNO de los productos
//         //se devuelven ambas listas completas, sin modificar el stock de los productos ni crear el objeto RESERVA 
//         //en este primer caso, no se pueden realizar reservas porque la lista de noDisponibles tiene al menos un elemento
//         if (!noDisponibles.isEmpty()){
//             return ResultadoReservaDTO.builder()
//                 .reservas(new ArrayList<>())
//                 .disponibles(disponibles)
//                 .noDisponibles(noDisponibles)
//                 .build();
//         }  
//         else {
//             //ahora la lista de noDisponibles esta totalmente vacia, por lo que se puede continuar con la reserva
//             List<ReservaDTO> reservas = new ArrayList<>();

//             //obtenemos cada item de la solicitud
//             for (ItemReservaDTO item: solicitud.getItems()){

//                 //buscamos el producto en la bdd
//                 Producto producto = repoProducto.findById(item.getProductoId())
//                     .orElseThrow(() -> new ProductoNoEncontradoExcepcion(item.getProductoId()));
                
//                 //construccion de la reserva
//                 Reserva reserva = Reserva.builder()
//                     .producto(producto)
//                     .pedidoId(solicitud.getPedidoId())
//                     .cantidad(item.getCantidad())
//                     .estado(EstadoReserva.ACTIVA)
//                     .build();
                
//                 //descontamos el stock de la cantidad reservada del producto 
//                 producto.setCantidadDisponible(producto.getCantidadDisponible() - item.getCantidad());

//                 //guardamos los cambios
//                 repoProducto.save(producto);
//                 Reserva reservaGuardada = repoReserva.save(reserva);

//                 //convertimos la reserva guardada a DTO y la agregamos a la lista de reservas
//                 reservas.add(ReservaMapper.toDTO(reservaGuardada));

               
                
//             }
//              //devolvemos el resultado de la operacion completa
//                 return ResultadoReservaDTO.builder()
//                     .reservas(reservas)
//                     .disponibles(disponibles)
//                     .noDisponibles(noDisponibles)
//                     .build();
//         }  

//     }
// }

package com.tiendaonline.inventario.servicios;

import com.tiendaonline.inventario.excepciones.ProductoNoEncontradoExcepcion;
import com.tiendaonline.inventario.excepciones.StockInsuficienteExcepcion;
import org.springframework.stereotype.Service;
import com.tiendaonline.inventario.dto.*;
import com.tiendaonline.inventario.clases.*;
import com.tiendaonline.inventario.repositorio.*;
import com.tiendaonline.inventario.mapper.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventarioServicioImpl implements InventarioServicio {

    private final ProductoRepositorio repoProducto;
    private final ReservaRepositorio repoReserva;

    public InventarioServicioImpl(ProductoRepositorio repoProducto, ReservaRepositorio repoReserva) {
        this.repoProducto = repoProducto;
        this.repoReserva = repoReserva;
    }

    @Override
    public Integer consultarStock(Long id) {
        Producto producto = repoProducto.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoExcepcion(id));
        return producto.getCantidadDisponible();
    }

    @Override 
    @Transactional 
    public List<ReservaDTO> reservar(CrearReservasDTO solicitud) {
        
        // 1. Agrupar ítems repetidos y sumar sus cantidades para evitar el bug del stock duplicado
        Map<Long, Integer> cantidadesRequeridas = solicitud.getItems().stream()
                .collect(Collectors.groupingBy(ItemReservaDTO::getProductoId, 
                        Collectors.summingInt(ItemReservaDTO::getCantidad)));

        List<String> erroresStock = new ArrayList<>();
        List<Producto> productosAActualizar = new ArrayList<>();
        List<Reserva> reservasACrear = new ArrayList<>();

        // 2. Primer pase: validar todo reteniendo las entidades (sin modificar nada todavía)
        for (Map.Entry<Long, Integer> entry : cantidadesRequeridas.entrySet()) {
            Long productoId = entry.getKey();
            Integer cantidadNecesaria = entry.getValue();

            // Para concurrencia: En el repositorio, este método debería usar @Lock(LockModeType.PESSIMISTIC_WRITE)
            Producto producto = repoProducto.findById(productoId)
                    .orElseThrow(() -> new ProductoNoEncontradoExcepcion(productoId));

            if (producto.getCantidadDisponible() < cantidadNecesaria) {
                erroresStock.add("Producto ID " + productoId + " sin stock. Requerido: " + 
                        cantidadNecesaria + ", Disponible: " + producto.getCantidadDisponible());
            } else {
                productosAActualizar.add(producto);
                reservasACrear.add(Reserva.builder()
                        .producto(producto)
                        .pedidoId(solicitud.getPedidoId())
                        .cantidad(cantidadNecesaria)
                        .estado(EstadoReserva.ACTIVA)
                        .build());
            }
        }

        // 3. Si hay errores, se corta la ejecución lanzando la excepción con todos los detalles
        if (!erroresStock.isEmpty()) {
            throw new StockInsuficienteExcepcion(erroresStock);
        }

        // 4. Si hay stock de todo, modificamos las entidades y guardamos
        List<ReservaDTO> reservasConfirmadas = new ArrayList<>();
        
        for (int i = 0; i < productosAActualizar.size(); i++) {
            Producto producto = productosAActualizar.get(i);
            Reserva reserva = reservasACrear.get(i);

            // Se descuenta el stock (JPA hace el update automático por estar en @Transactional)
            producto.setCantidadDisponible(producto.getCantidadDisponible() - reserva.getCantidad());
            
            Reserva reservaGuardada = repoReserva.save(reserva);
            reservasConfirmadas.add(ReservaMapper.toDTO(reservaGuardada));
        }

        return reservasConfirmadas;
    }

    @Override
    @Transactional
    public void liberarPorPedido(Long pedidoId) {
        // Diseño idempotente: solo buscar reservas ACTIVAS
        List<Reserva> reservas = repoReserva.findByPedidoIdAndEstado(pedidoId, EstadoReserva.ACTIVA);
        
        for (Reserva reserva : reservas) {
            reserva.setEstado(EstadoReserva.LIBERADA);
            Producto producto = reserva.getProducto();
            producto.setCantidadDisponible(producto.getCantidadDisponible() + reserva.getCantidad());
            // JPA guarda automáticamente el estado y la cantidad en DB al hacer commit de la transacción
        }
    }
}