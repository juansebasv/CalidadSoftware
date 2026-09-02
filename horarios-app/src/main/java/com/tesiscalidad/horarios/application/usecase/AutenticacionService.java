package com.tesiscalidad.horarios.application.usecase;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.auth.SesionAutenticada;
import com.tesiscalidad.horarios.application.port.in.AutenticacionUseCase;
import com.tesiscalidad.horarios.application.support.TrazaAuditoria;
import com.tesiscalidad.horarios.domain.enums.AccionAuditoria;
import com.tesiscalidad.horarios.domain.exception.AutenticacionException;
import com.tesiscalidad.horarios.domain.exception.DominioException;
import com.tesiscalidad.horarios.domain.model.Usuario;
import com.tesiscalidad.horarios.domain.port.out.HasheadorContrasenaPort;
import com.tesiscalidad.horarios.domain.port.out.ParametrosAplicacionPort;
import com.tesiscalidad.horarios.domain.port.out.RelojPort;
import com.tesiscalidad.horarios.domain.port.out.UsuarioRepositoryPort;
import com.tesiscalidad.horarios.domain.vo.ClaveHash;
import com.tesiscalidad.horarios.domain.vo.Credenciales;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

/**
 * Flujo de autenticacion con proteccion contra fuerza bruta:
 * <ul>
 *   <li>mensaje generico ante cualquier fallo (no revela si el usuario existe);</li>
 *   <li>verificacion contra un hash ficticio si la cuenta no existe (timing);</li>
 *   <li>contador de intentos + bloqueo temporal delegado en {@link Usuario};</li>
 *   <li>rehash transparente si el costo del hash almacenado quedo obsoleto;</li>
 *   <li>auditoria de todos los desenlaces.</li>
 * </ul>
 */
@ApplicationScoped
public class AutenticacionService implements AutenticacionUseCase {

    private static final String ENTIDAD = "SESION";
    /** Hash BCrypt valido de una contrasena aleatoria; solo para nivelar tiempos. */
    private static final ClaveHash HASH_FICTICIO =
            ClaveHash.deHashExistente("$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW");

    private final UsuarioRepositoryPort repositorio;
    private final HasheadorContrasenaPort hasheador;
    private final RelojPort reloj;
    private final ParametrosAplicacionPort parametros;
    private final TrazaAuditoria auditoria;

    protected AutenticacionService() {
        this(null, null, null, null, null);
    }

    @Inject
    public AutenticacionService(UsuarioRepositoryPort repositorio, HasheadorContrasenaPort hasheador,
                                RelojPort reloj, ParametrosAplicacionPort parametros, TrazaAuditoria auditoria) {
        this.repositorio = repositorio;
        this.hasheador = hasheador;
        this.reloj = reloj;
        this.parametros = parametros;
        this.auditoria = auditoria;
    }

    @Override
    @Transactional
    public SesionAutenticada iniciarSesion(String login, char[] contrasena, ContextoPeticion contexto) {
        char[] copia = contrasena == null ? new char[0] : contrasena.clone();
        try {
            Credenciales credenciales = construirCredenciales(login, copia, contexto);
            Instant ahora = reloj.ahora();
            Optional<Usuario> encontrado = repositorio.buscarPorLogin(credenciales.login());

            if (encontrado.isEmpty()) {
                hasheador.verificar(credenciales.contrasena(), HASH_FICTICIO); // timing
                registrarFallo(credenciales.login(), "Cuenta inexistente", contexto);
                throw AutenticacionException.credencialesInvalidas();
            }

            Usuario usuario = encontrado.get();
            verificarEstadoCuenta(usuario, ahora, contexto);

            if (!hasheador.verificar(credenciales.contrasena(), usuario.getClave())) {
                manejarContrasenaIncorrecta(usuario, ahora, contexto);
                throw AutenticacionException.credencialesInvalidas();
            }

            return completarLoginExitoso(usuario, credenciales.contrasena(), ahora, contexto);
        } finally {
            Arrays.fill(copia, '\0');
            if (contrasena != null) {
                Arrays.fill(contrasena, '\0');
            }
        }
    }

    @Override
    public void cerrarSesion(String login, ContextoPeticion contexto) {
        auditoria.exito(AccionAuditoria.LOGOUT, ENTIDAD, login, "Cierre de sesion", contexto);
    }

    private Credenciales construirCredenciales(String login, char[] copia, ContextoPeticion contexto) {
        try {
            return Credenciales.de(login, copia);
        } catch (DominioException ex) {
            registrarFallo(login, "Formato de credenciales invalido", contexto);
            throw AutenticacionException.credencialesInvalidas();
        }
    }

    private void verificarEstadoCuenta(Usuario usuario, Instant ahora, ContextoPeticion contexto) {
        try {
            usuario.asegurarPuedeAutenticar(ahora);
        } catch (AutenticacionException ex) {
            auditoria.error(AccionAuditoria.LOGIN_FALLIDO, ENTIDAD, usuario.getLogin(),
                    ex.getMessage(), contexto);
            throw ex;
        }
    }

    private void manejarContrasenaIncorrecta(Usuario usuario, Instant ahora, ContextoPeticion contexto) {
        boolean seBloqueo = usuario.registrarIntentoFallido(parametros.politicaBloqueo(), ahora);
        repositorio.actualizarEstadoAcceso(usuario);
        registrarFallo(usuario.getLogin(),
                "Contrasena incorrecta (intento " + usuario.getIntentosFallidos() + ")", contexto);
        if (seBloqueo) {
            auditoria.error(AccionAuditoria.CUENTA_BLOQUEADA, ENTIDAD, usuario.getLogin(),
                    "Bloqueo por " + parametros.politicaBloqueo().minutosBloqueo() + " minutos", contexto);
        }
    }

    private SesionAutenticada completarLoginExitoso(Usuario usuario, char[] contrasena,
                                                   Instant ahora, ContextoPeticion contexto) {
        if (hasheador.requiereRehash(usuario.getClave())) {
            usuario.cambiarClave(hasheador.hashear(contrasena));
            repositorio.guardar(usuario);
        }
        usuario.registrarAccesoExitoso(ahora);
        repositorio.actualizarEstadoAcceso(usuario);
        auditoria.exito(AccionAuditoria.LOGIN, ENTIDAD, usuario.getLogin(), "Inicio de sesion", contexto);
        return SesionAutenticada.de(usuario, ahora);
    }

    private void registrarFallo(String login, String detalle, ContextoPeticion contexto) {
        auditoria.error(AccionAuditoria.LOGIN_FALLIDO, ENTIDAD, login, detalle, contexto);
    }
}
