package com.tesiscalidad.horarios.domain.port.out;

import com.tesiscalidad.horarios.domain.enums.DiaSemana;
import com.tesiscalidad.horarios.domain.model.SesionClase;
import com.tesiscalidad.horarios.domain.support.Pagina;
import com.tesiscalidad.horarios.domain.support.Paginacion;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia de las sesiones de clase programadas. */
public interface SesionClaseRepositoryPort {

    Optional<SesionClase> buscarPorId(Long id);

    boolean existeMateriaGrupoDia(Long idMateria, String grupo, DiaSemana dia, Long idExcluir);

    SesionClase guardar(SesionClase sesion);

    Pagina<SesionClase> listar(Integer semestre, Paginacion paginacion);

    List<SesionClase> listarPorProfesor(Long idProfesor);

    List<SesionClase> listarPorSemestreYJornada(Integer semestre, String jornada);

    List<SesionClase> listarPorDia(DiaSemana dia);

    List<SesionClase> listarTodas();

    void eliminar(Long id);
}
