package com.tesiscalidad.horarios.application.port.in;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.materia.ActualizarMateriaComando;
import com.tesiscalidad.horarios.application.dto.materia.CrearMateriaComando;
import com.tesiscalidad.horarios.application.dto.materia.MateriaVista;
import com.tesiscalidad.horarios.domain.support.Pagina;

import java.util.List;

/** Puerto de entrada: gestion del catalogo de materias. */
public interface GestionarMateriasUseCase {

    MateriaVista crear(CrearMateriaComando comando, ContextoPeticion contexto);

    MateriaVista actualizar(Long id, ActualizarMateriaComando comando, ContextoPeticion contexto);

    void cambiarEstado(Long id, boolean activa, ContextoPeticion contexto);

    void eliminar(Long id, ContextoPeticion contexto);

    MateriaVista obtener(Long id);

    Pagina<MateriaVista> listar(String texto, Integer semestre, int pagina, int tamano);

    List<MateriaVista> listarActivas();
}
