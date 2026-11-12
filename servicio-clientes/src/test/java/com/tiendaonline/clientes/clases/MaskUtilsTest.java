package com.tiendaonline.clientes.clases;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MaskUtilsTest {

    @Test
    void testMaskNombreDejaSoloLaInicialDeCadaPalabra() {
        String nombre = "Juan Perez";

        String resultadoObtenido = MaskUtils.maskNombre(nombre);

        assertEquals("J*** P***", resultadoObtenido);
    }

    @Test
    void testMaskNombreConUnaSolaPalabra() {
        String nombre = "Juan";

        String resultadoObtenido = MaskUtils.maskNombre(nombre);

        assertEquals("J***", resultadoObtenido);
    }

    @Test
    void testMaskNombreIgnoraLosEspaciosSobrantes() {
        String nombre = "  Juan   Perez  ";

        String resultadoObtenido = MaskUtils.maskNombre(nombre);

        assertEquals("J*** P***", resultadoObtenido);
    }

    @Test
    void testMaskNombreNullDevuelveNull() {
        assertNull(MaskUtils.maskNombre(null));
    }

    @Test
    void testMaskNombreVacioDevuelveElMismoValor() {
        assertEquals("", MaskUtils.maskNombre(""));
        assertEquals("   ", MaskUtils.maskNombre("   "));
    }

    @Test
    void testMaskEmailOcultaUsuarioYDominioYConservaLaExtension() {
        String email = "juan.perez@gmail.com";

        String resultadoObtenido = MaskUtils.maskEmail(email);

        assertEquals("****@****.com", resultadoObtenido);
    }

    @Test
    void testMaskEmailConDominioDeVariasExtensionesConservaLaUltima() {
        String email = "ana@dominio.com.ar";

        String resultadoObtenido = MaskUtils.maskEmail(email);

        assertEquals("****@****.ar", resultadoObtenido);
    }

    @Test
    void testMaskEmailConDominioSinPuntoNoAgregaExtension() {
        String email = "ana@localhost";

        String resultadoObtenido = MaskUtils.maskEmail(email);

        assertEquals("****@****", resultadoObtenido);
    }

    @Test
    void testMaskEmailSinArrobaSeDevuelveTalCual() {
        String email = "no-es-un-email";

        String resultadoObtenido = MaskUtils.maskEmail(email);

        assertEquals("no-es-un-email", resultadoObtenido);
    }

    @Test
    void testMaskEmailNullDevuelveNull() {
        assertNull(MaskUtils.maskEmail(null));
    }
}
