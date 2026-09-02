package com.tesiscalidad.horarios.adapter.in.web;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.auth.SesionAutenticada;
import com.tesiscalidad.horarios.domain.enums.RolUsuario;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.UUID;

/**
 * Estado de la sesion del usuario en la aplicacion web. Mantiene la identidad
 * autenticada, el tema visual elegido y construye el {@link ContextoPeticion}
 * que se pasa a los casos de uso.
 */
@Named("sesionUsuarioBean")
@SessionScoped
public class SesionUsuarioBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String TEMA_POR_DEFECTO = "nova-light";

    private SesionAutenticada sesion;
    private String tema = TEMA_POR_DEFECTO;

    public boolean isAutenticado() {
        return sesion != null;
    }

    /** Accion de navegacion para index.xhtml. */
    public String paginaInicial() {
        return isAutenticado()
                ? "/app/dashboard.xhtml?faces-redirect=true"
                : "/login.xhtml?faces-redirect=true";
    }

    /** Accion de navegacion para login.xhtml: salta al tablero si ya hay sesion. */
    public String irAlTableroSiAutenticado() {
        return isAutenticado() ? "/app/dashboard.xhtml?faces-redirect=true" : null;
    }

    public void establecerSesion(SesionAutenticada sesion) {
        this.sesion = sesion;
    }

    public void cerrar() {
        this.sesion = null;
    }

    public SesionAutenticada getSesion() {
        return sesion;
    }

    public String getNombre() {
        return sesion == null ? "" : sesion.getNombre();
    }

    public String getLogin() {
        return sesion == null ? "" : sesion.getLogin();
    }

    public String getRol() {
        return sesion == null ? "" : sesion.getRol().getCodigo();
    }

    public boolean isPuedeGestionar() {
        return sesion != null && sesion.getRol().puedeGestionar();
    }

    public boolean isPuedeAdministrarUsuarios() {
        return sesion != null && sesion.getRol().puedeAdministrarUsuarios();
    }

    public boolean tieneRol(String rol) {
        return sesion != null && sesion.getRol() == RolUsuario.desdeCodigo(rol);
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema == null || tema.isBlank() ? TEMA_POR_DEFECTO : tema;
    }

    /** Contexto para los casos de uso (actor + ip + correlationId). */
    public ContextoPeticion contexto() {
        String ip = "desconocida";
        String correlationId = UUID.randomUUID().toString();
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null) {
            Object req = fc.getExternalContext().getRequest();
            if (req instanceof HttpServletRequest) {
                ip = ((HttpServletRequest) req).getRemoteAddr();
            }
        }
        if (sesion == null) {
            return ContextoPeticion.anonimo(ip, correlationId);
        }
        return new ContextoPeticion(sesion.getLogin(), sesion.getRol(), ip, correlationId);
    }
}
