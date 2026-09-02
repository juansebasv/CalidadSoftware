package com.tesiscalidad.horarios.adapter.in.web.filter;

import org.slf4j.MDC;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Anade cabeceras de seguridad a todas las respuestas y coloca el correlationId
 * en el MDC de logging durante la peticion.
 */
@WebFilter(urlPatterns = {"/*"})
public class FiltroSeguridadYTrazas implements Filter {

    private static final String CID = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String correlationId = req.getHeader("X-Correlation-Id");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put(CID, correlationId);

        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "SAMEORIGIN");
        resp.setHeader("Referrer-Policy", "same-origin");
        resp.setHeader("X-Correlation-Id", correlationId);
        resp.setHeader("Content-Security-Policy",
                "default-src 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline'; "
                        + "script-src 'self' 'unsafe-inline' 'unsafe-eval'; font-src 'self' data:");

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
