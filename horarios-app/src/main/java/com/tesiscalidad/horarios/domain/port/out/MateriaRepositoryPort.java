package com.tesiscalidad.horarios.domain.port.out;

import com.tesiscalidad.horarios.domain.model.Materia;
import com.tesiscalidad.horarios.domain.support.Pagina;
import com.tesiscalidad.horarios.domain.support.Paginacion;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia de materias. */
public interface MateriaRepositoryPort {

    Optional<Materia> buscarPorId(Long id);

    Optional<Materia> buscarPorCodigo(String codigo);

    boolean existeCodigo(String codigo);

    Materia guardar(Materia materia);

    Pagina<Materia> listar(String texto, Integer semestre, Paginacion paginacion);

    List<Materia> listarActivas();

    void eliminar(Long id);
}
