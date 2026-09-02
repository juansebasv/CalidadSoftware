package com.tesiscalidad.horarios.domain.service;

import com.tesiscalidad.horarios.domain.enums.DiaSemana;
import com.tesiscalidad.horarios.domain.enums.EstadoFranja;
import com.tesiscalidad.horarios.domain.enums.Jornada;
import com.tesiscalidad.horarios.domain.enums.OrigenRegistro;
import com.tesiscalidad.horarios.domain.exception.ReglaNegocioException;
import com.tesiscalidad.horarios.domain.model.DisponibilidadProfesor;
import com.tesiscalidad.horarios.domain.model.SesionClase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiciosDominioTest {

    private final DetectorConflictosHorario detector = new DetectorConflictosHorario();
    private final ValidadorProgramacionSesion validador = new ValidadorProgramacionSesion();

    @Test
    void detectorSinDatosNiConflictos() {
        assertThat(detector.detectar(null)).isEmpty();
        assertThat(detector.detectar(List.of())).isEmpty();
        SesionClase s = sesion(1L, 10L, "A", DiaSemana.LUNES, 8, 2, 1, Jornada.DIURNA);
        assertThat(detector.detectar(List.of(s))).isEmpty();
    }

    @Test
    void detectorDocenteSolapado() {
        SesionClase a = sesion(1L, 10L, "A", DiaSemana.LUNES, 8, 2, 1, Jornada.DIURNA);
        SesionClase b = sesion(2L, 10L, "B", DiaSemana.LUNES, 9, 2, 3, Jornada.DIURNA);
        List<ConflictoHorario> c = detector.detectar(List.of(a, b));
        assertThat(c).extracting(ConflictoHorario::getTipo)
                .contains(ConflictoHorario.Tipo.DOCENTE_SOLAPADO);
        assertThat(c.get(0).getDetalle()).isNotBlank();
        assertThat(c.get(0).toString()).contains("DOCENTE_SOLAPADO");
    }

    @Test
    void detectorCohorteSolapada() {
        SesionClase a = sesion(1L, 10L, "A", DiaSemana.MARTES, 8, 2, 5, Jornada.DIURNA);
        SesionClase b = sesion(2L, 20L, "A", DiaSemana.MARTES, 9, 2, 5, Jornada.DIURNA);
        assertThat(detector.detectar(List.of(a, b)))
                .anyMatch(x -> x.getTipo() == ConflictoHorario.Tipo.COHORTE_SOLAPADA);
    }

    @Test
    void conflictoHorarioEquality() {
        ConflictoHorario a = new ConflictoHorario(ConflictoHorario.Tipo.DOCENTE_SOLAPADO, 1L, 2L, "d");
        ConflictoHorario b = new ConflictoHorario(ConflictoHorario.Tipo.DOCENTE_SOLAPADO, 1L, 2L, "otro");
        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a.equals(a)).isTrue();
        assertThat(a.equals("x")).isFalse();
        assertThat(ConflictoHorario.Tipo.COHORTE_SOLAPADA.getDescripcion()).isNotBlank();
    }

    @Test
    void validadorSinProfesorSoloRevisaCruces() {
        SesionClase candidata = sesion(null, null, "A", DiaSemana.LUNES, 8, 2, 1, Jornada.DIURNA);
        assertThatCode(() -> validador.validar(candidata, false, List.of(), List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void validadorExigeHabilitacion() {
        SesionClase candidata = sesion(null, 10L, "A", DiaSemana.LUNES, 8, 2, 1, Jornada.DIURNA);
        assertThatThrownBy(() -> validador.validar(candidata, false, disp(10L, DiaSemana.LUNES, 8, 9), List.of()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no esta habilitado");
    }

    @Test
    void validadorExigeDisponibilidad() {
        SesionClase candidata = sesion(null, 10L, "A", DiaSemana.LUNES, 8, 2, 1, Jornada.DIURNA);
        assertThatThrownBy(() -> validador.validar(candidata, true, disp(10L, DiaSemana.LUNES, 8, 8), List.of()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("disponibilidad");
    }

    @Test
    void validadorDetectaCruceDocenteYCohorte() {
        SesionClase candidata = sesion(null, 10L, "A", DiaSemana.LUNES, 8, 2, 1, Jornada.DIURNA);
        SesionClase otraDocente = sesion(50L, 10L, "Z", DiaSemana.LUNES, 9, 2, 9, Jornada.NOCTURNA);
        assertThatThrownBy(() -> validador.validar(candidata, true,
                disp(10L, DiaSemana.LUNES, 8, 10), List.of(otraDocente)))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("dicta otra sesion");

        SesionClase candidata2 = sesion(null, null, "A", DiaSemana.LUNES, 8, 2, 1, Jornada.DIURNA);
        SesionClase otraCohorte = sesion(60L, 99L, "A", DiaSemana.LUNES, 9, 2, 1, Jornada.DIURNA);
        assertThatThrownBy(() -> validador.validar(candidata2, false, List.of(), List.of(otraCohorte)))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("grupo");
    }

    @Test
    void validadorIgnoraLaMismaSesionYNoSolapadas() {
        SesionClase existente = sesion(5L, 10L, "A", DiaSemana.LUNES, 8, 2, 1, Jornada.DIURNA);
        SesionClase noSolapa = sesion(6L, 10L, "B", DiaSemana.LUNES, 10, 2, 1, Jornada.DIURNA);
        assertThatCode(() -> validador.validar(existente, true, disp(10L, DiaSemana.LUNES, 8, 10),
                List.of(existente, noSolapa))).doesNotThrowAnyException();
    }

    private SesionClase sesion(Long id, Long prof, String grupo, DiaSemana dia, int hora, int dur,
                               int sem, Jornada jornada) {
        return id == null
                ? SesionClase.crear(1L, prof, grupo, dia, hora, dur, sem, jornada)
                : SesionClase.reconstruir(id, 1L, prof, grupo, dia, hora, dur, sem, jornada);
    }

    private List<DisponibilidadProfesor> disp(Long prof, DiaSemana dia, int desde, int hasta) {
        return java.util.stream.IntStream.rangeClosed(desde, hasta)
                .mapToObj(h -> DisponibilidadProfesor.reconstruir((long) h, prof, dia, h,
                        EstadoFranja.LIBRE, OrigenRegistro.GENERADO))
                .collect(java.util.stream.Collectors.toList());
    }
}
