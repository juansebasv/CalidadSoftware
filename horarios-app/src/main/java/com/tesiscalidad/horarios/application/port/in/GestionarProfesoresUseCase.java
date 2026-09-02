package com.tesiscalidad.horarios.application.port.in;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.profesor.ActualizarProfesorComando;
import com.tesiscalidad.horarios.application.dto.profesor.CrearProfesorComando;
import com.tesiscalidad.horarios.application.dto.profesor.ProfesorVista;
import com.tesiscalidad.horarios.domain.support.Pagina;

import java.util.List;

/** Puerto de entrada: gestion de la planta docente. */
public interface GestionarProfesoresUseCase {

    ProfesorVista crear(CrearProfesorComando comando, ContextoPeticion contexto);

    ProfesorVista actualizar(Long id, ActualizarProfesorComando comando, ContextoPeticion contexto);

    void cambiarEstado(Long id, boolean activo, ContextoPeticion contexto);

    void eliminar(Long id, ContextoPeticion contexto);

    ProfesorVista obtener(Long id);

    Pagina<ProfesorVista> listar(String texto, int pagina, int tamano);

    List<ProfesorVista> listarActivos();
}
