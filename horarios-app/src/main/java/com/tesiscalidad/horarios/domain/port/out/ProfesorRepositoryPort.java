package com.tesiscalidad.horarios.domain.port.out;

import com.tesiscalidad.horarios.domain.model.Profesor;
import com.tesiscalidad.horarios.domain.support.Pagina;
import com.tesiscalidad.horarios.domain.support.Paginacion;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia de profesores. */
public interface ProfesorRepositoryPort {

    Optional<Profesor> buscarPorId(Long id);

    Optional<Profesor> buscarPorCodigo(String codigo);

    Optional<Profesor> buscarPorIdUsuario(Long idUsuario);

    boolean existeCodigo(String codigo);

    Profesor guardar(Profesor profesor);

    Pagina<Profesor> listar(String texto, Paginacion paginacion);

    List<Profesor> listarActivos();

    void eliminar(Long id);
}
