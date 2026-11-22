package com.tiendaonline.inventario.servicios;

import com.tiendaonline.inventario.excepciones.ProductoNoEncontradoExcepcion;
import com.tiendaonline.inventario.excepciones.StockInsuficienteExcepcion;
import lombok.extern.slf4j.Slf4j;
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

import static net.logstash.logback.argument.StructuredArguments.kv;


@Slf4j
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
        log.debug("Consultando stock {}", kv("productoId", id));

        Producto producto = repoProducto.findById(id)
                .orElseThrow(() -> {
                    log.warn("Producto no encontrado {}", kv("productoId", id));
                    return new ProductoNoEncontradoExcepcion(id);
                });

        Integer stock = producto.getCantidadDisponible();
        log.info("Stock consultado {}",
                kv("productoId", id),
                kv("cantidadDisponible", stock));

        return stock;
    }

    @Override 
    @Transactional 
    public List<ReservaDTO> reservar(CrearReservasDTO solicitud) {

        log.info("Iniciando reserva de stock {}",
                kv("pedidoId", solicitud.getPedidoId()));


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
                    .orElseThrow(() -> {
                        log.warn("Producto no encontrado durante la reserva {}",
                                kv("productoId", productoId));
                        return new ProductoNoEncontradoExcepcion(productoId);
                    });

            if (producto.getCantidadDisponible() < cantidadNecesaria) {
                erroresStock.add("Producto ID " + productoId + " sin stock. Requerido: " + 
                        cantidadNecesaria + ", Disponible: " + producto.getCantidadDisponible());

                log.warn("Stock insuficiente {} {} {}",
                        kv("productoId", productoId),
                        kv("cantidadRequerida", cantidadNecesaria),
                        kv("cantidadDisponible", producto.getCantidadDisponible()));

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
            log.warn("No se pudo completar la reserva {} {}",
                    kv("pedidoId", solicitud.getPedidoId()),
                    kv("cantidadProductosSinStock", erroresStock.size()));

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

        log.info("Reservas creadas correctamente {} {}",
                kv("pedidoId", solicitud.getPedidoId()),
                kv("cantidadReservas", reservasConfirmadas.size()));

        return reservasConfirmadas;
    }

    @Override
    @Transactional
    public void liberarPorPedido(Long pedidoId) {
        log.info("Iniciando liberación de reservas {}", kv("pedidoId", pedidoId));

        // Diseño idempotente: solo buscar reservas ACTIVAS
        List<Reserva> reservas = repoReserva.findByPedidoIdAndEstado(pedidoId, EstadoReserva.ACTIVA);
        
        for (Reserva reserva : reservas) {
            reserva.setEstado(EstadoReserva.LIBERADA);
            Producto producto = reserva.getProducto();
            producto.setCantidadDisponible(producto.getCantidadDisponible() + reserva.getCantidad());
            // JPA guarda automáticamente el estado y la cantidad en DB al hacer commit de la transacción
        }
        log.info("Reservas liberadas {} {}",
                kv("pedidoId", pedidoId),
                kv("cantidadReservasLiberadas", reservas.size()));
    }
}