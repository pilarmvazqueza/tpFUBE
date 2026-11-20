package com.tiendaonline.pedidos.batch;

import com.tiendaonline.pedidos.dto.ResumenExpiracionDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EjecutorExpiracionTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job job;

    private EjecutorExpiracion crearEjecutor() {
        return new EjecutorExpiracion(jobLauncher, job);
    }

    private JobExecution armarEjecucion(BatchStatus estado) {
        JobExecution ejecucion = new JobExecution(1L);
        ejecucion.setStartTime(LocalDateTime.now().minusSeconds(2));
        ejecucion.setEndTime(LocalDateTime.now());
        ejecucion.setStatus(estado);
        return ejecucion;
    }

    @Test
    void testEjecutarDevuelveElResumenDeLaCorrida() throws Exception {
        when(jobLauncher.run(eq(job), any(JobParameters.class))).thenReturn(armarEjecucion(BatchStatus.COMPLETED));

        ResumenExpiracionDTO resumen = crearEjecutor().ejecutar();

        assertEquals("COMPLETED", resumen.getEstado());
    }

    @Test
    void testEjecutarLanzaElJobConUnParametroDeEjecucion() throws Exception {
        when(jobLauncher.run(eq(job), any(JobParameters.class))).thenReturn(armarEjecucion(BatchStatus.COMPLETED));

        crearEjecutor().ejecutar();

        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher).run(eq(job), captor.capture());
        assertNotNull(captor.getValue().getLong("ejecutadoEn"));
    }

    @Test
    void testEjecutarInformaElEstadoFailedSiLaCorridaFalla() throws Exception {
        when(jobLauncher.run(eq(job), any(JobParameters.class))).thenReturn(armarEjecucion(BatchStatus.FAILED));

        ResumenExpiracionDTO resumen = crearEjecutor().ejecutar();

        assertEquals("FAILED", resumen.getEstado());
    }

    @Test
    void testEjecutarEnvuelveLasExcepcionesDelLauncherEnIllegalStateException() throws Exception {
        when(jobLauncher.run(eq(job), any(JobParameters.class)))
                .thenThrow(new JobExecutionAlreadyRunningException("ya esta corriendo"));

        assertThrows(IllegalStateException.class, () -> crearEjecutor().ejecutar());
    }
}
