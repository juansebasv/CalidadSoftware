package com.tesiscalidad.horarios.application.usecase;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.profesor.ActualizarProfesorComando;
import com.tesiscalidad.horarios.application.dto.profesor.CrearProfesorComando;
import com.tesiscalidad.horarios.application.dto.profesor.ProfesorVista;
import com.tesiscalidad.horarios.application.support.Autorizador;
import com.tesiscalidad.horarios.application.support.TrazaAuditoria;
import com.tesiscalidad.horarios.domain.enums.RolUsuario;
import com.tesiscalidad.horarios.domain.enums.TipoContrato;
import com.tesiscalidad.horarios.domain.exception.ConflictoDatosException;
import com.tesiscalidad.horarios.domain.exception.RecursoNoEncontradoException;
import com.tesiscalidad.horarios.domain.model.Profesor;
import com.tesiscalidad.horarios.domain.model.Usuario;
import com.tesiscalidad.horarios.domain.enums.RolUsuario;
import com.tesiscalidad.horarios.domain.port.out.ProfesorRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.UsuarioRepositoryPort;
import com.tesiscalidad.horarios.domain.support.Pagina;
import com.tesiscalidad.horarios.domain.vo.ClaveHash;
import com.tesiscalidad.horarios.support.ParametrosFalsos;
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
class ProfesorServiceTest {

    private static final ClaveHash HASH =
            ClaveHash.deHashExistente("$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW");

    @Mock
    ProfesorRepositoryPort repositorio;
    @Mock
    UsuarioRepositoryPort usuarioRepositorio;
    @Mock
    TrazaAuditoria auditoria;

    private ProfesorService service;
    private final ContextoPeticion ctx = new ContextoPeticion("admin", RolUsuario.ADMIN, "ip", "cid");

    @BeforeEach
    void setUp() {
        service = new ProfesorService(repositorio, usuarioRepositorio, new ParametrosFalsos(),
                new Autorizador(), auditoria);
        lenient().when(repositorio.guardar(any())).thenAnswer(i -> {
            Profesor p = i.getArgument(0);
            return p.getId() != null ? p
                    : Profesor.reconstruir(7L, p.getCodigo().valor(), p.getNombre(), p.getTipoContrato(),
                    p.isDisponible(), p.getIdUsuario(), p.isActivo());
        });
    }

    @Test
    void crearProfesorSinCuenta() {
        when(repositorio.existeCodigo("DOC-9")).thenReturn(false);
        ProfesorVista v = service.crear(new CrearProfesorComando("DOC-9", "Docente Uno", "PLANTA", true, null), ctx);
        assertThat(v.getId()).isEqualTo(7L);
        assertThat(v.getIdUsuario()).isNull();
    }

    @Test
    void crearProfesorConCuentaValida() {
        when(repositorio.existeCodigo(any())).thenReturn(false);
        when(usuarioRepositorio.buscarPorId(3L)).thenReturn(Optional.of(
                Usuario.reconstruir(3L, "user", "User", HASH, RolUsuario.PROFESOR, true, 0, null, null)));
        when(repositorio.buscarPorIdUsuario(3L)).thenReturn(Optional.empty());
        ProfesorVista v = service.crear(new CrearProfesorComando("DOC-10", "Docente Dos", "CATEDRA", true, 3L), ctx);
        assertThat(v.getIdUsuario()).isEqualTo(3L);
    }

    @Test
    void crearProfesorConCuentaInexistente() {
        when(repositorio.existeCodigo(any())).thenReturn(false);
        when(usuarioRepositorio.buscarPorId(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.crear(
                new CrearProfesorComando("DOC-11", "Docente", "PLANTA", true, 404L), ctx))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void crearProfesorConCuentaYaVinculada() {
        when(repositorio.existeCodigo(any())).thenReturn(false);
        when(usuarioRepositorio.buscarPorId(3L)).thenReturn(Optional.of(
                Usuario.reconstruir(3L, "u", "U", HASH, RolUsuario.PROFESOR, true, 0, null, null)));
        when(repositorio.buscarPorIdUsuario(3L)).thenReturn(Optional.of(
                Profesor.reconstruir(99L, "DOC-X", "Otro", TipoContrato.PLANTA, true, 3L, true)));
        assertThatThrownBy(() -> service.crear(
                new CrearProfesorComando("DOC-12", "Docente", "PLANTA", true, 3L), ctx))
                .isInstanceOf(ConflictoDatosException.class);
    }

    @Test
    void crearRechazaCodigoDuplicado() {
        when(repositorio.existeCodigo("DUP")).thenReturn(true);
        assertThatThrownBy(() -> service.crear(
                new CrearProfesorComando("DUP", "Docente", "PLANTA", true, null), ctx))
                .isInstanceOf(ConflictoDatosException.class);
    }

    @Test
    void actualizarDesvinculaCuandoIdEsNull() {
        Profesor existente = Profesor.reconstruir(7L, "DOC-7", "Viejo", TipoContrato.PLANTA, true, 5L, true);
        when(repositorio.buscarPorId(7L)).thenReturn(Optional.of(existente));
        ProfesorVista v = service.actualizar(7L,
                new ActualizarProfesorComando("Nuevo", "OCASIONAL", false, null), ctx);
        assertThat(v.getIdUsuario()).isNull();
        assertThat(v.isDisponible()).isFalse();
    }

    @Test
    void cambiarEstadoEliminarObtenerListar() {
        Profesor p = Profesor.reconstruir(7L, "DOC-7", "N", TipoContrato.PLANTA, true, null, true);
        when(repositorio.buscarPorId(7L)).thenReturn(Optional.of(p));
        service.cambiarEstado(7L, false, ctx);
        service.eliminar(7L, ctx);
        verify(repositorio).eliminar(7L);
        assertThat(service.obtener(7L).getCodigo()).isEqualTo("DOC-7");
        when(repositorio.listar(any(), any())).thenReturn(new Pagina<>(List.of(p), 0, 25, 1));
        assertThat(service.listar(null, 0, 10).getContenido()).hasSize(1);
        when(repositorio.listarActivos()).thenReturn(List.of(p));
        assertThat(service.listarActivos()).hasSize(1);
    }
}
