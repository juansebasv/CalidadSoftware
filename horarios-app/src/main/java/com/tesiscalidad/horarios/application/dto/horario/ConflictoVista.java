package com.tesiscalidad.horarios.application.dto.horario;

import com.tesiscalidad.horarios.domain.service.ConflictoHorario;

/** Proyeccion de un cruce de horario para la pantalla de validacion. */
public final class ConflictoVista {

    private final String tipo;
    private final String tipoEtiqueta;
    private final Long sesionA;
    private final Long sesionB;
    private final String detalle;

    public ConflictoVista(String tipo, String tipoEtiqueta, Long sesionA, Long sesionB, String detalle) {
        this.tipo = tipo;
        this.tipoEtiqueta = tipoEtiqueta;
        this.sesionA = sesionA;
        this.sesionB = sesionB;
        this.detalle = detalle;
    }

    public static ConflictoVista de(ConflictoHorario c) {
        return new ConflictoVista(c.getTipo().name(), c.getTipo().getDescripcion(),
                c.getSesionA(), c.getSesionB(), c.getDetalle());
    }

    public String getTipo() {
        return tipo;
    }

    public String getTipoEtiqueta() {
        return tipoEtiqueta;
    }

    public Long getSesionA() {
        return sesionA;
    }

    public Long getSesionB() {
        return sesionB;
    }

    public String getDetalle() {
        return detalle;
    }
}
