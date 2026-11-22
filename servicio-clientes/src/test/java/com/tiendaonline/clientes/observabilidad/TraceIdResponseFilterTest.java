package com.tiendaonline.clientes.observabilidad;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TraceIdResponseFilterTest {

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final MockFilterChain cadena = new MockFilterChain();

    @SuppressWarnings("unchecked")
    private ObjectProvider<Tracer> proveedorCon(Tracer tracer) {
        ObjectProvider<Tracer> proveedor = mock(ObjectProvider.class);
        when(proveedor.getIfAvailable()).thenReturn(tracer);
        return proveedor;
    }

    @Test
    void testAgregaElTraceIdEnElHeaderDeLaRespuesta() throws Exception {
        TraceContext contexto = mock(TraceContext.class);
        when(contexto.traceId()).thenReturn("6af2539f8ece8232a1b2c3d4e5f60718");
        Span span = mock(Span.class);
        when(span.context()).thenReturn(contexto);
        Tracer tracer = mock(Tracer.class);
        when(tracer.currentSpan()).thenReturn(span);

        new TraceIdResponseFilter(proveedorCon(tracer)).doFilter(request, response, cadena);

        assertEquals("6af2539f8ece8232a1b2c3d4e5f60718", response.getHeader("X-Trace-Id"));
    }

    @Test
    void testSinSpanActivoNoAgregaElHeader() throws Exception {
        Tracer tracer = mock(Tracer.class);
        when(tracer.currentSpan()).thenReturn(null);

        new TraceIdResponseFilter(proveedorCon(tracer)).doFilter(request, response, cadena);

        assertNull(response.getHeader("X-Trace-Id"));
    }

    @Test
    void testSinTracerDisponibleNoAgregaElHeader() throws Exception {
        new TraceIdResponseFilter(proveedorCon(null)).doFilter(request, response, cadena);

        assertNull(response.getHeader("X-Trace-Id"));
    }

    @Test
    void testContinuaLaCadenaDeFiltros() throws Exception {
        new TraceIdResponseFilter(proveedorCon(null)).doFilter(request, response, cadena);

        assertNotNull(cadena.getRequest());
    }
}
