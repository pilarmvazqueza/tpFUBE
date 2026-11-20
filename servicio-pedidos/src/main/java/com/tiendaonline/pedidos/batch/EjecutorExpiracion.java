package com.tiendaonline.pedidos.batch;

import com.tiendaonline.pedidos.dto.ResumenExpiracionDTO;
import com.tiendaonline.pedidos.mapper.ResumenExpiracionMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

@Component
public class EjecutorExpiracion {

    private final JobLauncher jobLauncher;
    private final Job expirarPedidosJob;

    public EjecutorExpiracion(JobLauncher jobLauncher, Job expirarPedidosJob) {
        this.jobLauncher = jobLauncher;
        this.expirarPedidosJob = expirarPedidosJob;
    }

    public ResumenExpiracionDTO ejecutar() {
        JobParameters parametros = new JobParametersBuilder()
                .addLong("ejecutadoEn", System.currentTimeMillis())
                .toJobParameters();
        try {
            JobExecution ejecucion = jobLauncher.run(expirarPedidosJob, parametros);
            return ResumenExpiracionMapper.toDTO(ejecucion);
        } catch (JobExecutionException e) {
            throw new IllegalStateException("No se pudo lanzar el job de expiracion de pedidos", e);
        }
    }
}
