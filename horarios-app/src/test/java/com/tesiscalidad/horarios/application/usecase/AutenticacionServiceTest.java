package com.tesiscalidad.horarios.application.usecase;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.auth.SesionAutenticada;
import com.tesiscalidad.horarios.application.support.TrazaAuditoria;
import com.tesiscalidad.horarios.domain.enums.AccionAuditoria;
import com.tesiscalidad.horarios.domain.enums.RolUsuario;
import com.tesiscalidad.horarios.domain.exception.AutenticacionException;
import com.tesiscalidad.horarios.domain.model.Usuario;
import com.tesiscalidad.horarios.domain.port.out.HasheadorContrasenaPort;
import com.tesiscalidad.horarios.domain.port.out.UsuarioRepositoryPort;
import com.tesiscalidad.horarios.domain.vo.ClaveHash;
import com.tesiscalidad.horarios.support.ParametrosFalsos;
import com.tesiscalidad.horarios.support.RelojFijo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticacionServiceTest {

    private static final ClaveHash HASH_12 =
            ClaveHash.deHashExistente("$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW");
    private static final ClaveHash HASH_08 =
            ClaveHash.deHashExistente("$2a$08$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW");

    @Mock
    UsuarioRepositoryPort repositorio;
    @Mock
    HasheadorContrasenaPort hasheador;
    @Mock
    TrazaAuditoria auditoria;

    private AutenticacionService service;
    private final ContextoPeticion ctx = ContextoPeticion.anonimo("ip", "cid");

    @BeforeEach
    void setUp() {
        service = new AutenticacionService(repositorio, hasheador, RelojFijo.en("2026-02-01T10:00:00Z"),
                new ParametrosFalsos(), auditoria);
    }

    private Usuario usuario(int intentos) {
        return Usuario.reconstruir(1L, "jdperez", "Juan Perez", HASH_12, RolUsuario.PROFESOR,
                true, intentos, null, null);
    }

    @Test
    void loginCorrecto() {
        when(repositorio.buscarPorLogin("jdperez")).thenReturn(Optional.of(usuario(0)));
        when(hasheador.verificar(any(), eq(HASH_12))).thenReturn(true);
        when(hasheador.requiereRehash(HASH_12)).thenReturn(false);

        SesionAutenticada s = service.iniciarSesion("jdperez", "Password12".toCharArray(), ctx);
        assertThat(s.getLogin()).isEqualTo("jdperez");
        assertThat(s.getRol()).isEqualTo(RolUsuario.PROFESOR);
        verify(repositorio).actualizarEstadoAcceso(any());
        verify(auditoria).exito(eq(AccionAuditoria.LOGIN), any(), any(), any(), any());
    }

    @Test
    void loginRehasheaCuandoCostoObsoleto() {
        Usuario u = Usuario.reconstruir(1L, "jdperez", "Juan", HASH_08, RolUsuario.ADMIN, true, 0, null, null);
        when(repositorio.buscarPorLogin("jdperez")).thenReturn(Optional.of(u));
        when(hasheador.verificar(any(), eq(HASH_08))).thenReturn(true);
        when(hasheador.requiereRehash(HASH_08)).thenReturn(true);
        when(hasheador.hashear(any())).thenReturn(HASH_12);

        service.iniciarSesion("jdperez", "Password12".toCharArray(), ctx);
        verify(hasheador).hashear(any());
        verify(repositorio).guardar(any());
    }

    @Test
    void loginUsuarioInexistenteEsGenericoYAudita() {
        when(repositorio.buscarPorLogin("fantasma")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.iniciarSesion("fantasma", "Password12".toCharArray(), ctx))
                .isInstanceOf(AutenticacionException.class);
        verify(hasheador).verificar(any(), any()); // hash ficticio para nivelar tiempos
        verify(auditoria).error(eq(AccionAuditoria.LOGIN_FALLIDO), any(), any(), any(), any());
    }

    @Test
    void loginCuentaInactiva() {
        Usuario u = usuario(0);
        u.desactivar();
        when(repositorio.buscarPorLogin("jdperez")).thenReturn(Optional.of(u));
        assertThatThrownBy(() -> service.iniciarSesion("jdperez", "Password12".toCharArray(), ctx))
                .isInstanceOf(AutenticacionException.class);
        verify(auditoria).error(eq(AccionAuditoria.LOGIN_FALLIDO), any(), any(), any(), any());
    }

    @Test
    void loginContrasenaIncorrectaIncrementaContador() {
        when(repositorio.buscarPorLogin("jdperez")).thenReturn(Optional.of(usuario(0)));
        when(hasheador.verificar(any(), eq(HASH_12))).thenReturn(false);
        assertThatThrownBy(() -> service.iniciarSesion("jdperez", "Password12".toCharArray(), ctx))
                .isInstanceOf(AutenticacionException.class);
        verify(repositorio).actualizarEstadoAcceso(any());
        verify(auditoria).error(eq(AccionAuditoria.LOGIN_FALLIDO), any(), any(), any(), any());
        verify(auditoria, never()).error(eq(AccionAuditoria.CUENTA_BLOQUEADA), any(), any(), any(), any());
    }

    @Test
    void loginContrasenaIncorrectaQueBloqueaCuenta() {
        when(repositorio.buscarPorLogin("jdperez")).thenReturn(Optional.of(usuario(2))); // politica: max 3
        when(hasheador.verificar(any(), eq(HASH_12))).thenReturn(false);
        assertThatThrownBy(() -> service.iniciarSesion("jdperez", "Password12".toCharArray(), ctx))
                .isInstanceOf(AutenticacionException.class);
        verify(auditoria).error(eq(AccionAuditoria.CUENTA_BLOQUEADA), any(), any(), any(), any());
    }

    @Test
    void loginFormatoInvalido() {
        assertThatThrownBy(() -> service.iniciarSesion("ab", "x".toCharArray(), ctx))
                .isInstanceOf(AutenticacionException.class);
        verify(auditoria).error(eq(AccionAuditoria.LOGIN_FALLIDO), any(), any(), any(), any());
    }

    @Test
    void cerrarSesionAudita() {
        service.cerrarSesion("jdperez", ctx);
        verify(auditoria).exito(eq(AccionAuditoria.LOGOUT), any(), any(), any(), any());
    }
}
