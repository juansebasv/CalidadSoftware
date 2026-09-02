package com.tesiscalidad.horarios.domain.port.out;

import com.tesiscalidad.horarios.domain.model.Perfil;

import java.util.List;

/** Puerto de persistencia de perfiles (habilitacion profesor-materia). */
public interface PerfilRepositoryPort {

    List<Perfil> listarPorProfesor(Long idProfesor);

    List<Perfil> listarPorMateria(Long idMateria);

    boolean existe(Long idProfesor, Long idMateria);

    Perfil guardar(Perfil perfil);

    void eliminar(Long idProfesor, Long idMateria);

    long contarPorMateria(Long idMateria);
}
