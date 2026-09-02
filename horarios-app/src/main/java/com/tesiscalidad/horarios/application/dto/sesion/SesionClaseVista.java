package com.tesiscalidad.horarios.application.dto.sesion;

import com.tesiscalidad.horarios.domain.model.SesionClase;

/** Proyeccion de lectura de una sesion de clase, con datos denormalizados. */
public final class SesionClaseVista {

    private final Long id;
    private final Long idMateria;
    private final String codigoMateria;
    private final String nombreMateria;
    private final Long idProfesor;
    private final String nombreProfesor;
    private final String grupo;
    private final String dia;
    private final String diaEtiqueta;
    private final int hora;
    private final int horaFin;
    private final int duracion;
    private final int semestre;
    private final String jornada;
    private final String jornadaEtiqueta;

    @SuppressWarnings("java:S107") // proyeccion plana: el numero de campos es intrinseco
    public SesionClaseVista(Long id, Long idMateria, String codigoMateria, String nombreMateria,
                            Long idProfesor, String nombreProfesor, String grupo, String dia,
                            String diaEtiqueta, int hora, int horaFin, int duracion, int semestre,
                            String jornada, String jornadaEtiqueta) {
        this.id = id;
        this.idMateria = idMateria;
        this.codigoMateria = codigoMateria;
        this.nombreMateria = nombreMateria;
        this.idProfesor = idProfesor;
        this.nombreProfesor = nombreProfesor;
        this.grupo = grupo;
        this.dia = dia;
        this.diaEtiqueta = diaEtiqueta;
        this.hora = hora;
        this.horaFin = horaFin;
        this.duracion = duracion;
        this.semestre = semestre;
        this.jornada = jornada;
        this.jornadaEtiqueta = jornadaEtiqueta;
    }

    public static SesionClaseVista de(SesionClase s, String codigoMateria, String nombreMateria,
                                      String nombreProfesor) {
        return new SesionClaseVista(s.getId(), s.getIdMateria(), codigoMateria, nombreMateria,
                s.getIdProfesor(), nombreProfesor, s.getGrupo(),
                s.getDia().getCodigo(), s.getDia().getEtiqueta(),
                s.getHora(), s.franja().horaFin(), s.getDuracion(), s.getSemestre(),
                s.getJornada().getCodigo(), s.getJornada().getEtiqueta());
    }

    public Long getId() {
        return id;
    }

    public Long getIdMateria() {
        return idMateria;
    }

    public String getCodigoMateria() {
        return codigoMateria;
    }

    public String getNombreMateria() {
        return nombreMateria;
    }

    public Long getIdProfesor() {
        return idProfesor;
    }

    public String getNombreProfesor() {
        return nombreProfesor;
    }

    public String getGrupo() {
        return grupo;
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

    public int getHoraFin() {
        return horaFin;
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

    public String getJornadaEtiqueta() {
        return jornadaEtiqueta;
    }
}
