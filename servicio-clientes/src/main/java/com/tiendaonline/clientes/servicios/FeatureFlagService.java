package com.tiendaonline.clientes.servicios;

import com.statsig.sdk.Statsig;
import com.statsig.sdk.StatsigUser;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FeatureFlagService {

    private final String serverSecret;

    public FeatureFlagService(@Value("${statsig.server-secret}") String serverSecret) {
        this.serverSecret = serverSecret;
    }

    @PostConstruct
    void inicializar() {
        if (serverSecret.isBlank()) {
            log.warn("Statsig sin clave configurada");
            return;
        }
        try {
            Statsig.initializeAsync(serverSecret).get();   // espera a bajar las reglas al arrancar
        } catch (Exception e) {
            log.error("No se pudo inicializar Statsig", e);
        }
    }

    public boolean isEnabled(String flag) {
        if (!Statsig.isInitialized()) {
            log.warn("Statsig no disponible: se asume '{}' activo", flag);
            return true;                                    // sin Statsig, enmascarar
        }
        return Statsig.checkGateSync(new StatsigUser("servicio-clientes"), flag);
    }

    @PreDestroy
    void cerrar() {
        if (Statsig.isInitialized()) {
            Statsig.shutdown();
        }
    }
}