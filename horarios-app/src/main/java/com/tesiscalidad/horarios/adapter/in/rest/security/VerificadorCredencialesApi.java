package com.tesiscalidad.horarios.adapter.in.rest.security;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.domain.model.Usuario;
import com.tesiscalidad.horarios.domain.port.out.HasheadorContrasenaPort;
import com.tesiscalidad.horarios.domain.port.out.RelojPort;
import com.tesiscalidad.horarios.domain.port.out.UsuarioRepositoryPort;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Optional;

/**
 * Verificacion de credenciales para la API REST (HTTP Basic). Respeta cuenta
 * activa y bloqueo temporal, pero no incrementa contadores ni escribe auditoria
 * (eso corresponde al flujo de login de la aplicacion web).
 */
@ApplicationScoped
public class VerificadorCredencialesApi {

    private final UsuarioRepositoryPort usuarioRepositorio;
    private final HasheadorContrasenaPort hasheador;
    private final RelojPort reloj;

    protected VerificadorCredencialesApi() {
        this(null, null, null);
    }

    @Inject
    public VerificadorCredencialesApi(UsuarioRepositoryPort usuarioRepositorio,
                                      HasheadorContrasenaPort hasheador, RelojPort reloj) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.hasheador = hasheador;
        this.reloj = reloj;
    }

    public Optional<ContextoPeticion> autenticar(String login, char[] contrasena, String ip, String correlationId) {
        char[] copia = contrasena == null ? new char[0] : contrasena.clone();
        try {
            Optional<Usuario> usuario = usuarioRepositorio.buscarPorLogin(login == null ? "" : login.toLowerCase())
                    .filter(Usuario::isActivo)
                    .filter(u -> !u.estaBloqueado(reloj.ahora()))
                    .filter(u -> hasheador.verificar(copia, u.getClave()));
            return usuario.map(u -> new ContextoPeticion(u.getLogin(), u.getRol(), ip, correlationId));
        } finally {
            Arrays.fill(copia, '\0');
        }
    }
}
