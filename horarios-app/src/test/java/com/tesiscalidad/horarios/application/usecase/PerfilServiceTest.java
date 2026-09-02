package com.tesiscalidad.horarios.application.usecase;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.support.Autorizador;
import com.tesiscalidad.horarios.application.support.TrazaAuditoria;
import com.tesiscalidad.horarios.domain.enums.RolUsuario;
import com.tesiscalidad.horarios.domain.enums.TipoContrato;
import com.tesiscalidad.horarios.domain.enums.TipoMateria;
import com.tesiscalidad.horarios.domain.exception.ConflictoDatosException;
import com.tesiscalidad.horarios.domain.exception.RecursoNoEncontradoException;
import com.tesiscalidad.horarios.domain.model.Materia;
import com.tesiscalidad.horarios.domain.model.Perfil;
import com.tesiscalidad.horarios.domain.model.Profesor;
import com.tesiscalidad.horarios.domain.port.out.MateriaRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.PerfilRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.ProfesorRepositoryPort;
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
class PerfilServiceTest {

    @Mock PerfilRepositoryPort perfilRepositorio;
    @Mock ProfesorRepositoryPort profesorRepositorio;
    @Mock MateriaRepositoryPort materiaRepositorio;
    @Mock TrazaAuditoria auditoria;

    private PerfilService service;
    private final ContextoPeticion ctx = new ContextoPeticion("admin", RolUsuario.ADMIN, "ip", "cid");

    @BeforeEach
    void setUp() {
        service = new PerfilService(perfilRepositorio, profesorRepositorio, materiaRepositorio,
                new Autorizador(), auditoria);
        lenient().when(profesorRepositorio.buscarPorId(1L)).thenReturn(Optional.of(
                Profesor.reconstruir(1L, "DOC-1", "Docente", TipoContrato.PLANTA, true, null, true)));
    }

    private Materia materia(long id, String codigo) {
        return Materia.reconstruir(id, codigo, "M " + id, TipoMateria.TEORICA, 3, 4, 1, true);
    }

    @Test
    void listaHabilitadasYDisponibles() {
        when(materiaRepositorio.listarActivas()).thenReturn(List.of(materia(10, "AAA"), materia(20, "BBB")));
        when(perfilRepositorio.listarPorProfesor(1L)).thenReturn(List.of(Perfil.reconstruir(1L, 1L, 10L)));
        assertThat(service.materiasHabilitadas(1L)).extracting("id").containsExactly(10L);
        assertThat(service.materiasDisponibles(1L)).extracting("id").containsExactly(20L);
    }

    @Test
    void habilitarNuevaMateria() {
        when(materiaRepositorio.buscarPorId(10L)).thenReturn(Optional.of(materia(10, "AAA")));
        when(perfilRepositorio.existe(1L, 10L)).thenReturn(false);
        service.habilitar(1L, 10L, ctx);
        verify(perfilRepositorio).guardar(any(Perfil.class));
        verify(auditoria).exito(any(), any(), any(), any(), any());
    }

    @Test
    void habilitarRechazaDuplicado() {
        when(materiaRepositorio.buscarPorId(10L)).thenReturn(Optional.of(materia(10, "AAA")));
        when(perfilRepositorio.existe(1L, 10L)).thenReturn(true);
        assertThatThrownBy(() -> service.habilitar(1L, 10L, ctx)).isInstanceOf(ConflictoDatosException.class);
    }

    @Test
    void habilitarConMateriaInexistente() {
        when(materiaRepositorio.buscarPorId(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.habilitar(1L, 99L, ctx))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void deshabilitarExistenteYNoExistente() {
        when(perfilRepositorio.existe(1L, 10L)).thenReturn(true);
        service.deshabilitar(1L, 10L, ctx);
        verify(perfilRepositorio).eliminar(1L, 10L);

        when(perfilRepositorio.existe(1L, 77L)).thenReturn(false);
        assertThatThrownBy(() -> service.deshabilitar(1L, 77L, ctx))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void operacionesConProfesorInexistente() {
        when(profesorRepositorio.buscarPorId(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.materiasHabilitadas(404L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
