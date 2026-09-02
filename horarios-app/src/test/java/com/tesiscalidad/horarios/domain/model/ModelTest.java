package com.tesiscalidad.horarios.domain.model;

import com.tesiscalidad.horarios.domain.enums.AccionAuditoria;
import com.tesiscalidad.horarios.domain.enums.DiaSemana;
import com.tesiscalidad.horarios.domain.enums.Jornada;
import com.tesiscalidad.horarios.domain.enums.OrigenRegistro;
import com.tesiscalidad.horarios.domain.enums.ResultadoAuditoria;
import com.tesiscalidad.horarios.domain.enums.RolUsuario;
import com.tesiscalidad.horarios.domain.enums.TipoContrato;
import com.tesiscalidad.horarios.domain.enums.TipoMateria;
import com.tesiscalidad.horarios.domain.exception.AutenticacionException;
import com.tesiscalidad.horarios.domain.exception.ValidacionException;
import com.tesiscalidad.horarios.domain.vo.ClaveHash;
import com.tesiscalidad.horarios.domain.vo.PoliticaBloqueo;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModelTest {

    private static final ClaveHash HASH =
            ClaveHash.deHashExistente("$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW");
    private static final Instant T0 = Instant.parse("2026-01-01T08:00:00Z");
    private static final PoliticaBloqueo POLITICA = PoliticaBloqueo.de(3, 15);

    @Test
    void usuarioCrearValida() {
        Usuario u = Usuario.crear("Nuevo.User", "Nombre Valido", HASH, RolUsuario.PROFESOR);
        assertThat(u.getLogin()).isEqualTo("nuevo.user");
        assertThat(u.getId()).isNull();
        assertThat(u.isActivo()).isTrue();
        assertThatThrownBy(() -> Usuario.crear("ab", "Nombre", HASH, RolUsuario.PROFESOR))
                .isInstanceOf(ValidacionException.class);
        assertThatThrownBy(() -> Usuario.crear("valido", "no", HASH, RolUsuario.PROFESOR))
                .isInstanceOf(ValidacionException.class);
    }

    @Test
    void usuarioBloqueoTrasIntentosFallidos() {
        Usuario u = reconstruirUsuario();
        assertThat(u.registrarIntentoFallido(POLITICA, T0)).isFalse();
        assertThat(u.registrarIntentoFallido(POLITICA, T0)).isFalse();
        assertThat(u.registrarIntentoFallido(POLITICA, T0)).isTrue();
        assertThat(u.estaBloqueado(T0)).isTrue();
        assertThat(u.minutosRestantesBloqueo(T0)).isBetween(1L, 16L);
        assertThatThrownBy(() -> u.asegurarPuedeAutenticar(T0))
                .isInstanceOf(AutenticacionException.class);
        assertThat(u.estaBloqueado(T0.plusSeconds(3600))).isFalse();
        assertThat(u.minutosRestantesBloqueo(T0.plusSeconds(3600))).isZero();
    }

    @Test
    void usuarioAccesoExitosoReinicia() {
        Usuario u = reconstruirUsuario();
        u.registrarIntentoFallido(POLITICA, T0);
        u.registrarAccesoExitoso(T0);
        assertThat(u.getIntentosFallidos()).isZero();
        assertThat(u.getBloqueadoHasta()).isNull();
        assertThat(u.getUltimoAcceso()).isEqualTo(T0);
    }

    @Test
    void usuarioInactivoNoAutentica() {
        Usuario u = reconstruirUsuario();
        u.desactivar();
        assertThatThrownBy(() -> u.asegurarPuedeAutenticar(T0)).isInstanceOf(AutenticacionException.class);
        u.activar();
        u.asegurarPuedeAutenticar(T0);
        u.cambiarRol(RolUsuario.ADMIN);
        u.renombrar("Otro Nombre");
        u.cambiarClave(HASH);
        assertThat(u.getRol()).isEqualTo(RolUsuario.ADMIN);
        assertThat(u.getNombre()).isEqualTo("Otro Nombre");
    }

    @Test
    void materiaCrearActualizarEstado() {
        Materia m = Materia.crear("INF999", "Materia Test", TipoMateria.TEORICA, 3, 4, 2);
        assertThat(m.getCodigo().valor()).isEqualTo("INF999");
        assertThat(m.isActiva()).isTrue();
        m.actualizar("Nuevo Nombre", TipoMateria.PRACTICA, 4, 6, 3);
        assertThat(m.getNombre()).isEqualTo("Nuevo Nombre");
        assertThat(m.getCreditos()).isEqualTo(4);
        m.desactivar();
        assertThat(m.isActiva()).isFalse();
        m.activar();
        assertThat(m.isActiva()).isTrue();
        Materia r = Materia.reconstruir(9L, "MAT100", "X", TipoMateria.TEORICA, 3, 4, 1, false);
        assertThat(r.getId()).isEqualTo(9L);
        assertThatThrownBy(() -> Materia.crear("INF1", "M", TipoMateria.TEORICA, 99, 4, 2))
                .isInstanceOf(ValidacionException.class);
    }

    @Test
    void profesorVinculoUsuario() {
        Profesor p = Profesor.crear("DOC-9", "Docente Test", TipoContrato.PLANTA, true);
        assertThat(p.getIdUsuario()).isNull();
        p.vincularUsuario(5L);
        assertThat(p.getIdUsuario()).isEqualTo(5L);
        p.desvincularUsuario();
        assertThat(p.getIdUsuario()).isNull();
        p.actualizar("Otro", TipoContrato.CATEDRA, false);
        assertThat(p.isDisponible()).isFalse();
        p.desactivar();
        p.activar();
        assertThat(p.isActivo()).isTrue();
        Profesor r = Profesor.reconstruir(3L, "DOC-3", "R", TipoContrato.OCASIONAL, false, 7L, true);
        assertThat(r.getIdUsuario()).isEqualTo(7L);
    }

    @Test
    void perfilEquality() {
        Perfil a = Perfil.crear(1L, 2L);
        Perfil b = Perfil.reconstruir(9L, 1L, 2L);
        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a.equals(a)).isTrue();
        assertThat(a.equals("x")).isFalse();
        assertThat(a.getIdProfesor()).isEqualTo(1L);
        assertThatThrownBy(() -> Perfil.crear(null, 2L)).isInstanceOf(ValidacionException.class);
    }

    @Test
    void disponibilidadOcuparLiberar() {
        DisponibilidadProfesor d = DisponibilidadProfesor.crear(1L, DiaSemana.LUNES, 8, OrigenRegistro.MANUAL);
        assertThat(d.estaLibre()).isTrue();
        d.ocupar();
        assertThat(d.estaLibre()).isFalse();
        d.liberar();
        assertThat(d.estaLibre()).isTrue();
        assertThat(d.comoFranja().horaInicio()).isEqualTo(8);
        DisponibilidadProfesor r = DisponibilidadProfesor.reconstruir(4L, 1L, DiaSemana.MARTES, 10,
                com.tesiscalidad.horarios.domain.enums.EstadoFranja.OCUPADA, OrigenRegistro.GENERADO);
        assertThat(r.getId()).isEqualTo(4L);
    }

    @Test
    void sesionClaseReprogramarYDocente() {
        SesionClase s = SesionClase.crear(1L, null, "a", DiaSemana.LUNES, 8, 2, 1, Jornada.DIURNA);
        assertThat(s.getGrupo()).isEqualTo("A");
        assertThat(s.tieneProfesor()).isFalse();
        s.asignarProfesor(3L);
        assertThat(s.tieneProfesor()).isTrue();
        s.quitarProfesor();
        assertThat(s.tieneProfesor()).isFalse();
        s.reprogramar(DiaSemana.MARTES, 10, 3, Jornada.NOCTURNA);
        assertThat(s.getDia()).isEqualTo(DiaSemana.MARTES);
        assertThat(s.franja().horaFin()).isEqualTo(13);
        assertThatThrownBy(() -> SesionClase.crear(1L, null, "??", DiaSemana.LUNES, 8, 2, 1, Jornada.DIURNA))
                .isInstanceOf(ValidacionException.class);
    }

    @Test
    void registroAuditoriaBuilder() {
        RegistroAuditoria r = RegistroAuditoria.builder(AccionAuditoria.CREAR, ResultadoAuditoria.EXITO)
                .fecha(T0).usuario("admin").entidad("MATERIA", 5L).detalle("alta").ip("127.0.0.1")
                .correlationId("cid-1").build();
        assertThat(r.getUsuarioLogin()).isEqualTo("admin");
        assertThat(r.getEntidadId()).isEqualTo("5");
        assertThat(r.getAccion()).isEqualTo(AccionAuditoria.CREAR);
        assertThat(r.getFecha()).isEqualTo(T0);
        RegistroAuditoria r2 = RegistroAuditoria.builder(AccionAuditoria.LOGIN, ResultadoAuditoria.ERROR)
                .entidad("SESION", null).build();
        assertThat(r2.getEntidadId()).isNull();
        assertThatThrownBy(() -> RegistroAuditoria.builder(null, ResultadoAuditoria.EXITO))
                .isInstanceOf(ValidacionException.class);
    }

    private Usuario reconstruirUsuario() {
        return Usuario.reconstruir(1L, "jdperez", "Juan Perez", HASH, RolUsuario.PROFESOR,
                true, 0, null, null);
    }
}
