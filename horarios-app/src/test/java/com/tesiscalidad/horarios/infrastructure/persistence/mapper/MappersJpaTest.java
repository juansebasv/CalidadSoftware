package com.tesiscalidad.horarios.infrastructure.persistence.mapper;

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
import com.tesiscalidad.horarios.domain.vo.ClaveHash;
import com.tesiscalidad.horarios.infrastructure.persistence.entity.AuditoriaEntity;
import com.tesiscalidad.horarios.infrastructure.persistence.entity.DisponibilidadProfesorEntity;
import com.tesiscalidad.horarios.infrastructure.persistence.entity.MateriaEntity;
import com.tesiscalidad.horarios.infrastructure.persistence.entity.ProfesorEntity;
import com.tesiscalidad.horarios.infrastructure.persistence.entity.SesionClaseEntity;
import com.tesiscalidad.horarios.infrastructure.persistence.entity.UsuarioEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MappersJpaTest {

    private static final String HASH = "$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW";
    private static final Instant T0 = Instant.parse("2026-03-01T12:00:00Z");

    @Test
    void tiempoJpaConvierteAmbosSentidosYNull() {
        assertThat(TiempoJpa.aInstant(null)).isNull();
        assertThat(TiempoJpa.aOffset(null)).isNull();
        OffsetDateTime odt = TiempoJpa.aOffset(T0);
        assertThat(TiempoJpa.aInstant(odt)).isEqualTo(T0);
    }

    @Test
    void usuarioJpaRoundTrip() {
        UsuarioEntity e = new UsuarioEntity();
        e.setId(7L);
        e.setLogin("jdperez");
        e.setNombre("Juan Perez");
        e.setClave(HASH);
        e.setTipo("PROFESOR");
        e.setActivo(true);
        e.setIntentosFallidos(2);
        e.setBloqueadoHasta(TiempoJpa.aOffset(T0));
        e.setUltimoAcceso(TiempoJpa.aOffset(T0));

        Usuario dom = UsuarioJpaMapper.aDominio(e);
        assertThat(dom.getLogin()).isEqualTo("jdperez");
        assertThat(dom.getRol()).isEqualTo(RolUsuario.PROFESOR);
        assertThat(dom.getIntentosFallidos()).isEqualTo(2);

        UsuarioEntity nueva = UsuarioJpaMapper.aEntidadNueva(dom);
        assertThat(nueva.getClave()).isEqualTo(HASH);
        assertThat(nueva.getTipo()).isEqualTo("PROFESOR");
        assertThat(nueva.isActivo()).isTrue();
    }

    @Test
    void materiaJpaRoundTrip() {
        Materia dom = Materia.reconstruir(3L, "INF300", "Nombre", TipoMateria.TEORICO_PRACTICA, 4, 6, 5, false);
        MateriaEntity e = MateriaJpaMapper.aEntidadNueva(dom);
        assertThat(e.getCodigo()).isEqualTo("INF300");
        assertThat(e.getTipo()).isEqualTo("TP");
        assertThat(e.isActiva()).isFalse();
        Materia vuelta = MateriaJpaMapper.aDominio(setId(e, 3L));
        assertThat(vuelta.getSemestre()).isEqualTo(5);
        assertThat(vuelta.getIntensidadHoraria()).isEqualTo(6);
    }

    @Test
    void profesorJpaRoundTripYDisponibilidad() {
        Profesor dom = Profesor.reconstruir(4L, "DOC-4", "Docente", TipoContrato.CATEDRA, true, 9L, true);
        ProfesorEntity e = ProfesorJpaMapper.aEntidadNueva(dom);
        assertThat(e.getDisponibilidad()).isEqualTo("1");
        assertThat(e.getIdUsuario()).isEqualTo(9L);
        e.setId(4L);
        Profesor vuelta = ProfesorJpaMapper.aDominio(e);
        assertThat(vuelta.isDisponible()).isTrue();
        assertThat(vuelta.getTipoContrato()).isEqualTo(TipoContrato.CATEDRA);

        Profesor noDisp = Profesor.reconstruir(5L, "DOC-5", "X", TipoContrato.PLANTA, false, null, false);
        assertThat(ProfesorJpaMapper.aEntidadNueva(noDisp).getDisponibilidad()).isEqualTo("0");
    }

    @Test
    void disponibilidadJpaRoundTrip() {
        DisponibilidadProfesor dom = DisponibilidadProfesor.reconstruir(6L, 4L, DiaSemana.MIERCOLES, 8,
                EstadoFranja.OCUPADA, OrigenRegistro.MANUAL);
        DisponibilidadProfesorEntity e = DisponibilidadJpaMapper.aEntidadNueva(dom);
        assertThat(e.getDia()).isEqualTo("3");
        assertThat(e.getHora()).isEqualTo("08");
        assertThat(e.getEstado()).isEqualTo("1");
        assertThat(e.getManual()).isEqualTo("1");
        e.setId(6L);
        DisponibilidadProfesor vuelta = DisponibilidadJpaMapper.aDominio(e);
        assertThat(vuelta.getHora()).isEqualTo(8);
        assertThat(vuelta.getEstado()).isEqualTo(EstadoFranja.OCUPADA);
    }

    @Test
    void sesionClaseJpaRoundTrip() {
        SesionClase dom = SesionClase.reconstruir(7L, 1L, 2L, "A", DiaSemana.LUNES, 9, 2, 3, Jornada.NOCTURNA);
        SesionClaseEntity e = SesionClaseJpaMapper.aEntidadNueva(dom);
        assertThat(e.getHora()).isEqualTo("09");
        assertThat(e.getJornada()).isEqualTo("N");
        assertThat(e.getGrupo()).isEqualTo("A");
        e.setId(7L);
        SesionClase vuelta = SesionClaseJpaMapper.aDominio(e);
        assertThat(vuelta.getDuracion()).isEqualTo(2);
        assertThat(vuelta.getIdProfesor()).isEqualTo(2L);
    }

    @Test
    void auditoriaJpaMapper() {
        RegistroAuditoria dom = RegistroAuditoria.builder(AccionAuditoria.CREAR, ResultadoAuditoria.EXITO)
                .fecha(T0).usuario("admin").entidad("MATERIA", 5L).detalle("alta").ip("ip").correlationId("cid").build();
        AuditoriaEntity e = AuditoriaJpaMapper.aEntidadNueva(dom);
        assertThat(e.getAccion()).isEqualTo("CREAR");
        assertThat(e.getResultado()).isEqualTo("EXITO");
        assertThat(e.getEntidadId()).isEqualTo("5");

        e.setId(11L);
        RegistroAuditoria vuelta = AuditoriaJpaMapper.aDominio(e);
        assertThat(vuelta.getId()).isEqualTo(11L);
        assertThat(vuelta.getAccion()).isEqualTo(AccionAuditoria.CREAR);
        assertThat(vuelta.getUsuarioLogin()).isEqualTo("admin");
    }

    private MateriaEntity setId(MateriaEntity e, Long id) {
        e.setId(id);
        return e;
    }
}
