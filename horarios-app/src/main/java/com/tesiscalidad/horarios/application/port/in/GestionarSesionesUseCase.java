package com.tesiscalidad.horarios.application.port.in;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.sesion.ProgramarSesionComando;
import com.tesiscalidad.horarios.application.dto.sesion.ReprogramarSesionComando;
import com.tesiscalidad.horarios.application.dto.sesion.SesionClaseVista;
import com.tesiscalidad.horarios.domain.support.Pagina;

/** Puerto de entrada: programacion de sesiones de clase. */
public interface GestionarSesionesUseCase {

    SesionClaseVista programar(ProgramarSesionComando comando, ContextoPeticion contexto);

    SesionClaseVista reprogramar(Long id, ReprogramarSesionComando comando, ContextoPeticion contexto);

    SesionClaseVista asignarDocente(Long id, Long idProfesor, ContextoPeticion contexto);

    SesionClaseVista quitarDocente(Long id, ContextoPeticion contexto);

    void cancelar(Long id, ContextoPeticion contexto);

    SesionClaseVista obtener(Long id);

    Pagina<SesionClaseVista> listar(Integer semestre, int pagina, int tamano);
}
