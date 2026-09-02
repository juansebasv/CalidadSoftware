package com.tesiscalidad.horarios;

import com.tesiscalidad.horarios.application.dto.ContextoPeticion;
import com.tesiscalidad.horarios.application.dto.materia.CrearMateriaComando;
import com.tesiscalidad.horarios.application.dto.sesion.ProgramarSesionComando;
import com.tesiscalidad.horarios.application.dto.sesion.ReprogramarSesionComando;
import com.tesiscalidad.horarios.application.dto.usuario.ActualizarUsuarioComando;
import com.tesiscalidad.horarios.application.support.Autorizador;
import com.tesiscalidad.horarios.application.support.TrazaAuditoria;
import com.tesiscalidad.horarios.application.usecase.MateriaService;
import com.tesiscalidad.horarios.application.usecase.SesionClaseService;
import com.tesiscalidad.horarios.application.usecase.UsuarioService;
import com.tesiscalidad.horarios.domain.enums.DiaSemana;
import com.tesiscalidad.horarios.domain.enums.EstadoFranja;
import com.tesiscalidad.horarios.domain.enums.Jornada;
import com.tesiscalidad.horarios.domain.enums.OrigenRegistro;
import com.tesiscalidad.horarios.domain.enums.RolUsuario;
import com.tesiscalidad.horarios.domain.enums.TipoContrato;
import com.tesiscalidad.horarios.domain.enums.TipoMateria;
import com.tesiscalidad.horarios.domain.exception.CodigoError;
import com.tesiscalidad.horarios.domain.exception.ValidacionException;
import com.tesiscalidad.horarios.domain.model.DisponibilidadProfesor;
import com.tesiscalidad.horarios.domain.model.Materia;
import com.tesiscalidad.horarios.domain.model.Profesor;
import com.tesiscalidad.horarios.domain.model.SesionClase;
import com.tesiscalidad.horarios.domain.model.Usuario;
import com.tesiscalidad.horarios.domain.port.out.DisponibilidadRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.HasheadorContrasenaPort;
import com.tesiscalidad.horarios.domain.port.out.MateriaRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.PerfilRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.ProfesorRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.RelojPort;
import com.tesiscalidad.horarios.domain.port.out.SesionClaseRepositoryPort;
import com.tesiscalidad.horarios.domain.port.out.UsuarioRepositoryPort;
import com.tesiscalidad.horarios.domain.service.ValidadorProgramacionSesion;
import com.tesiscalidad.horarios.domain.support.Preconditions;
import com.tesiscalidad.horarios.domain.vo.ClaveHash;
import com.tesiscalidad.horarios.domain.vo.Credenciales;
import com.tesiscalidad.horarios.support.ParametrosFalsos;
import com.tesiscalidad.horarios.support.RelojFijo;
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
import static org.mockito.Mockito.when;

/** Pruebas dirigidas a ramas condicionales poco cubiertas. */
@ExtendWith(MockitoExtension.class)
class CoberturaRamasTest {

    private static final ClaveHash HASH =
            ClaveHash.deHashExistente("$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW");
    private final ContextoPeticion ctx = new ContextoPeticion("admin", RolUsuario.ADMIN, "ip", "cid");

    @Mock MateriaRepositoryPort materiaRepo;
    @Mock UsuarioRepositoryPort usuarioRepo;
    @Mock HasheadorContrasenaPort hasheador;
    @Mock SesionClaseRepositoryPort sesionRepo;
    @Mock ProfesorRepositoryPort profesorRepo;
    @Mock PerfilRepositoryPort perfilRepo;
    @Mock DisponibilidadRepositoryPort dispoRepo;
    @Mock TrazaAuditoria auditoria;

    private final RelojPort reloj = RelojFijo.en("2026-05-01T09:00:00Z");

    // ---- Preconditions: ramas de exito de cada guarda -------------------------
    @Test
    void preconditionsRamasDeExito() {
        assertThat(Preconditions.textoRequerido("  x ", "c")).isEqualTo("x");
        assertThat(Preconditions.longitud("abc", "c", 1, 5)).isEqualTo("abc");
        assertThat(Preconditions.rango(3, "c", 1, 5)).isEqualTo(3);
        assertThat(Preconditions.coincidePatron("A1", "c", "^[A-Z0-9]+$")).isEqualTo("A1");
        Preconditions.cumple(true, "ok");
        assertThatThrownBy(() -> Preconditions.textoRequerido(null, "c")).isInstanceOf(ValidacionException.class);
    }

    // ---- Credenciales: login ya en minusculas / sin espacios -----------------
    @Test
    void credencialesConLoginYaNormalizado() {
        Credenciales c = Credenciales.de("jdperez", "Password12".toCharArray());
        assertThat(c.login()).isEqualTo("jdperez");
    }

    // ---- CodigoError: catalogo completo -------------------------------------
    @Test
    void codigoErrorCatalogoCompleto() {
        for (CodigoError e : CodigoError.values()) {
            assertThat(e.getCodigo()).isNotBlank();
            assertThat(e.getMensajePorDefecto()).isNotBlank();
        }
    }

