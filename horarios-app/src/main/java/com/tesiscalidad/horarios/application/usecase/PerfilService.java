package com.tesiscalidad.horarios.application.usecase;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.materia.MateriaVista;
import com.tesiscalidad.horarios.application.port.in.GestionarPerfilesUseCase;
import com.tesiscalidad.horarios.application.support.Autorizador;
import com.tesiscalidad.horarios.application.support.TrazaAuditoria;
import com.tesiscalidad.horarios.domain.enums.AccionAuditoria;
import com.tesiscalidad.horarios.domain.exception.ConflictoDatosException;
import com.tesiscalidad.horarios.domain.exception.RecursoNoEncontradoException;
import com.tesiscalidad.horarios.domain.model.Materia;
import com.tesiscalidad.horarios.domain.model.Perfil;
import com.tesiscalidad.horarios.domain.port.out.MateriaRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.PerfilRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.ProfesorRepositoryPort;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Implementacion del caso de uso de gestion de perfiles profesor-materia. */
@ApplicationScoped
public class PerfilService implements GestionarPerfilesUseCase {

    private static final String ENTIDAD = "PERFIL";

    private final PerfilRepositoryPort perfilRepositorio;
    private final ProfesorRepositoryPort profesorRepositorio;
    private final MateriaRepositoryPort materiaRepositorio;
    private final Autorizador autorizador;
    private final TrazaAuditoria auditoria;

    protected PerfilService() {
        this(null, null, null, null, null);
    }

    @Inject
    public PerfilService(PerfilRepositoryPort perfilRepositorio, ProfesorRepositoryPort profesorRepositorio,
                         MateriaRepositoryPort materiaRepositorio, Autorizador autorizador,
                         TrazaAuditoria auditoria) {
        this.perfilRepositorio = perfilRepositorio;
        this.profesorRepositorio = profesorRepositorio;
        this.materiaRepositorio = materiaRepositorio;
        this.autorizador = autorizador;
        this.auditoria = auditoria;
    }

    @Override
    public List<MateriaVista> materiasHabilitadas(Long idProfesor) {
        exigeProfesor(idProfesor);
        Set<Long> idsHabilitadas = perfilRepositorio.listarPorProfesor(idProfesor).stream()
                .map(Perfil::getIdMateria).collect(Collectors.toSet());
        return materiaRepositorio.listarActivas().stream()
                .filter(m -> idsHabilitadas.contains(m.getId()))
                .map(MateriaVista::de)
                .collect(Collectors.toList());
    }

    @Override
    public List<MateriaVista> materiasDisponibles(Long idProfesor) {
        exigeProfesor(idProfesor);
        Set<Long> idsHabilitadas = perfilRepositorio.listarPorProfesor(idProfesor).stream()
                .map(Perfil::getIdMateria).collect(Collectors.toSet());
        return materiaRepositorio.listarActivas().stream()
                .filter(m -> !idsHabilitadas.contains(m.getId()))
                .map(MateriaVista::de)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void habilitar(Long idProfesor, Long idMateria, ContextoPeticion contexto) {
        autorizador.exigeGestion(contexto);
        exigeProfesor(idProfesor);
        exigeMateria(idMateria);
        if (perfilRepositorio.existe(idProfesor, idMateria)) {
            throw new ConflictoDatosException("El profesor ya esta habilitado para esa materia");
        }
        perfilRepositorio.guardar(Perfil.crear(idProfesor, idMateria));
        auditoria.exito(AccionAuditoria.CREAR, ENTIDAD, idProfesor + ":" + idMateria,
                "Habilitacion de materia " + idMateria + " para profesor " + idProfesor, contexto);
    }

    @Override
    @Transactional
    public void deshabilitar(Long idProfesor, Long idMateria, ContextoPeticion contexto) {
        autorizador.exigeGestion(contexto);
        if (!perfilRepositorio.existe(idProfesor, idMateria)) {
            throw new RecursoNoEncontradoException("Perfil", idProfesor + ":" + idMateria);
        }
        perfilRepositorio.eliminar(idProfesor, idMateria);
        auditoria.exito(AccionAuditoria.ELIMINAR, ENTIDAD, idProfesor + ":" + idMateria,
                "Deshabilitacion de materia " + idMateria + " para profesor " + idProfesor, contexto);
    }

    private void exigeProfesor(Long idProfesor) {
        profesorRepositorio.buscarPorId(idProfesor)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesor", idProfesor));
    }

    private Materia exigeMateria(Long idMateria) {
        return materiaRepositorio.buscarPorId(idMateria)
                .orElseThrow(() -> new RecursoNoEncontradoException("Materia", idMateria));
    }
}
