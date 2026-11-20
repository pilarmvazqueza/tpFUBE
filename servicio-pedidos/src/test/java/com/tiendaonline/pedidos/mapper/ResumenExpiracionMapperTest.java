package com.tiendaonline.pedidos.mapper;

import com.tiendaonline.pedidos.dto.ResumenExpiracionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ResumenExpiracionMapperTest {

    private JobExecution armarEjecucion(BatchStatus estado, LocalDateTime inicio, LocalDateTime fin) {
        JobExecution ejecucion = new JobExecution(1L);
        ejecucion.setStatus(estado);
        ejecucion.setStartTime(inicio);
        ejecucion.setEndTime(fin);
        return ejecucion;
    }

    @Test
    void testToDTOSumaLosContadoresDeLosSteps() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 19, 2, 0, 0);
        JobExecution ejecucion = armarEjecucion(BatchStatus.COMPLETED, inicio, inicio.plusSeconds(3));
        StepExecution paso = ejecucion.createStepExecution("pasoExpirarPedidos");
        paso.setReadCount(12);
        paso.setWriteCount(10);
        paso.setRollbackCount(1);

        ResumenExpiracionDTO resumen = ResumenExpiracionMapper.toDTO(ejecucion);

        assertEquals("COMPLETED", resumen.getEstado());
        assertEquals(12, resumen.getLeidos());
        assertEquals(10, resumen.getExpirados());
        assertEquals(1, resumen.getChunksRevertidos());
    }

    @Test
    void testToDTOCalculaLaDuracionEnMilisegundos() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 19, 2, 0, 0);
        JobExecution ejecucion = armarEjecucion(BatchStatus.COMPLETED, inicio, inicio.plusSeconds(3));

        ResumenExpiracionDTO resumen = ResumenExpiracionMapper.toDTO(ejecucion);

        assertEquals(3000, resumen.getDuracionMs());
        assertEquals(inicio, resumen.getInicio());
        assertEquals(inicio.plusSeconds(3), resumen.getFin());
    }

    @Test
    void testToDTOSinStepsDevuelveContadoresEnCero() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 19, 2, 0, 0);
        JobExecution ejecucion = armarEjecucion(BatchStatus.FAILED, inicio, inicio.plusSeconds(1));

        ResumenExpiracionDTO resumen = ResumenExpiracionMapper.toDTO(ejecucion);

        assertEquals("FAILED", resumen.getEstado());
        assertEquals(0, resumen.getLeidos());
        assertEquals(0, resumen.getExpirados());
    }

    @Test
    void testToDTOSinFechaDeFinUsaLaHoraActual() {
        JobExecution ejecucion = armarEjecucion(BatchStatus.STARTED, LocalDateTime.now().minusSeconds(1), null);

        ResumenExpiracionDTO resumen = ResumenExpiracionMapper.toDTO(ejecucion);

        assertNotNull(resumen.getFin());
    }
}
