package com.tesiscalidad.horarios.application.dto.sesion;

/** Datos de entrada para cambiar dia / hora / duracion / jornada de una sesion. */
public final class ReprogramarSesionComando {

    private final String dia;
    private final int hora;
    private final int duracion;
    private final String jornada;

    public ReprogramarSesionComando(String dia, int hora, int duracion, String jornada) {
        this.dia = dia;
        this.hora = hora;
        this.duracion = duracion;
        this.jornada = jornada;
    }

    public String getDia() {
        return dia;
    }

    public int getHora() {
        return hora;
    }

    public int getDuracion() {
        return duracion;
    }

    public String getJornada() {
        return jornada;
    }
}
