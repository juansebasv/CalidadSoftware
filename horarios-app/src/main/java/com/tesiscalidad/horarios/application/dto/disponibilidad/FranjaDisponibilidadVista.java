package com.tesiscalidad.horarios.application.dto.disponibilidad;

import com.tesiscalidad.horarios.domain.model.DisponibilidadProfesor;

/** Proyeccion de una celda de la rejilla de disponibilidad docente. */
public final class FranjaDisponibilidadVista {

    private final Long id;
    private final Long idProfesor;
    private final String dia;
    private final String diaEtiqueta;
    private final int hora;
    private final String estado;
    private final String estadoEtiqueta;
    private final String origen;

    public FranjaDisponibilidadVista(Long id, Long idProfesor, String dia, String diaEtiqueta, int hora,
                                     String estado, String estadoEtiqueta, String origen) {
        this.id = id;
        this.idProfesor = idProfesor;
        this.dia = dia;
        this.diaEtiqueta = diaEtiqueta;
        this.hora = hora;
        this.estado = estado;
        this.estadoEtiqueta = estadoEtiqueta;
        this.origen = origen;
    }

    public static FranjaDisponibilidadVista de(DisponibilidadProfesor d) {
        return new FranjaDisponibilidadVista(d.getId(), d.getIdProfesor(),
                d.getDia().getCodigo(), d.getDia().getEtiqueta(), d.getHora(),
                d.getEstado().getCodigo(), d.getEstado().getEtiqueta(), d.getOrigen().getEtiqueta());
    }

    public Long getId() {
        return id;
    }

    public Long getIdProfesor() {
        return idProfesor;
    }

    public String getDia() {
        return dia;
    }

    public String getDiaEtiqueta() {
        return diaEtiqueta;
    }

    public int getHora() {
        return hora;
    }

    public String getEstado() {
        return estado;
    }

    public String getEstadoEtiqueta() {
        return estadoEtiqueta;
    }

    public String getOrigen() {
        return origen;
    }
}
