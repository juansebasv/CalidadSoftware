package com.tesiscalidad.horarios.application.port.in;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.disponibilidad.FranjaDisponibilidadVista;

import java.util.List;

/** Puerto de entrada: rejilla de disponibilidad horaria del profesor. */
public interface GestionarDisponibilidadUseCase {

    List<FranjaDisponibilidadVista> listar(Long idProfesor);

    FranjaDisponibilidadVista marcar(Long idProfesor, String dia, int hora, ContextoPeticion contexto);

    void liberar(Long idProfesor, String dia, int hora, ContextoPeticion contexto);

    int generarRejilla(Long idProfesor, List<String> dias, int horaInicio, int horaFin,
                       ContextoPeticion contexto);

    void limpiar(Long idProfesor, ContextoPeticion contexto);
}
