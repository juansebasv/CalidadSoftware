package com.tesiscalidad.horarios.application.usecase;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.materia.ActualizarMateriaComando;
import com.tesiscalidad.horarios.application.dto.materia.CrearMateriaComando;
import com.tesiscalidad.horarios.application.dto.materia.MateriaVista;
import com.tesiscalidad.horarios.application.support.Autorizador;
import com.tesiscalidad.horarios.application.support.TrazaAuditoria;
import com.tesiscalidad.horarios.domain.enums.RolUsuario;
import com.tesiscalidad.horarios.domain.enums.TipoMateria;
import com.tesiscalidad.horarios.domain.exception.ConflictoDatosException;
import com.tesiscalidad.horarios.domain.exception.RecursoNoEncontradoException;
import com.tesiscalidad.horarios.domain.exception.AutorizacionException;
import com.tesiscalidad.horarios.domain.model.Materia;
import com.tesiscalidad.horarios.domain.port.out.MateriaRepositoryPort;
import com.tesiscalidad.horarios.domain.support.Pagina;
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
class MateriaServiceTest {

    @Mock
    MateriaRepositoryPort repositorio;
    @Mock
    TrazaAuditoria auditoria;

    private MateriaService service;
    private final ContextoPeticion ctx = new ContextoPeticion("admin", RolUsuario.ADMIN, "ip", "cid");
    private final ContextoPeticion profesorCtx = new ContextoPeticion("p", RolUsuario.PROFESOR, "ip", "cid");

    @BeforeEach
    void setUp() {
        service = new MateriaService(repositorio, new ParametrosFalsos(), new Autorizador(), auditoria);
        lenient().when(repositorio.guardar(any())).thenAnswer(i -> {
            Materia m = i.getArgument(0);
            return m.getId() != null ? m
                    : Materia.reconstruir(99L, m.getCodigo().valor(), m.getNombre(), m.getTipo(),
                    m.getCreditos(), m.getIntensidadHoraria(), m.getSemestre(), m.isActiva());
        });
    }

    @Test
    void crearMateriaValida() {
        when(repositorio.existeCodigo("INF999")).thenReturn(false);
        MateriaVista v = service.crear(new CrearMateriaComando("INF999", "Nueva Materia", "T", 3, 4, 2), ctx);
        assertThat(v.getId()).isEqualTo(99L);
        assertThat(v.getCodigo()).isEqualTo("INF999");
        verify(auditoria).exito(any(), any(), any(), any(), any());
    }

    @Test
    void crearRechazaCodigoDuplicado() {
        when(repositorio.existeCodigo("DUP")).thenReturn(true);
        assertThatThrownBy(() -> service.crear(new CrearMateriaComando("DUP", "X Materia", "T", 3, 4, 1), ctx))
                .isInstanceOf(ConflictoDatosException.class);
    }

    @Test
    void crearRequiereRolGestor() {
        assertThatThrownBy(() -> service.crear(
                new CrearMateriaComando("INF1", "X Materia", "T", 3, 4, 1), profesorCtx))
                .isInstanceOf(AutorizacionException.class);
    }

    @Test
    void actualizarMateriaExistente() {
        Materia existente = Materia.reconstruir(5L, "INF500", "Vieja", TipoMateria.TEORICA, 3, 4, 3, true);
        when(repositorio.buscarPorId(5L)).thenReturn(Optional.of(existente));
        MateriaVista v = service.actualizar(5L, new ActualizarMateriaComando("Nueva", "P", 4, 6, 4), ctx);
        assertThat(v.getNombre()).isEqualTo("Nueva");
        assertThat(v.getTipo()).isEqualTo("P");
    }

    @Test
    void actualizarInexistenteLanza() {
        when(repositorio.buscarPorId(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.actualizar(404L,
                new ActualizarMateriaComando("N", "T", 3, 4, 1), ctx))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void cambiarEstadoYEliminar() {
        Materia existente = Materia.reconstruir(5L, "INF500", "M", TipoMateria.TEORICA, 3, 4, 3, true);
        when(repositorio.buscarPorId(5L)).thenReturn(Optional.of(existente));
        service.cambiarEstado(5L, false, ctx);
        service.eliminar(5L, ctx);
        verify(repositorio).eliminar(5L);
    }

    @Test
    void obtenerYListar() {
        Materia m = Materia.reconstruir(1L, "INF100", "M", TipoMateria.TEORICA, 3, 4, 1, true);
        when(repositorio.buscarPorId(1L)).thenReturn(Optional.of(m));
        assertThat(service.obtener(1L).getCodigo()).isEqualTo("INF100");

        when(repositorio.listar(any(), any(), any())).thenReturn(new Pagina<>(List.of(m), 0, 25, 1));
        assertThat(service.listar("inf", null, 0, 0).getContenido()).hasSize(1);

        when(repositorio.listarActivas()).thenReturn(List.of(m));
        assertThat(service.listarActivas()).hasSize(1);
    }
}
