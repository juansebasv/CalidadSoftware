package com.tesiscalidad.horarios.adapter.in.web;

import com.tesiscalidad.horarios.application.port.in.AutenticacionUseCase;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

/** Acciones de sesion invocadas desde la plantilla (logout, cambio de tema). */
@Named("sesionAcciones")
@RequestScoped
public class SesionBeanAcciones {

    @Inject
    AutenticacionUseCase autenticacionUseCase;

    @Inject
    SesionUsuarioBean sesionUsuario;

    public String cerrarSesion() {
        if (sesionUsuario.isAutenticado()) {
            autenticacionUseCase.cerrarSesion(sesionUsuario.getLogin(), sesionUsuario.contexto());
        }
        sesionUsuario.cerrar();
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }
}
