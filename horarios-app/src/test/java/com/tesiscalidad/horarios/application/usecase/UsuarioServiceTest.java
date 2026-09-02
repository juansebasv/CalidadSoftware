package com.tesiscalidad.horarios.application.usecase;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.usuario.ActualizarUsuarioComando;
import com.tesiscalidad.horarios.application.dto.usuario.CambiarContrasenaComando;
import com.tesiscalidad.horarios.application.dto.usuario.CrearUsuarioComando;
import com.tesiscalidad.horarios.application.support.Autorizador;
import com.tesiscalidad.horarios.application.support.TrazaAuditoria;
import com.tesiscalidad.horarios.domain.enums.RolUsuario;
import com.tesiscalidad.horarios.domain.exception.AutorizacionException;
import com.tesiscalidad.horarios.domain.exception.ConflictoDatosException;
import com.tesiscalidad.horarios.domain.exception.RecursoNoEncontradoException;
import com.tesiscalidad.horarios.domain.exception.ValidacionException;
import com.tesiscalidad.horarios.domain.model.Usuario;
import com.tesiscalidad.horarios.domain.port.out.HasheadorContrasenaPort;
import com.tesiscalidad.horarios.domain.port.out.UsuarioRepositoryPort;
import com.tesiscalidad.horarios.domain.support.Pagina;
import com.tesiscalidad.horarios.domain.vo.ClaveHash;
import com.tesiscalidad.horarios.support.ParametrosFalsos;
import com.tesiscalidad.horarios.support.RelojFijo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final ClaveHash HASH =
            ClaveHash.deHashExistente("$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW");

    @Mock
    UsuarioRepositoryPort repositorio;
    @Mock
    HasheadorContrasenaPort hasheador;
    @Mock
    TrazaAuditoria auditoria;

    private UsuarioService service;
    private final ContextoPeticion ctx = new ContextoPeticion("admin", RolUsuario.ADMIN, "ip", "cid");
    private final ContextoPeticion coordCtx = new ContextoPeticion("c", RolUsuario.COORDINADOR, "ip", "cid");

    @BeforeEach
    void setUp() {
        service = new UsuarioService(repositorio, hasheador, RelojFijo.en("2026-02-01T10:00:00Z"),
                new ParametrosFalsos(), new Autorizador(), auditoria);
        lenient().when(hasheador.hashear(any())).thenReturn(HASH);
        lenient().when(repositorio.guardar(any())).thenAnswer(i -> {
            Usuario u = i.getArgument(0);
            return u.getId() != null ? u
                    : Usuario.reconstruir(50L, u.getLogin(), u.getNombre(), u.getClave(), u.getRol(),
                    u.isActivo(), u.getIntentosFallidos(), u.getBloqueadoHasta(), u.getUltimoAcceso());
        });
    }

    @Test
    void crearCuentaValida() {
        when(repositorio.existeLogin("nuevo")).thenReturn(false);
        var vista = service.crear(new CrearUsuarioComando("nuevo", "Nombre Valido",
                "Password12".toCharArray(), "PROFESOR"), ctx);
        assertThat(vista.getId()).isEqualTo(50L);
        assertThat(vista.getRol()).isEqualTo("PROFESOR");
        verify(hasheador).hashear(any());
    }

    @Test
    void crearRechazaContrasenaCorta() {
        assertThatThrownBy(() -> service.crear(new CrearUsuarioComando("nuevo", "Nombre Valido",
                "corta".toCharArray(), "PROFESOR"), ctx)).isInstanceOf(ValidacionException.class);
    }

    @Test
    void crearRechazaLoginDuplicado() {
        when(repositorio.existeLogin("dup")).thenReturn(true);
        assertThatThrownBy(() -> service.crear(new CrearUsuarioComando("dup", "Nombre Valido",
                "Password12".toCharArray(), "PROFESOR"), ctx)).isInstanceOf(ConflictoDatosException.class);
    }

    @Test
    void crearRequiereRolAdmin() {
        assertThatThrownBy(() -> service.crear(new CrearUsuarioComando("x", "Nombre Valido",
                "Password12".toCharArray(), "PROFESOR"), coordCtx)).isInstanceOf(AutorizacionException.class);
    }

    @Test
    void actualizarYContrasenaYEstado() {
        Usuario u = Usuario.reconstruir(50L, "user", "Viejo Nombre", HASH, RolUsuario.PROFESOR,
                true, 2, null, null);
        when(repositorio.buscarPorId(50L)).thenReturn(Optional.of(u));

        service.actualizar(50L, new ActualizarUsuarioComando("Nuevo Nombre", "COORDINADOR"), ctx);
        service.cambiarContrasena(50L, new CambiarContrasenaComando("OtraClave12".toCharArray()), ctx);
        service.cambiarEstado(50L, false, ctx);
        service.desbloquear(50L, ctx);
        verify(repositorio).actualizarEstadoAcceso(any());
        service.eliminar(50L, ctx);
        verify(repositorio).eliminar(50L);
    }

    @Test
    void obtenerInexistenteLanza() {
        when(repositorio.buscarPorId(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.obtener(404L)).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void listarMapeaVista() {
        Usuario u = Usuario.reconstruir(1L, "user", "N", HASH, RolUsuario.ADMIN, true, 0, null, null);
        when(repositorio.listar(any(), any())).thenReturn(new Pagina<>(List.of(u), 0, 25, 1));
        assertThat(service.listar(null, 0, 0).getContenido()).hasSize(1);
    }
}
