package com.tesiscalidad.horarios.application.port.in;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.materia.MateriaVista;

import java.util.List;

/** Puerto de entrada: habilitacion de materias por profesor (perfiles). */
public interface GestionarPerfilesUseCase {

    List<MateriaVista> materiasHabilitadas(Long idProfesor);

    List<MateriaVista> materiasDisponibles(Long idProfesor);

    void habilitar(Long idProfesor, Long idMateria, ContextoPeticion contexto);

    void deshabilitar(Long idProfesor, Long idMateria, ContextoPeticion contexto);
}
