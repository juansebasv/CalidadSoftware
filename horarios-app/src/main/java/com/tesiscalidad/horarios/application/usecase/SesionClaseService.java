package com.tesiscalidad.horarios.application.usecase;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.sesion.ProgramarSesionComando;
import com.tesiscalidad.horarios.application.dto.sesion.ReprogramarSesionComando;
import com.tesiscalidad.horarios.application.dto.sesion.SesionClaseVista;
import com.tesiscalidad.horarios.application.port.in.GestionarSesionesUseCase;
import com.tesiscalidad.horarios.application.support.Autorizador;
import com.tesiscalidad.horarios.application.support.TrazaAuditoria;
import com.tesiscalidad.horarios.domain.enums.AccionAuditoria;
import com.tesiscalidad.horarios.domain.enums.DiaSemana;
import com.tesiscalidad.horarios.domain.enums.Jornada;
import com.tesiscalidad.horarios.domain.exception.ConflictoDatosException;
import com.tesiscalidad.horarios.domain.exception.RecursoNoEncontradoException;
import com.tesiscalidad.horarios.domain.exception.ReglaNegocioException;
import com.tesiscalidad.horarios.domain.model.Materia;
import com.tesiscalidad.horarios.domain.model.Profesor;
import com.tesiscalidad.horarios.domain.model.SesionClase;
import com.tesiscalidad.horarios.domain.port.out.DisponibilidadRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.MateriaRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.ParametrosAplicacionPort;
import com.tesiscalidad.horarios.domain.port.out.PerfilRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.ProfesorRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.SesionClaseRepositoryPort;
import com.tesiscalidad.horarios.domain.service.ValidadorProgramacionSesion;
import com.tesiscalidad.horarios.domain.support.Pagina;
import com.tesiscalidad.horarios.domain.support.Paginacion;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/** Implementacion del caso de uso de programacion de sesiones de clase. */
@ApplicationScoped
public class SesionClaseService implements GestionarSesionesUseCase {

    private static final String ENTIDAD = "SESION_CLASE";
    private static final String SIN_DOCENTE = "(sin asignar)";

    private final SesionClaseRepositoryPort repositorio;
    private final MateriaRepositoryPort materiaRepositorio;
    private final ProfesorRepositoryPort profesorRepositorio;
    private final PerfilRepositoryPort perfilRepositorio;
    private final DisponibilidadRepositoryPort disponibilidadRepositorio;
    private final ValidadorProgramacionSesion validador;
    private final ParametrosAplicacionPort parametros;
    private final Autorizador autorizador;
    private final TrazaAuditoria auditoria;

    protected SesionClaseService() {
        this(null, null, null, null, null, null, null, null, null);
    }

    @Inject
    @SuppressWarnings("java:S107") // orquestador: depende legitimamente de varios puertos
    public SesionClaseService(SesionClaseRepositoryPort repositorio, MateriaRepositoryPort materiaRepositorio,
                              ProfesorRepositoryPort profesorRepositorio, PerfilRepositoryPort perfilRepositorio,
                              DisponibilidadRepositoryPort disponibilidadRepositorio,
                              ValidadorProgramacionSesion validador, ParametrosAplicacionPort parametros,
                              Autorizador autorizador, TrazaAuditoria auditoria) {
        this.repositorio = repositorio;
        this.materiaRepositorio = materiaRepositorio;
        this.profesorRepositorio = profesorRepositorio;
        this.perfilRepositorio = perfilRepositorio;
        this.disponibilidadRepositorio = disponibilidadRepositorio;
        this.validador = validador;
        this.parametros = parametros;
        this.autorizador = autorizador;
        this.auditoria = auditoria;
    }

    @Override
    @Transactional
    public SesionClaseVista programar(ProgramarSesionComando c, ContextoPeticion contexto) {
        autorizador.exigeGestion(contexto);
        Materia materia = exigeMateriaActiva(c.getIdMateria());
        DiaSemana dia = DiaSemana.desdeCodigo(c.getDia());
        if (repositorio.existeMateriaGrupoDia(c.getIdMateria(), normalizarGrupo(c.getGrupo()), dia, null)) {
            throw new ConflictoDatosException(
                    "Ya existe una sesion de esa materia/grupo el " + dia.getEtiqueta());
        }
        SesionClase candidata = SesionClase.crear(c.getIdMateria(), c.getIdProfesor(), c.getGrupo(), dia,
                c.getHora(), c.getDuracion(), c.getSemestre(), Jornada.desdeCodigo(c.getJornada()));
        validarProgramacion(candidata);
        SesionClase guardada = repositorio.guardar(candidata);
        auditoria.exito(AccionAuditoria.CREAR, ENTIDAD, guardada.getId(),
                "Sesion " + materia.getCodigo().valor() + " grupo " + guardada.getGrupo(), contexto);
        return proyectar(guardada);
    }

