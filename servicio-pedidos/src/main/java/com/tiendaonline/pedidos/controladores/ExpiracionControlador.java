package com.tiendaonline.pedidos.controladores;

import com.tiendaonline.pedidos.batch.EjecutorExpiracion;
import com.tiendaonline.pedidos.dto.ResumenExpiracionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@RestController
@RequestMapping("/api/v1/pedidos/expiraciones")
public class ExpiracionControlador {

    private final EjecutorExpiracion ejecutor;

    public ExpiracionControlador(EjecutorExpiracion ejecutor) {
        this.ejecutor = ejecutor;
    }

    @PostMapping
    public ResponseEntity<ResumenExpiracionDTO> expirar() {
        log.info("POST /api/v1/pedidos/expiraciones - Inicio de ejecucion de expiraciones");
        ResumenExpiracionDTO resumen = ejecutor.ejecutar();
        if ("COMPLETED".equals(resumen.getEstado())) {
            log.info("POST /api/v1/pedidos/expiraciones - Ejecucion finalizada {}", kv("estado", resumen.getEstado()));
        } else {
            log.warn("POST /api/v1/pedidos/expiraciones - Ejecucion no completada {}", kv("estado", resumen.getEstado()));
        }
        return new ResponseEntity<>(resumen, HttpStatus.CREATED);
    }
}
