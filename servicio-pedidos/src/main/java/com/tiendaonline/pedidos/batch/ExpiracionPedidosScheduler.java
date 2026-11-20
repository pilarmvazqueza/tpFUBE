package com.tiendaonline.pedidos.batch;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExpiracionPedidosScheduler {

    private final EjecutorExpiracion ejecutor;

    public ExpiracionPedidosScheduler(EjecutorExpiracion ejecutor) {
        this.ejecutor = ejecutor;
    }

    @Scheduled(cron = "${pedidos.expiracion.cron}")
    public void expirarPedidos() {
        ejecutor.ejecutar();
    }
}
