package com.tiendaonline.pedidos.controladores;

import com.tiendaonline.pedidos.batch.EjecutorExpiracion;
import com.tiendaonline.pedidos.dto.ResumenExpiracionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        log.info("POST /api/v1/pedidos/expiraciones - Inicio de ejecución de expiraciones");
        ResumenExpiracionDTO resumen = ejecutor.ejecutar();
        log.info("POST /api/v1/pedidos/expiraciones - Ejecución finalizada correctamente");
        return new ResponseEntity<>(resumen, HttpStatus.CREATED);
    }
}