    // ---- MateriaService.crear con contexto de COORDINADOR (rama puedeGestionar) --
    @Test
    void materiaCreaConRolCoordinador() {
        MateriaService svc = new MateriaService(materiaRepo, new ParametrosFalsos(), new Autorizador(), auditoria);
        when(materiaRepo.existeCodigo(any())).thenReturn(false);
        when(materiaRepo.guardar(any())).thenAnswer(i -> {
            Materia m = i.getArgument(0);
            return Materia.reconstruir(1L, m.getCodigo().valor(), m.getNombre(), m.getTipo(),
                    m.getCreditos(), m.getIntensidadHoraria(), m.getSemestre(), m.isActiva());
        });
        ContextoPeticion coord = new ContextoPeticion("c", RolUsuario.COORDINADOR, "ip", "cid");
        assertThat(svc.crear(new CrearMateriaComando("INF999", "Materia Nueva", "P", 3, 4, 2), coord)
                .getCodigo()).isEqualTo("INF999");
    }

    // ---- UsuarioService.cambiarEstado activar (rama true) -------------------
    @Test
    void usuarioCambiarEstadoActivar() {
        UsuarioService svc = new UsuarioService(usuarioRepo, hasheador, reloj, new ParametrosFalsos(),
                new Autorizador(), auditoria);
        Usuario u = Usuario.reconstruir(1L, "u", "Nombre", HASH, RolUsuario.PROFESOR, false, 0, null, null);
        when(usuarioRepo.buscarPorId(1L)).thenReturn(Optional.of(u));
        lenient().when(usuarioRepo.guardar(any())).thenReturn(u);
        svc.cambiarEstado(1L, true, ctx);
        assertThat(u.isActivo()).isTrue();
        svc.actualizar(1L, new ActualizarUsuarioComando("Otro Nombre", "ADMIN"), ctx);
        assertThat(u.getRol()).isEqualTo(RolUsuario.ADMIN);
    }

    // ---- SesionClaseService: programar SIN docente (salta perfil/dispo) -----
    @Test
    void sesionProgramaSinDocenteYReprogramaSinDocente() {
        SesionClaseService svc = new SesionClaseService(sesionRepo, materiaRepo, profesorRepo, perfilRepo,
                dispoRepo, new ValidadorProgramacionSesion(), new ParametrosFalsos(), new Autorizador(), auditoria);
        when(materiaRepo.buscarPorId(1L)).thenReturn(Optional.of(
                Materia.reconstruir(1L, "INF100", "M", TipoMateria.TEORICA, 3, 4, 1, true)));
        when(sesionRepo.existeMateriaGrupoDia(any(), any(), any(), any())).thenReturn(false);
        when(sesionRepo.listarPorDia(any())).thenReturn(List.of());
        when(sesionRepo.guardar(any())).thenAnswer(i -> {
            SesionClase s = i.getArgument(0);
            return s.getId() != null ? s
                    : SesionClase.reconstruir(1L, s.getIdMateria(), s.getIdProfesor(), s.getGrupo(), s.getDia(),
                    s.getHora(), s.getDuracion(), s.getSemestre(), s.getJornada());
        });
        var v = svc.programar(new ProgramarSesionComando(1L, null, "A", "1", 8, 2, 1, "D"), ctx);
        assertThat(v.getNombreProfesor()).isEqualTo("(sin asignar)");

        when(sesionRepo.buscarPorId(1L)).thenReturn(Optional.of(
                SesionClase.reconstruir(1L, 1L, null, "A", DiaSemana.LUNES, 8, 2, 1, Jornada.DIURNA)));
        var r = svc.reprogramar(1L, new ReprogramarSesionComando("2", 10, 2, "D"), ctx);
        assertThat(r.getDiaEtiqueta()).isEqualTo("Martes");
    }

    // ---- Validador: disponibilidad justa (rama de cobertura completa) -------
    @Test
    void validadorDisponibilidadExactaOk() {
        ValidadorProgramacionSesion v = new ValidadorProgramacionSesion();
        SesionClase s = SesionClase.crear(1L, 9L, "A", DiaSemana.LUNES, 8, 2, 1, Jornada.DIURNA);
        List<DisponibilidadProfesor> disp = List.of(
                DisponibilidadProfesor.reconstruir(1L, 9L, DiaSemana.LUNES, 8, EstadoFranja.LIBRE, OrigenRegistro.MANUAL),
                DisponibilidadProfesor.reconstruir(2L, 9L, DiaSemana.LUNES, 9, EstadoFranja.OCUPADA, OrigenRegistro.MANUAL));
        v.validar(s, true, disp, List.of());
    }

    // ---- Profesor sin cuenta: rama COALESCE / id_usuario null ---------------
    @Test
    void profesorSinCuenta() {
        Profesor p = Profesor.crear("DOC-9", "Docente Sin Cuenta", TipoContrato.OCASIONAL, false);
        assertThat(p.getIdUsuario()).isNull();
        assertThat(p.isDisponible()).isFalse();
    }
}
