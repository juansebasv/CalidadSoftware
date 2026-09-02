package com.tesiscalidad.horarios.domain.model;

import com.tesiscalidad.horarios.domain.enums.DiaSemana;
import com.tesiscalidad.horarios.domain.enums.Jornada;
import com.tesiscalidad.horarios.domain.support.Preconditions;
import com.tesiscalidad.horarios.domain.support.ReglasHorario;
import com.tesiscalidad.horarios.domain.vo.Franja;

/** Sesion de clase programada de una materia/grupo, con docente asignado. */
public final class SesionClase {

    public static final String PATRON_GRUPO = "^[A-Z0-9]{1,3}$";

    private final Long id;
    private final Long idMateria;
    private Long idProfesor;
    private final String grupo;
    private DiaSemana dia;
    private int hora;
    private int duracion;
    private int semestre;
    private Jornada jornada;

    private SesionClase(Long id, Long idMateria, Long idProfesor, String grupo, DiaSemana dia,
                        int hora, int duracion, int semestre, Jornada jornada) {
        this.id = id;
        this.idMateria = idMateria;
        this.idProfesor = idProfesor;
        this.grupo = grupo;
        this.dia = dia;
        this.hora = hora;
        this.duracion = duracion;
        this.semestre = semestre;
        this.jornada = jornada;
    }

    public static SesionClase crear(Long idMateria, Long idProfesor, String grupo, DiaSemana dia,
                                    int hora, int duracion, int semestre, Jornada jornada) {
        String grupoNorm = Preconditions
                .coincidePatron(grupo == null ? null : grupo.trim().toUpperCase(), "grupo", PATRON_GRUPO);
        Franja.de(dia, hora, duracion); // valida dia + hora + duracion + limite
        return new SesionClase(null,
                Preconditions.requerido(idMateria, "idMateria"),
                idProfesor, grupoNorm, dia, hora, duracion,
                Preconditions.rango(semestre, "semestre", ReglasHorario.SEMESTRE_MINIMO, ReglasHorario.SEMESTRE_MAXIMO),
                Preconditions.requerido(jornada, "jornada"));
    }

    public static SesionClase reconstruir(Long id, Long idMateria, Long idProfesor, String grupo, DiaSemana dia,
                                          int hora, int duracion, int semestre, Jornada jornada) {
        Franja.de(dia, hora, duracion);
        return new SesionClase(Preconditions.requerido(id, "id"),
                Preconditions.requerido(idMateria, "idMateria"), idProfesor,
                Preconditions.coincidePatron(grupo, "grupo", PATRON_GRUPO),
                dia, hora, duracion, semestre, jornada);
    }

    public void reprogramar(DiaSemana dia, int hora, int duracion, Jornada jornada) {
        Franja.de(dia, hora, duracion);
        this.dia = dia;
        this.hora = hora;
        this.duracion = duracion;
        this.jornada = Preconditions.requerido(jornada, "jornada");
    }

    public void asignarProfesor(Long idProfesor) {
        this.idProfesor = Preconditions.requerido(idProfesor, "idProfesor");
    }

    public void quitarProfesor() {
        this.idProfesor = null;
    }

    public boolean tieneProfesor() {
        return idProfesor != null;
    }

    public Franja franja() {
        return Franja.de(dia, hora, duracion);
    }

    public Long getId() {
        return id;
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

    public DiaSemana getDia() {
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

    public Jornada getJornada() {
        return jornada;
    }
}
