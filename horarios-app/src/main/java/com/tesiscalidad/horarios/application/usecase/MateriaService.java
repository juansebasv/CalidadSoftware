package com.tesiscalidad.horarios.application.usecase;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.materia.ActualizarMateriaComando;
import com.tesiscalidad.horarios.application.dto.materia.CrearMateriaComando;
import com.tesiscalidad.horarios.application.dto.materia.MateriaVista;
import com.tesiscalidad.horarios.application.port.in.GestionarMateriasUseCase;
import com.tesiscalidad.horarios.application.support.Autorizador;
import com.tesiscalidad.horarios.application.support.TrazaAuditoria;
import com.tesiscalidad.horarios.domain.enums.AccionAuditoria;
import com.tesiscalidad.horarios.domain.enums.TipoMateria;
import com.tesiscalidad.horarios.domain.exception.ConflictoDatosException;
import com.tesiscalidad.horarios.domain.exception.RecursoNoEncontradoException;
import com.tesiscalidad.horarios.domain.model.Materia;
import com.tesiscalidad.horarios.domain.port.out.MateriaRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.ParametrosAplicacionPort;
import com.tesiscalidad.horarios.domain.support.Pagina;
import com.tesiscalidad.horarios.domain.support.Paginacion;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/** Implementacion del caso de uso de gestion de materias. */
@ApplicationScoped
public class MateriaService implements GestionarMateriasUseCase {

    private static final String ENTIDAD = "MATERIA";

    private final MateriaRepositoryPort repositorio;
    private final ParametrosAplicacionPort parametros;
    private final Autorizador autorizador;
    private final TrazaAuditoria auditoria;

    /** Requerido por CDI para el proxy de ambito; no se usa en tiempo de ejecucion. */
    protected MateriaService() {
        this(null, null, null, null);
    }

    @Inject
    public MateriaService(MateriaRepositoryPort repositorio, ParametrosAplicacionPort parametros,
                          Autorizador autorizador, TrazaAuditoria auditoria) {
        this.repositorio = repositorio;
        this.parametros = parametros;
        this.autorizador = autorizador;
        this.auditoria = auditoria;
    }

    @Override
    @Transactional
    public MateriaVista crear(CrearMateriaComando comando, ContextoPeticion contexto) {
        autorizador.exigeGestion(contexto);
        if (repositorio.existeCodigo(comando.getCodigo())) {
            throw new ConflictoDatosException("Ya existe una materia con codigo " + comando.getCodigo());
        }
        Materia materia = Materia.crear(comando.getCodigo(), comando.getNombre(),
                TipoMateria.desdeCodigo(comando.getTipo()),
                comando.getCreditos(), comando.getIntensidadHoraria(), comando.getSemestre());
        Materia guardada = repositorio.guardar(materia);
        auditoria.exito(AccionAuditoria.CREAR, ENTIDAD, guardada.getId(),
                "Alta de materia " + guardada.getCodigo().valor(), contexto);
        return MateriaVista.de(guardada);
    }

    @Override
    @Transactional
    public MateriaVista actualizar(Long id, ActualizarMateriaComando comando, ContextoPeticion contexto) {
        autorizador.exigeGestion(contexto);
        Materia materia = cargar(id);
        materia.actualizar(comando.getNombre(), TipoMateria.desdeCodigo(comando.getTipo()),
                comando.getCreditos(), comando.getIntensidadHoraria(), comando.getSemestre());
        Materia guardada = repositorio.guardar(materia);
        auditoria.exito(AccionAuditoria.ACTUALIZAR, ENTIDAD, id,
                "Actualizacion de materia " + guardada.getCodigo().valor(), contexto);
        return MateriaVista.de(guardada);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, boolean activa, ContextoPeticion contexto) {
        autorizador.exigeGestion(contexto);
        Materia materia = cargar(id);
        if (activa) {
            materia.activar();
        } else {
            materia.desactivar();
        }
        repositorio.guardar(materia);
        auditoria.exito(AccionAuditoria.ACTUALIZAR, ENTIDAD, id,
                (activa ? "Activacion" : "Desactivacion") + " de materia", contexto);
    }

    @Override
    @Transactional
    public void eliminar(Long id, ContextoPeticion contexto) {
        autorizador.exigeGestion(contexto);
        cargar(id);
        repositorio.eliminar(id);
        auditoria.exito(AccionAuditoria.ELIMINAR, ENTIDAD, id, "Eliminacion de materia", contexto);
    }

    @Override
    public MateriaVista obtener(Long id) {
        return MateriaVista.de(cargar(id));
    }

    @Override
    public Pagina<MateriaVista> listar(String texto, Integer semestre, int pagina, int tamano) {
        Paginacion paginacion = Paginacion.de(pagina,
                tamano <= 0 ? parametros.tamanoPaginaPorDefecto() : tamano,
                parametros.tamanoPaginaMaximo());
        return repositorio.listar(texto, semestre, paginacion).mapear(MateriaVista::de);
    }

    @Override
    public List<MateriaVista> listarActivas() {
        return repositorio.listarActivas().stream().map(MateriaVista::de).collect(Collectors.toList());
    }

    private Materia cargar(Long id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Materia", id));
    }
}
