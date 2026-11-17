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