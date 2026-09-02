package com.tesiscalidad.horarios.domain.vo;

import com.tesiscalidad.horarios.domain.enums.DiaSemana;
import com.tesiscalidad.horarios.domain.support.Preconditions;
import com.tesiscalidad.horarios.domain.support.ReglasHorario;

import java.util.Objects;

/**
 * Bloque de tiempo en la rejilla semanal: dia + hora de inicio + duracion.
 * Contiene la logica de solapamiento usada por el detector de conflictos.
 */
public final class Franja {

    private final DiaSemana dia;
    private final int horaInicio;
    private final int duracionBloques;

    private Franja(DiaSemana dia, int horaInicio, int duracionBloques) {
        this.dia = dia;
        this.horaInicio = horaInicio;
        this.duracionBloques = duracionBloques;
    }

    public static Franja de(DiaSemana dia, int horaInicio, int duracionBloques) {
        Preconditions.requerido(dia, "dia");
        Preconditions.rango(horaInicio, "hora", ReglasHorario.HORA_MINIMA, ReglasHorario.HORA_MAXIMA);
        Preconditions.rango(duracionBloques, "duracion",
                ReglasHorario.DURACION_MINIMA_BLOQUES, ReglasHorario.DURACION_MAXIMA_BLOQUES);
        int horaFin = horaInicio + duracionBloques * ReglasHorario.HORAS_POR_BLOQUE;
        Preconditions.cumple(horaFin <= ReglasHorario.HORA_MAXIMA + 1,
                "La sesion se extiende mas alla del horario permitido.");
        return new Franja(dia, horaInicio, duracionBloques);
    }

    /** Franja puntual de un bloque (disponibilidad del profesor). */
    public static Franja puntual(DiaSemana dia, int hora) {
        return de(dia, hora, ReglasHorario.DURACION_MINIMA_BLOQUES);
    }

    public DiaSemana dia() {
        return dia;
    }

    public int horaInicio() {
        return horaInicio;
    }

    public int horaFin() {
        return horaInicio + duracionBloques * ReglasHorario.HORAS_POR_BLOQUE;
    }

    public int duracionBloques() {
        return duracionBloques;
    }

    public boolean mismoDia(Franja otra) {
        return otra != null && this.dia == otra.dia;
    }

    /** {@code true} si comparten dia y sus intervalos horarios se intersectan. */
    public boolean seSolapaCon(Franja otra) {
        if (!mismoDia(otra)) {
            return false;
        }
        return this.horaInicio < otra.horaFin() && otra.horaInicio < this.horaFin();
    }

    /** {@code true} si la franja cubre por completo una hora concreta del dia. */
    public boolean cubreHora(DiaSemana diaConsulta, int hora) {
        return this.dia == diaConsulta && hora >= horaInicio && hora < horaFin();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Franja)) {
            return false;
        }
        Franja franja = (Franja) o;
        return horaInicio == franja.horaInicio
                && duracionBloques == franja.duracionBloques
                && dia == franja.dia;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dia, horaInicio, duracionBloques);
    }

    @Override
    public String toString() {
        return dia.getEtiqueta() + " " + String.format("%02d:00-%02d:00", horaInicio, horaFin());
    }
}
