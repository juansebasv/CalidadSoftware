package com.tesiscalidad.horarios.domain.model;

import com.tesiscalidad.horarios.domain.support.Preconditions;

import java.util.Objects;

/** Habilitacion de un profesor para dictar una materia (relacion M:N). */
public final class Perfil {

    private final Long id;
    private final Long idProfesor;
    private final Long idMateria;

    private Perfil(Long id, Long idProfesor, Long idMateria) {
        this.id = id;
        this.idProfesor = idProfesor;
        this.idMateria = idMateria;
    }

    public static Perfil crear(Long idProfesor, Long idMateria) {
        return new Perfil(null,
                Preconditions.requerido(idProfesor, "idProfesor"),
                Preconditions.requerido(idMateria, "idMateria"));
    }

    public static Perfil reconstruir(Long id, Long idProfesor, Long idMateria) {
        return new Perfil(Preconditions.requerido(id, "id"),
                Preconditions.requerido(idProfesor, "idProfesor"),
                Preconditions.requerido(idMateria, "idMateria"));
    }

    public Long getId() {
        return id;
    }

    public Long getIdProfesor() {
        return idProfesor;
    }

    public Long getIdMateria() {
        return idMateria;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Perfil)) {
            return false;
        }
        Perfil perfil = (Perfil) o;
        return Objects.equals(idProfesor, perfil.idProfesor) && Objects.equals(idMateria, perfil.idMateria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProfesor, idMateria);
    }
}
