package com.tesiscalidad.horarios.application.dto.materia;

/** Datos de entrada para dar de alta una materia. */
public final class CrearMateriaComando {

    private final String codigo;
    private final String nombre;
    private final String tipo;
    private final int creditos;
    private final int intensidadHoraria;
    private final int semestre;

    public CrearMateriaComando(String codigo, String nombre, String tipo,
                               int creditos, int intensidadHoraria, int semestre) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.creditos = creditos;
        this.intensidadHoraria = intensidadHoraria;
        this.semestre = semestre;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
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
}
