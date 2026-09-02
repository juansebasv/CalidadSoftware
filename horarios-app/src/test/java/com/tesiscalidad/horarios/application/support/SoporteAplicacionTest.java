package com.tesiscalidad.horarios.application.support;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.domain.enums.AccionAuditoria;
import com.tesiscalidad.horarios.domain.enums.RolUsuario;
import com.tesiscalidad.horarios.domain.exception.AutorizacionException;
import com.tesiscalidad.horarios.domain.model.RegistroAuditoria;
import com.tesiscalidad.horarios.domain.port.out.AuditoriaRepositoryPort;
import com.tesiscalidad.horarios.support.ParametrosFalsos;
import com.tesiscalidad.horarios.support.RelojFijo;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SoporteAplicacionTest {

    private final AuditoriaRepositoryPort repo = mock(AuditoriaRepositoryPort.class);
    private final RelojFijo reloj = RelojFijo.en("2026-02-01T10:00:00Z");

    private ContextoPeticion ctx() {
        return new ContextoPeticion("admin", RolUsuario.ADMIN, "10.0.0.1", "cid-9");
    }

    @Test
    void trazaAuditoriaEscribeRegistroCompleto() {
        TrazaAuditoria traza = new TrazaAuditoria(repo, reloj, new ParametrosFalsos());
        traza.exito(AccionAuditoria.CREAR, "MATERIA", 7L, "alta", ctx());
        ArgumentCaptor<RegistroAuditoria> captor = ArgumentCaptor.forClass(RegistroAuditoria.class);
        verify(repo).registrar(captor.capture());
        RegistroAuditoria r = captor.getValue();
        assertThat(r.getUsuarioLogin()).isEqualTo("admin");
        assertThat(r.getEntidadId()).isEqualTo("7");
        assertThat(r.getIp()).isEqualTo("10.0.0.1");
        assertThat(r.getFecha()).isEqualTo(reloj.ahora());
    }

    @Test
    void trazaAuditoriaRespetaFlagDeConfiguracion() {
        TrazaAuditoria traza = new TrazaAuditoria(repo, reloj, new ParametrosFalsos().conAuditoria(false));
        traza.error(AccionAuditoria.LOGIN_FALLIDO, "SESION", "x", "detalle", null);
        verify(repo, never()).registrar(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void autorizadorPermiteYDeniega() {
        Autorizador autorizador = new Autorizador();
        assertThatCode(() -> autorizador.exigeGestion(ctx())).doesNotThrowAnyException();
        assertThatCode(() -> autorizador.exigeAdministracionUsuarios(ctx())).doesNotThrowAnyException();

        ContextoPeticion profesor = new ContextoPeticion("p", RolUsuario.PROFESOR, "ip", "c");
        assertThatThrownBy(() -> autorizador.exigeGestion(profesor)).isInstanceOf(AutorizacionException.class);

        ContextoPeticion coord = new ContextoPeticion("c", RolUsuario.COORDINADOR, "ip", "c");
        assertThatThrownBy(() -> autorizador.exigeAdministracionUsuarios(coord))
                .isInstanceOf(AutorizacionException.class);

        assertThatThrownBy(() -> autorizador.exigeGestion(null)).isInstanceOf(AutorizacionException.class);
        assertThatThrownBy(() -> autorizador.exigeGestion(ContextoPeticion.anonimo("ip", "c")))
                .isInstanceOf(AutorizacionException.class);
    }
}
