package com.tesiscalidad.horarios.application.usecase;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.profesor.ActualizarProfesorComando;
import com.tesiscalidad.horarios.application.dto.profesor.CrearProfesorComando;
import com.tesiscalidad.horarios.application.dto.profesor.ProfesorVista;
import com.tesiscalidad.horarios.application.port.in.GestionarProfesoresUseCase;
import com.tesiscalidad.horarios.application.support.Autorizador;
import com.tesiscalidad.horarios.application.support.TrazaAuditoria;
import com.tesiscalidad.horarios.domain.enums.AccionAuditoria;
import com.tesiscalidad.horarios.domain.enums.TipoContrato;
import com.tesiscalidad.horarios.domain.exception.ConflictoDatosException;
import com.tesiscalidad.horarios.domain.exception.RecursoNoEncontradoException;
import com.tesiscalidad.horarios.domain.model.Profesor;
import com.tesiscalidad.horarios.domain.port.out.ParametrosAplicacionPort;
import com.tesiscalidad.horarios.domain.port.out.ProfesorRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.UsuarioRepositoryPort;
import com.tesiscalidad.horarios.domain.support.Pagina;
import com.tesiscalidad.horarios.domain.support.Paginacion;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/** Implementacion del caso de uso de gestion de profesores. */
@ApplicationScoped
public class ProfesorService implements GestionarProfesoresUseCase {

    private static final String ENTIDAD = "PROFESOR";

    private final ProfesorRepositoryPort repositorio;
    private final UsuarioRepositoryPort usuarioRepositorio;
    private final ParametrosAplicacionPort parametros;
    private final Autorizador autorizador;
    private final TrazaAuditoria auditoria;

    protected ProfesorService() {
        this(null, null, null, null, null);
    }

    @Inject
    public ProfesorService(ProfesorRepositoryPort repositorio, UsuarioRepositoryPort usuarioRepositorio,
                           ParametrosAplicacionPort parametros, Autorizador autorizador,
                           TrazaAuditoria auditoria) {
        this.repositorio = repositorio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.parametros = parametros;
        this.autorizador = autorizador;
        this.auditoria = auditoria;
    }

    @Override
    @Transactional
    public ProfesorVista crear(CrearProfesorComando comando, ContextoPeticion contexto) {
        autorizador.exigeGestion(contexto);
        if (repositorio.existeCodigo(comando.getCodigo())) {
            throw new ConflictoDatosException("Ya existe un profesor con codigo " + comando.getCodigo());
        }
        Profesor profesor = Profesor.crear(comando.getCodigo(), comando.getNombre(),
                TipoContrato.desdeCodigo(comando.getTipoContrato()), comando.isDisponible());
        vincularUsuarioSiAplica(profesor, comando.getIdUsuario());
        Profesor guardado = repositorio.guardar(profesor);
        auditoria.exito(AccionAuditoria.CREAR, ENTIDAD, guardado.getId(),
                "Alta de profesor " + guardado.getCodigo().valor(), contexto);
        return ProfesorVista.de(guardado);
    }

    @Override
    @Transactional
    public ProfesorVista actualizar(Long id, ActualizarProfesorComando comando, ContextoPeticion contexto) {
        autorizador.exigeGestion(contexto);
        Profesor profesor = cargar(id);
        profesor.actualizar(comando.getNombre(), TipoContrato.desdeCodigo(comando.getTipoContrato()),
                comando.isDisponible());
        if (comando.getIdUsuario() == null) {
            profesor.desvincularUsuario();
        } else {
            vincularUsuarioSiAplica(profesor, comando.getIdUsuario());
        }
        Profesor guardado = repositorio.guardar(profesor);
        auditoria.exito(AccionAuditoria.ACTUALIZAR, ENTIDAD, id,
                "Actualizacion de profesor " + guardado.getCodigo().valor(), contexto);
        return ProfesorVista.de(guardado);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, boolean activo, ContextoPeticion contexto) {
        autorizador.exigeGestion(contexto);
        Profesor profesor = cargar(id);
        if (activo) {
            profesor.activar();
        } else {
            profesor.desactivar();
        }
        repositorio.guardar(profesor);
        auditoria.exito(AccionAuditoria.ACTUALIZAR, ENTIDAD, id,
                (activo ? "Activacion" : "Desactivacion") + " de profesor", contexto);
    }

    @Override
    @Transactional
    public void eliminar(Long id, ContextoPeticion contexto) {
        autorizador.exigeGestion(contexto);
        cargar(id);
        repositorio.eliminar(id);
        auditoria.exito(AccionAuditoria.ELIMINAR, ENTIDAD, id, "Eliminacion de profesor", contexto);
    }

    @Override
    public ProfesorVista obtener(Long id) {
        return ProfesorVista.de(cargar(id));
    }

    @Override
    public Pagina<ProfesorVista> listar(String texto, int pagina, int tamano) {
        Paginacion paginacion = Paginacion.de(pagina,
                tamano <= 0 ? parametros.tamanoPaginaPorDefecto() : tamano,
                parametros.tamanoPaginaMaximo());
        return repositorio.listar(texto, paginacion).mapear(ProfesorVista::de);
    }

    @Override
    public List<ProfesorVista> listarActivos() {
        return repositorio.listarActivos().stream().map(ProfesorVista::de).collect(Collectors.toList());
    }

    private void vincularUsuarioSiAplica(Profesor profesor, Long idUsuario) {
        if (idUsuario == null) {
            return;
        }
        usuarioRepositorio.buscarPorId(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", idUsuario));
        repositorio.buscarPorIdUsuario(idUsuario).ifPresent(otro -> {
            if (!otro.getId().equals(profesor.getId())) {
                throw new ConflictoDatosException("El usuario ya esta vinculado a otro profesor");
            }
        });
        profesor.vincularUsuario(idUsuario);
    }

    private Profesor cargar(Long id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesor", id));
    }
}
