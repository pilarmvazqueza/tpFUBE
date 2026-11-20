package com.tiendaonline.pedidos.batch;

import com.tiendaonline.pedidos.clases.Pedido;
import com.tiendaonline.pedidos.integracion.InventarioClient;
import com.tiendaonline.pedidos.repositorio.PedidoRepositorio;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class ExpirarPedidoEscritor implements ItemWriter<Pedido> {

    private final PedidoRepositorio repositorio;
    private final InventarioClient inventarioClient;

    public ExpirarPedidoEscritor(PedidoRepositorio repositorio, InventarioClient inventarioClient) {
        this.repositorio = repositorio;
        this.inventarioClient = inventarioClient;
    }

    @Override
    public void write(Chunk<? extends Pedido> chunk) {
        repositorio.saveAll(chunk.getItems());
        chunk.getItems().forEach(pedido -> inventarioClient.liberarPorPedido(pedido.getId()));
    }
}