    @Override
    @Transactional
    public SesionClaseVista reprogramar(Long id, ReprogramarSesionComando c, ContextoPeticion contexto) {
        autorizador.exigeGestion(contexto);
        SesionClase sesion = cargar(id);
        sesion.reprogramar(DiaSemana.desdeCodigo(c.getDia()), c.getHora(), c.getDuracion(),
                Jornada.desdeCodigo(c.getJornada()));
        if (repositorio.existeMateriaGrupoDia(sesion.getIdMateria(), sesion.getGrupo(), sesion.getDia(), id)) {
            throw new ConflictoDatosException(
                    "Ya existe una sesion de esa materia/grupo el " + sesion.getDia().getEtiqueta());
        }
        validarProgramacion(sesion);
        SesionClase guardada = repositorio.guardar(sesion);
        auditoria.exito(AccionAuditoria.ACTUALIZAR, ENTIDAD, id, "Reprogramacion de sesion", contexto);
        return proyectar(guardada);
    }

    @Override
    @Transactional
    public SesionClaseVista asignarDocente(Long id, Long idProfesor, ContextoPeticion contexto) {
        autorizador.exigeGestion(contexto);
        SesionClase sesion = cargar(id);
        profesorRepositorio.buscarPorId(idProfesor)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesor", idProfesor));
        sesion.asignarProfesor(idProfesor);
        validarProgramacion(sesion);
        SesionClase guardada = repositorio.guardar(sesion);
        auditoria.exito(AccionAuditoria.ACTUALIZAR, ENTIDAD, id,
                "Asignacion del docente " + idProfesor, contexto);
        return proyectar(guardada);
    }

    @Override
    @Transactional
    public SesionClaseVista quitarDocente(Long id, ContextoPeticion contexto) {
        autorizador.exigeGestion(contexto);
        SesionClase sesion = cargar(id);
        sesion.quitarProfesor();
        SesionClase guardada = repositorio.guardar(sesion);
        auditoria.exito(AccionAuditoria.ACTUALIZAR, ENTIDAD, id, "Retiro del docente asignado", contexto);
        return proyectar(guardada);
    }

    @Override
    @Transactional
    public void cancelar(Long id, ContextoPeticion contexto) {
        autorizador.exigeGestion(contexto);
        cargar(id);
        repositorio.eliminar(id);
        auditoria.exito(AccionAuditoria.ELIMINAR, ENTIDAD, id, "Cancelacion de sesion", contexto);
    }

    @Override
    public SesionClaseVista obtener(Long id) {
        return proyectar(cargar(id));
    }

    @Override
    public Pagina<SesionClaseVista> listar(Integer semestre, int pagina, int tamano) {
        Paginacion paginacion = Paginacion.de(pagina,
                tamano <= 0 ? parametros.tamanoPaginaPorDefecto() : tamano,
                parametros.tamanoPaginaMaximo());
        return repositorio.listar(semestre, paginacion).mapear(this::proyectar);
    }

    private void validarProgramacion(SesionClase candidata) {
        boolean habilitado = candidata.tieneProfesor()
                && perfilRepositorio.existe(candidata.getIdProfesor(), candidata.getIdMateria());
        List<SesionClase> mismasDelDia = repositorio.listarPorDia(candidata.getDia());
        validador.validar(candidata, habilitado,
                candidata.tieneProfesor()
                        ? disponibilidadRepositorio.listarPorProfesor(candidata.getIdProfesor())
                        : List.of(),
                mismasDelDia);
    }

    private String normalizarGrupo(String grupo) {
        if (grupo == null || !grupo.trim().toUpperCase().matches(SesionClase.PATRON_GRUPO)) {
            throw new ReglaNegocioException("Grupo con formato no valido.");
        }
        return grupo.trim().toUpperCase();
    }

    private Materia exigeMateriaActiva(Long idMateria) {
        Materia materia = materiaRepositorio.buscarPorId(idMateria)
                .orElseThrow(() -> new RecursoNoEncontradoException("Materia", idMateria));
        if (!materia.isActiva()) {
            throw new ReglaNegocioException("No se puede programar una materia inactiva.");
        }
        return materia;
    }

    private SesionClase cargar(Long id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesion de clase", id));
    }

    private SesionClaseVista proyectar(SesionClase s) {
        Materia materia = materiaRepositorio.buscarPorId(s.getIdMateria()).orElse(null);
        String codigo = materia == null ? "?" : materia.getCodigo().valor();
        String nombreMateria = materia == null ? "?" : materia.getNombre();
        String nombreProfesor = SIN_DOCENTE;
        if (s.getIdProfesor() != null) {
            Optional<Profesor> profesor = profesorRepositorio.buscarPorId(s.getIdProfesor());
            nombreProfesor = profesor.map(Profesor::getNombre).orElse(SIN_DOCENTE);
        }
        return SesionClaseVista.de(s, codigo, nombreMateria, nombreProfesor);
    }
}
