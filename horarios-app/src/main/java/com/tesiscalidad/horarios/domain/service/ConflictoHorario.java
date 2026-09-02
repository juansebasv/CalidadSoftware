package com.tesiscalidad.horarios.domain.service;

import java.util.Objects;

/** Resultado de la deteccion de cruces de horario. */
public final class ConflictoHorario {

    /** Naturaleza del cruce detectado. */
    public enum Tipo {
        DOCENTE_SOLAPADO("El docente tiene dos sesiones que se cruzan"),
        COHORTE_SOLAPADA("El grupo tiene dos sesiones que se cruzan"),
        DOCENTE_SIN_DISPONIBILIDAD("El docente no tiene disponibilidad en la franja");

        private final String descripcion;

        Tipo(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    private final Tipo tipo;
    private final Long sesionA;
    private final Long sesionB;
    private final String detalle;

    public ConflictoHorario(Tipo tipo, Long sesionA, Long sesionB, String detalle) {
        this.tipo = tipo;
        this.sesionA = sesionA;
        this.sesionB = sesionB;
        this.detalle = detalle;
    }

    public Tipo getTipo() {
        return tipo;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConflictoHorario)) {
            return false;
        }
        ConflictoHorario that = (ConflictoHorario) o;
        return tipo == that.tipo
                && Objects.equals(sesionA, that.sesionA)
                && Objects.equals(sesionB, that.sesionB);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipo, sesionA, sesionB);
    }

    @Override
    public String toString() {
        return tipo + " [" + sesionA + ", " + sesionB + "] " + detalle;
    }
}
