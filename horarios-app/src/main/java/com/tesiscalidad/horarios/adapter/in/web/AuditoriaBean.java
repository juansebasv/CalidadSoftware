package com.tesiscalidad.horarios.adapter.in.web;

import com.tesiscalidad.horarios.application.dto.auditoria.AuditoriaVista;
import com.tesiscalidad.horarios.application.port.in.ConsultarAuditoriaUseCase;
import com.tesiscalidad.horarios.domain.enums.AccionAuditoria;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

/** Controlador de la vista de consulta de auditoria. */
@Named("auditoriaBean")
@ViewScoped
public class AuditoriaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    ConsultarAuditoriaUseCase useCase;

    @Inject
    SesionUsuarioBean sesion;

    private PaginaLazyModel<AuditoriaVista> modelo;
    private String filtroUsuario;
    private String filtroAccion;

    @PostConstruct
    public void init() {
        this.modelo = new PaginaLazyModel<>(
                (pagina, tamano, texto) -> useCase.listar(filtroUsuario, filtroAccion, pagina, tamano,
                        sesion.contexto()));
    }

    public void aplicarFiltro() {
        modelo.setTexto(null);
    }

    public AccionAuditoria[] getAcciones() {
        return AccionAuditoria.values();
    }

    public PaginaLazyModel<AuditoriaVista> getModelo() {
        return modelo;
    }

    public String getFiltroUsuario() {
        return filtroUsuario;
    }

    public void setFiltroUsuario(String filtroUsuario) {
        this.filtroUsuario = filtroUsuario;
    }

    public String getFiltroAccion() {
        return filtroAccion;
    }

    public void setFiltroAccion(String filtroAccion) {
        this.filtroAccion = filtroAccion;
    }
}
