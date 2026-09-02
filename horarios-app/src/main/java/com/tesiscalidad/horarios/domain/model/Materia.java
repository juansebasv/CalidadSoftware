package com.tesiscalidad.horarios.domain.model;

import com.tesiscalidad.horarios.domain.enums.TipoMateria;
import com.tesiscalidad.horarios.domain.support.Preconditions;
import com.tesiscalidad.horarios.domain.support.ReglasHorario;
import com.tesiscalidad.horarios.domain.vo.Codigo;

/** Asignatura del plan de estudios. */
public final class Materia {

    private static final int NOMBRE_MIN = 3;
    private static final int NOMBRE_MAX = 120;
    private static final int CREDITOS_MIN = 1;
    private static final int CREDITOS_MAX = 12;
    private static final int IH_MIN = 1;
    private static final int IH_MAX = 20;

    private final Long id;
    private final Codigo codigo;
    private String nombre;
    private TipoMateria tipo;
    private int creditos;
    private int intensidadHoraria;
    private int semestre;
    private boolean activa;

    private Materia(Long id, Codigo codigo, String nombre, TipoMateria tipo,
                    int creditos, int intensidadHoraria, int semestre, boolean activa) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.creditos = creditos;
        this.intensidadHoraria = intensidadHoraria;
        this.semestre = semestre;
        this.activa = activa;
    }

    public static Materia crear(String codigo, String nombre, TipoMateria tipo,
                                int creditos, int intensidadHoraria, int semestre) {
        return new Materia(null, Codigo.de(codigo),
                Preconditions.longitud(nombre, "nombre", NOMBRE_MIN, NOMBRE_MAX),
                Preconditions.requerido(tipo, "tipo"),
                Preconditions.rango(creditos, "creditos", CREDITOS_MIN, CREDITOS_MAX),
                Preconditions.rango(intensidadHoraria, "ih", IH_MIN, IH_MAX),
                Preconditions.rango(semestre, "semestre", ReglasHorario.SEMESTRE_MINIMO, ReglasHorario.SEMESTRE_MAXIMO),
                true);
    }

    public static Materia reconstruir(Long id, String codigo, String nombre, TipoMateria tipo,
                                      int creditos, int intensidadHoraria, int semestre, boolean activa) {
        return new Materia(Preconditions.requerido(id, "id"), Codigo.de(codigo), nombre, tipo,
                creditos, intensidadHoraria, semestre, activa);
    }

    public void actualizar(String nombre, TipoMateria tipo, int creditos, int intensidadHoraria, int semestre) {
        this.nombre = Preconditions.longitud(nombre, "nombre", NOMBRE_MIN, NOMBRE_MAX);
        this.tipo = Preconditions.requerido(tipo, "tipo");
        this.creditos = Preconditions.rango(creditos, "creditos", CREDITOS_MIN, CREDITOS_MAX);
        this.intensidadHoraria = Preconditions.rango(intensidadHoraria, "ih", IH_MIN, IH_MAX);
        this.semestre = Preconditions.rango(semestre, "semestre",
                ReglasHorario.SEMESTRE_MINIMO, ReglasHorario.SEMESTRE_MAXIMO);
    }

    public void activar() {
        this.activa = true;
    }

    public void desactivar() {
        this.activa = false;
    }

    public Long getId() {
        return id;
    }

    public Codigo getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoMateria getTipo() {
        return tipo;
    }

    public int getCreditos() {
        return creditos;
    }

    public int getIntensidadHoraria() {
        return intensidadHoraria;
    }

    public int getSemestre() {
        return semestre;
    }

    public boolean isActiva() {
        return activa;
    }
}
