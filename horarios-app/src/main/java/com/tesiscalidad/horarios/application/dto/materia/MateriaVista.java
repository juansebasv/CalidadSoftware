package com.tesiscalidad.horarios.application.dto.materia;

import com.tesiscalidad.horarios.domain.model.Materia;

/** Proyeccion de lectura de una materia (salida de casos de uso y de la API). */
public final class MateriaVista {

    private final Long id;
    private final String codigo;
    private final String nombre;
    private final String tipo;
    private final String tipoEtiqueta;
    private final int creditos;
    private final int intensidadHoraria;
    private final int semestre;
    private final boolean activa;

    public MateriaVista(Long id, String codigo, String nombre, String tipo, String tipoEtiqueta,
                        int creditos, int intensidadHoraria, int semestre, boolean activa) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.tipoEtiqueta = tipoEtiqueta;
        this.creditos = creditos;
        this.intensidadHoraria = intensidadHoraria;
        this.semestre = semestre;
        this.activa = activa;
    }

    public static MateriaVista de(Materia m) {
        return new MateriaVista(m.getId(), m.getCodigo().valor(), m.getNombre(),
                m.getTipo().getCodigo(), m.getTipo().getEtiqueta(),
                m.getCreditos(), m.getIntensidadHoraria(), m.getSemestre(), m.isActiva());
    }

    public Long getId() {
        return id;
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

    public String getTipoEtiqueta() {
        return tipoEtiqueta;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MateriaVista)) {
            return false;
        }
        return id != null && id.equals(((MateriaVista) o).id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }
}
