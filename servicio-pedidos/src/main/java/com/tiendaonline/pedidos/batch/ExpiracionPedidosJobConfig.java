package com.tiendaonline.pedidos.batch;

import com.tiendaonline.pedidos.clases.EstadoPedido;
import com.tiendaonline.pedidos.clases.Pedido;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Map;

@Configuration
@EnableScheduling
public class ExpiracionPedidosJobConfig {

    private static final int TAMANIO_CHUNK = 10;
    private static final int HORAS_PARA_EXPIRAR = 24;

    @Bean
    @StepScope
    public JpaCursorItemReader<Pedido> lectorPedidosVencidos(EntityManagerFactory entityManagerFactory) {
        return new JpaCursorItemReaderBuilder<Pedido>()
                .name("lectorPedidosVencidos")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select p from Pedido p where p.estado = :estado and p.fechaCreacion < :limite order by p.id")
                .parameterValues(Map.of(
                        "estado", EstadoPedido.CREADO,
                        "limite", LocalDateTime.now().minusHours(HORAS_PARA_EXPIRAR)))
                .build();
    }

    @Bean
    public Step pasoExpirarPedidos(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   JpaCursorItemReader<Pedido> lectorPedidosVencidos,
                                   ExpirarPedidoProcesador procesador,
                                   ExpirarPedidoEscritor escritor) {
        return new StepBuilder("pasoExpirarPedidos", jobRepository)
                .<Pedido, Pedido>chunk(TAMANIO_CHUNK, transactionManager)
                .reader(lectorPedidosVencidos)
                .processor(procesador)
                .writer(escritor)
                .build();
    }

    @Bean
    public Job expirarPedidosJob(JobRepository jobRepository, Step pasoExpirarPedidos, ResumenExpiracionListener listener) {
        return new JobBuilder("expirarPedidosJob", jobRepository)
                .listener(listener)
                .start(pasoExpirarPedidos)
                .build();
    }
}
