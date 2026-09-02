package com.tesiscalidad.horarios.application.usecase;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.usuario.ActualizarUsuarioComando;
import com.tesiscalidad.horarios.application.dto.usuario.CambiarContrasenaComando;
import com.tesiscalidad.horarios.application.dto.usuario.CrearUsuarioComando;
import com.tesiscalidad.horarios.application.dto.usuario.UsuarioVista;
import com.tesiscalidad.horarios.application.port.in.GestionarUsuariosUseCase;
import com.tesiscalidad.horarios.application.support.Autorizador;
import com.tesiscalidad.horarios.application.support.TrazaAuditoria;
import com.tesiscalidad.horarios.domain.enums.AccionAuditoria;
import com.tesiscalidad.horarios.domain.enums.RolUsuario;
import com.tesiscalidad.horarios.domain.exception.ConflictoDatosException;
import com.tesiscalidad.horarios.domain.exception.RecursoNoEncontradoException;
import com.tesiscalidad.horarios.domain.exception.ValidacionException;
import com.tesiscalidad.horarios.domain.model.Usuario;
import com.tesiscalidad.horarios.domain.port.out.HasheadorContrasenaPort;
import com.tesiscalidad.horarios.domain.port.out.ParametrosAplicacionPort;
import com.tesiscalidad.horarios.domain.port.out.RelojPort;
import com.tesiscalidad.horarios.domain.port.out.UsuarioRepositoryPort;
import com.tesiscalidad.horarios.domain.support.Pagina;
import com.tesiscalidad.horarios.domain.support.Paginacion;
import com.tesiscalidad.horarios.domain.vo.ClaveHash;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Arrays;

/** Implementacion del caso de uso de administracion de usuarios. */
@ApplicationScoped
public class UsuarioService implements GestionarUsuariosUseCase {

    private static final String ENTIDAD = "USUARIO";

    private final UsuarioRepositoryPort repositorio;
    private final HasheadorContrasenaPort hasheador;
    private final RelojPort reloj;
    private final ParametrosAplicacionPort parametros;
    private final Autorizador autorizador;
    private final TrazaAuditoria auditoria;

    protected UsuarioService() {
        this(null, null, null, null, null, null);
    }

    @Inject
    public UsuarioService(UsuarioRepositoryPort repositorio, HasheadorContrasenaPort hasheador, RelojPort reloj,
                          ParametrosAplicacionPort parametros, Autorizador autorizador, TrazaAuditoria auditoria) {
        this.repositorio = repositorio;
        this.hasheador = hasheador;
        this.reloj = reloj;
        this.parametros = parametros;
        this.autorizador = autorizador;
        this.auditoria = auditoria;
    }

    @Override
    @Transactional
    public UsuarioVista crear(CrearUsuarioComando comando, ContextoPeticion contexto) {
        autorizador.exigeAdministracionUsuarios(contexto);
        char[] plano = comando.getContrasena();
        try {
            exigeLongitudContrasena(plano);
            if (repositorio.existeLogin(comando.getLogin().trim().toLowerCase())) {
                throw new ConflictoDatosException("Ya existe una cuenta con login " + comando.getLogin());
            }
            ClaveHash hash = hasheador.hashear(plano);
            Usuario usuario = Usuario.crear(comando.getLogin(), comando.getNombre(), hash,
                    RolUsuario.desdeCodigo(comando.getRol()));
            Usuario guardado = repositorio.guardar(usuario);
            auditoria.exito(AccionAuditoria.CREAR, ENTIDAD, guardado.getId(),
                    "Alta de usuario " + guardado.getLogin(), contexto);
            return UsuarioVista.de(guardado, reloj.ahora());
        } finally {
            Arrays.fill(plano, '\0');
            comando.limpiar();
        }
    }

    @Override
    @Transactional
    public UsuarioVista actualizar(Long id, ActualizarUsuarioComando comando, ContextoPeticion contexto) {
        autorizador.exigeAdministracionUsuarios(contexto);
        Usuario usuario = cargar(id);
        usuario.renombrar(comando.getNombre());
        usuario.cambiarRol(RolUsuario.desdeCodigo(comando.getRol()));
        Usuario guardado = repositorio.guardar(usuario);
        auditoria.exito(AccionAuditoria.ACTUALIZAR, ENTIDAD, id,
                "Actualizacion de usuario " + guardado.getLogin(), contexto);
        return UsuarioVista.de(guardado, reloj.ahora());
    }

    @Override
    @Transactional
    public void cambiarContrasena(Long id, CambiarContrasenaComando comando, ContextoPeticion contexto) {
        autorizador.exigeAdministracionUsuarios(contexto);
        char[] plano = comando.getNuevaContrasena();
        try {
            exigeLongitudContrasena(plano);
            Usuario usuario = cargar(id);
            usuario.cambiarClave(hasheador.hashear(plano));
            repositorio.guardar(usuario);
            auditoria.exito(AccionAuditoria.ACTUALIZAR, ENTIDAD, id, "Cambio de contrasena", contexto);
        } finally {
            Arrays.fill(plano, '\0');
            comando.limpiar();
        }
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, boolean activo, ContextoPeticion contexto) {
        autorizador.exigeAdministracionUsuarios(contexto);
        Usuario usuario = cargar(id);
        if (activo) {
            usuario.activar();
        } else {
            usuario.desactivar();
        }
        repositorio.guardar(usuario);
        auditoria.exito(AccionAuditoria.ACTUALIZAR, ENTIDAD, id,
                (activo ? "Activacion" : "Desactivacion") + " de usuario", contexto);
    }

    @Override
    @Transactional
    public void desbloquear(Long id, ContextoPeticion contexto) {
        autorizador.exigeAdministracionUsuarios(contexto);
        Usuario usuario = cargar(id);
        usuario.registrarAccesoExitoso(reloj.ahora());
        repositorio.actualizarEstadoAcceso(usuario);
        auditoria.exito(AccionAuditoria.ACTUALIZAR, ENTIDAD, id, "Desbloqueo manual de cuenta", contexto);
    }

    @Override
    @Transactional
    public void eliminar(Long id, ContextoPeticion contexto) {
        autorizador.exigeAdministracionUsuarios(contexto);
        cargar(id);
        repositorio.eliminar(id);
        auditoria.exito(AccionAuditoria.ELIMINAR, ENTIDAD, id, "Eliminacion de usuario", contexto);
    }

    @Override
    public UsuarioVista obtener(Long id) {
        return UsuarioVista.de(cargar(id), reloj.ahora());
    }

    @Override
    public Pagina<UsuarioVista> listar(String texto, int pagina, int tamano) {
        Paginacion paginacion = Paginacion.de(pagina,
                tamano <= 0 ? parametros.tamanoPaginaPorDefecto() : tamano,
                parametros.tamanoPaginaMaximo());
        java.time.Instant ahora = reloj.ahora();
        return repositorio.listar(texto, paginacion).mapear(u -> UsuarioVista.de(u, ahora));
    }

    private void exigeLongitudContrasena(char[] plano) {
        if (plano == null || plano.length < parametros.longitudMinimaContrasena()) {
            throw new ValidacionException(
                    "La contrasena debe tener al menos " + parametros.longitudMinimaContrasena() + " caracteres.");
        }
    }

    private Usuario cargar(Long id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }
}
