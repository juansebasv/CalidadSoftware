package com.tesiscalidad.horarios.application.dto;

import com.tesiscalidad.horarios.application.dto.auditoria.AuditoriaVista;
import com.tesiscalidad.horarios.application.dto.auth.SesionAutenticada;
import com.tesiscalidad.horarios.application.dto.disponibilidad.FranjaDisponibilidadVista;
import com.tesiscalidad.horarios.application.dto.horario.ConflictoVista;
import com.tesiscalidad.horarios.application.dto.horario.ConteoVista;
import com.tesiscalidad.horarios.application.dto.horario.TableroVista;
import com.tesiscalidad.horarios.application.dto.materia.MateriaVista;
import com.tesiscalidad.horarios.application.dto.profesor.ProfesorVista;
import com.tesiscalidad.horarios.application.dto.sesion.SesionClaseVista;
import com.tesiscalidad.horarios.application.dto.usuario.CambiarContrasenaComando;
import com.tesiscalidad.horarios.application.dto.usuario.CrearUsuarioComando;
import com.tesiscalidad.horarios.application.dto.usuario.UsuarioVista;
import com.tesiscalidad.horarios.domain.enums.AccionAuditoria;
import com.tesiscalidad.horarios.domain.enums.DiaSemana;
import com.tesiscalidad.horarios.domain.enums.EstadoFranja;
import com.tesiscalidad.horarios.domain.enums.Jornada;
import com.tesiscalidad.horarios.domain.enums.OrigenRegistro;
import com.tesiscalidad.horarios.domain.enums.ResultadoAuditoria;
import com.tesiscalidad.horarios.domain.enums.RolUsuario;
import com.tesiscalidad.horarios.domain.enums.TipoContrato;
import com.tesiscalidad.horarios.domain.enums.TipoMateria;
import com.tesiscalidad.horarios.domain.model.DisponibilidadProfesor;
import com.tesiscalidad.horarios.domain.model.Materia;
import com.tesiscalidad.horarios.domain.model.Profesor;
import com.tesiscalidad.horarios.domain.model.RegistroAuditoria;
import com.tesiscalidad.horarios.domain.model.SesionClase;
import com.tesiscalidad.horarios.domain.model.Usuario;
import com.tesiscalidad.horarios.domain.service.ConflictoHorario;
import com.tesiscalidad.horarios.domain.vo.ClaveHash;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DtosTest {

    private static final ClaveHash HASH =
            ClaveHash.deHashExistente("$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW");
    private static final Instant AHORA = Instant.parse("2026-04-01T08:00:00Z");

    @Test
    void materiaVistaFactoriaYEquals() {
        Materia m = Materia.reconstruir(1L, "INF101", "Intro", TipoMateria.TEORICA, 3, 4, 1, true);
        MateriaVista v = MateriaVista.de(m);
        assertThat(v.getCodigo()).isEqualTo("INF101");
        assertThat(v.getTipoEtiqueta()).isEqualTo("Teorica");
        assertThat(v.isActiva()).isTrue();
        assertThat(v).isEqualTo(MateriaVista.de(m)).hasSameHashCodeAs(MateriaVista.de(m));
        assertThat(v.equals(v)).isTrue();
        assertThat(v.equals("x")).isFalse();
        assertThat(v.getCreditos()).isEqualTo(3);
        assertThat(v.getIntensidadHoraria()).isEqualTo(4);
        assertThat(v.getSemestre()).isEqualTo(1);
        assertThat(v.getId()).isEqualTo(1L);
        assertThat(v.getNombre()).isEqualTo("Intro");
        assertThat(v.getTipo()).isEqualTo("T");
    }

    @Test
    void profesorVistaFactoria() {
        Profesor p = Profesor.reconstruir(2L, "DOC-2", "Docente", TipoContrato.OCASIONAL, false, 5L, true);
        ProfesorVista v = ProfesorVista.de(p);
        assertThat(v.getTipoContratoEtiqueta()).isEqualTo("Ocasional");
        assertThat(v.isDisponible()).isFalse();
        assertThat(v.getIdUsuario()).isEqualTo(5L);
        assertThat(v.isActivo()).isTrue();
        assertThat(v.getCodigo()).isEqualTo("DOC-2");
        assertThat(v.getNombre()).isEqualTo("Docente");
        assertThat(v.getTipoContrato()).isEqualTo("OCASIONAL");
        assertThat(v.getId()).isEqualTo(2L);
    }

    @Test
    void usuarioVistaOcultaHashYFormateaFecha() {
        Usuario u = Usuario.reconstruir(3L, "admin", "Admin", HASH, RolUsuario.ADMIN, true, 1, null, AHORA);
        UsuarioVista v = UsuarioVista.de(u, AHORA);
        assertThat(v.getRol()).isEqualTo("ADMIN");
        assertThat(v.isBloqueado()).isFalse();
        assertThat(v.getIntentosFallidos()).isEqualTo(1);
        assertThat(v.getUltimoAccesoTexto()).contains("2026-04-01");
        assertThat(v.getLogin()).isEqualTo("admin");
        assertThat(v.getNombre()).isEqualTo("Admin");
        assertThat(v.getId()).isEqualTo(3L);
        assertThat(v.isActivo()).isTrue();
        assertThat(v.getUltimoAcceso()).isEqualTo(AHORA);

        Usuario sinAcceso = Usuario.crear("nuevo", "Nuevo Nombre", HASH, RolUsuario.CONSULTA);
        assertThat(UsuarioVista.de(sinAcceso, AHORA).getUltimoAccesoTexto()).isEqualTo("-");
    }

    @Test
    void sesionClaseVista() {
        SesionClase s = SesionClase.reconstruir(4L, 1L, 2L, "A", DiaSemana.LUNES, 8, 2, 1, Jornada.DIURNA);
        SesionClaseVista v = SesionClaseVista.de(s, "INF101", "Intro", "Docente X");
        assertThat(v.getHoraFin()).isEqualTo(10);
        assertThat(v.getDiaEtiqueta()).isEqualTo("Lunes");
        assertThat(v.getJornadaEtiqueta()).isEqualTo("Diurna");
        assertThat(v.getCodigoMateria()).isEqualTo("INF101");
        assertThat(v.getNombreProfesor()).isEqualTo("Docente X");
        assertThat(v.getNombreMateria()).isEqualTo("Intro");
        assertThat(v.getGrupo()).isEqualTo("A");
        assertThat(v.getHora()).isEqualTo(8);
        assertThat(v.getDuracion()).isEqualTo(2);
        assertThat(v.getSemestre()).isEqualTo(1);
        assertThat(v.getJornada()).isEqualTo("D");
        assertThat(v.getDia()).isEqualTo("1");
        assertThat(v.getId()).isEqualTo(4L);
        assertThat(v.getIdMateria()).isEqualTo(1L);
        assertThat(v.getIdProfesor()).isEqualTo(2L);
    }

    @Test
    void franjaDisponibilidadVista() {
        DisponibilidadProfesor d = DisponibilidadProfesor.reconstruir(5L, 3L, DiaSemana.MARTES, 10,
                EstadoFranja.OCUPADA, OrigenRegistro.GENERADO);
        FranjaDisponibilidadVista v = FranjaDisponibilidadVista.de(d);
        assertThat(v.getDiaEtiqueta()).isEqualTo("Martes");
        assertThat(v.getEstado()).isEqualTo("1");
        assertThat(v.getEstadoEtiqueta()).isEqualTo("Ocupada");
        assertThat(v.getOrigen()).isEqualTo("Generado");
        assertThat(v.getHora()).isEqualTo(10);
        assertThat(v.getDia()).isEqualTo("2");
        assertThat(v.getId()).isEqualTo(5L);
        assertThat(v.getIdProfesor()).isEqualTo(3L);
    }

    @Test
    void auditoriaVista() {
        RegistroAuditoria r = RegistroAuditoria.reconstruir(6L, AHORA, "admin", AccionAuditoria.LOGIN,
                "SESION", "1", ResultadoAuditoria.EXITO, "ok", "1.2.3.4", "cid");
        AuditoriaVista v = AuditoriaVista.de(r);
        assertThat(v.getAccion()).isEqualTo("LOGIN");
        assertThat(v.getResultado()).isEqualTo("EXITO");
        assertThat(v.getFechaTexto()).contains("2026-04-01");
        assertThat(v.getUsuario()).isEqualTo("admin");
        assertThat(v.getEntidad()).isEqualTo("SESION");
        assertThat(v.getEntidadId()).isEqualTo("1");
        assertThat(v.getDetalle()).isEqualTo("ok");
        assertThat(v.getIp()).isEqualTo("1.2.3.4");
        assertThat(v.getCorrelationId()).isEqualTo("cid");
        assertThat(v.getId()).isEqualTo(6L);

        AuditoriaVista sinFecha = AuditoriaVista.de(RegistroAuditoria.reconstruir(7L, null, null,
                AccionAuditoria.LOGOUT, null, null, ResultadoAuditoria.ERROR, null, null, null));
        assertThat(sinFecha.getFechaTexto()).isEqualTo("-");
    }

    @Test
    void conflictoYConteoYTableroVista() {
        ConflictoVista c = ConflictoVista.de(new ConflictoHorario(
                ConflictoHorario.Tipo.COHORTE_SOLAPADA, 1L, 2L, "detalle"));
        assertThat(c.getTipo()).isEqualTo("COHORTE_SOLAPADA");
        assertThat(c.getTipoEtiqueta()).isNotBlank();
        assertThat(c.getSesionA()).isEqualTo(1L);
        assertThat(c.getSesionB()).isEqualTo(2L);
        assertThat(c.getDetalle()).isEqualTo("detalle");

        ConteoVista cv = new ConteoVista("Lunes", 5);
        assertThat(cv.getEtiqueta()).isEqualTo("Lunes");
        assertThat(cv.getValor()).isEqualTo(5);

        TableroVista t = new TableroVista(1, 2, 3, 4, 1, 2,
                List.of(cv), List.of(cv), List.of(cv), List.of(cv));
        assertThat(t.getTotalMaterias()).isEqualTo(1);
        assertThat(t.getTotalProfesores()).isEqualTo(2);
        assertThat(t.getTotalUsuarios()).isEqualTo(3);
        assertThat(t.getTotalSesiones()).isEqualTo(4);
        assertThat(t.getSesionesSinDocente()).isEqualTo(1);
        assertThat(t.getTotalConflictos()).isEqualTo(2);
        assertThat(t.getSesionesPorDia()).hasSize(1);
        assertThat(t.getSesionesPorSemestre()).hasSize(1);
        assertThat(t.getMateriasPorTipo()).hasSize(1);
        assertThat(t.getProfesoresPorContrato()).hasSize(1);
    }

    @Test
    void sesionAutenticadaYContextoYComandos() {
        Usuario u = Usuario.reconstruir(8L, "admin", "Admin", HASH, RolUsuario.ADMIN, true, 0, null, null);
        SesionAutenticada s = SesionAutenticada.de(u, AHORA);
        assertThat(s.getIdUsuario()).isEqualTo(8L);
        assertThat(s.getLogin()).isEqualTo("admin");
        assertThat(s.getNombre()).isEqualTo("Admin");
        assertThat(s.getRol()).isEqualTo(RolUsuario.ADMIN);
        assertThat(s.getInicioSesion()).isEqualTo(AHORA);

        ContextoPeticion ctx = new ContextoPeticion("admin", RolUsuario.ADMIN, "ip", "cid");
        assertThat(ctx.esAnonimo()).isFalse();
        assertThat(ctx.getUsuarioLogin()).isEqualTo("admin");
        assertThat(ctx.getIp()).isEqualTo("ip");
        assertThat(ctx.getCorrelationId()).isEqualTo("cid");
        assertThat(ContextoPeticion.anonimo("ip", "cid").esAnonimo()).isTrue();

        CrearUsuarioComando cu = new CrearUsuarioComando("l", "n", "clave1234".toCharArray(), "ADMIN");
        assertThat(cu.getContrasena()).containsExactly("clave1234".toCharArray());
        cu.limpiar();
        assertThat(cu.getContrasena()).containsOnly('\0');

        CambiarContrasenaComando cc = new CambiarContrasenaComando("nueva1234".toCharArray());
        assertThat(cc.getNuevaContrasena()).hasSize(9);
        cc.limpiar();
        assertThat(cc.getNuevaContrasena()).containsOnly('\0');
    }
}
