package com.tiendaonline.pedidos.integracion;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;

@Configuration
public class ClienteHttpConfig {

    @Bean
    public RestClientCustomizer restClientConJdkHttpClient() {
        return builder -> builder.requestFactory(new JdkClientHttpRequestFactory());
    }
}
