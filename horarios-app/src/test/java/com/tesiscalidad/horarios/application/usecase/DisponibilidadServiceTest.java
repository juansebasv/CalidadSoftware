package com.tesiscalidad.horarios.application.usecase;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.support.Autorizador;
import com.tesiscalidad.horarios.application.support.TrazaAuditoria;
import com.tesiscalidad.horarios.domain.enums.DiaSemana;
import com.tesiscalidad.horarios.domain.enums.EstadoFranja;
import com.tesiscalidad.horarios.domain.enums.OrigenRegistro;
import com.tesiscalidad.horarios.domain.enums.RolUsuario;
import com.tesiscalidad.horarios.domain.enums.TipoContrato;
import com.tesiscalidad.horarios.domain.exception.ConflictoDatosException;
import com.tesiscalidad.horarios.domain.exception.RecursoNoEncontradoException;
import com.tesiscalidad.horarios.domain.exception.ValidacionException;
import com.tesiscalidad.horarios.domain.model.DisponibilidadProfesor;
import com.tesiscalidad.horarios.domain.model.Profesor;
import com.tesiscalidad.horarios.domain.port.out.DisponibilidadRepositoryPort;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisponibilidadServiceTest {

    @Mock DisponibilidadRepositoryPort repositorio;
    @Mock ProfesorRepositoryPort profesorRepositorio;
    @Mock TrazaAuditoria auditoria;

    private DisponibilidadService service;
    private final ContextoPeticion ctx = new ContextoPeticion("admin", RolUsuario.ADMIN, "ip", "cid");

    @BeforeEach
    void setUp() {
        service = new DisponibilidadService(repositorio, profesorRepositorio, new Autorizador(), auditoria);
        lenient().when(profesorRepositorio.buscarPorId(1L)).thenReturn(Optional.of(
                Profesor.reconstruir(1L, "DOC-1", "Docente", TipoContrato.PLANTA, true, null, true)));
        lenient().when(repositorio.guardar(any())).thenAnswer(i -> {
            DisponibilidadProfesor d = i.getArgument(0);
            return DisponibilidadProfesor.reconstruir(5L, d.getIdProfesor(), d.getDia(), d.getHora(),
                    d.getEstado(), d.getOrigen());
        });
    }

    @Test
    void listar() {
        when(repositorio.listarPorProfesor(1L)).thenReturn(List.of(
                DisponibilidadProfesor.reconstruir(1L, 1L, DiaSemana.LUNES, 8, EstadoFranja.LIBRE, OrigenRegistro.MANUAL)));
        assertThat(service.listar(1L)).hasSize(1);
    }

    @Test
    void marcarFranjaNueva() {
        when(repositorio.buscar(1L, DiaSemana.LUNES, 8)).thenReturn(Optional.empty());
        var v = service.marcar(1L, "1", 8, ctx);
        assertThat(v.getHora()).isEqualTo(8);
        verify(auditoria).exito(any(), any(), any(), any(), any());
    }

    @Test
    void marcarRechazaFranjaDuplicada() {
        when(repositorio.buscar(1L, DiaSemana.LUNES, 8)).thenReturn(Optional.of(
                DisponibilidadProfesor.reconstruir(1L, 1L, DiaSemana.LUNES, 8, EstadoFranja.LIBRE, OrigenRegistro.MANUAL)));
        assertThatThrownBy(() -> service.marcar(1L, "1", 8, ctx)).isInstanceOf(ConflictoDatosException.class);
    }

    @Test
    void liberarExistenteYNoExistente() {
        when(repositorio.buscar(1L, DiaSemana.LUNES, 8)).thenReturn(Optional.of(
                DisponibilidadProfesor.reconstruir(9L, 1L, DiaSemana.LUNES, 8, EstadoFranja.LIBRE, OrigenRegistro.MANUAL)));
        service.liberar(1L, "1", 8, ctx);
        verify(repositorio).eliminar(9L);

        when(repositorio.buscar(1L, DiaSemana.MARTES, 9)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.liberar(1L, "2", 9, ctx))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void generarRejillaCreaFranjasFaltantes() {
        when(repositorio.buscar(anyLong(), any(), anyInt())).thenReturn(Optional.empty());
        int creadas = service.generarRejilla(1L, List.of("1", "2"), 8, 10, ctx);
        assertThat(creadas).isEqualTo(6); // 2 dias x 3 horas
        verify(repositorio).guardarLote(any());
    }

    @Test
    void generarRejillaRangoInvalido() {
        assertThatThrownBy(() -> service.generarRejilla(1L, List.of(), 8, 10, ctx))
                .isInstanceOf(ValidacionException.class);
        assertThatThrownBy(() -> service.generarRejilla(1L, List.of("1"), 20, 10, ctx))
                .isInstanceOf(ValidacionException.class);
    }

    @Test
    void limpiar() {
        service.limpiar(1L, ctx);
        verify(repositorio).eliminarPorProfesor(1L);
    }

    @Test
    void operacionConProfesorInexistente() {
        when(profesorRepositorio.buscarPorId(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.listar(404L)).isInstanceOf(RecursoNoEncontradoException.class);
    }
}
