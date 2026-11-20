package com.tiendaonline.pedidos.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Component
public class ResumenExpiracionListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Inicia el job {} (ejecucion {})",
                jobExecution.getJobInstance().getJobName(), jobExecution.getId());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long leidos = jobExecution.getStepExecutions()
                .stream().mapToLong(StepExecution::getReadCount)
                .sum();
        long expirados = jobExecution.getStepExecutions()
                .stream().mapToLong(StepExecution::getWriteCount)
                .sum();
        long revertidos = jobExecution.getStepExecutions()
                .stream().mapToLong(StepExecution::getRollbackCount)
                .sum();
        LocalDateTime fin = jobExecution.getEndTime() != null ? jobExecution.getEndTime() : LocalDateTime.now();
        long duracionMs = Duration.between(jobExecution.getStartTime(), fin).toMillis();

        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("Resumen de expiracion: estado={}, leidos={}, expirados={}, duracionMs={}",
                    jobExecution.getStatus(), leidos, expirados, duracionMs);
        } else {
            log.error("Resumen de expiracion: estado={}, leidos={}, expirados={}, chunksRevertidos={}, duracionMs={}, errores={}",
                    jobExecution.getStatus(), leidos, expirados, revertidos, duracionMs,
                    jobExecution.getAllFailureExceptions().stream().map(Throwable::getMessage).toList());
        }
    }
}
