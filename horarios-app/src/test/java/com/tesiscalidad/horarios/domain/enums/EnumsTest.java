package com.tesiscalidad.horarios.domain.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnumsTest {

    @Test
    void rolUsuarioDesdeCodigoYCapacidades() {
        assertThat(RolUsuario.desdeCodigo("admin")).isEqualTo(RolUsuario.ADMIN);
        assertThat(RolUsuario.ADMIN.puedeGestionar()).isTrue();
        assertThat(RolUsuario.ADMIN.puedeAdministrarUsuarios()).isTrue();
        assertThat(RolUsuario.COORDINADOR.puedeGestionar()).isTrue();
        assertThat(RolUsuario.COORDINADOR.puedeAdministrarUsuarios()).isFalse();
        assertThat(RolUsuario.PROFESOR.puedeGestionar()).isFalse();
        assertThat(RolUsuario.CONSULTA.getCodigo()).isEqualTo("CONSULTA");
        assertThatThrownBy(() -> RolUsuario.desdeCodigo("x")).isInstanceOf(com.tesiscalidad.horarios.domain.exception.ValidacionException.class);
        assertThatThrownBy(() -> RolUsuario.desdeCodigo(null)).isInstanceOf(com.tesiscalidad.horarios.domain.exception.ValidacionException.class);
    }

    @Test
    void tipoMateria() {
        assertThat(TipoMateria.desdeCodigo("tp")).isEqualTo(TipoMateria.TEORICO_PRACTICA);
        assertThat(TipoMateria.TEORICA.getCodigo()).isEqualTo("T");
        assertThat(TipoMateria.PRACTICA.getEtiqueta()).isEqualTo("Practica");
        assertThatThrownBy(() -> TipoMateria.desdeCodigo("z")).isInstanceOf(com.tesiscalidad.horarios.domain.exception.ValidacionException.class);
    }

    @Test
    void tipoContrato() {
        assertThat(TipoContrato.desdeCodigo("PLANTA")).isEqualTo(TipoContrato.PLANTA);
        assertThat(TipoContrato.CATEDRA.getEtiqueta()).isEqualTo("Catedra");
        assertThatThrownBy(() -> TipoContrato.desdeCodigo("temp")).isInstanceOf(com.tesiscalidad.horarios.domain.exception.ValidacionException.class);
    }

    @Test
    void diaSemana() {
        assertThat(DiaSemana.desdeCodigo("1")).isEqualTo(DiaSemana.LUNES);
        assertThat(DiaSemana.VIERNES.getOrden()).isEqualTo(5);
        assertThat(DiaSemana.SABADO.getEtiqueta()).isEqualTo("Sabado");
        assertThatThrownBy(() -> DiaSemana.desdeCodigo("9")).isInstanceOf(com.tesiscalidad.horarios.domain.exception.ValidacionException.class);
    }

    @Test
    void jornada() {
        assertThat(Jornada.desdeCodigo("d")).isEqualTo(Jornada.DIURNA);
        assertThat(Jornada.NOCTURNA.getEtiqueta()).isEqualTo("Nocturna");
        assertThatThrownBy(() -> Jornada.desdeCodigo("")).isInstanceOf(com.tesiscalidad.horarios.domain.exception.ValidacionException.class);
    }

    @Test
    void estadoFranjaYOrigen() {
        assertThat(EstadoFranja.desdeCodigo("0").estaLibre()).isTrue();
        assertThat(EstadoFranja.OCUPADA.estaLibre()).isFalse();
        assertThat(EstadoFranja.OCUPADA.getEtiqueta()).isEqualTo("Ocupada");
        assertThat(OrigenRegistro.desdeCodigo("1")).isEqualTo(OrigenRegistro.MANUAL);
        assertThat(OrigenRegistro.GENERADO.getEtiqueta()).isEqualTo("Generado");
        assertThatThrownBy(() -> EstadoFranja.desdeCodigo("7")).isInstanceOf(com.tesiscalidad.horarios.domain.exception.ValidacionException.class);
        assertThatThrownBy(() -> OrigenRegistro.desdeCodigo("7")).isInstanceOf(com.tesiscalidad.horarios.domain.exception.ValidacionException.class);
    }

    @Test
    void accionYResultadoAuditoria() {
        assertThat(AccionAuditoria.valueOf("LOGIN")).isEqualTo(AccionAuditoria.LOGIN);
        assertThat(ResultadoAuditoria.values()).containsExactly(ResultadoAuditoria.EXITO, ResultadoAuditoria.ERROR);
    }
}
