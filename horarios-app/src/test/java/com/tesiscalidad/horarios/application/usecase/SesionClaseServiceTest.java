package com.tesiscalidad.horarios.application.usecase;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.sesion.ProgramarSesionComando;
import com.tesiscalidad.horarios.application.dto.sesion.ReprogramarSesionComando;
import com.tesiscalidad.horarios.application.support.Autorizador;
import com.tesiscalidad.horarios.application.support.TrazaAuditoria;
import com.tesiscalidad.horarios.domain.enums.DiaSemana;
import com.tesiscalidad.horarios.domain.enums.EstadoFranja;
import com.tesiscalidad.horarios.domain.enums.Jornada;
import com.tesiscalidad.horarios.domain.enums.OrigenRegistro;
import com.tesiscalidad.horarios.domain.enums.RolUsuario;
import com.tesiscalidad.horarios.domain.enums.TipoContrato;
import com.tesiscalidad.horarios.domain.enums.TipoMateria;
import com.tesiscalidad.horarios.domain.exception.ConflictoDatosException;
import com.tesiscalidad.horarios.domain.exception.ReglaNegocioException;
import com.tesiscalidad.horarios.domain.model.DisponibilidadProfesor;
import com.tesiscalidad.horarios.domain.model.Materia;
import com.tesiscalidad.horarios.domain.model.Profesor;
import com.tesiscalidad.horarios.domain.model.SesionClase;
import com.tesiscalidad.horarios.domain.port.out.DisponibilidadRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.MateriaRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.PerfilRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.ProfesorRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.SesionClaseRepositoryPort;
import com.tesiscalidad.horarios.domain.service.ValidadorProgramacionSesion;
import com.tesiscalidad.horarios.support.ParametrosFalsos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SesionClaseServiceTest {

    @Mock SesionClaseRepositoryPort repositorio;
    @Mock MateriaRepositoryPort materiaRepositorio;
    @Mock ProfesorRepositoryPort profesorRepositorio;
    @Mock PerfilRepositoryPort perfilRepositorio;
    @Mock DisponibilidadRepositoryPort disponibilidadRepositorio;
    @Mock TrazaAuditoria auditoria;

    private SesionClaseService service;
    private final ContextoPeticion ctx = new ContextoPeticion("admin", RolUsuario.ADMIN, "ip", "cid");

    @BeforeEach
    void setUp() {
        service = new SesionClaseService(repositorio, materiaRepositorio, profesorRepositorio, perfilRepositorio,
                disponibilidadRepositorio, new ValidadorProgramacionSesion(), new ParametrosFalsos(),
                new Autorizador(), auditoria);
        lenient().when(materiaRepositorio.buscarPorId(1L)).thenReturn(Optional.of(
                Materia.reconstruir(1L, "INF100", "Materia", TipoMateria.TEORICA, 3, 4, 1, true)));
        lenient().when(profesorRepositorio.buscarPorId(10L)).thenReturn(Optional.of(
                Profesor.reconstruir(10L, "DOC-10", "Docente", TipoContrato.PLANTA, true, null, true)));
        lenient().when(repositorio.guardar(any())).thenAnswer(i -> {
            SesionClase s = i.getArgument(0);
            return s.getId() != null ? s
                    : SesionClase.reconstruir(77L, s.getIdMateria(), s.getIdProfesor(), s.getGrupo(), s.getDia(),
                    s.getHora(), s.getDuracion(), s.getSemestre(), s.getJornada());
        });
    }

    private List<DisponibilidadProfesor> disponibilidadCompleta() {
        return IntStream.rangeClosed(6, 22)
                .mapToObj(h -> DisponibilidadProfesor.reconstruir((long) h, 10L, DiaSemana.LUNES, h,
                        EstadoFranja.LIBRE, OrigenRegistro.GENERADO))
                .collect(Collectors.toList());
    }

    @Test
    void programarSesionValida() {
        when(perfilRepositorio.existe(10L, 1L)).thenReturn(true);
        when(disponibilidadRepositorio.listarPorProfesor(10L)).thenReturn(disponibilidadCompleta());
        when(repositorio.listarPorDia(DiaSemana.LUNES)).thenReturn(List.of());
        when(repositorio.existeMateriaGrupoDia(1L, "A", DiaSemana.LUNES, null)).thenReturn(false);

        var v = service.programar(new ProgramarSesionComando(1L, 10L, "A", "1", 8, 2, 1, "D"), ctx);
        assertThat(v.getId()).isEqualTo(77L);
        verify(auditoria).exito(any(), any(), any(), any(), any());
    }

    @Test
    void programarRechazaMateriaInactiva() {
        when(materiaRepositorio.buscarPorId(2L)).thenReturn(Optional.of(
                Materia.reconstruir(2L, "INF200", "M", TipoMateria.TEORICA, 3, 4, 1, false)));
        assertThatThrownBy(() -> service.programar(
                new ProgramarSesionComando(2L, null, "A", "1", 8, 2, 1, "D"), ctx))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void programarRechazaMateriaGrupoDiaDuplicado() {
        when(repositorio.existeMateriaGrupoDia(1L, "A", DiaSemana.LUNES, null)).thenReturn(true);
        assertThatThrownBy(() -> service.programar(
                new ProgramarSesionComando(1L, null, "A", "1", 8, 2, 1, "D"), ctx))
                .isInstanceOf(ConflictoDatosException.class);
    }

    @Test
    void programarRechazaDocenteSinPerfil() {
        when(perfilRepositorio.existe(10L, 1L)).thenReturn(false);
        when(disponibilidadRepositorio.listarPorProfesor(10L)).thenReturn(disponibilidadCompleta());
        when(repositorio.listarPorDia(DiaSemana.LUNES)).thenReturn(List.of());
        when(repositorio.existeMateriaGrupoDia(any(), any(), any(), any())).thenReturn(false);
        assertThatThrownBy(() -> service.programar(
                new ProgramarSesionComando(1L, 10L, "A", "1", 8, 2, 1, "D"), ctx))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void reprogramarYReasignarDocente() {
        SesionClase existente = SesionClase.reconstruir(77L, 1L, 10L, "A", DiaSemana.LUNES, 8, 2, 1, Jornada.DIURNA);
        when(repositorio.buscarPorId(77L)).thenReturn(Optional.of(existente));
        when(perfilRepositorio.existe(10L, 1L)).thenReturn(true);
        when(disponibilidadRepositorio.listarPorProfesor(10L)).thenReturn(disponibilidadCompleta());
        when(repositorio.listarPorDia(any())).thenReturn(List.of());
        when(repositorio.existeMateriaGrupoDia(any(), any(), any(), anyLong())).thenReturn(false);

        service.reprogramar(77L, new ReprogramarSesionComando("1", 10, 2, "D"), ctx);
        service.asignarDocente(77L, 10L, ctx);
        verify(repositorio, org.mockito.Mockito.atLeast(2)).guardar(any());
    }

    @Test
    void quitarDocenteYCancelarYListar() {
        SesionClase existente = SesionClase.reconstruir(77L, 1L, 10L, "A", DiaSemana.LUNES, 8, 2, 1, Jornada.DIURNA);
        when(repositorio.buscarPorId(77L)).thenReturn(Optional.of(existente));
        service.quitarDocente(77L, ctx);
        service.cancelar(77L, ctx);
        verify(repositorio).eliminar(77L);

        when(repositorio.listar(any(), any())).thenReturn(
                new com.tesiscalidad.horarios.domain.support.Pagina<>(List.of(existente), 0, 25, 1));
        assertThat(service.listar(1, 0, 0).getContenido()).hasSize(1);
        assertThat(service.obtener(77L).getId()).isEqualTo(77L);
    }
}
