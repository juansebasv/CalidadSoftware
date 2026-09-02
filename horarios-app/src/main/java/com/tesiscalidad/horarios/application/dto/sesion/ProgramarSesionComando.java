package com.tesiscalidad.horarios.application.dto.sesion;

/** Datos de entrada para programar una nueva sesion de clase. */
public final class ProgramarSesionComando {

    private final Long idMateria;
    private final Long idProfesor;
    private final String grupo;
    private final String dia;
    private final int hora;
    private final int duracion;
    private final int semestre;
    private final String jornada;

    public ProgramarSesionComando(Long idMateria, Long idProfesor, String grupo, String dia,
                                  int hora, int duracion, int semestre, String jornada) {
        this.idMateria = idMateria;
        this.idProfesor = idProfesor;
        this.grupo = grupo;
        this.dia = dia;
        this.hora = hora;
        this.duracion = duracion;
        this.semestre = semestre;
        this.jornada = jornada;
    }

    public Long getIdMateria() {
        return idMateria;
    }

    public Long getIdProfesor() {
        return idProfesor;
    }

    public String getGrupo() {
        return grupo;
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

    public int getSemestre() {
        return semestre;
    }

    public String getJornada() {
        return jornada;
    }
}
