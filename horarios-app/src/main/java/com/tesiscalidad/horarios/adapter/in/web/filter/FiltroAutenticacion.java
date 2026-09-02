package com.tesiscalidad.horarios.adapter.in.web.filter;

import com.tesiscalidad.horarios.adapter.in.web.SesionUsuarioBean;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Protege las vistas bajo {@code /app/*}: exige sesion iniciada. */
@WebFilter(urlPatterns = {"/app/*"})
public class FiltroAutenticacion implements Filter {

    @Inject
    SesionUsuarioBean sesionUsuario;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (sesionUsuario != null && sesionUsuario.isAutenticado()) {
            chain.doFilter(request, response);
            return;
        }
        String destino = req.getContextPath() + "/login.xhtml";
        if (esPeticionAjax(req)) {
            resp.setHeader("X-Redirect", destino);
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            resp.sendRedirect(destino);
        }
    }

    private boolean esPeticionAjax(HttpServletRequest req) {
        return "partial/ajax".equals(req.getHeader("Faces-Request"));
    }
}
