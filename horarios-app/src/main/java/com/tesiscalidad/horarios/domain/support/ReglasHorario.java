package com.tesiscalidad.horarios.domain.support;

/** Constantes estructurales del horario academico (sin numeros magicos). */
public final class ReglasHorario {

    /** Primera hora de clase permitida (24h). Coincide con el CHECK de la BD. */
    public static final int HORA_MINIMA = 6;

    /** Ultima hora de inicio de clase permitida (24h). */
    public static final int HORA_MAXIMA = 22;

    /** Duracion minima de una sesion, en bloques. */
    public static final int DURACION_MINIMA_BLOQUES = 1;

    /** Duracion maxima de una sesion, en bloques. */
    public static final int DURACION_MAXIMA_BLOQUES = 8;

    /** Horas que ocupa un bloque academico. */
    public static final int HORAS_POR_BLOQUE = 1;

    public static final int SEMESTRE_MINIMO = 1;
    public static final int SEMESTRE_MAXIMO = 12;

    private ReglasHorario() {
    }
}
