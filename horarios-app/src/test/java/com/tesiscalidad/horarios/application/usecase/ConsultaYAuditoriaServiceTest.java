package com.tesiscalidad.horarios.application.usecase;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.support.Autorizador;
import com.tesiscalidad.horarios.domain.enums.AccionAuditoria;
import com.tesiscalidad.horarios.domain.enums.DiaSemana;
import com.tesiscalidad.horarios.domain.enums.Jornada;
import com.tesiscalidad.horarios.domain.enums.ResultadoAuditoria;
import com.tesiscalidad.horarios.domain.enums.RolUsuario;
import com.tesiscalidad.horarios.domain.enums.TipoContrato;
import com.tesiscalidad.horarios.domain.enums.TipoMateria;
import com.tesiscalidad.horarios.domain.exception.AutorizacionException;
import com.tesiscalidad.horarios.domain.model.Materia;
import com.tesiscalidad.horarios.domain.model.Profesor;
import com.tesiscalidad.horarios.domain.model.RegistroAuditoria;
import com.tesiscalidad.horarios.domain.model.SesionClase;
import com.tesiscalidad.horarios.domain.port.out.AuditoriaRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.MateriaRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.ProfesorRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.SesionClaseRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.UsuarioRepositoryPort;
import com.tesiscalidad.horarios.domain.service.DetectorConflictosHorario;
import com.tesiscalidad.horarios.domain.support.Pagina;
import com.tesiscalidad.horarios.support.ParametrosFalsos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaYAuditoriaServiceTest {

    @Mock SesionClaseRepositoryPort sesionRepositorio;
    @Mock MateriaRepositoryPort materiaRepositorio;
    @Mock ProfesorRepositoryPort profesorRepositorio;
    @Mock UsuarioRepositoryPort usuarioRepositorio;
    @Mock AuditoriaRepositoryPort auditoriaRepositorio;

    private ConsultaHorarioService consulta;
    private AuditoriaConsultaService auditoriaConsulta;

    @BeforeEach
    void setUp() {
        consulta = new ConsultaHorarioService(sesionRepositorio, materiaRepositorio, profesorRepositorio,
                usuarioRepositorio, new DetectorConflictosHorario());
        auditoriaConsulta = new AuditoriaConsultaService(auditoriaRepositorio, new ParametrosFalsos(),
                new Autorizador());
        lenient().when(materiaRepositorio.listarActivas()).thenReturn(List.of(
                Materia.reconstruir(1L, "INF100", "Materia", TipoMateria.TEORICA, 3, 4, 1, true)));
        lenient().when(profesorRepositorio.listarActivos()).thenReturn(List.of(
                Profesor.reconstruir(10L, "DOC-10", "Docente", TipoContrato.PLANTA, true, null, true)));
    }

    private SesionClase sesion(long id, Long prof, DiaSemana dia, int hora) {
        return SesionClase.reconstruir(id, 1L, prof, "A", dia, hora, 2, 1, Jornada.DIURNA);
    }

    @Test
    void horarioPorProfesorYSemestre() {
        when(sesionRepositorio.listarPorProfesor(10L)).thenReturn(List.of(sesion(1L, 10L, DiaSemana.LUNES, 8)));
        assertThat(consulta.horarioDeProfesor(10L)).hasSize(1)
                .first().extracting("nombreProfesor").isEqualTo("Docente");

        when(sesionRepositorio.listarPorSemestreYJornada(1, "D")).thenReturn(List.of(sesion(2L, null, DiaSemana.MARTES, 10)));
        assertThat(consulta.horarioPorSemestre(1, "D")).first()
                .extracting("nombreProfesor").isEqualTo("(sin asignar)");
    }

    @Test
    void detectarConflictosYTablero() {
        List<SesionClase> sesiones = List.of(
                sesion(1L, 10L, DiaSemana.LUNES, 8),
                sesion(2L, 10L, DiaSemana.LUNES, 9));
        when(sesionRepositorio.listarTodas()).thenReturn(sesiones);
        assertThat(consulta.detectarConflictos()).isNotEmpty();

        when(materiaRepositorio.listar(any(), any(), any())).thenReturn(new Pagina<>(List.of(), 0, 1, 12));
        when(profesorRepositorio.listar(any(), any())).thenReturn(new Pagina<>(List.of(), 0, 1, 8));
        when(usuarioRepositorio.listar(any(), any())).thenReturn(new Pagina<>(List.of(), 0, 1, 16));
        var t = consulta.tablero();
        assertThat(t.getTotalMaterias()).isEqualTo(12);
        assertThat(t.getTotalSesiones()).isEqualTo(2);
        assertThat(t.getTotalConflictos()).isPositive();
        assertThat(t.getSesionesPorDia()).isNotEmpty();
    }

    @Test
    void auditoriaListaConAutorizacion() {
        RegistroAuditoria r = RegistroAuditoria.reconstruir(1L, Instant.now(), "admin",
                AccionAuditoria.LOGIN, null, null, ResultadoAuditoria.EXITO, "ok", "ip", "cid");
        when(auditoriaRepositorio.listar(any(), any(), any())).thenReturn(new Pagina<>(List.of(r), 0, 25, 1));
        ContextoPeticion ctx = new ContextoPeticion("admin", RolUsuario.ADMIN, "ip", "cid");
        assertThat(auditoriaConsulta.listar("admin", "LOGIN", 0, 0, ctx).getContenido()).hasSize(1);
        assertThat(auditoriaConsulta.listar("", "", 0, 0, ctx).getContenido()).hasSize(1);
    }

    @Test
    void auditoriaRechazaRolSinPermiso() {
        ContextoPeticion profesor = new ContextoPeticion("p", RolUsuario.PROFESOR, "ip", "cid");
        assertThatThrownBy(() -> auditoriaConsulta.listar(null, null, 0, 0, profesor))
                .isInstanceOf(AutorizacionException.class);
    }
}
