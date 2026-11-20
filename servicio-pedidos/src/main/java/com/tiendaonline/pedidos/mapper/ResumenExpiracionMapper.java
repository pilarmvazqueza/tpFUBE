package com.tiendaonline.pedidos.mapper;

import com.tiendaonline.pedidos.dto.ResumenExpiracionDTO;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

import java.time.Duration;
import java.time.LocalDateTime;

public class ResumenExpiracionMapper {

    private ResumenExpiracionMapper() {
    }

    public static ResumenExpiracionDTO toDTO(JobExecution ejecucion) {
        long leidos = ejecucion.getStepExecutions().stream().mapToLong(StepExecution::getReadCount).sum();
        long expirados = ejecucion.getStepExecutions().stream().mapToLong(StepExecution::getWriteCount).sum();
        long revertidos = ejecucion.getStepExecutions().stream().mapToLong(StepExecution::getRollbackCount).sum();
        LocalDateTime fin = ejecucion.getEndTime() != null ? ejecucion.getEndTime() : LocalDateTime.now();
        long duracionMs = Duration.between(ejecucion.getStartTime(), fin).toMillis();

        return new ResumenExpiracionDTO(ejecucion.getStatus().name(), leidos, expirados, revertidos,
                ejecucion.getStartTime(), fin, duracionMs);
    }
}
