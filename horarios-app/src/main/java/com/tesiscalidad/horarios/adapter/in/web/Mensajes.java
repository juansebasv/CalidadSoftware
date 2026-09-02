package com.tesiscalidad.horarios.adapter.in.web;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/** Utilidad para emitir mensajes de PrimeFaces (growl / messages). */
public final class Mensajes {

    private Mensajes() {
    }

    public static void exito(String resumen, String detalle) {
        emitir(FacesMessage.SEVERITY_INFO, resumen, detalle);
    }

    public static void advertencia(String resumen, String detalle) {
        emitir(FacesMessage.SEVERITY_WARN, resumen, detalle);
    }

    public static void error(String resumen, String detalle) {
        emitir(FacesMessage.SEVERITY_ERROR, resumen, detalle);
    }

    private static void emitir(FacesMessage.Severity severidad, String resumen, String detalle) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null) {
            fc.addMessage(null, new FacesMessage(severidad, resumen, detalle));
        }
    }
}
