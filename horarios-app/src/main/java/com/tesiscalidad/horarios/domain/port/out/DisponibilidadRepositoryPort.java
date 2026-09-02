package com.tesiscalidad.horarios.domain.port.out;

import com.tesiscalidad.horarios.domain.enums.DiaSemana;
import com.tesiscalidad.horarios.domain.model.DisponibilidadProfesor;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia de la rejilla de disponibilidad docente. */
public interface DisponibilidadRepositoryPort {

    List<DisponibilidadProfesor> listarPorProfesor(Long idProfesor);

    Optional<DisponibilidadProfesor> buscar(Long idProfesor, DiaSemana dia, int hora);

    DisponibilidadProfesor guardar(DisponibilidadProfesor disponibilidad);

    void guardarLote(List<DisponibilidadProfesor> disponibilidades);

    void eliminar(Long id);

    void eliminarPorProfesor(Long idProfesor);
}
