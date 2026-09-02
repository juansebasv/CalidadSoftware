package com.tesiscalidad.horarios.application.dto.materia;

/** Datos de entrada para modificar una materia existente. */
public final class ActualizarMateriaComando {

    private final String nombre;
    private final String tipo;
    private final int creditos;
    private final int intensidadHoraria;
    private final int semestre;

    public ActualizarMateriaComando(String nombre, String tipo,
                                    int creditos, int intensidadHoraria, int semestre) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.creditos = creditos;
        this.intensidadHoraria = intensidadHoraria;
        this.semestre = semestre;
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
