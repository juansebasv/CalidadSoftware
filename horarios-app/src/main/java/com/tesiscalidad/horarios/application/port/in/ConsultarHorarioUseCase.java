package com.tesiscalidad.horarios.application.port.in;

import com.tesiscalidad.horarios.application.dto.horario.ConflictoVista;
import com.tesiscalidad.horarios.application.dto.horario.TableroVista;
import com.tesiscalidad.horarios.application.dto.sesion.SesionClaseVista;

import java.util.List;

/** Puerto de entrada: consultas y agregados del horario. */
public interface ConsultarHorarioUseCase {

    List<SesionClaseVista> horarioDeProfesor(Long idProfesor);

    List<SesionClaseVista> horarioPorSemestre(int semestre, String jornada);

    List<ConflictoVista> detectarConflictos();

    TableroVista tablero();
}
